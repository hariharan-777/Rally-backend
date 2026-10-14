package com.userservice.User.model;

/**
 * What happened when a player was booked into a game.
 * The weight is how much the outcome counts towards the reliability score (1.0 = fully reliable).
 */
public enum AttendanceStatus {
    ATTENDED(1.0),
    LATE_CANCELLATION(0.5),
    NO_SHOW(0.0);

    private final double weight;

    AttendanceStatus(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}
