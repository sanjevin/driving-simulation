package com.drivingsimulator.model;

/** A notable outcome produced while processing a command. */
public sealed interface SimulationEvent permits CollisionEvent, InvalidCommandEvent, BoundaryEvent {
    int step();
}
