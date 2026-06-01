package com.fooddelivery.orderservice.exception;

import com.fooddelivery.orderservice.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import liquibase.exception.CustomPreconditionErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler{
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<?> handleOrderNotFound(OrderNotFoundException exception, HttpServletRequest request){

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(exception.getMessage())
                .time(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        log.warn("Order not found [traceId = {}]: {}", errorResponse.getTraceId(), exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<?> handleCustomerNotFound(CustomerNotFoundException exception, HttpServletRequest request){

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(exception.getMessage())
                .time(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        log.warn("Customer not found [traceId = {}]: {}", errorResponse.getTraceId(), exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<?> handleItemNotFound(ItemNotFoundException exception, HttpServletRequest request){

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(exception.getMessage())
                .time(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        log.warn("Item not found [traceId = {}]: {}", errorResponse.getTraceId(), exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request){

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(exception.getMessage())
                .time(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation failed [traceId={}]: {}", errorResponse.getTraceId(), message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception exception, HttpServletRequest request){

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(exception.getMessage())
                .time(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        log.error("Validation failed [traceId={}]: {}", errorResponse.getTraceId(), exception.getMessage(), exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
