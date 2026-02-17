package fdu.kaoyanrank.exception;

public class ServiceException extends RuntimeException {
    private final Integer code;

    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public ServiceException(String message) {
        this(500, message);
    }

    public Integer getCode() {
        return code;
    }
}
