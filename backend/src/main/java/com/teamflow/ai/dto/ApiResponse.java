package com.teamflow.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Generic standardized API response wrapper.
 *
 * @param <T> type of the payload returned to the client
 */
@Getter
@Setter
public class ApiResponse<T> extends BaseResponse {

    private T data;

    public ApiResponse() {
        super();
    }

    private ApiResponse(boolean success, String message, T data) {
        super(success, message, Instant.now());
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

}
