package com.swp391.e_Motion_be.exception;


import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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

}
