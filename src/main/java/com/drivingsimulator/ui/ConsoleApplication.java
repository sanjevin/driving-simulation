package com.drivingsimulator.ui;

import com.drivingsimulator.enums.Direction;
import com.drivingsimulator.exception.ValidationException;
import com.drivingsimulator.model.CarPlan;
import com.drivingsimulator.model.Field;
import com.drivingsimulator.model.Position;
import com.drivingsimulator.service.SimulationSession;

import java.util.Objects;

/** Coordinates console interaction; movement and collision logic stay in the service layer. */
public final class ConsoleApplication {
    private final ConsoleReader reader;
    private final ConsolePresenter presenter;

    public ConsoleApplication(ConsoleReader reader, ConsolePresenter presenter) {
        this.reader = Objects.requireNonNull(reader, "reader");
        this.presenter = Objects.requireNonNull(presenter, "presenter");
    }

    public void run() {
        presenter.printWelcome();
        while (true) {
            Field field = promptForField();
            SimulationSession session = new SimulationSession(field);
            presenter.printFieldCreated(field.width(), field.height());

            if (!runFieldMenu(session)) {
                presenter.printGoodbye();
                return;
            }
        }
    }

    /** @return true when the user selected Start over. */
    private boolean runFieldMenu(SimulationSession session) {
        while (true) {
            presenter.printMainMenu();
            presenter.printPrompt("Selection:");
            switch (reader.readLine().trim()) {
                case "1" -> {
                    addCar(session);
                    presenter.printCars(session.cars());
                }
                case "2" -> {
                    if (!session.hasCars()) {
                        presenter.printNoCars();
                        continue;
                    }
                    presenter.printCars(session.cars());
                    presenter.printSimulationResult(session.run());
                    return promptAfterSimulation();
                }
                default -> presenter.printInvalidSelection();
            }
        }
    }

    private Field promptForField() {
        while (true) {
            try {
                presenter.printPrompt("Please enter the width and height of the simulation field in x y format:");
                String[] values = tokens(reader.readLine(), 2,
                        "Field must use exactly two numbers: width height.");
                return new Field(parsePositiveInt(values[0], "width"), parsePositiveInt(values[1], "height"));
            } catch (ValidationException exception) {
                presenter.printInvalidField(exception.getMessage());
            }
        }
    }

    private void addCar(SimulationSession session) {
        try {
            presenter.printPrompt("Please enter the name of the car:");
            String name = reader.readLine();
            presenter.printPrompt("Please enter initial position in x y Direction format:");
            String[] values = tokens(reader.readLine(), 3, "Position must use exactly: x y Direction.");
            Position position = new Position(parseInt(values[0], "x"), parseInt(values[1], "y"));
            Direction direction = Direction.parse(values[2]);
            presenter.printPrompt("Please enter the commands for car " + name.trim() + ":");
            String commands = reader.readLine();
            session.addCar(new CarPlan(name, position, direction, commands));
        } catch (ValidationException exception) {
            presenter.printCarNotAdded(exception.getMessage());
        }
    }

    private boolean promptAfterSimulation() {
        while (true) {
            presenter.printPostSimulationMenu();
            presenter.printPrompt("Selection:");
            switch (reader.readLine().trim()) {
                case "1" -> {
                    return true;
                }
                case "2" -> {
                    return false;
                }
                default -> presenter.printInvalidSelection();
            }
        }
    }

    private static String[] tokens(String line, int expectedCount, String error) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException(error);
        }
        String[] tokens = trimmed.split("\\s+");
        if (tokens.length != expectedCount) {
            throw new ValidationException(error);
        }
        return tokens;
    }

    private static int parsePositiveInt(String value, String label) {
        int parsed = parseInt(value, label);
        if (parsed <= 0) {
            throw new ValidationException(label + " must be a positive whole number.");
        }
        return parsed;
    }

    private static int parseInt(String value, String label) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new ValidationException(label + " must be a whole number within Java integer range.");
        }
    }
}
