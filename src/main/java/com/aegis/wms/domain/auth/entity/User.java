package com.aegis.wms.domain.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码(加密存储)
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色类型: admin-管理员, operator-操作员, viewer-查看员
     */
    private String roleType;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态: 1-正常, 0-停用
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}