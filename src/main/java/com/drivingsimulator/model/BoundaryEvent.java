package com.drivingsimulator.model;

/** A forward command was ignored because it would leave the field. */
public record BoundaryEvent(int step, String carName, Position attemptedPosition) implements SimulationEvent {
}
