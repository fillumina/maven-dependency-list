package com.fillumina.maven.reverse.dependency;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Runs a command line with its output captured, so a test can assert on what the
 * tool printed rather than on what it returned.
 */
final class CommandOutput {

    private CommandOutput() {
    }

    /**
     * Returns what {@code runnable} printed, as {stdout, stderr}, and leaves the
     * real streams as it found them.
     */
    static String[] capture(IoRunnable runnable) throws IOException {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(err, true, StandardCharsets.UTF_8));
            runnable.run();
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
        return new String[]{
                out.toString(StandardCharsets.UTF_8),
                err.toString(StandardCharsets.UTF_8)};
    }

    @FunctionalInterface
    interface IoRunnable {
        void run() throws IOException;
    }
}
