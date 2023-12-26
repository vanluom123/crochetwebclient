package org.xiaofeng.webclient.common;

/**
 * ServerErrorException class
 */
public class ServerErrorException extends RuntimeException {
    /**
     * Constructor
     */
    public ServerErrorException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message Message
     */
    public ServerErrorException(String message) {
        super(message);
    }
}
