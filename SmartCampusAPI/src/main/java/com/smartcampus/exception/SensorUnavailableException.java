package com.smartcampus.exception;

public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String sensorId) {
        super("Sensor " + sensorId + " is currently under maintenance and cannot accept new readings.");
    }
}