package fish;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Shared-class mode keeps one Class per bot class, and the JDK caches an enum's constants per Class: Enum.valueOf,
 * EnumMap and EnumSet would then hand one robot another robot's constants. A bot that uses them runs isolated.
 */
final class SharedSafety {
    private static final Map<String, Boolean> cache = new HashMap<>();

    static boolean check(File dir, String pkg) {
        return cache.computeIfAbsent(dir.getAbsolutePath() + ":" + pkg, k -> scan(dir.toPath().resolve(pkg.replace('.', '/'))));
    }

    private static boolean scan(Path root) {
        boolean[] safe = {true};
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(p -> p.toString().endsWith(".class")).forEach(p -> {
                try {
                    new ClassReader(Files.readAllBytes(p)).accept(new ClassVisitor(Opcodes.ASM9) {
                        @Override
                        public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex) {
                            // Every enum's compiler-made valueOf(String) calls Enum.valueOf; calling it is what counts.
                            boolean enumValueOf = name.equals("valueOf") && desc.startsWith("(Ljava/lang/String;)");
                            return new MethodVisitor(Opcodes.ASM9) {
                                @Override
                                public void visitMethodInsn(int op, String owner, String n, String d, boolean itf) {
                                    boolean botEnumValueOf = n.equals("valueOf") && d.equals("(Ljava/lang/String;)L" + owner + ";")
                                            && !owner.startsWith("java/");
                                    if (owner.equals("java/lang/Enum") && n.equals("valueOf") && !enumValueOf
                                            || botEnumValueOf
                                            || owner.equals("java/lang/Class") && n.startsWith("getEnumConstants")
                                            || owner.startsWith("java/util/Enum") || owner.contains("EnumSet"))
                                        unsafe(owner + "." + n);
                                }

                                @Override
                                public void visitTypeInsn(int op, String type) {
                                    if (type.startsWith("java/util/Enum")) unsafe(type);
                                }

                                private void unsafe(String what) {
                                    if (safe[0])
                                        System.err.println("fish: " + root + " uses " + what + "; running it isolated");
                                    safe[0] = false;
                                }
                            };
                        }
                    }, ClassReader.SKIP_DEBUG);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            return false;
        }
        return safe[0];
    }
}
