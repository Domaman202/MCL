package ru.DmN.mcl.impl.exception;

import ru.DmN.mcl.api.exception.MCLException;

public class MCLModLoadException extends MCLException {
    public MCLModLoadException(String message) {
        super(message);
    }

    public MCLModLoadException(Throwable cause) {
        super(cause);
    }
}
