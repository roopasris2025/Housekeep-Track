package com.stayease.housekeeping.exception;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String message) { super(message); }
}
