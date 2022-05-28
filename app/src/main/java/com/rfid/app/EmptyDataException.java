package com.rfid.app;

public class EmptyDataException extends Exception {
    public EmptyDataException(String type) {
        super(type + " cannot be null or empty");
    }
}
