package com.teamflow.ai.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

/**
 * The single response envelope returned by every TeamFlow.AI endpoint.
 *
 * <p>Carries the seven fields required by the platform API contract: timestamp,
 * status, success, message, data, errors and path. Null members are omitted from
 * serialization so success payloads do not carry an empty {@code errors} array and
 * failures do not carry a null {@code data} key.
 *
 * @param <T> payload type
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Builder.Default
    private Instant timestamp = Instant.now();

    /** HTTP status code, duplicated in the body so clients can log a single object. */
    private int status;

    private boolean success;

    private String message;

    private T data;

    /** Field-level validation failures, or other multi-part error detail. */
    private List<ApiError> errors;

    /** Request URI that produced this response. */
    private String path;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status(200).success(true).message("Success").data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status(200).success(true).message(message).data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder()
                .status(201).success(true).message(message).data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message, String path) {
        return ApiResponse.<T>builder()
                .status(status).success(false).message(message).path(path)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message, List<ApiError> errors, String path) {
        return ApiResponse.<T>builder()
                .status(status).success(false).message(message).errors(errors).path(path)
                .build();
    }
}
