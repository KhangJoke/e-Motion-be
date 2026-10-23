package com.swp391.e_Motion_be.dto.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int status = 200;
    private Integer code = 1000;
    private String message;
    private T data;

    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.code = (status == 200 || status == 201) ? 1000 : status;
        this.message = message;
        this.data = data;
    }

    public void setStatus(int status) {
        this.status = status;
        if (this.code == null || this.code == 1000) {
            this.code = (status == 200 || status == 201) ? 1000 : status;
        }
    }
}

