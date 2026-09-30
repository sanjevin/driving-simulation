package com.drivingsimulator.model;

/** A moving car attempted to enter an occupied coordinate. */
public record CollisionEvent(int step, String movingCar, String occupyingCar,
                             Position attemptedPosition) implements SimulationEvent {
}
