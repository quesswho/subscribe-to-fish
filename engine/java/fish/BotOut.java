package fish;

import battlecode.common.Team;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** A team's System.out: every line starts with RoboPrintStream's "[A: #id@round] " header. */
final class BotOut extends PrintStream {
    private final OutputStream sink;
    private final Team team;
    private byte[] header = new byte[0];
    private boolean lineStart = true;

    BotOut(OutputStream sink, Team team) {
        super(sink, false, StandardCharsets.UTF_8);
        this.sink = sink;
        this.team = team;
    }

    void header(int id, int round) {
        header = ("[" + team + ": #" + id + "@" + round + "] ").getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public synchronized void write(int b) {
        write(new byte[] {(byte) b}, 0, 1);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        try {
            for (int i = off; i < off + len; i++) {
                if (lineStart) sink.write(header);
                sink.write(b[i]);
                lineStart = b[i] == '\n';
            }
        } catch (java.io.IOException e) {
            setError();
        }
    }

    @Override
    public synchronized void flush() {
        try {
            sink.flush();
        } catch (java.io.IOException e) {
            setError();
        }
    }
}
