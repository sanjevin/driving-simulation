package com.drivingsimulator.service;

import com.drivingsimulator.enums.CarStatus;
import com.drivingsimulator.enums.Direction;
import com.drivingsimulator.exception.ValidationException;
import com.drivingsimulator.model.BoundaryEvent;
import com.drivingsimulator.model.CarPlan;
import com.drivingsimulator.model.CarResult;
import com.drivingsimulator.model.CollisionEvent;
import com.drivingsimulator.model.Field;
import com.drivingsimulator.model.InvalidCommandEvent;
import com.drivingsimulator.model.Position;
import com.drivingsimulator.model.SimulationEvent;
import com.drivingsimulator.model.SimulationResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Stateless, deterministic simulation engine.
 *
 * <p>For every step, cars are processed in their insertion order. A car sees
 * state changes made by preceding cars in that step. A collision freezes the
 * moving car and its occupant at their existing safe coordinates; other cars
 * continue. The engine creates all runtime state locally, so it is free of
 * retained state and locking concerns.</p>
 */
public final class SimulationEngine {
    public SimulationResult run(Field field, List<CarPlan> inputCars) {
        Objects.requireNonNull(field, "field");
        List<CarPlan> plans = List.copyOf(inputCars);
        validatePlans(field, plans);

        List<RuntimeCar> runtimeCars = new ArrayList<>();
        Map<Position, RuntimeCar> occupants = new LinkedHashMap<>();
        int longestCommandList = 0;
        for (CarPlan plan : plans) {
            RuntimeCar runtimeCar = new RuntimeCar(plan);
            runtimeCars.add(runtimeCar);
            occupants.put(runtimeCar.position, runtimeCar);
            longestCommandList = Math.max(longestCommandList, plan.commands().length());
        }

        List<SimulationEvent> events = new ArrayList<>();
        for (int step = 1; step <= longestCommandList; step++) {
            for (RuntimeCar car : runtimeCars) {
                if (car.crashed || car.plan.commands().length() < step) {
                    continue;
                }
                executeCommand(field, step, car, occupants, events);
            }
        }

        return new SimulationResult(runtimeCars.stream()
                .map(car -> new CarResult(
                        car.plan.name(),
                        car.position,
                        car.direction,
                        car.crashed ? CarStatus.CRASHED : CarStatus.COMPLETED))
                .toList(), events);
    }

    private void executeCommand(Field field, int step, RuntimeCar car,
                                Map<Position, RuntimeCar> occupants,
                                List<SimulationEvent> events) {
        char command = Character.toUpperCase(car.plan.commands().charAt(step - 1));
        switch (command) {
            case 'L' -> car.direction = car.direction.left();
            case 'R' -> car.direction = car.direction.right();
            case 'F' -> moveForward(field, step, car, occupants, events);
            default -> events.add(new InvalidCommandEvent(step, car.plan.name(), command));
        }
    }

    private void moveForward(Field field, int step, RuntimeCar car,
                             Map<Position, RuntimeCar> occupants,
                             List<SimulationEvent> events) {
        Position attemptedPosition = car.direction.move(car.position);
        if (!field.contains(attemptedPosition)) {
            events.add(new BoundaryEvent(step, car.plan.name(), attemptedPosition));
            return;
        }

        RuntimeCar occupant = occupants.get(attemptedPosition);
        if (occupant != null) {
            car.crashed = true;
            occupant.crashed = true;
            events.add(new CollisionEvent(step, car.plan.name(), occupant.plan.name(), attemptedPosition));
            return;
        }

        occupants.remove(car.position);
        car.position = attemptedPosition;
        occupants.put(car.position, car);
    }

    private void validatePlans(Field field, List<CarPlan> plans) {
        if (plans.isEmpty()) {
            throw new ValidationException("Add at least one car before running the simulation.");
        }

        Set<String> names = new LinkedHashSet<>();
        Set<Position> positions = new LinkedHashSet<>();
        for (CarPlan plan : plans) {
            if (!field.contains(plan.start())) {
                throw new ValidationException("Starting position " + plan.start() + " is outside the field.");
            }
            if (!names.add(plan.name().toLowerCase(Locale.ROOT))) {
                throw new ValidationException("Car name '" + plan.name() + "' is already in use.");
            }
            if (!positions.add(plan.start())) {
                throw new ValidationException("Starting position " + plan.start() + " is already occupied.");
            }
        }
    }

    private static final class RuntimeCar {
        private final CarPlan plan;
        private Position position;
        private Direction direction;
        private boolean crashed;

        private RuntimeCar(CarPlan plan) {
            this.plan = plan;
            this.position = plan.start();
            this.direction = plan.direction();
        }
    }
}
