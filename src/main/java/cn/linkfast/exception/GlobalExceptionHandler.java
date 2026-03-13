package cn.linkfast.exception;

import cn.linkfast.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.stream.Collectors;


/**
 * 全局异常处理器
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Result<String> handleBusinessException(BusinessException e, HttpServletRequest request) {
        logger.error("业务异常: 请求地址={},异常信息={}", request.getRequestURL(), e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logger.error("参数异常: 请求地址={},异常信息={}", request.getRequestURL(), e.getMessage(), e);
        return Result.error(400, "参数错误: " + e.getMessage());
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    public Result<String> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        logger.error("空指针异常: 请求地址={}", request.getRequestURL(), e);
        return Result.error(500, "系统内部错误");
    }

    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<String> handleException(Exception e, HttpServletRequest request) {
        logger.error("系统异常: 请求地址={}", request.getRequestURL(), e);
        return Result.error(500, "系统繁忙，请稍后再试");
    }

    /**
     * 处理【框架自动抛出】的注解式参数校验异常（@Valid/@Validated）
     * 优化点：返回格式统一为Result<String>，和其他方法保持一致
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseBody
    public Result<String> handleValidationException(Exception e, HttpServletRequest request) {
        String errorMsg;
        // 区分异常类型，统一提取错误信息
        if (e instanceof MethodArgumentNotValidException) {
            // POST JSON 参数校验：拼接所有字段错误
            errorMsg = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        } else if (e instanceof BindException) {
            // GET/表单参数校验：单字段错误（BindException 通常只有一个字段错误）
            FieldError fieldError = ((BindException) e).getFieldError();
            errorMsg = fieldError != null ? (fieldError.getField() + ": " + fieldError.getDefaultMessage()) : "参数格式错误";
        } else {
            errorMsg = "参数校验异常";
        }
        logger.error("参数校验异常: 请求地址={},异常信息={}", request.getRequestURL(), errorMsg, e);
        return Result.error(400, "参数校验失败: " + errorMsg);
    }

}