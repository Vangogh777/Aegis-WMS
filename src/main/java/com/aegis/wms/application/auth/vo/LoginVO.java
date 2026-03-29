package com.aegis.wms.application.auth.vo;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginVO {

    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色类型
     */
    private String roleType;
}