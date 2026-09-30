package com.drivingsimulator.test;

/** Plain-Java test entry point invoked by start-test.sh. */
public final class DrivingSimulatorTest {
    private DrivingSimulatorTest() {
    }

    public static void main(String[] args) {
        TestSuite suite = new TestSuite(System.out);
        SimulationEngineTest.register(suite);
        SimulationSessionTest.register(suite);
        ConsolePresenterTest.register(suite);
        if (suite.printSummary() > 0) {
            System.exit(1);
        }
    }
}
