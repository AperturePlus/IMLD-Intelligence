package xenosoft.imldintelligence.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> fail(String message) {
        return ApiResponse.<T>builder()
                .code(ResultCode.INTERNAL_ERROR.getCode())
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> fail(ResultCode resultCode) {
        return ApiResponse.<T>builder()
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .build();
    }

    public static <T> ApiResponse<T> fail(ResultCode resultCode, String message) {
        return ApiResponse.<T>builder()
                .code(resultCode.getCode())
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.<T>builder()
                .code(ResultCode.BAD_REQUEST.getCode())
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> unauthorized() {
        return ApiResponse.<T>builder()
                .code(ResultCode.UNAUTHORIZED.getCode())
                .message(ResultCode.UNAUTHORIZED.getMessage())
                .build();
    }

    public static <T> ApiResponse<T> forbidden() {
        return ApiResponse.<T>builder()
                .code(ResultCode.FORBIDDEN.getCode())
                .message(ResultCode.FORBIDDEN.getMessage())
                .build();
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .code(ResultCode.NOT_FOUND.getCode())
                .message(message)
                .build();
    }
}