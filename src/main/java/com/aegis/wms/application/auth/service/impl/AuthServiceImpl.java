package com.aegis.wms.application.auth.service.impl;

import com.aegis.wms.application.auth.dto.LoginDTO;
import com.aegis.wms.application.auth.service.AuthService;
import com.aegis.wms.application.auth.vo.LoginVO;
import com.aegis.wms.application.auth.vo.UserVO;
import com.aegis.wms.common.security.JwtUtil;
import com.aegis.wms.domain.auth.entity.User;
import com.aegis.wms.domain.auth.repository.UserRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userRepository.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已停用");
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoleType());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRoleType(user.getRoleType());

        return vo;
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            return null;
        }

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRoleType(user.getRoleType());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initDefaultAdmin() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, "admin");
        if (userRepository.selectCount(wrapper) > 0) {
            return; // 已存在，不重复创建
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("lcyfgl2018"));
        admin.setRealName("系统管理员");
        admin.setRoleType("admin");
        admin.setPhone("13800138000");
        admin.setStatus(1);

        userRepository.insert(admin);
    }
}