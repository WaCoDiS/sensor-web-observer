package de.wacodis.codede.sentinel.exception;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class ParsingException extends Exception {
    public ParsingException() {
        super();
    }

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsingException(Throwable cause) {
        super(cause);
    }
}
