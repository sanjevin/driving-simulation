package com.drivingsimulator.service;

import com.drivingsimulator.exception.ValidationException;
import com.drivingsimulator.model.CarPlan;
import com.drivingsimulator.model.Field;
import com.drivingsimulator.model.Position;
import com.drivingsimulator.model.SimulationResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Holds one user-configured field and its initial car plans.
 *
 * <p>The session validates setup concerns. The engine receives an immutable
 * snapshot for every run, which prevents state leaking between runs.</p>
 */
public final class SimulationSession {
    private final Field field;
    private final SimulationEngine engine;
    private final Map<String, CarPlan> carsByCanonicalName = new LinkedHashMap<>();
    private final Map<Position, String> initialOccupants = new LinkedHashMap<>();

    public SimulationSession(Field field) {
        this(field, new SimulationEngine());
    }

    SimulationSession(Field field, SimulationEngine engine) {
        this.field = Objects.requireNonNull(field, "field");
        this.engine = Objects.requireNonNull(engine, "engine");
    }

    public void addCar(CarPlan plan) {
        Objects.requireNonNull(plan, "plan");
        if (!field.contains(plan.start())) {
            throw new ValidationException("Starting position " + plan.start() + " is outside the field.");
        }

        String canonicalName = canonicalName(plan.name());
        if (carsByCanonicalName.containsKey(canonicalName)) {
            throw new ValidationException("Car name '" + plan.name() + "' is already in use.");
        }

        String existingName = initialOccupants.get(plan.start());
        if (existingName != null) {
            throw new ValidationException("Starting position " + plan.start()
                    + " is already occupied by car '" + existingName + "'.");
        }

        carsByCanonicalName.put(canonicalName, plan);
        initialOccupants.put(plan.start(), plan.name());
    }

    public List<CarPlan> cars() {
        return List.copyOf(carsByCanonicalName.values());
    }

    public boolean hasCars() {
        return !carsByCanonicalName.isEmpty();
    }

    public SimulationResult run() {
        return engine.run(field, cars());
    }

    private static String canonicalName(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
