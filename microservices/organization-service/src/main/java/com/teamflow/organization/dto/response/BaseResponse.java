package com.teamflow.organization.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Common response envelope fields shared across all API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse {

    private boolean success;
    private String message;
    private Instant timestamp = Instant.now();

}
