package com.swp391.e_Motion_be.exception;


import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ControllerAdvice
public class GlobalExceptionHandler {

    //Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus(500);
        response.setMessage("Internal Server Error: " + ex.getMessage());
        response.setData(null);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    //AccessDenied Exception
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(Exception ex) {
        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus(403);
        response.setMessage("Forbidden: " + ex.getMessage());
        response.setData(null);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    //Unauthorized Exception
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(Exception ex) {
        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus(401);
        response.setMessage("Unauthorized: " + ex.getMessage());
        response.setData(null);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    // Exception bắt bằng AppException
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<String>> handlingRuntimeException(AppException ex){
        ApiResponse<String> apiResponse = new ApiResponse<>();
        ErrorCode errorCode = ex.getErrorCode();
        apiResponse.setStatus(errorCode.getStatusCode().value());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    // Exception bắt bằng MethodArgumentNotValidException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleBadRequestException(MethodArgumentNotValidException e){
        ApiResponse<String> apiResponse = new ApiResponse<>();
        String message = Stream.concat(
                e.getBindingResult().getFieldErrors().stream()
                        .map(err -> err.getField() + ": " + err.getDefaultMessage()),
                e.getBindingResult().getGlobalErrors().stream()
                        .map(err -> err.getObjectName() + ": " + err.getDefaultMessage())
        ).collect(Collectors.joining("; "));
        apiResponse.setStatus(400);
        apiResponse.setMessage(message);
        return ResponseEntity.badRequest().body(apiResponse);
    }

    // Lỗi input của field enum invalid, sai format date time
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handlingInvalidEnumValueException(HttpMessageNotReadableException ex){
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(400);

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormatException) {
            if (invalidFormatException.getTargetType() == LocalDateTime.class) {
                // Đây là lỗi parse LocalDateTime
                String value = invalidFormatException.getValue().toString();
                apiResponse.setMessage("Invalid datetime format: '" + value +
                        "'. Please use format yyyy-MM-dd'T'HH:mm:ss");
            } else {
                // Lỗi enum hoặc type khác
                String fullMessage = invalidFormatException.getOriginalMessage();
                int colonIndex = fullMessage.indexOf(":");
                if (colonIndex != -1 && colonIndex + 1 < fullMessage.length()) {
                    fullMessage = fullMessage.substring(colonIndex + 1).trim();
                }
                apiResponse.setMessage("Invalid value: " + fullMessage);
            }
        } else {
            apiResponse.setMessage("Invalid request body");
        }

        return ResponseEntity.badRequest().body(apiResponse);
    }
}
