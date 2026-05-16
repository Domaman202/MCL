package ru.DmN.mca.impl.exception;

public class MCAModLoadException extends MCALoaderException {
    public MCAModLoadException(String message) {
        super(message);
    }

    public MCAModLoadException(Throwable cause) {
        super(cause);
    }
}
