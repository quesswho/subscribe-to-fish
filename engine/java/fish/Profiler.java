package fish;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * FISH_PROFILE=FILE: samples the engine thread every millisecond and writes the hottest frames (self and
 * inclusive) to FILE when the process ends. Wall-clock sampling of bot code and the host, for finding slow spots.
 */
final class Profiler {
    private static final Map<String, Integer> self = new HashMap<>(), total = new HashMap<>();
    private static int samples;

    static void start(Thread target, String path) {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    return;
                }
                StackTraceElement[] st = target.getStackTrace();
                if (st.length == 0) continue;
                synchronized (Profiler.class) {
                    samples++;
                    self.merge(frame(st[0]), 1, Integer::sum);
                    java.util.Set<String> seen = new java.util.HashSet<>();
                    for (StackTraceElement e : st)
                        if (seen.add(frame(e))) total.merge(frame(e), 1, Integer::sum);
                }
            }
        }, "fish-profiler");
        t.setDaemon(true);
        t.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> write(path)));
    }

    private static String frame(StackTraceElement e) {
        return e.getClassName() + "." + e.getMethodName();
    }

    private static synchronized void write(String path) {
        try (FileWriter w = new FileWriter(path)) {
            w.write("samples " + samples + "\n\nself:\n");
            dump(w, self);
            w.write("\ninclusive:\n");
            dump(w, total);
        } catch (IOException ignored) {
        }
    }

    private static void dump(FileWriter w, Map<String, Integer> m) throws IOException {
        var rows = new java.util.ArrayList<>(m.entrySet());
        rows.sort((a, b) -> b.getValue() - a.getValue());
        for (int i = 0; i < Math.min(60, rows.size()); i++)
            w.write(String.format("%6.2f%%  %s%n", 100.0 * rows.get(i).getValue() / samples, rows.get(i).getKey()));
    }
}
