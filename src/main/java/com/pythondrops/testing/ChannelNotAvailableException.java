package com.pythondrops.testing;

public class ChannelNotAvailableException extends Exception {
    String message;
    public ChannelNotAvailableException (String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
