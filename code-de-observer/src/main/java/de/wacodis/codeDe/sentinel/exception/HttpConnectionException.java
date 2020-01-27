package de.wacodis.codeDe.sentinel.exception;

public class HttpConnectionException extends Exception {

    public HttpConnectionException(String msg, Exception ex) {
        super(msg, ex);
    }
}
