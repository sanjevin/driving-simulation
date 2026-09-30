package com.drivingsimulator.model;

/** An unsupported command character was intentionally ignored. */
public record InvalidCommandEvent(int step, String carName, char command) implements SimulationEvent {
}
