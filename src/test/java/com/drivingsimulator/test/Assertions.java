package com.drivingsimulator.test;

import com.drivingsimulator.enums.CarStatus;
import com.drivingsimulator.enums.Direction;
import com.drivingsimulator.model.CarResult;
import com.drivingsimulator.model.CollisionEvent;
import com.drivingsimulator.model.Position;
import com.drivingsimulator.model.SimulationResult;

final class Assertions {
    private Assertions() {
    }

    static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    static void equal(Object expected, Object actual, String message) {
        require(expected.equals(actual), message + " Expected " + expected + " but was " + actual + ".");
    }

    static void car(SimulationResult result, String name, Position position,
                    Direction direction, CarStatus status) {
        CarResult actual = result.carNamed(name);
        equal(position, actual.position(), name + " position differs.");
        equal(direction, actual.direction(), name + " direction differs.");
        equal(status, actual.status(), name + " status differs.");
    }

    static void collision(SimulationResult result, int step, String movingCar,
                          String occupyingCar, Position attemptedPosition) {
        boolean found = result.events().stream().anyMatch(event -> event instanceof CollisionEvent collision
                && collision.step() == step
                && collision.movingCar().equals(movingCar)
                && collision.occupyingCar().equals(occupyingCar)
                && collision.attemptedPosition().equals(attemptedPosition));
        require(found, "Expected collision " + movingCar + " -> " + occupyingCar + " at "
                + attemptedPosition + " on step " + step + ".");
    }

    static void noCollision(SimulationResult result) {
        require(result.events().stream().noneMatch(CollisionEvent.class::isInstance),
                "Expected no collision event.");
    }

    static void throwsException(Class<? extends Throwable> expected, TestSuite.TestAction action,
                                String message) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (expected.isInstance(actual)) {
                return;
            }
            throw new AssertionError(message + " Received " + actual.getClass().getSimpleName() + " instead.");
        }
        throw new AssertionError(message);
    }
}
