package com.ceitechs.domain.service.service;

/**
 * @author  iddymagohe on 7/30/16.
 * @since 1.0
 */
public class EntityExists extends Exception {
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */

    public EntityExists(String message) {
        super(message);
    }
}
