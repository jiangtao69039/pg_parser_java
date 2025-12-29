package com.github.ttttz.pgParser;

/**
 * Exception thrown when SQL parsing fails
 */
public class PgQueryException extends Exception {

    private final int cursorPosition;

    public PgQueryException(String message) {
        super(message);
        this.cursorPosition = -1;
    }

    public PgQueryException(String message, int cursorPosition) {
        super(message + (cursorPosition > 0 ? " at position " + cursorPosition : ""));
        this.cursorPosition = cursorPosition;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }
}
