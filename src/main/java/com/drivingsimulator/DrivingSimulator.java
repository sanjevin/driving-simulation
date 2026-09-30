package com.drivingsimulator;

import com.drivingsimulator.exception.InputClosedException;
import com.drivingsimulator.ui.ConsoleApplication;
import com.drivingsimulator.ui.ConsolePresenter;
import com.drivingsimulator.ui.ConsoleReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/** Application entry point. */
public final class DrivingSimulator {
    private DrivingSimulator() {
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            System.err.println("Usage: java com.drivingsimulator.DrivingSimulator");
            System.exit(2);
            return;
        }

        ConsolePresenter presenter = new ConsolePresenter(System.out);
        try {
            new ConsoleApplication(
                    new ConsoleReader(new BufferedReader(new InputStreamReader(System.in))),
                    presenter
            ).run();
        } catch (InputClosedException ignored) {
            presenter.printInputClosed();
        }
    }
}
