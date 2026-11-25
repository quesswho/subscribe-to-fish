package fish;

import battlecode.instrumenter.InstrumentationException;
import battlecode.instrumenter.TeamClassLoaderFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * One class loader for all robots of a team. It instruments exactly as TeamClassLoaderFactory.Loader does, then
 * rewrites the result so that each robot still has its own static state and its own class initialisation:
 *
 * - the static fields of a robot class X (team classes, instrumented.* copies of the JDK, the injected RobotMonitor
 *   family) that are not compile-time constants move to a class X$$F, so every getstatic/putstatic keeps its size;
 *   Ctx.switchTo swaps their values with the running robot's holder (an X$$S) at each robot switch;
 * - X's static initialiser becomes a plain method. X$$S.c() initialises X for the running robot, and is called
 *   where the JVM would initialise X (new, getstatic, putstatic, invokestatic; superclass first), except where an
 *   earlier call on every path already did it.
 *
 * Bytecode counts are compiled into the instrumented code before this rewrite, so they do not change. The JVM then
 * loads, links and JIT-compiles each class once per team (and per worker, across games) instead of once per robot.
 */
final class SharedLoader extends ClassLoader {
    static final Set<String> ALWAYS_REDEFINE = Set.of(
            "battlecode.instrumenter.inject.ObjectMethods",
            "battlecode.instrumenter.inject.InstrumentableFunctions",
            "battlecode.instrumenter.inject.System",
            "battlecode.instrumenter.inject.RobotMonitor",
            "battlecode.common.Clock");
    /** Initialised by the sandbox when it is set up, as SandboxedRobotPlayer does; bot code never triggers them. */
    static final List<String> EAGER = List.of(
            "battlecode/instrumenter/inject/RobotMonitor", "battlecode/instrumenter/inject/System");
    private static final String CTX = "fish/Ctx";
    private static final String CLINIT = "$fish$clinit";
    static final String HOLDER = "$$S";
    static final String FIELDS = "$$F";

    private final TeamClassLoaderFactory factory;
    private final TeamClassLoaderFactory.Loader instrumenter;
    private final Map<String, Info> infos = new HashMap<>();
    private final Map<String, Class<?>> defined = new HashMap<>();
    private int nextIndex;

    // Read by Ctx: indices of classes whose statics are swapped, and a zero-valued holder for each.
    int[] swapped = new int[64];
    int swappedCount;
    Statics[] defaults = new Statics[64];
    boolean[] lazy = new boolean[64];

    /** What the rewrite needs to know about a robot class. Names are internal (a/b/C). */
    private static final class Info {
        String name, superName;
        String[] interfaces;
        boolean isInterface, hasClinit;
        int index;
        /** name + desc of each static field, true if it moves to X$$F. */
        final Map<String, Boolean> staticFields = new HashMap<>();
        final List<FieldNode> moved = new ArrayList<>();
        final Set<String> methods = new HashSet<>();
        final Set<String> defaultMethods = new HashSet<>();
        ClassNode node;
        Boolean needsInit;
    }

    /** FISH_CLASS_CACHE: instrumented and rewritten classes on disk, so new workers skip that work. */
    private final java.nio.file.Path cacheDir;

    SharedLoader(TeamClassLoaderFactory factory, java.io.File teamDir) {
        super(SharedLoader.class.getClassLoader());
        this.factory = factory;
        this.instrumenter = factory.createLoader(false);
        setDefaultAssertionStatus(true);
        String root = System.getenv("FISH_CLASS_CACHE");
        this.cacheDir = root == null ? null : java.nio.file.Path.of(root, cacheKey(teamDir));
    }

