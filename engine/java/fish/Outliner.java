package fish;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Splits oversized methods of an instrumented robot class into helper methods, so the JIT's C1 compiler, which gives
 * up on the bot's giant generated methods ("out of virtual registers"), can compile them.
 *
 * It runs after instrumentation, when every bytecode charge is already an explicit RobotMonitor.incrementBytecodes(n)
 * call. A region of a method moves into a new private static helper unchanged, so the same charges happen in the
 * same order and a robot pauses at the same points (a pause inside a helper is just a deeper continuation frame).
 * The call to the helper and the code around it are not instrumented, so nothing a bot can observe changes.
 *
 * A region starts and ends at labels that carry a stack map frame with an empty operand stack, so the types of the
 * locals at its edges are exact. It has a single entry: no jump from outside lands inside it. Its exits (jumps out,
 * falling through its end, returns) become an exit code the helper returns, and the caller dispatches on it. Locals
 * live at the entry are passed as arguments; locals the region writes that are live at an exit come back through
 * Spill, a static area outside the bot: the helper stores them just before it returns and the caller loads them
 * just after the call, with no instrumented code (so no pause, so no other robot) in between.
 */
final class Outliner {
    static final int MIN_INSNS = 1500;  // methods at least this many real instructions are split
    static final int TARGET = 900;      // a region is at most this many real instructions
    static final int MIN_REGION = 120;  // smaller regions are not worth a call
    private static final int MAX_EXITS = 250;
    private static final String DEBUG = System.getenv("FISH_OUTLINE_DEBUG");

    /** The class hierarchy, for frames: the superclass of an internal name, or null when unknown or an interface. */
    interface Hierarchy {
        String superName(String internal);
    }

    private final ClassNode cn;
    private int helpers;

    private Outliner(ClassNode cn) {
        this.cn = cn;
    }

