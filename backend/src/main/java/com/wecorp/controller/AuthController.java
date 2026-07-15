package com.wecorp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import com.wecorp.common.exception.BusinessException;
import com.wecorp.common.result.R;
import com.wecorp.common.utils.JwtUtils;
import com.wecorp.entity.Menu;
import com.wecorp.entity.Role;
import com.wecorp.entity.User;
import com.wecorp.entity.UserRole;
import com.wecorp.mapper.MenuMapper;
import com.wecorp.mapper.RoleMapper;
import com.wecorp.mapper.RoleMenuMapper;
import com.wecorp.mapper.UserRoleMapper;
import com.wecorp.entity.RoleMenu;
import com.wecorp.service.SystemService;
import com.wecorp.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final SystemService systemService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        User user = userService.getByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }

        String accessToken = jwtUtils.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());
        long expires = System.currentTimeMillis() + jwtExpiration;
        long refreshExpires = System.currentTimeMillis() + refreshExpiration;

        // 从 t_user_role + t_role 关联表查真实角色
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()));
        List<String> roles = List.of();
        if (!userRoles.isEmpty()) {
            Set<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
            roles = roleMapper.selectBatchIds(roleIds).stream()
                    .map(Role::getRoleKey)
                    .collect(Collectors.toList());
        }
        // 从 t_role_menu + t_menu 查询真实权限
        List<String> permissions;
        if (roles.contains("admin")) {
            permissions = List.of("*:*:*");
        } else {
            Set<Long> allRoleIds = userRoles.stream()
                    .map(UserRole::getRoleId).collect(Collectors.toSet());
            if (allRoleIds.isEmpty()) {
                permissions = List.of();
            } else {
                // 查这些角色关联的菜单ID
                List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                        new LambdaQueryWrapper<RoleMenu>().in(RoleMenu::getRoleId, allRoleIds));
                Set<Long> menuIds = roleMenus.stream()
                        .map(RoleMenu::getMenuId).collect(Collectors.toSet());
                if (menuIds.isEmpty()) {
                    permissions = List.of();
                } else {
                    // 查其中按钮类型且有 permission 标识的菜单
                    List<Menu> buttonMenus = menuMapper.selectList(
                            new LambdaQueryWrapper<Menu>()
                                    .in(Menu::getId, menuIds)
                                    .eq(Menu::getMenuType, 1)
                                    .isNotNull(Menu::getPermission));
                    permissions = buttonMenus.stream()
                            .map(Menu::getPermission)
                            .distinct()
                            .collect(Collectors.toList());
                }
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        data.put("roles", roles);
        data.put("permissions", permissions);
        data.put("expires", new Date(expires));
        data.put("refreshExpires", new Date(refreshExpires));

        return R.ok(data);
    }

    @GetMapping("/routes")
    public R<List<Map<String, Object>>> getRoutes(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        List<String> userRoles = getCurrentUserRoles(authHeader);

        if (userRoles.isEmpty()) {
            return R.ok(List.of());
        }

        // 统一从 t_role_menu 查询角色关联的菜单ID（admin 也走此路径）
        Set<Long> roleIds = userRoles.stream()
                .flatMap(roleKey -> {
                    Role role = roleMapper.selectOne(
                            new LambdaQueryWrapper<Role>().eq(Role::getRoleKey, roleKey));
                    return role != null ? Stream.of(role.getId()) : Stream.empty();
                })
                .collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            return R.ok(List.of());
        }
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenu>().in(RoleMenu::getRoleId, roleIds));
        Set<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId).collect(Collectors.toSet());
        if (menuIds.isEmpty()) {
            return R.ok(List.of());
        }
        return R.ok(systemService.getRouteTreeByMenuIds(menuIds));
    }

    private List<String> getCurrentUserRoles(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return List.of();
        String token = authHeader.substring(7);
        try {
            Long userId = jwtUtils.getUserId(token);
            List<UserRole> userRoles = userRoleMapper.selectList(
                    new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
            if (userRoles.isEmpty()) return List.of();
            Set<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
            return roleMapper.selectBatchIds(roleIds).stream()
                    .map(Role::getRoleKey).collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    @PostMapping("/refresh-token")
    public R<Map<String, Object>> refreshToken(@RequestBody Map<String, String> body) {
        String refreshTokenValue = body.get("refreshToken");
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new BusinessException("refreshToken 不能为空");
        }
        if (!jwtUtils.isRefreshTokenValid(refreshTokenValue)) {
            throw new BusinessException("refreshToken 无效或已过期");
        }

        Long userId = jwtUtils.getUserId(refreshTokenValue);
        String username = jwtUtils.getUsername(refreshTokenValue);

        String newAccessToken = jwtUtils.generateToken(userId, username);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId, username);
        long expires = System.currentTimeMillis() + jwtExpiration;
        long refreshExpires = System.currentTimeMillis() + refreshExpiration;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("accessToken", newAccessToken);
        data.put("refreshToken", newRefreshToken);
        data.put("expires", new Date(expires));
        data.put("refreshExpires", new Date(refreshExpires));

        return R.ok(data);
    }

    @Data
    public static class LoginDTO {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;
    }
}