    /** Changes with the bot's class files, this loader's code (the rewrite) and the JDK (instrumented copies). */
    private static String cacheKey(java.io.File teamDir) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            // The rewrite's code: this class and the outliner it runs (a change to either changes the output).
            for (String c : new String[] {"SharedLoader.class", "Outliner.class", "Spill.class"})
                try (var in = SharedLoader.class.getResourceAsStream(c)) {
                    md.update(in.readAllBytes());
                }
            md.update((OUTLINE ? "outline" : "whole").getBytes());
            md.update(System.getProperty("java.version").getBytes());
            try (var files = java.nio.file.Files.walk(teamDir.toPath())) {
                for (var p : (Iterable<java.nio.file.Path>) files.filter(f -> f.toString().endsWith(".class")).sorted()::iterator) {
                    md.update(teamDir.toPath().relativize(p).toString().getBytes());
                    md.update(java.nio.file.Files.readAllBytes(p));
                }
            }
            return java.util.HexFormat.of().formatHex(md.digest(), 0, 12);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] cached(String internal, String kind, java.util.function.Supplier<byte[]> make) {
        if (cacheDir == null) return make.get();
        java.nio.file.Path p = cacheDir.resolve(internal + "." + kind);
        try {
            return java.nio.file.Files.readAllBytes(p);
        } catch (java.io.IOException missing) {
            byte[] b = make.get();
            try {
                java.nio.file.Files.createDirectories(p.getParent());
                java.nio.file.Path tmp = p.resolveSibling(p.getFileName() + "." + ProcessHandle.current().pid());
                java.nio.file.Files.write(tmp, b);
                java.nio.file.Files.move(tmp, p, java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (java.io.IOException ignored) {
                // a read-only cache just means doing the work
            }
            return b;
        }
    }

    private final Map<String, Boolean> robotClass = new HashMap<>();

    private boolean isRobotClass(String dotted) {
        Boolean known = robotClass.get(dotted);
        if (known == null) {
            known = ALWAYS_REDEFINE.contains(dotted) || dotted.startsWith("instrumented.") || factory.hasTeamClass(dotted);
            robotClass.put(dotted, known);
        }
        return known;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = defined.get(name);
        if (c != null) return c;
        String base = name.endsWith(HOLDER) || name.endsWith(FIELDS) ? name.substring(0, name.length() - 3) : null;
        if (base != null && isRobotClass(base)) {
            Info info = info(base.replace('.', '/'));
            byte[] b = name.endsWith(HOLDER) ? holderBytes(info) : fieldsBytes(info);
            c = defineClass(name, b, 0, b.length);
            defined.put(name, c);
            if (name.endsWith(HOLDER) && !info.moved.isEmpty()) register(info, c);
            // The fields only change for a robot once its holder exists, so register the holder with them.
            if (name.endsWith(FIELDS)) loadClass(base + HOLDER, false);
        } else if (isRobotClass(name)) {
            String internal = name.replace('.', '/');
            byte[] b = cached(internal, "rw", () -> rewrite(info(internal)));
            c = defineClass(name, b, 0, b.length);
            defined.put(name, c);
        } else {
            return super.loadClass(name, resolve);
        }
        if (resolve) resolveClass(c);
        return c;
    }

    private void register(Info info, Class<?> holder) {
        try {
            var ctor = holder.getDeclaredConstructor();
            ctor.setAccessible(true);
            if (info.index >= defaults.length) {
                defaults = Arrays.copyOf(defaults, Math.max(info.index + 1, 2 * defaults.length));
                lazy = Arrays.copyOf(lazy, defaults.length);
            }
            defaults[info.index] = (Statics) ctor.newInstance();
            lazy[info.index] = !needsInit(info) && !EAGER.contains(info.name);
            if (swappedCount == swapped.length) swapped = Arrays.copyOf(swapped, 2 * swapped.length);
            swapped[swappedCount++] = info.index;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public java.net.URL getResource(String name) {
        throw new RuntimeException("You can't use getResource() from a player class!");
    }

    // ---- instrumentation (TeamClassLoaderFactory.Loader.loadClass) ----

    private byte[] instrumented(String dotted) {
        if (ALWAYS_REDEFINE.contains(dotted)) {
            ClassReader cr = TeamClassLoaderFactory.normalReader(dotted);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
            cr.accept(cw, 0);
            return cw.toByteArray();
        }
        if (factory.hasTeamClass(dotted)) {
            assertAllowedPackage(dotted);
            return instrumenter.instrument(factory.teamReader(dotted), true, false);
        }
        return instrumenter.instrument(TeamClassLoaderFactory.normalReader(dotted), false, false);
    }

    private static void assertAllowedPackage(String name) {
        for (String bad : new String[] {"battlecode.", "java.", "com.sun.", "sun.", "org.apache.", "org.objectweb.",
                "kotlin."})
            if (name.startsWith(bad))
                throw new InstrumentationException(InstrumentationException.Type.ILLEGAL,
                        "Sorry, you can't use the package '" + bad + "' in a player");
    }

    private Info info(String internal) {
        Info i = infos.get(internal);
        if (i != null) return i;
        ClassNode cn = new ClassNode();
        new ClassReader(cached(internal, "inst", () -> instrumented(internal.replace('/', '.')))).accept(cn, 0);
        i = new Info();
        i.name = cn.name;
        i.superName = cn.superName;
        i.interfaces = cn.interfaces.toArray(new String[0]);
        i.isInterface = (cn.access & Opcodes.ACC_INTERFACE) != 0;
        i.index = nextIndex++;
        i.node = cn;
        for (FieldNode f : cn.fields) {
            if ((f.access & Opcodes.ACC_STATIC) == 0) continue;
            boolean constant = (f.access & Opcodes.ACC_FINAL) != 0 && f.value != null;
            i.staticFields.put(f.name + f.desc, !constant);
            if (!constant) i.moved.add(f);
        }
        for (MethodNode m : cn.methods) {
            if (m.name.equals("<clinit>")) i.hasClinit = true;
            i.methods.add(m.name + m.desc);
            if (i.isInterface && (m.access & (Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT)) == 0)
                i.defaultMethods.add(m.name + m.desc);
        }
        infos.put(internal, i);
        return i;
    }

    private Info robotInfo(String internal) {
        return internal != null && isRobotClass(internal.replace('/', '.')) ? info(internal) : null;
    }

    /** Whether initialising the class runs code: it, or a class initialised with it, has a static initialiser. */
    private boolean needsInit(Info i) {
        if (i.needsInit != null) return i.needsInit;
        i.needsInit = false;
        boolean n = i.hasClinit;
        if (!i.isInterface) {
            Info s = robotInfo(i.superName);
            if (s != null && needsInit(s)) n = true;
            for (Info itf : initInterfaces(i))
                if (needsInit(itf)) n = true;
        }
        i.needsInit = n;
        return n;
    }

    /** JVMS 5.5: superinterfaces that declare default methods are initialised with the class. */
    private List<Info> initInterfaces(Info i) {
        List<Info> out = new ArrayList<>();
        for (String name : i.interfaces) {
            Info itf = robotInfo(name);
            if (itf == null) continue;
            if (!itf.defaultMethods.isEmpty()) out.add(itf);
            for (Info sup : initInterfaces(itf))
                if (!out.contains(sup)) out.add(sup);
        }
        return out;
    }

    /** JVMS 5.4.3.2: the robot class declaring static field name+desc as seen from owner, or null. */
    private Info fieldOwner(String owner, String key) {
        Info i = robotInfo(owner);
        if (i == null) return null;
        if (i.staticFields.containsKey(key)) return i;
        for (String itf : i.interfaces) {
            Info hit = fieldOwner(itf, key);
            if (hit != null) return hit;
        }
        return i.superName == null ? null : fieldOwner(i.superName, key);
    }

    /** JVMS 5.4.3.3: the robot class declaring static method name+desc as seen from owner, or null. */
    private Info methodOwner(String owner, String key, boolean itf) {
        Info i = robotInfo(owner);
        if (i == null) return null;
        if (i.methods.contains(key)) return i;
        return itf || i.superName == null ? null : methodOwner(i.superName, key, false);
    }

    /** A class and its superclasses: certainly initialised while code of the class runs. */
    private Set<String> initialisedWhileRunning(Info i) {
        Set<String> out = new HashSet<>();
        for (Info c = i; c != null; c = robotInfo(c.superName)) out.add(c.name);
        return out;
    }

    // ---- the rewrite ----

    private byte[] rewrite(Info info) {
        ClassNode cn = info.node;
        Set<String> self = initialisedWhileRunning(info);
        for (MethodNode m : cn.methods) {
            if (m.name.equals("<clinit>")) {
                m.name = CLINIT;
                m.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC;
            }
            rewriteMethod(m, self);
        }
        cn.fields.removeIf(f -> (f.access & Opcodes.ACC_STATIC) != 0 && Boolean.TRUE.equals(info.staticFields.get(f.name + f.desc)));
        // Oversized methods are split into helpers so C1 can compile them (see Outliner); their frames are recomputed.
        boolean split = OUTLINE && Outliner.apply(cn);
        ClassWriter cw = split ? new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String a, String b) {
                return commonSuperClass(a, b);
            }
        } : new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);
        info.node = null;
        byte[] out = cw.toByteArray();
        String dump = System.getenv("FISH_OUTLINE_DUMP");
        if (split && dump != null) {
            try {
                java.nio.file.Path p = java.nio.file.Path.of(dump, cn.name + ".class");
                java.nio.file.Files.createDirectories(p.getParent());
                java.nio.file.Files.write(p, out);
            } catch (java.io.IOException ignored) {
            }
        }
        return out;
    }

    /** FISH_NO_OUTLINE=1 keeps the giant methods whole (to compare speed). */
    static final boolean OUTLINE = System.getenv("FISH_NO_OUTLINE") == null;

    /** The closest common superclass of two classes, robot classes included, for recomputed frames. */
    private String commonSuperClass(String a, String b) {
        List<String> chain = superChain(a);
        if (chain == null) return "java/lang/Object";
        for (String c = b; c != null; c = superOf(c))
            if (chain.contains(c)) return c;
        return "java/lang/Object";
    }

    private List<String> superChain(String a) {
        List<String> out = new ArrayList<>();
        for (String c = a; c != null; c = superOf(c)) out.add(c);
        return out;
    }

    /** The superclass of a class (null for Object, interfaces and unknown classes). */
    private String superOf(String internal) {
        if (internal.equals("java/lang/Object")) return null;
        Info i = robotInfo(internal);
        if (i != null) return i.isInterface ? null : i.superName;
        try {
            Class<?> c = Class.forName(internal.replace('/', '.'), false, getParent());
            if (c.isInterface()) return null;
            Class<?> sup = c.getSuperclass();
            return sup == null ? null : sup.getName().replace('.', '/');
        } catch (ClassNotFoundException | LinkageError e) {
            return null;
        }
    }

    private void rewriteMethod(MethodNode m, Set<String> self) {
        InsnList code = m.instructions;
        AbstractInsnNode[] insns = code.toArray();
        // Per instruction: the robot class it initialises (an index into `classes`), or -1.
        int[] trigger = new int[insns.length];
        List<Info> classes = new ArrayList<>();
        Map<Info, Integer> classIds = new HashMap<>();
        for (int k = 0; k < insns.length; k++) {
            AbstractInsnNode insn = insns[k];
            Info owner = null;
            int op = insn.getOpcode();
            if (op == Opcodes.GETSTATIC || op == Opcodes.PUTSTATIC) {
                FieldInsnNode f = (FieldInsnNode) insn;
                owner = fieldOwner(f.owner, f.name + f.desc);
                if (owner != null && owner.staticFields.get(f.name + f.desc)) f.owner = owner.name + FIELDS;
            } else if (op == Opcodes.INVOKESTATIC) {
                MethodInsnNode mi = (MethodInsnNode) insn;
                owner = methodOwner(mi.owner, mi.name + mi.desc, mi.itf);
            } else if (op == Opcodes.NEW) {
                owner = robotInfo(((TypeInsnNode) insn).desc);
            }
            trigger[k] = -1;
            if (owner != null && !self.contains(owner.name) && !EAGER.contains(owner.name) && needsInit(owner))
                trigger[k] = classIds.computeIfAbsent(owner, o -> {
                    classes.add(o);
                    return classes.size() - 1;
                });
        }
        if (classes.isEmpty()) return;
        BitSet[] done = initialisedBefore(m, insns, trigger);
        for (int k = 0; k < insns.length; k++) {
            if (trigger[k] < 0 || (done[k] != null && done[k].get(trigger[k]))) continue;
            MethodInsnNode init =
                    new MethodInsnNode(Opcodes.INVOKESTATIC, classes.get(trigger[k]).name + HOLDER, "c", "()V", false);
            // After a NEW, not before it: a frame holding the uninitialised object (`new X(c ? a : b)`) names the
            // label right before the NEW, which must stay there. NEW only allocates, so the order is not observable.
            if (insns[k].getOpcode() == Opcodes.NEW) code.insert(insns[k], init);
            else code.insertBefore(insns[k], init);
        }
    }

    /**
     * Forward must-analysis over the control flow graph: done[k] holds the classes a trigger has initialised on
     * every path to instruction k (null when k is unreachable). Exception handlers start from what held at the
     * start of the instruction that threw, since a failed initialisation still counts as started.
     */
    private static BitSet[] initialisedBefore(MethodNode m, AbstractInsnNode[] insns, int[] trigger) {
        int n = insns.length;
        Map<LabelNode, Integer> at = new HashMap<>();
        for (int k = 0; k < n; k++)
            if (insns[k] instanceof LabelNode l) at.put(l, k);
        List<int[]> handlers = new ArrayList<>();  // start, end, handler
        for (TryCatchBlockNode t : m.tryCatchBlocks)
            handlers.add(new int[] {at.get(t.start), at.get(t.end), at.get(t.handler)});

        BitSet[] in = new BitSet[n];
        ArrayDeque<Integer> work = new ArrayDeque<>();
        if (n > 0) {
            in[0] = new BitSet();
            work.add(0);
        }
        boolean[] queued = new boolean[n];
        if (n > 0) queued[0] = true;
        while (!work.isEmpty()) {
            int k = work.poll();
            queued[k] = false;
            BitSet out = (BitSet) in[k].clone();
            if (trigger[k] >= 0) out.set(trigger[k]);
            for (int[] h : handlers)
                if (k >= h[0] && k < h[1]) flow(in, work, queued, h[2], in[k]);
            AbstractInsnNode insn = insns[k];
            int op = insn.getOpcode();
            if (insn instanceof JumpInsnNode j) {
                flow(in, work, queued, at.get(j.label), out);
                if (op != Opcodes.GOTO && op != Opcodes.JSR && k + 1 < n) flow(in, work, queued, k + 1, out);
            } else if (insn instanceof TableSwitchInsnNode s) {
                flow(in, work, queued, at.get(s.dflt), out);
                for (LabelNode l : s.labels) flow(in, work, queued, at.get(l), out);
            } else if (insn instanceof LookupSwitchInsnNode s) {
                flow(in, work, queued, at.get(s.dflt), out);
                for (LabelNode l : s.labels) flow(in, work, queued, at.get(l), out);
            } else if (!(op >= Opcodes.IRETURN && op <= Opcodes.RETURN) && op != Opcodes.ATHROW && op != Opcodes.RET
                    && k + 1 < n) {
                flow(in, work, queued, k + 1, out);
            }
        }
        return in;
    }

    private static void flow(BitSet[] in, ArrayDeque<Integer> work, boolean[] queued, int to, BitSet state) {
        if (in[to] == null) {
            in[to] = (BitSet) state.clone();
        } else {
            BitSet merged = (BitSet) in[to].clone();
            merged.and(state);
            if (merged.equals(in[to])) return;
            in[to] = merged;
        }
        if (!queued[to]) {
            queued[to] = true;
            work.add(to);
        }
    }

    /** X$$F: X's moved static fields, as plain public statics. */
    private byte[] fieldsBytes(Info info) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC, info.name + FIELDS, null,
                "java/lang/Object", null);
        for (FieldNode f : info.moved)
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, f.name, f.desc, null, null).visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    /**
     * X$$S implements Statics: one robot's copy of X$$F, plus
     *   static void c()   initialise X for the running robot unless it already is.
     */
    private byte[] holderBytes(Info info) {
        String holder = info.name + HOLDER, fields = info.name + FIELDS;
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String a, String b) {
                return "java/lang/Object";
            }
        };
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC, holder, null,
                "java/lang/Object", new String[] {"fish/Statics"});
        for (FieldNode f : info.moved)
            cw.visitField(Opcodes.ACC_PRIVATE, f.name, f.desc, null, null).visitEnd();

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "save", "()V", null, null);
        mv.visitCode();
        for (FieldNode f : info.moved) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETSTATIC, fields, f.name, f.desc);
            mv.visitFieldInsn(Opcodes.PUTFIELD, holder, f.name, f.desc);
        }
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "fresh", "()Lfish/Statics;", null, null);
        mv.visitCode();
        mv.visitTypeInsn(Opcodes.NEW, holder);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, holder, "<init>", "()V", false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "load", "()V", null, null);
        mv.visitCode();
        for (FieldNode f : info.moved) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, holder, f.name, f.desc);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, fields, f.name, f.desc);
        }
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // c(): if (IDX < h.length && h[IDX] != null) return; JVMS 5.5: mark X initialised for this robot (so
        // re-entry from its own initialiser proceeds), initialise the superclass and default-method interfaces,
        // then run X's static initialiser. The statics hold their default values until then.
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "c", "()V", null, null);
        mv.visitCode();
        Label init = new Label();
        mv.visitFieldInsn(Opcodes.GETSTATIC, CTX, "cur", "L" + CTX + ";");
        mv.visitFieldInsn(Opcodes.GETFIELD, CTX, "holders", "[Ljava/lang/Object;");
        mv.visitVarInsn(Opcodes.ASTORE, 0);
        pushInt(mv, info.index);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInsn(Opcodes.ARRAYLENGTH);
        mv.visitJumpInsn(Opcodes.IF_ICMPGE, init);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        pushInt(mv, info.index);
        mv.visitInsn(Opcodes.AALOAD);
        mv.visitJumpInsn(Opcodes.IFNULL, init);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitLabel(init);
        pushInt(mv, info.index);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, CTX, "grow", "(I)[Ljava/lang/Object;", false);
        pushInt(mv, info.index);
        mv.visitTypeInsn(Opcodes.NEW, holder);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, holder, "<init>", "()V", false);
        mv.visitInsn(Opcodes.AASTORE);
        if (!info.isInterface) {
            Info sup = robotInfo(info.superName);
            if (sup != null && needsInit(sup))
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, sup.name + HOLDER, "c", "()V", false);
            for (Info itf : initInterfaces(info))
                if (needsInit(itf)) mv.visitMethodInsn(Opcodes.INVOKESTATIC, itf.name + HOLDER, "c", "()V", false);
        }
        if (info.hasClinit) mv.visitMethodInsn(Opcodes.INVOKESTATIC, info.name, CLINIT, "()V", info.isInterface);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void pushInt(MethodVisitor mv, int v) {
        if (v >= -1 && v <= 5) mv.visitInsn(Opcodes.ICONST_0 + v);
        else if (v >= Byte.MIN_VALUE && v <= Byte.MAX_VALUE) mv.visitIntInsn(Opcodes.BIPUSH, v);
        else if (v >= Short.MIN_VALUE && v <= Short.MAX_VALUE) mv.visitIntInsn(Opcodes.SIPUSH, v);
        else mv.visitLdcInsn(v);
    }
}
