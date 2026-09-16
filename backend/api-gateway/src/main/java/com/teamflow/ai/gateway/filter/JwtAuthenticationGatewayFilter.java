package com.teamflow.ai.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Rejects unauthenticated traffic at the edge.
 *
 * <p>This is a first line of defence, not the only one. Each service independently
 * re-validates the token with the same {@link JwtService}, because a gateway-only
 * check would leave services wide open to anything reaching them directly inside
 * the cluster network.
 *
 * <p>To prevent header spoofing, any inbound {@code X-User-Id} style headers are
 * stripped and re-populated from verified claims. Without this, a caller could
 * simply set {@code X-User-Id} themselves and impersonate another user.
 */
@Slf4j
@Component
public class JwtAuthenticationGatewayFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";

    /** Paths reachable without a token. Everything else requires one. */
    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/verify-email",
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs");

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationGatewayFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, "Authentication is required to access this resource");
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (!jwtService.isTokenValid(token)) {
            return unauthorized(exchange, "Token is invalid or has expired");
        }

        var claims = jwtService.parseClaims(token);
        if (!jwtService.isAccessToken(claims)) {
            return unauthorized(exchange, "A refresh token cannot be used to access this resource");
        }

        ServerHttpRequest mutated = request.mutate()
                .headers(headers -> {
                    headers.remove(HEADER_USER_ID);
                    headers.remove(HEADER_USER_EMAIL);
                    headers.add(HEADER_USER_ID, jwtService.extractUserId(claims).toString());
                    headers.add(HEADER_USER_EMAIL, jwtService.extractEmail(claims));
                })
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<Void> body = ApiResponse.error(
                HttpStatus.UNAUTHORIZED.value(), message, exchange.getRequest().getURI().getPath());
        try {
            DataBuffer buffer = response.bufferFactory().wrap(objectMapper.writeValueAsBytes(body));
            return response.writeWith(Mono.just(buffer));
        } catch (Exception ex) {
            log.error("Failed to serialize gateway error response", ex);
            return response.setComplete();
        }
    }

    /**
     * Runs before routing so an unauthenticated request never reaches a downstream
     * service or consumes a connection from its pool.
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
