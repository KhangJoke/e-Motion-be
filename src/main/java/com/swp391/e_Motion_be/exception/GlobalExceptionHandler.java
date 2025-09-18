package com.swp391.e_Motion_be.exception;


import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

//     exception
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse<String>> handlingRuntimeException(){
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    // Exception bắt bằng appException
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<String>> handlingRuntimeException(AppException ex){
        ApiResponse<String> apiResponse = new ApiResponse<>();
        ErrorCode errorCode = ex.getErrorCode();
        apiResponse.setStatus(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleBadRequestException(MethodArgumentNotValidException e){
        ApiResponse<String> apiResponse = new ApiResponse<>();
        String message = "";

        for(FieldError fieldError : e.getFieldErrors()){
            message += fieldError.getField() +": "+ fieldError.getDefaultMessage() + "\n";
        }
        apiResponse.setStatus(2003);
        apiResponse.setMessage(message);
        return ResponseEntity.badRequest().body(apiResponse);
    }
}
