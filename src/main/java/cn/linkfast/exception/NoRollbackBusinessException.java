package cn.linkfast.exception;

import lombok.Getter;

/**
 * 不触发事务回滚的业务异常。
 * 用于对方系统已落库、但我方响应处理失败的场景，
 * 此时不应回滚本地已插入的数据，需配合
 * {@code @Transactional(noRollbackFor = NoRollbackBusinessException.class)} 使用。
 */
@Getter
public class NoRollbackBusinessException extends RuntimeException {

    private final String userMessage;

    public NoRollbackBusinessException(String message) {
        super(message);
        this.userMessage = message;
    }

    public NoRollbackBusinessException(String message, Throwable cause) {
        super(message, cause);
        this.userMessage = message;
    }

}
