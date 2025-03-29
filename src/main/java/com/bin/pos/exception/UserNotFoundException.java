package com.bin.pos.exception;

import java.io.Serial;

public class UserNotFoundException extends RuntimeException {


    @Serial
    private static final long serialVersionUID = -5237426547503710140L;

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }


}
