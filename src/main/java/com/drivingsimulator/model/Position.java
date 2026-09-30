package com.drivingsimulator.model;

/** Immutable coordinate in the field. */
public record Position(int x, int y) {
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
