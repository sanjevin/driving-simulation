package com.drivingsimulator.model;

import com.drivingsimulator.exception.ValidationException;

/** A rectangular field whose valid coordinates are 0..width-1 and 0..height-1. */
public record Field(int width, int height) {
    public Field {
        if (width <= 0 || height <= 0) {
            throw new ValidationException("Field width and height must both be positive.");
        }
    }

    public boolean contains(Position position) {
        return position.x() >= 0 && position.x() < width
                && position.y() >= 0 && position.y() < height;
    }
}
