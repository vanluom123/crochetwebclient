package org.xiaofeng.webclient.common;

public class ServerErrorException extends RuntimeException {
    public ServerErrorException() {
        super();
    }

    public ServerErrorException(String message) {
        super(message);
    }
}