    /** Splits the oversized methods of cn in place; returns whether anything changed. */
    static boolean apply(ClassNode cn) {
        Outliner o = new Outliner(cn);
        boolean changed = false;
        for (MethodNode m : new ArrayList<>(cn.methods)) {
            if (m.name.startsWith("<") || (m.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) continue;
            if (!m.tryCatchBlocks.isEmpty() || realSize(m.instructions.getFirst(), null) < MIN_INSNS) continue;
            try {
                changed |= o.split(m);
            } catch (RuntimeException e) {
                // A method the analysis cannot handle stays as it is.
            }
        }
        return changed;
    }

    private static int realSize(AbstractInsnNode from, AbstractInsnNode to) {
        int n = 0;
        for (AbstractInsnNode i = from; i != null && i != to; i = i.getNext())
            if (i.getOpcode() >= 0) n++;
        return n;
    }

    // ---- per method analysis ----

    private AbstractInsnNode[] insns;
    private int n;
    private boolean[] emptyStack;       // labels whose frame has an empty operand stack
    private Object[][] types;           // expanded frame locals (slot-indexed) at nodes that start a frame, else null
    private BitSet[] liveIn;
    private int[] realBefore;           // real instructions before node i
    private Map<LabelNode, Integer> index;
    private List<List<Integer>> sources;  // per node: jump/switch sources that target it (labels only)

    private boolean split(MethodNode m) {
        insns = m.instructions.toArray();
        n = insns.length;
        index = new HashMap<>();
        for (int i = 0; i < n; i++)
            if (insns[i] instanceof LabelNode l) index.put(l, i);
        expandFrames(m);
        liveness(m);
        realBefore = new int[n + 1];
        for (int i = 0; i < n; i++) realBefore[i + 1] = realBefore[i] + (insns[i].getOpcode() >= 0 ? 1 : 0);
        sources = new ArrayList<>(n);
        for (int i = 0; i < n; i++) sources.add(null);
        for (int i = 0; i < n; i++)
            for (int t : jumpTargets(insns[i])) {
                if (sources.get(t) == null) sources.set(t, new ArrayList<>());
                sources.get(t).add(i);
            }

        // Cut candidates: labels with a frame and an empty stack.
        List<Integer> cuts = new ArrayList<>();
        for (int i = 0; i < n; i++)
            if (insns[i] instanceof LabelNode && types[i] != null && emptyStack[i])
                cuts.add(i);

        List<int[]> regions = new ArrayList<>();
        for (int a = 0; a < cuts.size(); a++) {
            int s = cuts.get(a), best = -1;
            for (int b = a + 1; b < cuts.size(); b++) {
                int e = cuts.get(b);
                if (realBefore[e] - realBefore[s] > TARGET) break;
                if (valid(s, e)) best = b;
            }
            if (best >= 0 && realBefore[cuts.get(best)] - realBefore[s] >= MIN_REGION) {
                regions.add(new int[] {s, cuts.get(best)});
                a = best - 1;
            }
        }
        if (regions.isEmpty()) return false;
        // Build helpers last to first, so earlier node indices stay valid.
        for (int r = regions.size() - 1; r >= 0; r--) outline(m, regions.get(r)[0], regions.get(r)[1]);
        m.localVariables = null;  // their ranges may point into moved code
        return true;
    }

    private int[] jumpTargets(AbstractInsnNode i) {
        if (i instanceof JumpInsnNode j) return new int[] {index.get(j.label)};
        if (i instanceof TableSwitchInsnNode t) {
            int[] out = new int[t.labels.size() + 1];
            for (int k = 0; k < t.labels.size(); k++) out[k] = index.get(t.labels.get(k));
            out[t.labels.size()] = index.get(t.dflt);
            return out;
        }
        if (i instanceof LookupSwitchInsnNode t) {
            int[] out = new int[t.labels.size() + 1];
            for (int k = 0; k < t.labels.size(); k++) out[k] = index.get(t.labels.get(k));
            out[t.labels.size()] = index.get(t.dflt);
            return out;
        }
        return new int[0];
    }

    private static boolean fallsThrough(AbstractInsnNode i) {
        int op = i.getOpcode();
        if (op < 0) return true;
        return !(op == Opcodes.GOTO || (op >= Opcodes.IRETURN && op <= Opcodes.RETURN) || op == Opcodes.ATHROW
                || i instanceof TableSwitchInsnNode || i instanceof LookupSwitchInsnNode);
    }

    /** Can [s, e) become a helper: one entry, exits to cut points, nothing the helper cannot do. */
    private boolean valid(int s, int e) {
        int exits = 0;
        for (int i = s; i < e; i++) {
            AbstractInsnNode x = insns[i];
            if (i > s && sources.get(i) != null)
                for (int src : sources.get(i))
                    if (src < s || src >= e) return false;
            int op = x.getOpcode();
            if (op == Opcodes.MONITORENTER || op == Opcodes.MONITOREXIT || op == Opcodes.JSR || op == Opcodes.RET) return false;
            if (op == Opcodes.INVOKESPECIAL && !((MethodInsnNode) x).name.equals("<init>")) return false;
            for (int t : jumpTargets(x))
                if (t < s || t >= e) {
                    if (types[t] == null || !emptyStack[t]) return false;
                    exits++;
                }
        }
        if (exits > MAX_EXITS) return false;
        for (int slot = liveIn[s].nextSetBit(0); slot >= 0; slot = liveIn[s].nextSetBit(slot + 1))
            if (!knownType(types[s], slot)) return false;
        // Locals the region writes that an exit needs must have exact types there too.
        BitSet written = new BitSet();
        for (int i = s; i < e; i++) {
            if (insns[i] instanceof VarInsnNode v && isStore(v.getOpcode())) written.set(v.var);
            if (insns[i] instanceof IincInsnNode inc) written.set(inc.var);
        }
        for (int i = s; i < e; i++)
            for (int t : jumpTargets(insns[i]))
                if ((t < s || t >= e) && !spillTypesKnown(t, written)) return false;
        return spillTypesKnown(e, written);
    }

    private boolean spillTypesKnown(int t, BitSet written) {
        BitSet need = (BitSet) liveIn[t].clone();
        need.and(written);
        for (int slot = need.nextSetBit(0); slot >= 0; slot = need.nextSetBit(slot + 1))
            if (!knownType(types[t], slot)) return false;
        return true;
    }

    private static boolean knownType(Object[] t, int slot) {
        if (t == null || slot >= t.length) return false;
        Object v = t[slot];
        return v == Opcodes.INTEGER || v == Opcodes.FLOAT || v == Opcodes.LONG || v == Opcodes.DOUBLE || v instanceof String;
    }

    // ---- frames: compressed StackMapTable frames expanded to slot-indexed local types ----

    private void expandFrames(MethodNode m) {
        types = new Object[n][];
        emptyStack = new boolean[n];
        List<Object> cur = new ArrayList<>();
        if ((m.access & Opcodes.ACC_STATIC) == 0) cur.add(cn.name);
        for (Type t : Type.getArgumentTypes(m.desc)) cur.add(frameType(t));
        Object[] initial = slots(cur);
        // The method start: a cut candidate only if the first node is a label.
        for (int i = 0; i < n; i++) {
            if (insns[i] instanceof FrameNode f) {
                switch (f.type) {
                    case Opcodes.F_NEW, Opcodes.F_FULL -> {
                        cur = new ArrayList<>(f.local);
                    }
                    case Opcodes.F_APPEND -> cur.addAll(f.local);
                    case Opcodes.F_CHOP -> {
                        for (int k = 0; k < f.local.size(); k++) cur.remove(cur.size() - 1);
                    }
                    default -> { }  // F_SAME, F_SAME1: locals unchanged
                }
                Object[] t = slots(cur);
                boolean empty = f.type == Opcodes.F_SAME || f.type == Opcodes.F_APPEND || f.type == Opcodes.F_CHOP
                        || ((f.type == Opcodes.F_NEW || f.type == Opcodes.F_FULL) && (f.stack == null || f.stack.isEmpty()));
                // The frame belongs to the label just before it (line numbers may sit between them).
                for (int j = i - 1; j >= 0; j--) {
                    if (insns[j] instanceof LabelNode) {
                        types[j] = t;
                        emptyStack[j] = empty;
                        break;
                    }
                    if (insns[j].getOpcode() >= 0) break;
                }
            }
        }
        if (n > 0 && insns[0] instanceof LabelNode && types[0] == null) {
            types[0] = initial;
            emptyStack[0] = true;
        }
    }

    private static Object frameType(Type t) {
        return switch (t.getSort()) {
            case Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT, Type.INT -> Opcodes.INTEGER;
            case Type.FLOAT -> Opcodes.FLOAT;
            case Type.LONG -> Opcodes.LONG;
            case Type.DOUBLE -> Opcodes.DOUBLE;
            default -> t.getInternalName();
        };
    }

    /** Frame locals (longs and doubles one entry each) to a slot-indexed array (their second slot TOP). */
    private static Object[] slots(List<Object> locals) {
        List<Object> out = new ArrayList<>();
        for (Object v : locals) {
            out.add(v);
            if (v == Opcodes.LONG || v == Opcodes.DOUBLE) out.add(Opcodes.TOP);
        }
        return out.toArray();
    }

    // ---- liveness ----

    private void liveness(MethodNode m) {
        liveIn = new BitSet[n];
        for (int i = 0; i < n; i++) liveIn[i] = new BitSet();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = n - 1; i >= 0; i--) {
                AbstractInsnNode x = insns[i];
                BitSet out = new BitSet();
                if (fallsThrough(x) && i + 1 < n) out.or(liveIn[i + 1]);
                for (int t : jumpTargets(x)) out.or(liveIn[t]);
                if (x instanceof VarInsnNode v) {
                    if (isStore(v.getOpcode())) out.clear(v.var);
                    else out.set(v.var);
                } else if (x instanceof IincInsnNode inc) {
                    out.set(inc.var);
                }
                if (!out.equals(liveIn[i])) {
                    liveIn[i] = out;
                    changed = true;
                }
            }
        }
    }

    private static boolean isStore(int op) {
        return op >= Opcodes.ISTORE && op <= Opcodes.ASTORE;
    }

    private static int size(Object frameType) {
        return frameType == Opcodes.LONG || frameType == Opcodes.DOUBLE ? 2 : 1;
    }

    private static String desc(Object frameType) {
        if (frameType == Opcodes.INTEGER) return "I";
        if (frameType == Opcodes.FLOAT) return "F";
        if (frameType == Opcodes.LONG) return "J";
        if (frameType == Opcodes.DOUBLE) return "D";
        String s = (String) frameType;
        return s.startsWith("[") ? s : "L" + s + ";";
    }

    private static int load(Object t) {
        return t == Opcodes.INTEGER ? Opcodes.ILOAD : t == Opcodes.FLOAT ? Opcodes.FLOAD : t == Opcodes.LONG ? Opcodes.LLOAD
                : t == Opcodes.DOUBLE ? Opcodes.DLOAD : Opcodes.ALOAD;
    }

    private static int store(Object t) {
        return load(t) - Opcodes.ILOAD + Opcodes.ISTORE;
    }

    /** Spill accessor suffix and descriptor char for a frame type. */
    private static String kind(Object t) {
        return t == Opcodes.INTEGER ? "I" : t == Opcodes.FLOAT ? "F" : t == Opcodes.LONG ? "J" : t == Opcodes.DOUBLE ? "D" : "A";
    }

    private static String spillDesc(Object t) {
        String k = kind(t);
        return k.equals("A") ? "Ljava/lang/Object;" : k;
    }

    // ---- building a helper ----

    private void outline(MethodNode m, int s, int e) {
        // Locals written inside the region.
        BitSet written = new BitSet();
        for (int i = s; i < e; i++) {
            if (insns[i] instanceof VarInsnNode v && isStore(v.getOpcode())) written.set(v.var);
            if (insns[i] instanceof IincInsnNode inc) written.set(inc.var);
        }
        // Exits: jump targets outside, the fall-through end, returns.
        List<Integer> exitTargets = new ArrayList<>();
        Map<Integer, Integer> exitCode = new HashMap<>();
        boolean returns = false;
        for (int i = s; i < e; i++) {
            AbstractInsnNode x = insns[i];
            for (int t : jumpTargets(x))
                if ((t < s || t >= e) && !exitCode.containsKey(t)) {
                    exitCode.put(t, exitTargets.size());
                    exitTargets.add(t);
                }
            int op = x.getOpcode();
            if (op >= Opcodes.IRETURN && op <= Opcodes.RETURN) returns = true;
        }
        AbstractInsnNode last = null;
        for (int i = e - 1; i >= s; i--)
            if (insns[i].getOpcode() >= 0) {
                last = insns[i];
                break;
            }
        boolean fallthrough = last == null || fallsThrough(last);
        if (fallthrough && !exitCode.containsKey(e)) {
            exitCode.put(e, exitTargets.size());
            exitTargets.add(e);
        }
        int returnCode = exitTargets.size();
        boolean coded = exitTargets.size() + (returns ? 1 : 0) > 1 || returns;

        // Parameters: live-in locals, in slot order.
        Object[] in = types[s];
        List<Integer> params = new ArrayList<>();
        StringBuilder pdesc = new StringBuilder("(");
        for (int slot = liveIn[s].nextSetBit(0); slot >= 0; slot = liveIn[s].nextSetBit(slot + 1)) {
            params.add(slot);
            pdesc.append(desc(in[slot]));
        }
        String hdesc = pdesc + (coded ? ")I" : ")V");
        String hname = m.name + "$o" + (helpers++);

        // Slot remapping: parameters first, then every other local the region uses. javac reuses a slot for values
        // of different sizes in different scopes, so a slot gets two new slots if it ever holds a long or double.
        BitSet wide = new BitSet();
        for (int i = s; i < e; i++) {
            int op = insns[i].getOpcode();
            if (insns[i] instanceof VarInsnNode v
                    && (op == Opcodes.LLOAD || op == Opcodes.DLOAD || op == Opcodes.LSTORE || op == Opcodes.DSTORE))
                wide.set(v.var);
        }
        for (int slot : params)
            if (size(in[slot]) == 2) wide.set(slot);
        for (int k = 0; k < exitTargets.size(); k++)
            for (int slot : spillsAt(exitTargets.get(k), written))
                if (size(types[exitTargets.get(k)][slot]) == 2) wide.set(slot);
        Map<Integer, Integer> remap = new HashMap<>();
        int next = 0;
        for (int slot : params) {
            remap.put(slot, next);
            next += wide.get(slot) ? 2 : 1;
        }
        for (int i = s; i < e; i++) {
            int var = insns[i] instanceof VarInsnNode v ? v.var : insns[i] instanceof IincInsnNode inc ? inc.var : -1;
            if (var < 0 || remap.containsKey(var)) continue;
            remap.put(var, next);
            next += wide.get(var) ? 2 : 1;
        }

        if (DEBUG != null && hname.startsWith(DEBUG))
            System.err.println("outline " + cn.name + "." + hname + hdesc + " s=" + s + " e=" + e + " params=" + params
                    + " remap=" + remap + " wide=" + wide + " in=" + java.util.Arrays.toString(in) + " exits=" + exitTargets
                    + " written=" + written);
        MethodNode h = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC, hname, hdesc, null, null);
        Map<LabelNode, LabelNode> labels = new HashMap<>();
        for (int i = s; i < e; i++)
            if (insns[i] instanceof LabelNode l) labels.put(l, new LabelNode());
        // Exit stubs: spill what the caller needs, return the exit code.
        List<LabelNode> stubs = new ArrayList<>();
        List<List<Integer>> spills = new ArrayList<>();
        for (int k = 0; k < exitTargets.size(); k++) {
            int t = exitTargets.get(k);
            LabelNode stub = new LabelNode();
            stubs.add(stub);
            labels.put((LabelNode) insns[t], stub);
            spills.add(spillsAt(t, written));
        }
        for (int i = s; i < e; i++) {
            AbstractInsnNode x = insns[i];
            if (x instanceof FrameNode || x instanceof LineNumberNode) continue;
            int op = x.getOpcode();
            if (op >= Opcodes.IRETURN && op <= Opcodes.RETURN) {
                if (op != Opcodes.RETURN)
                    h.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "fish/Spill", "r" + returnKind(op),
                            "(" + returnDesc(op) + ")V", false));
                h.instructions.add(intConst(returnCode));
                h.instructions.add(new InsnNode(Opcodes.IRETURN));
                continue;
            }
            AbstractInsnNode c = x.clone(labels);
            if (c instanceof VarInsnNode v) v.var = remap.get(v.var);
            if (c instanceof IincInsnNode inc) inc.var = remap.get(inc.var);
            h.instructions.add(c);
        }
        if (fallthrough) h.instructions.add(new JumpInsnNode(Opcodes.GOTO, stubs.get(exitCode.get(e))));
        for (int k = 0; k < exitTargets.size(); k++) {
            h.instructions.add(stubs.get(k));
            Object[] at = types[exitTargets.get(k)];
            List<Integer> sp = spills.get(k);
            for (int q = 0; q < sp.size(); q++) {
                int slot = sp.get(q);
                h.instructions.add(new VarInsnNode(load(at[slot]), remap.get(slot)));
                h.instructions.add(intConst(q));
                h.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "fish/Spill", "s" + kind(at[slot]),
                        "(" + spillDesc(at[slot]) + "I)V", false));
            }
            if (coded) {
                h.instructions.add(intConst(k));
                h.instructions.add(new InsnNode(Opcodes.IRETURN));
            } else {
                h.instructions.add(new InsnNode(Opcodes.RETURN));
            }
        }
        h.maxStack = 0;
        h.maxLocals = 0;
        cn.methods.add(h);

        // The call site replaces the region in the method; the label at s stays, so jumps to it still land here.
        InsnList call = new InsnList();
        for (int slot : params) call.add(new VarInsnNode(load(in[slot]), slot));
        call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, cn.name, hname, hdesc, false));
        List<LabelNode> dispatch = new ArrayList<>();
        if (coded) {
            for (int k = 0; k <= returnCode; k++) dispatch.add(new LabelNode());
            LabelNode dflt = dispatch.get(returnCode);
            call.add(new TableSwitchInsnNode(0, returnCode, dflt, dispatch.toArray(new LabelNode[0])));
        }
        for (int k = 0; k < exitTargets.size(); k++) {
            if (coded) call.add(dispatch.get(k));
            Object[] at = types[exitTargets.get(k)];
            List<Integer> sp = spills.get(k);
            for (int q = 0; q < sp.size(); q++) {
                int slot = sp.get(q);
                call.add(intConst(q));
                call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "fish/Spill", "g" + kind(at[slot]),
                        "(I)" + spillDesc(at[slot]), false));
                if (at[slot] instanceof String ref) call.add(new TypeInsnNode(Opcodes.CHECKCAST, ref));
                call.add(new VarInsnNode(store(at[slot]), slot));
            }
            call.add(new JumpInsnNode(Opcodes.GOTO, (LabelNode) insns[exitTargets.get(k)]));
        }
        if (coded) {
            call.add(dispatch.get(returnCode));
            Type rt = Type.getReturnType(m.desc);
            if (returns && rt.getSort() != Type.VOID) {
                String k = switch (rt.getSort()) {
                    case Type.LONG -> "J";
                    case Type.FLOAT -> "F";
                    case Type.DOUBLE -> "D";
                    case Type.OBJECT, Type.ARRAY -> "A";
                    default -> "I";
                };
                call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "fish/Spill", "rr" + k,
                        "()" + (k.equals("A") ? "Ljava/lang/Object;" : k), false));
                if (k.equals("A")) call.add(new TypeInsnNode(Opcodes.CHECKCAST, rt.getInternalName()));
                call.add(new InsnNode(rt.getOpcode(Opcodes.IRETURN)));
            } else if (returns) {
                call.add(new InsnNode(Opcodes.RETURN));
            } else {
                // No return exit: the default case is unreachable.
                call.add(new InsnNode(Opcodes.ACONST_NULL));
                call.add(new InsnNode(Opcodes.ATHROW));
            }
        }
        // Remove the region's nodes after its first label, then put the call there.
        AbstractInsnNode first = insns[s];
        for (int i = e - 1; i > s; i--) m.instructions.remove(insns[i]);
        m.instructions.insert(first, call);
    }

    /** The locals an exit to t needs back from the helper: written in the region and live at t. */
    private List<Integer> spillsAt(int t, BitSet written) {
        List<Integer> sp = new ArrayList<>();
        BitSet need = (BitSet) liveIn[t].clone();
        need.and(written);
        for (int slot = need.nextSetBit(0); slot >= 0; slot = need.nextSetBit(slot + 1)) sp.add(slot);
        return sp;
    }

    private static String returnKind(int op) {
        return switch (op) {
            case Opcodes.LRETURN -> "J";
            case Opcodes.FRETURN -> "F";
            case Opcodes.DRETURN -> "D";
            case Opcodes.ARETURN -> "A";
            default -> "I";
        };
    }

    private static String returnDesc(int op) {
        String k = returnKind(op);
        return k.equals("A") ? "Ljava/lang/Object;" : k;
    }

    private static AbstractInsnNode intConst(int v) {
        if (v >= -1 && v <= 5) return new InsnNode(Opcodes.ICONST_0 + v);
        if (v <= Short.MAX_VALUE) return new IntInsnNode(Opcodes.SIPUSH, v);
        return new LdcInsnNode(v);
    }
}
