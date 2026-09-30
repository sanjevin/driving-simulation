package com.drivingsimulator.test;

import com.drivingsimulator.enums.CarStatus;
import com.drivingsimulator.enums.Direction;
import com.drivingsimulator.exception.ValidationException;
import com.drivingsimulator.model.CarPlan;
import com.drivingsimulator.model.Field;
import com.drivingsimulator.model.Position;
import com.drivingsimulator.model.SimulationResult;
import com.drivingsimulator.service.SimulationSession;

final class SimulationSessionTest {
    private SimulationSessionTest() {
    }

    static void register(TestSuite suite) {
        suite.test("initial overlaps are rejected", SimulationSessionTest::initialOverlapRejected);
        suite.test("case-insensitive duplicate names are rejected", SimulationSessionTest::duplicateNameRejected);
        suite.test("invalid field and starting coordinates are rejected", SimulationSessionTest::invalidFieldAndPositionRejected);
        suite.test("blank names and invalid directions are rejected", SimulationSessionTest::invalidNameAndDirectionRejected);
        suite.test("each run starts from immutable initial state", SimulationSessionTest::repeatedRunsAreClean);
        suite.test("simulation requires at least one car", SimulationSessionTest::noCarsRejected);
    }

    private static void initialOverlapRejected() {
        SimulationSession session = new SimulationSession(new Field(2, 2));
        session.addCar(new CarPlan("A", new Position(0, 0), Direction.N, ""));
        Assertions.throwsException(ValidationException.class,
                () -> session.addCar(new CarPlan("B", new Position(0, 0), Direction.E, "F")),
                "Expected initial overlap to be rejected.");
    }

    private static void duplicateNameRejected() {
        SimulationSession session = new SimulationSession(new Field(2, 2));
        session.addCar(new CarPlan("A", new Position(0, 0), Direction.N, ""));
        Assertions.throwsException(ValidationException.class,
                () -> session.addCar(new CarPlan("a", new Position(1, 1), Direction.E, "F")),
                "Expected case-insensitive duplicate-name rejection.");
    }

    private static void invalidFieldAndPositionRejected() {
        Assertions.throwsException(ValidationException.class, () -> new Field(0, 1),
                "Expected zero-width field rejection.");
        Assertions.throwsException(ValidationException.class, () -> new Field(-1, 1),
                "Expected negative-width field rejection.");
        SimulationSession session = new SimulationSession(new Field(2, 2));
        Assertions.throwsException(ValidationException.class,
                () -> session.addCar(new CarPlan("A", new Position(-1, 0), Direction.N, "F")),
                "Expected negative coordinate rejection.");
        Assertions.throwsException(ValidationException.class,
                () -> session.addCar(new CarPlan("B", new Position(2, 0), Direction.N, "F")),
                "Expected upper boundary coordinate rejection.");
    }

    private static void invalidNameAndDirectionRejected() {
        Assertions.throwsException(ValidationException.class,
                () -> new CarPlan("  ", new Position(0, 0), Direction.N, "F"),
                "Expected blank name rejection.");
        Assertions.throwsException(ValidationException.class, () -> Direction.parse("Q"),
                "Expected invalid direction rejection.");
        Assertions.throwsException(ValidationException.class, () -> Direction.parse("NN"),
                "Expected multi-character direction rejection.");
    }

    private static void repeatedRunsAreClean() {
        SimulationSession session = new SimulationSession(new Field(4, 1));
        session.addCar(new CarPlan("A", new Position(0, 0), Direction.E, "FF"));
        SimulationResult first = session.run();
        SimulationResult second = session.run();
        Assertions.equal(first, second, "Repeated runs must use the same initial state.");
        Assertions.car(second, "A", new Position(2, 0), Direction.E, CarStatus.COMPLETED);
    }

    private static void noCarsRejected() {
        SimulationSession session = new SimulationSession(new Field(1, 1));
        Assertions.throwsException(ValidationException.class, session::run,
                "Expected no-car simulation rejection.");
    }
}
