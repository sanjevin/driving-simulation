package com.drivingsimulator.ui;

import com.drivingsimulator.exception.InputClosedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;

/** Raw console-input adapter. It deliberately has no simulation knowledge. */
public final class ConsoleReader {
    private final BufferedReader input;

    public ConsoleReader(BufferedReader input) {
        this.input = Objects.requireNonNull(input, "input");
    }

    public String readLine() {
        try {
            String line = input.readLine();
            if (line == null) {
                throw new InputClosedException();
            }
            return line;
        } catch (IOException exception) {
            throw new InputClosedException();
        }
    }
}
