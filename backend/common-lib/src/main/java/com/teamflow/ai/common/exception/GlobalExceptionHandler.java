package com.teamflow.ai.common.exception;

import com.teamflow.ai.common.dto.ApiError;
import com.teamflow.ai.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

/**
 * Translates every exception escaping a controller into the standard
 * {@link ApiResponse} envelope.
 *
 * <p>Registered through auto-configuration so each microservice inherits identical
 * error semantics without duplicating this class or widening its component scan.
 *
 * <p>Deliberate policy: internal exception messages are never echoed to the client
 * for unexpected failures. Doing so leaks SQL fragments, class names and file
 * paths. Unexpected errors are logged with a stack trace server-side and answered
 * with a generic message.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex, HttpServletRequest request) {
        ErrorCode code = ex.getErrorCode();
        log.warn("Business rule violation [{}] on {}: {}", code.code(), request.getRequestURI(), ex.getMessage());
        return build(code.status(), ex.getMessage(), null, request);
    }

    /** Bean-validation failures on {@code @Valid @RequestBody} arguments. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidArgument(MethodArgumentNotValidException ex,
                                                                  HttpServletRequest request) {
        List<ApiError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> ApiError.of(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        log.warn("Validation failed on {}: {} field error(s)", request.getRequestURI(), errors.size());
        return build(HttpStatus.BAD_REQUEST, "Validation failed", errors, request);
    }

    /** Constraint violations on {@code @Validated} method parameters and path variables. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex,
                                                                      HttpServletRequest request) {
        List<ApiError> errors = ex.getConstraintViolations().stream()
                .map(violation -> ApiError.of(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", errors, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex,
                                                                HttpServletRequest request) {
        List<ApiError> errors = List.of(ApiError.of(ex.getParameterName(), "Required parameter is missing"));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", errors, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                HttpServletRequest request) {
        List<ApiError> errors = List.of(ApiError.of(ex.getName(), "Value has an invalid format"));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", errors, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        log.warn("Malformed request body on {}", request.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, "Malformed or unreadable request body", null, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex,
                                                                  HttpServletRequest request) {
        log.warn("Authentication failed on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password", null, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex,
                                                                HttpServletRequest request) {
        log.warn("Access denied on {}", request.getRequestURI());
        return build(HttpStatus.FORBIDDEN, "You do not have permission to perform this action", null, request);
    }

    /**
     * Raised when two transactions mutate the same row concurrently. Surfaced as
     * 409 so the client can re-fetch and retry rather than silently overwriting.
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(OptimisticLockingFailureException ex,
                                                                  HttpServletRequest request) {
        log.warn("Optimistic lock conflict on {}", request.getRequestURI());
        return build(HttpStatus.CONFLICT,
                "This record was modified by another user. Reload and try again.", null, request);
    }

    /** Backstop for unique/FK constraints not caught by an explicit pre-check. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                 HttpServletRequest request) {
        log.error("Data integrity violation on {}", request.getRequestURI(), ex);
        return build(HttpStatus.CONFLICT,
                "Operation conflicts with existing data or a required reference is missing", null, request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandler(NoHandlerFoundException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "No endpoint found for this request", null, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support if it persists.", null, request);
    }

    private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message,
                                                    List<ApiError> errors, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status.value(), message, errors, request.getRequestURI()));
    }
}
