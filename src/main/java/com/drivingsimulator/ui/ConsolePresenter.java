package com.drivingsimulator.ui;

import com.drivingsimulator.model.CarPlan;
import com.drivingsimulator.model.CarResult;
import com.drivingsimulator.model.CollisionEvent;
import com.drivingsimulator.model.InvalidCommandEvent;
import com.drivingsimulator.model.SimulationEvent;
import com.drivingsimulator.model.SimulationResult;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** All console rendering is kept here so the engine never writes to stdout. */
public final class ConsolePresenter {
    private final PrintStream output;

    public ConsolePresenter(PrintStream output) {
        this.output = Objects.requireNonNull(output, "output");
    }

    public void printWelcome() {
        output.println("Welcome to Car Crash Java!");
    }

    public void printPrompt(String prompt) {
        output.println(prompt);
    }

    public void printFieldCreated(int width, int height) {
        output.println("You have created a field of " + width + " x " + height + ".");
    }

    public void printMainMenu() {
        output.println("\nPlease choose from the following options:");
        output.println("[1] Add a car to field");
        output.println("[2] Run simulation");
    }

    public void printPostSimulationMenu() {
        output.println("\nPlease choose from the following options:");
        output.println("[1] Start over");
        output.println("[2] Exit");
    }

    public void printInvalidField(String message) {
        output.println("Invalid field: " + message);
    }

    public void printCarNotAdded(String message) {
        output.println("Car was not added: " + message);
    }

    public void printInvalidSelection() {
        output.println("Please enter 1 or 2.");
    }

    public void printNoCars() {
        output.println("Add at least one car before running the simulation.");
    }

    public void printCars(List<CarPlan> cars) {
        output.println("\nYour current list of cars:");
        for (CarPlan car : cars) {
            output.println("- " + car.name() + ", " + car.start() + " " + car.direction()
                    + ", " + car.commands());
        }
    }

    public void printSimulationResult(SimulationResult result) {
        output.println("\nAfter simulation, the result is:");
        Map<String, CollisionEvent> firstCollisionByCar = firstCollisionByCar(result.events());
        for (CarResult car : result.cars()) {
            CollisionEvent collision = firstCollisionByCar.get(car.name());
            if (collision == null) {
                output.println("- " + car.name() + ", " + car.position() + " " + car.direction());
                continue;
            }

            String otherCar = collision.movingCar().equals(car.name())
                    ? collision.occupyingCar() : collision.movingCar();
            output.println("- " + car.name() + ", collides with " + otherCar + " at "
                    + collision.attemptedPosition() + " at step " + collision.step());
        }

        for (SimulationEvent event : result.events()) {
            if (event instanceof InvalidCommandEvent invalid) {
                output.println("  Ignored invalid command '" + invalid.command() + "' for "
                        + invalid.carName() + " at step " + invalid.step() + ".");
            }
        }
    }

    public void printGoodbye() {
        output.println("Thank you for running the simulation. Goodbye!");
    }

    public void printInputClosed() {
        output.println("\nInput closed. Goodbye!");
    }

    private static Map<String, CollisionEvent> firstCollisionByCar(List<SimulationEvent> events) {
        Map<String, CollisionEvent> collisions = new LinkedHashMap<>();
        for (SimulationEvent event : events) {
            if (event instanceof CollisionEvent collision) {
                collisions.putIfAbsent(collision.movingCar(), collision);
                collisions.putIfAbsent(collision.occupyingCar(), collision);
            }
        }
        return collisions;
    }
}
