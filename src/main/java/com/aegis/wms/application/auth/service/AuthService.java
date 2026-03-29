package com.aegis.wms.application.auth.service;

import com.aegis.wms.application.auth.dto.LoginDTO;
import com.aegis.wms.application.auth.vo.LoginVO;
import com.aegis.wms.application.auth.vo.UserVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUser(Long userId);

    /**
     * 初始化默认管理员账号
     */
    void initDefaultAdmin();
}