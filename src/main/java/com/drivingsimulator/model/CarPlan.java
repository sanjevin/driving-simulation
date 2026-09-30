package com.drivingsimulator.model;

import com.drivingsimulator.enums.Direction;
import com.drivingsimulator.exception.ValidationException;

import java.util.Objects;

/** Immutable user-provided starting state and command list for one car. */
public record CarPlan(String name, Position start, Direction direction, String commands) {
    public CarPlan {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Car name cannot be blank.");
        }
        name = name.trim();
        start = Objects.requireNonNull(start, "start");
        direction = Objects.requireNonNull(direction, "direction");
        commands = commands == null ? "" : commands.trim();
    }
}
