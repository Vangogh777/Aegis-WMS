package com.aegis.wms.interfaces.controller.auth;

import com.aegis.wms.application.auth.dto.LoginDTO;
import com.aegis.wms.application.auth.service.AuthService;
import com.aegis.wms.application.auth.vo.LoginVO;
import com.aegis.wms.application.auth.vo.UserVO;
import com.aegis.wms.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "登录、获取用户信息")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     */
    @Operation(summary = "登录", description = "用户登录获取Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        LoginVO vo = authService.login(dto);
        return Result.success(vo);
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户信息")
    @GetMapping("/user-info")
    public Result<UserVO> getUserInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserVO vo = authService.getCurrentUser(userId);
        return Result.success(vo);
    }

    /**
     * 初始化默认管理员
     */
    @Operation(summary = "初始化管理员", description = "初始化默认管理员账号(仅首次使用)")
    @PostMapping("/init")
    public Result<String> initAdmin() {
        authService.initDefaultAdmin();
        return Result.success("初始化成功");
    }
}