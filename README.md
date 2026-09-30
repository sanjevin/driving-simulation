# Driving Simulation

A dependency-free Java 17 CLI implementation of the driving simulation.

## Project structure

```text
src/main/java/com/drivingsimulator/
  enums/       Direction and car-status enums
  exception/   Input and validation exceptions
  model/       Immutable field, car, result, and event data
  service/     Session validation and the stateless simulation engine
  ui/          Console input orchestration and output rendering
  DrivingSimulator.java
src/test/java/com/drivingsimulator/test/
  DrivingSimulatorTest.java   Plain-Java test entry point
  *Test.java                  Focused engine, session, and presenter tests
```

Run it from this directory:

```bash
./start.sh
```

Run the built-in test suite:

```bash
./start-test.sh
```

## Behaviour decisions

- Cars execute sequentially in the order they were added. At each step, each non-crashed car executes one command before the next car is considered.
- `L` and `R` rotate a car. `F` moves it one coordinate only when the destination is in the field and unoccupied.
- Invalid commands are ignored, reported after the run, and still consume their position in the command sequence.
- A boundary-rejected `F` is ignored and consumes its command; subsequent commands still execute.
- A move into an occupied coordinate is a collision. The moving car and the occupied car freeze at their last safe coordinates. The simulation continues for every unaffected car.
- Frozen cars remain obstacles. A car that later attempts to enter their coordinate also freezes.
- Starting overlaps are rejected when a car is added. Names are unique without regard to case.
- Each call to run uses the original immutable car plans, and each script invocation compiles into a fresh temporary directory. No application state is persisted.

## Deadlock safety

The engine is single-threaded and holds no locks. It has no `synchronized`, `wait`, executor, or shared state; therefore it cannot enter a Java deadlock. Commands are finite and a blocked or invalid command is consumed, so it cannot logically stall either.
