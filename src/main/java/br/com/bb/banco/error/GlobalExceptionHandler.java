package br.com.bb.banco.error;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.servlet.http.HttpServletRequest;


@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> illegalArgumentExceptionHangler(IllegalArgumentException e, HttpServletRequest request) {
        return response(e, request, HttpStatus.BAD_REQUEST, null);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> nullPointerExceptionHandler(NullPointerException e, HttpServletRequest request) {
        return response(e, request, HttpStatus.BAD_REQUEST, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        return response(e, request, HttpStatus.BAD_REQUEST, null);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> noSuchElementExceptionHander(NoSuchElementException e, HttpServletRequest request) {
        return response(e, request, HttpStatus.NOT_FOUND, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValidHandler(MethodArgumentNotValidException e, HttpServletRequest request) {
        
        List<ErrorResponse.ValidationError> errors = e.getBindingResult().getFieldErrors().stream()
        .map(error -> new ErrorResponse.ValidationError(error.getField(), error.getDefaultMessage()))
        .collect(Collectors.toList());
        
        return response(e, request, HttpStatus.BAD_REQUEST, errors);
    }

    
    // Metodo utilitario para retornar uma resposta padronizada de erro
    private ResponseEntity<ErrorResponse> response(Exception e, HttpServletRequest request, HttpStatus status, List<ErrorResponse.ValidationError> errors) {
    
        ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now(ZoneOffset.of("-03:00")))
        .status(status.value())
        .error(status.getReasonPhrase())
        .message(e.getMessage())
        .path(request.getRequestURI())
        .errors(errors)
        .build();
    
        return ResponseEntity.status(status).body(errorResponse);
    }
}
