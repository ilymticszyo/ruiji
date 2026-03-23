package com.example.ruiji.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义元数据处理器
 *
 * @author ocx
 */
@Component
@Slf4j
public class MyMetaObjectHandle implements MetaObjectHandler {

    /**
     * 插入时自动填充公共字段
     *
     * @param metaObject 元对象
     * @author ocx
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充[insert]...");
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createUser", Long.class, currentId);
        this.strictInsertFill(metaObject, "updateUser", Long.class, currentId);
    }

    /**
     * 更新时自动填充公共字段
     *
     * @param metaObject 元对象
     * @author ocx
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充[update]...");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateUser", Long.class, BaseContext.getCurrentId());
    }
}
