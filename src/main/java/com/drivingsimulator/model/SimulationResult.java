package com.drivingsimulator.model;

import java.util.List;

/** Immutable result data returned by the simulation engine. */
public record SimulationResult(List<CarResult> cars, List<SimulationEvent> events) {
    public SimulationResult {
        cars = List.copyOf(cars);
        events = List.copyOf(events);
    }

    public CarResult carNamed(String name) {
        return cars.stream()
                .filter(car -> car.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing result for car " + name));
    }
}
