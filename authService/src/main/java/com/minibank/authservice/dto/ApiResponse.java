package com.minibank.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data                       // generates getters, setters, toString, equals, hashCode
@NoArgsConstructor          // default constructor
@AllArgsConstructor         // constructor with all fields
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Integer statusCode;

    // Static factory methods remain the same (Lombok doesn't replace these)
    public static <T> ApiResponse<T> success(T data, String message, Integer statusCode) {
        return new ApiResponse<>(true, message, data, statusCode);
    }

    public static <T> ApiResponse<T> error(String message, Integer statusCode) {
        return new ApiResponse<>(false, message, null, statusCode);
    }
}
