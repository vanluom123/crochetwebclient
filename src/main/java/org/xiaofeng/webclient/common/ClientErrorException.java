package org.xiaofeng.webclient.common;

/**
 * ClientErrorException class
 */
public class ClientErrorException extends RuntimeException {
    /**
     * Constructor
     */
    public ClientErrorException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message message
     */
    public ClientErrorException(String message) {
        super(message);
    }
}
