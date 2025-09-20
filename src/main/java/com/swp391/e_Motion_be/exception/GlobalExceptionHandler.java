package com.swp391.e_Motion_be.exception;


import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    //Exception
//    @ExceptionHandler(value = RuntimeException.class)
//    ResponseEntity<ApiResponse<String>> handlingRuntimeException(){
//        ApiResponse<String> apiResponse = new ApiResponse<>();
//        apiResponse.setStatus(ErrorCode.UNCAT+EGORIZED_EXCEPTION.getCode());
//        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
//        return ResponseEntity.badRequest().body(apiResponse);
//    }

    // Exception bắt bằng AppException
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<String>> handlingRuntimeException(AppException ex){
        ApiResponse<String> apiResponse = new ApiResponse<>();
        ErrorCode errorCode = ex.getErrorCode();
        apiResponse.setStatus(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    // Exception bắt bằng MethodArgumentNotValidException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleBadRequestException(MethodArgumentNotValidException e){
        ApiResponse<String> apiResponse = new ApiResponse<>();
        String message = e.getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        apiResponse.setStatus(2003);
        apiResponse.setMessage(message);
        return ResponseEntity.badRequest().body(apiResponse);
    }
}
