package io.sclera.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MaximoException extends RuntimeException {

    private String message;
    private Integer errorCode;
    private String path;

    public MaximoException(String message, Integer errorCode, String path) {
        super(message);
        this.message = message;
        this.errorCode = errorCode;
        this.path = path;
    }
}
