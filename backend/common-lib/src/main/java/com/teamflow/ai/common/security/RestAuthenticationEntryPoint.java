package com.teamflow.ai.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamflow.ai.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Renders unauthenticated requests as the standard JSON envelope.
 *
 * <p>Without this, Spring Security replies with an empty 401 and a
 * {@code WWW-Authenticate} header, which a React client cannot parse consistently.
 */
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication is required to access this resource",
                request.getRequestURI());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
