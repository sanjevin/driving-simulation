package com.drivingsimulator.test;

import com.drivingsimulator.enums.CarStatus;
import com.drivingsimulator.enums.Direction;
import com.drivingsimulator.model.BoundaryEvent;
import com.drivingsimulator.model.Field;
import com.drivingsimulator.model.InvalidCommandEvent;
import com.drivingsimulator.model.Position;
import com.drivingsimulator.model.SimulationResult;
import com.drivingsimulator.service.SimulationSession;

final class SimulationEngineTest {
    private SimulationEngineTest() {
    }

    static void register(TestSuite suite) {
        suite.test("single car follows the supplied scenario", SimulationEngineTest::singleCarScenario);
        suite.test("supplied multi-car scenario freezes both cars", SimulationEngineTest::suppliedMultiCarScenario);
        suite.test("collision freezes only involved cars", SimulationEngineTest::collisionAndContinuation);
        suite.test("a stationary occupant freezes when another car hits it", SimulationEngineTest::stationaryOccupantIsFrozen);
        suite.test("boundary F is ignored but following commands run", SimulationEngineTest::boundaryThenTurn);
        suite.test("all four field boundaries reject forward movement", SimulationEngineTest::allBoundaries);
        suite.test("invalid commands are ignored and reported", SimulationEngineTest::invalidCommand);
        suite.test("lowercase commands and directions are accepted", SimulationEngineTest::lowercaseCommandsAndDirection);
        suite.test("turning never creates a false collision", SimulationEngineTest::turnsDoNotCollide);
        suite.test("a car may enter a coordinate vacated earlier in the step", SimulationEngineTest::vacatedCoordinateIsNotCollision);
        suite.test("a frozen car remains an obstacle", SimulationEngineTest::frozenCarsRemainObstacles);
        suite.test("car add order is deterministic", SimulationEngineTest::insertionOrder);
        suite.test("empty command list completes without movement", SimulationEngineTest::emptyCommands);
        suite.test("one-cell field safely ignores all forward moves", SimulationEngineTest::oneCellField);
        suite.test("large field does not allocate a grid", SimulationEngineTest::largeField);
    }

    private static void singleCarScenario() {
        SimulationSession session = new SimulationSession(new Field(10, 10));
        session.addCar(new com.drivingsimulator.model.CarPlan("A", new Position(1, 2), Direction.N, "FFRFFFFRRL"));
        SimulationResult result = session.run();
        Assertions.car(result, "A", new Position(5, 4), Direction.S, CarStatus.COMPLETED);
        Assertions.require(result.events().isEmpty(), "Expected no events.");
    }

    private static void suppliedMultiCarScenario() {
        SimulationSession session = scenarioWithTwoCars();
        SimulationResult result = session.run();
        Assertions.car(result, "A", new Position(5, 4), Direction.E, CarStatus.CRASHED);
        Assertions.car(result, "B", new Position(5, 5), Direction.S, CarStatus.CRASHED);
        Assertions.collision(result, 7, "B", "A", new Position(5, 4));
    }

    private static void collisionAndContinuation() {
        SimulationSession session = new SimulationSession(new Field(4, 3));
        add(session, "A", 0, 0, Direction.E, "F");
        add(session, "B", 1, 0, Direction.W, "F");
        add(session, "C", 0, 2, Direction.E, "FFF");
        SimulationResult result = session.run();
        Assertions.car(result, "A", new Position(0, 0), Direction.E, CarStatus.CRASHED);
        Assertions.car(result, "B", new Position(1, 0), Direction.W, CarStatus.CRASHED);
        Assertions.car(result, "C", new Position(3, 2), Direction.E, CarStatus.COMPLETED);
        Assertions.collision(result, 1, "A", "B", new Position(1, 0));
    }

    private static void stationaryOccupantIsFrozen() {
        SimulationSession session = new SimulationSession(new Field(3, 5));
        add(session, "A", 1, 2, Direction.N, "F");
        add(session, "C", 1, 3, Direction.N, "F");

        SimulationResult result = session.run();

        Assertions.car(result, "A", new Position(1, 2), Direction.N, CarStatus.CRASHED);
        Assertions.car(result, "C", new Position(1, 3), Direction.N, CarStatus.CRASHED);
        Assertions.collision(result, 1, "A", "C", new Position(1, 3));
    }

    private static void boundaryThenTurn() {
        SimulationSession session = new SimulationSession(new Field(3, 3));
        add(session, "A", 1, 2, Direction.N, "FRF");
        SimulationResult result = session.run();
        Assertions.car(result, "A", new Position(2, 2), Direction.E, CarStatus.COMPLETED);
        Assertions.require(result.events().stream().anyMatch(event -> event instanceof BoundaryEvent boundary
                        && boundary.step() == 1 && boundary.carName().equals("A")),
                "Expected boundary event at step 1.");
    }

    private static void allBoundaries() {
        SimulationSession session = new SimulationSession(new Field(3, 3));
        add(session, "North", 1, 2, Direction.N, "F");
        add(session, "East", 2, 1, Direction.E, "F");
        add(session, "South", 1, 0, Direction.S, "F");
        add(session, "West", 0, 1, Direction.W, "F");
        SimulationResult result = session.run();
        Assertions.require(result.events().stream().filter(BoundaryEvent.class::isInstance).count() == 4,
                "Expected one boundary event for each direction.");
        Assertions.noCollision(result);
    }

