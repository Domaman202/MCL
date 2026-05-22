package ru.DmN.mcl.impl.exception;

public class MCLException extends RuntimeException {
    public MCLException(String message) {
        super(message);
    }

    public MCLException(Throwable cause) {
        super(cause);
    }
}
