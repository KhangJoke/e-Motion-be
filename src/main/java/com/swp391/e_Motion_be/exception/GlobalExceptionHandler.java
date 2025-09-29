package com.swp391.e_Motion_be.exception;


import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@ControllerAdvice
public class GlobalExceptionHandler {

    //Exception
//    @ExceptionHandler(value = Exception.class)
//    ResponseEntity<ApiResponse<String>> handlingRuntimeException(){
//        ApiResponse<String> apiResponse = new ApiResponse<>();
//        apiResponse.setStatus(ErrorCode.UNEXPECTED_EXCEPTION.getCode());
//        apiResponse.setMessage(ErrorCode.UNEXPECTED_EXCEPTION.getMessage());
//        return ResponseEntity.badRequest().body(apiResponse);
//    }

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
