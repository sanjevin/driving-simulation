package com.drivingsimulator.enums;

import com.drivingsimulator.exception.ValidationException;
import com.drivingsimulator.model.Position;

import java.util.Locale;

/** The four valid cardinal directions for a car. */
public enum Direction {
    N(0, 1),
    E(1, 0),
    S(0, -1),
    W(-1, 0);

    private final int deltaX;
    private final int deltaY;

    Direction(int deltaX, int deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public Direction left() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }

    public Direction right() {
        return values()[(ordinal() + 1) % values().length];
    }

    public Position move(Position position) {
        return new Position(position.x() + deltaX, position.y() + deltaY);
    }

    public static Direction parse(String value) {
        if (value == null || value.length() != 1) {
            throw new ValidationException("Direction must be one of N, E, S, or W.");
        }
        try {
            return valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("Direction must be one of N, E, S, or W.");
        }
    }
}
