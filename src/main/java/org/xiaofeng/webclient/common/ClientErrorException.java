package org.xiaofeng.webclient.common;

public class ClientErrorException extends RuntimeException {
    public ClientErrorException() {
        super();
    }

    public ClientErrorException(String message) {
        super(message);
    }
}
