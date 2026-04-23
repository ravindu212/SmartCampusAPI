package com.smartcampus.exception;

public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String roomId) {
        super("Room " + roomId + " cannot be deleted as it still has sensors assigned to it.");
    }
}