package xenosoft.imldintelligence.common.handler;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.common.dto.ResultCode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 *
 * <p>统一将常见参数校验、授权拒绝和运行时异常转换为标准 API 响应，
 * 同时保留原始 HTTP 状态码，便于前端和网关层稳定处理。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理请求体校验失败。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return badRequest(joinObjectErrors(ex.getBindingResult().getAllErrors()));
    }

    /**
     * 处理模型绑定失败。
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException ex) {
        return badRequest(joinObjectErrors(ex.getBindingResult().getAllErrors()));
    }

    /**
     * 处理方法级参数校验失败。
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        String message = ex.getParameterValidationResults().stream()
                .map(this::renderParameterValidationResult)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining("; "));
        return badRequest(defaultIfBlank(message, ResultCode.BAD_REQUEST.getMessage()));
    }

    /**
     * 处理约束校验失败。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        return badRequest(defaultIfBlank(message, ResultCode.BAD_REQUEST.getMessage()));
    }

    /**
     * 处理缺失请求头。
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        return badRequest("Required request header is missing: " + ex.getHeaderName());
    }

    /**
     * 处理缺失请求参数。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        return badRequest("Required request parameter is missing: " + ex.getParameterName());
    }

    /**
     * 处理参数类型不匹配。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String requiredType = ex.getRequiredType() == null ? "unknown" : ex.getRequiredType().getSimpleName();
        return badRequest("Parameter '%s' must be of type %s".formatted(ex.getName(), requiredType));
    }

    /**
     * 处理请求体解析失败。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return badRequest("Request body is malformed or unreadable");
    }

    /**
     * 处理授权拒绝。
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ResultCode.FORBIDDEN,
                defaultIfBlank(ex.getMessage(), ResultCode.FORBIDDEN.getMessage()));
    }

    /**
     * 处理显式状态异常。
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatusCode statusCode = ex.getStatusCode();
        ResultCode resultCode = resolveResultCode(statusCode);
        String message = defaultIfBlank(ex.getReason(), resultCode.getMessage());
        return buildResponse(statusCode, resultCode, message);
    }

    /**
     * 处理未分类运行时异常。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception ex) {
        log.error("Unhandled exception caught by global exception handler", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResultCode.INTERNAL_ERROR,
                ResultCode.INTERNAL_ERROR.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> badRequest(String message) {
        return buildResponse(HttpStatus.BAD_REQUEST, ResultCode.BAD_REQUEST,
                defaultIfBlank(message, ResultCode.BAD_REQUEST.getMessage()));
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(HttpStatusCode statusCode,
                                                            ResultCode resultCode,
                                                            String message) {
        return ResponseEntity.status(statusCode)
                .body(ApiResponse.fail(resultCode, defaultIfBlank(message, resultCode.getMessage())));
    }

    private String joinObjectErrors(List<ObjectError> errors) {
        return errors.stream()
                .map(this::renderObjectError)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining("; "));
    }

    private String renderObjectError(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return fieldError.getField() + ": " + defaultIfBlank(fieldError.getDefaultMessage(), "is invalid");
        }
        return defaultIfBlank(error.getDefaultMessage(), error.getObjectName() + " is invalid");
    }

    private String renderParameterValidationResult(ParameterValidationResult result) {
        String parameterName = result.getMethodParameter().getParameterName();
        return result.getResolvableErrors().stream()
                .map(error -> {
                    String resolvedName = defaultIfBlank(parameterName, result.getMethodParameter().getExecutable().getName());
                    String resolvedMessage = defaultIfBlank(error.getDefaultMessage(), "is invalid");
                    return resolvedName + ": " + resolvedMessage;
                })
                .collect(Collectors.joining(", "));
    }

    private ResultCode resolveResultCode(HttpStatusCode statusCode) {
        return switch (statusCode.value()) {
            case 400 -> ResultCode.BAD_REQUEST;
            case 401 -> ResultCode.UNAUTHORIZED;
            case 403 -> ResultCode.FORBIDDEN;
            case 404 -> ResultCode.NOT_FOUND;
            default -> statusCode.is4xxClientError() ? ResultCode.BAD_REQUEST : ResultCode.INTERNAL_ERROR;
        };
    }

    private String defaultIfBlank(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }
}

