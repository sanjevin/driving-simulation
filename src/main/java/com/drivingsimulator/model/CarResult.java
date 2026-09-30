package com.drivingsimulator.model;

import com.drivingsimulator.enums.CarStatus;
import com.drivingsimulator.enums.Direction;

/** The final externally visible state of one car after a run. */
public record CarResult(String name, Position position, Direction direction, CarStatus status) {
}
