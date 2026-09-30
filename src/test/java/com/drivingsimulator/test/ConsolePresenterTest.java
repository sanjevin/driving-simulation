package com.drivingsimulator.test;

import com.drivingsimulator.enums.Direction;
import com.drivingsimulator.model.CarPlan;
import com.drivingsimulator.model.Field;
import com.drivingsimulator.service.SimulationSession;
import com.drivingsimulator.ui.ConsolePresenter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

final class ConsolePresenterTest {
    private ConsolePresenterTest() {
    }

    static void register(TestSuite suite) {
        suite.test("collision output uses the requested symmetric format", ConsolePresenterTest::collisionOutputFormat);
    }

    private static void collisionOutputFormat() {
        SimulationSession session = new SimulationSession(new Field(10, 10));
        session.addCar(new CarPlan("A", new com.drivingsimulator.model.Position(1, 2), Direction.N, "FFRFFFFRRL"));
        session.addCar(new CarPlan("B", new com.drivingsimulator.model.Position(7, 8), Direction.W, "FFLFFFFFFF"));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new ConsolePresenter(new PrintStream(bytes)).printSimulationResult(session.run());
        String output = bytes.toString();

        Assertions.require(output.contains("- A, collides with B at (5,4) at step 7"),
                "Expected the requested result line for A.");
        Assertions.require(output.contains("- B, collides with A at (5,4) at step 7"),
                "Expected the requested result line for B.");
        Assertions.require(!output.contains("last safe position") && !output.contains("Both cars are frozen"),
                "Collision output must not expose internal crash-state wording.");
    }
}