    private static void invalidCommand() {
        SimulationSession session = new SimulationSession(new Field(5, 5));
        add(session, "A", 1, 1, Direction.N, "FXRF");
        SimulationResult result = session.run();
        Assertions.car(result, "A", new Position(2, 2), Direction.E, CarStatus.COMPLETED);
        Assertions.require(result.events().stream().anyMatch(event -> event instanceof InvalidCommandEvent invalid
                        && invalid.step() == 2 && invalid.command() == 'X'),
                "Expected ignored X at step 2.");
    }

    private static void lowercaseCommandsAndDirection() {
        SimulationSession session = new SimulationSession(new Field(4, 4));
        add(session, "A", 1, 1, Direction.parse("n"), "ffrffl");
        SimulationResult result = session.run();
        Assertions.car(result, "A", new Position(3, 3), Direction.N, CarStatus.COMPLETED);
        Assertions.require(result.events().isEmpty(), "Lowercase valid commands must not be invalid events.");
    }

    private static void turnsDoNotCollide() {
        SimulationSession session = new SimulationSession(new Field(3, 3));
        add(session, "A", 0, 0, Direction.N, "LRLR");
        add(session, "B", 0, 1, Direction.S, "RLRL");
        SimulationResult result = session.run();
        Assertions.noCollision(result);
        Assertions.car(result, "A", new Position(0, 0), Direction.N, CarStatus.COMPLETED);
        Assertions.car(result, "B", new Position(0, 1), Direction.S, CarStatus.COMPLETED);
    }

    private static void vacatedCoordinateIsNotCollision() {
        SimulationSession session = new SimulationSession(new Field(3, 1));
        add(session, "A", 1, 0, Direction.E, "F");
        add(session, "B", 0, 0, Direction.E, "F");
        SimulationResult result = session.run();
        Assertions.car(result, "A", new Position(2, 0), Direction.E, CarStatus.COMPLETED);
        Assertions.car(result, "B", new Position(1, 0), Direction.E, CarStatus.COMPLETED);
        Assertions.noCollision(result);
    }

    private static void frozenCarsRemainObstacles() {
        SimulationSession session = new SimulationSession(new Field(4, 2));
        add(session, "A", 0, 0, Direction.E, "F");
        add(session, "B", 1, 0, Direction.W, "F");
        add(session, "C", 2, 0, Direction.W, "F");
        SimulationResult result = session.run();
        Assertions.car(result, "C", new Position(2, 0), Direction.W, CarStatus.CRASHED);
        Assertions.collision(result, 1, "C", "B", new Position(1, 0));
    }

    private static void insertionOrder() {
        SimulationSession first = new SimulationSession(new Field(3, 1));
        add(first, "A", 1, 0, Direction.E, "F");
        add(first, "B", 0, 0, Direction.E, "F");
        SimulationResult firstResult = first.run();

        SimulationSession second = new SimulationSession(new Field(3, 1));
        add(second, "B", 0, 0, Direction.E, "F");
        add(second, "A", 1, 0, Direction.E, "F");
        SimulationResult secondResult = second.run();

        Assertions.car(firstResult, "A", new Position(2, 0), Direction.E, CarStatus.COMPLETED);
        Assertions.car(firstResult, "B", new Position(1, 0), Direction.E, CarStatus.COMPLETED);
        Assertions.car(secondResult, "B", new Position(0, 0), Direction.E, CarStatus.CRASHED);
        Assertions.car(secondResult, "A", new Position(1, 0), Direction.E, CarStatus.CRASHED);
    }

    private static void emptyCommands() {
        SimulationSession session = new SimulationSession(new Field(2, 2));
        add(session, "A", 1, 1, Direction.W, "");
        SimulationResult result = session.run();
        Assertions.car(result, "A", new Position(1, 1), Direction.W, CarStatus.COMPLETED);
        Assertions.require(result.events().isEmpty(), "Empty command list should not produce events.");
    }

    private static void oneCellField() {
        SimulationSession session = new SimulationSession(new Field(1, 1));
        add(session, "A", 0, 0, Direction.N, "FFLF");
        SimulationResult result = session.run();
        Assertions.car(result, "A", new Position(0, 0), Direction.W, CarStatus.COMPLETED);
        Assertions.require(result.events().stream().filter(BoundaryEvent.class::isInstance).count() == 3,
                "Expected all three forward commands to be boundary-rejected.");
    }

    private static void largeField() {
        SimulationSession session = new SimulationSession(new Field(Integer.MAX_VALUE, Integer.MAX_VALUE));
        add(session, "A", Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 2, Direction.E, "F");
        SimulationResult result = session.run();
        Assertions.car(result, "A", new Position(Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 2),
                Direction.E, CarStatus.COMPLETED);
    }

    private static SimulationSession scenarioWithTwoCars() {
        SimulationSession session = new SimulationSession(new Field(10, 10));
        add(session, "A", 1, 2, Direction.N, "FFRFFFFRRL");
        add(session, "B", 7, 8, Direction.W, "FFLFFFFFFF");
        return session;
    }

    private static void add(SimulationSession session, String name, int x, int y,
                            Direction direction, String commands) {
        session.addCar(new com.drivingsimulator.model.CarPlan(name, new Position(x, y), direction, commands));
    }
}
