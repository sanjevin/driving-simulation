package com.drivingsimulator.test;

import java.io.PrintStream;
import java.util.Objects;

/** Minimal test runner used to keep this project dependency-free. */
final class TestSuite {
    private final PrintStream output;
    private int total;
    private int failures;

    TestSuite(PrintStream output) {
        this.output = Objects.requireNonNull(output, "output");
    }

    void test(String name, TestAction action) {
        total++;
        try {
            action.run();
            output.println("PASS: " + name);
        } catch (Throwable failure) {
            failures++;
            output.println("FAIL: " + name + " - " + failure.getMessage());
        }
    }

    int printSummary() {
        output.println("\n" + (total - failures) + "/" + total + " tests passed.");
        return failures;
    }

    @FunctionalInterface
    interface TestAction {
        void run() throws Exception;
    }
}
