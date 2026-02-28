package fdu.kaoyanrank.exception;

import fdu.kaoyanrank.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.io.IOException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "请求参数不合法";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<String>> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("请求参数不合法");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, "请求体格式错误"));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Result<String>> handleServiceException(ServiceException e, HttpServletResponse response) {
        log.error("Service错误",e);
        String contentType = response.getContentType();
        if ((contentType != null && contentType.contains("text/event-stream")) || response.isCommitted()){
            try {
                response.getWriter().write("event:error\n");
                response.getWriter().write("data:" + e.getMessage() + "\n\n");
                response.getWriter().flush();
            } catch (IOException ex) {
                log.error("写出SSE错误信息失败", ex);
            }
            return null;
        }

        return buildErrorResponse(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<String>> handleException(Exception e) {
        if(!(e instanceof NoResourceFoundException)) {
            log.error("系统异常", e);
        }
        
        return buildErrorResponse(500, "系统开小差了");
    }

    private ResponseEntity<Result<String>> buildErrorResponse(Integer code, String message) {
        HttpStatus httpStatus = HttpStatus.resolve(code);
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ResponseEntity.status(httpStatus).body(Result.error(code, message));
    }
}
