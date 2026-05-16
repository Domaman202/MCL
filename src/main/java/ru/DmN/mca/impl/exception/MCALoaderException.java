package ru.DmN.mca.impl.exception;

public class MCALoaderException extends RuntimeException {
    public MCALoaderException(String message) {
        super(message);
    }

    public MCALoaderException(Throwable cause) {
        super(cause);
    }
}
