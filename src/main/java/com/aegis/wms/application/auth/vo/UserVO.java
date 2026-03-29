package com.aegis.wms.application.auth.vo;

import lombok.Data;

/**
 * 用户信息VO
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String roleType;
    private String phone;
    private Integer status;
}