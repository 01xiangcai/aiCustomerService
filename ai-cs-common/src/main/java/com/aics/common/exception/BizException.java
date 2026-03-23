package com.aics.common.exception;

import lombok.Getter;

/**
 * 全局业务异常
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final int code;

    public BizException(String message) {
        this(500, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /** 未授权 */
    public static BizException unauthorized(String message) {
        return new BizException(401, message);
    }

    /** 禁止访问 */
    public static BizException forbidden(String message) {
        return new BizException(403, message);
    }

    /** 资源不存在 */
    public static BizException notFound(String message) {
        return new BizException(404, message);
    }

    /** 参数错误 */
    public static BizException badRequest(String message) {
        return new BizException(400, message);
    }
}
