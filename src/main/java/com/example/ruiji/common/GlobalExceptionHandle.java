package com.example.ruiji.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器：统一处理 Controller 层抛出的异常，返回统一格式的 Res 响应
 */
@ControllerAdvice(annotations = {RestController.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandle {

    /**
     * 处理 SQL 唯一约束冲突（如用户名重复）
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Res<String> handleSQLIntegrityConstraintViolation(SQLIntegrityConstraintViolationException ex) {
        log.error("SQL 唯一约束冲突: {}", ex.getMessage());
        String msg = ex.getMessage();
        if (msg != null && msg.contains("Duplicate entry")) {
            String[] parts = msg.split("'");
            return Res.error(parts.length >= 2 ? parts[1] + " 已存在" : "数据已存在");
        }
        return Res.error("数据库操作失败");
    }

    /**
     * 处理 MyBatis-Plus 等抛出的重复键异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Res<String> handleDuplicateKey(DuplicateKeyException ex) {
        log.error("重复键异常: {}", ex.getMessage());
        return Res.error("数据已存在");
    }

    /**
     * 处理数据完整性违反（如外键约束等）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Res<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("数据完整性违反: {}", ex.getMessage());
        return Res.error("数据操作失败，请检查关联数据");
    }

    /**
     * 处理参数校验异常（@Valid 校验失败）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Res<String> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.error("参数校验失败: {}", msg);
        return Res.error(msg);
    }

    /**
     * 处理参数类型不匹配（如传入非数字到 Long 类型）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Res<String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("参数类型不匹配: {}", ex.getMessage());
        return Res.error("参数格式错误");
    }

    /**
     * 处理业务异常、运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Res<String> handleRuntimeException(RuntimeException ex) {
        log.error("运行时异常: ", ex);
        return Res.error(ex.getMessage() != null ? ex.getMessage() : "系统繁忙，请稍后重试");
    }

    /**
     * 兜底：处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public Res<String> handleException(Exception ex) {
        log.error("系统异常: ", ex);
        return Res.error("系统繁忙，请稍后重试");
    }
}
