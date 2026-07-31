package com.wecorp.common.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wecorp.common.annotation.OperationLog;
import com.wecorp.entity.Menu;
import com.wecorp.entity.User;
import com.wecorp.mapper.MenuMapper;
import com.wecorp.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final MenuMapper menuMapper;
    private final ObjectMapper objectMapper;

    /**
     * 拦截所有 Controller 的写操作（POST/PUT/DELETE），
     * 以及显式标注 @OperationLog 的方法（包括 GET）。
     */
    @Around("@annotation(com.wecorp.common.annotation.OperationLog) || " +
            "(@within(org.springframework.web.bind.annotation.RestController) && " +
            "(execution(* *(..)) && " +
            "(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            " @annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            " @annotation(org.springframework.web.bind.annotation.DeleteMapping))))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();

        // 跳过日志查询接口自身，避免死循环
        String uri = request.getRequestURI();
        if (uri.contains("/operation-log/")) {
            return joinPoint.proceed();
        }

        // 构建日志对象
        com.wecorp.entity.OperationLog operationLog = new com.wecorp.entity.OperationLog();
        operationLog.setRequestUrl(uri);
        operationLog.setRequestMethod(request.getMethod());
        operationLog.setIp(getClientIp(request));
        operationLog.setOs(parseOs(request.getHeader("User-Agent")));
        operationLog.setBrowser(parseBrowser(request.getHeader("User-Agent")));
        operationLog.setTraceId(request.getHeader("X-Trace-Id") != null
                ? request.getHeader("X-Trace-Id") : UUID.randomUUID().toString().replace("-", ""));

        // 请求头
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if ("authorization".equalsIgnoreCase(name)) continue;
            headers.put(name, request.getHeader(name));
        }
        operationLog.setRequestHeaders(toJson(headers));

        // 请求体
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                List<Object> bodyArgs = new ArrayList<>();
                for (Object arg : args) {
                    if (!(arg instanceof HttpServletRequest)
                            && !(arg instanceof HttpServletResponse)) {
                        bodyArgs.add(arg);
                    }
                }
                if (!bodyArgs.isEmpty()) {
                    operationLog.setRequestBody(objectMapper.writeValueAsString(
                            bodyArgs.size() == 1 ? bodyArgs.get(0) : bodyArgs));
                }
            }
        } catch (Exception e) {
            log.warn("序列化请求参数失败", e);
        }

        // 注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        operationLog.setMethod(signature.getDeclaringTypeName() + "." + method.getName());

        OperationLog annotation = method.getAnnotation(OperationLog.class);
        if (annotation != null) {
            if (StringUtils.hasText(annotation.module())) {
                operationLog.setModule(annotation.module());
            } else {
                operationLog.setModule(resolveModule(uri));
            }
            if (StringUtils.hasText(annotation.operation())) {
                operationLog.setOperation(annotation.operation());
            } else {
                operationLog.setOperation(guessOperation(request.getMethod()));
            }
        } else {
            operationLog.setModule(resolveModule(uri));
            operationLog.setOperation(guessOperation(request.getMethod()));
        }

        // 操作人信息
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User user) {
                operationLog.setOperatorId(user.getId());
                operationLog.setOperatorName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        } catch (Exception e) {
            log.debug("获取操作人信息失败", e);
        }

        // 执行目标方法
        Object result;
        try {
            result = joinPoint.proceed();
            operationLog.setStatusCode(200);
            try {
                String responseBody = objectMapper.writeValueAsString(result);
                if (responseBody.length() > 65535) {
                    responseBody = responseBody.substring(0, 65535);
                }
                operationLog.setResponseBody(responseBody);
            } catch (Exception e) {
                log.warn("序列化响应结果失败", e);
            }
        } catch (Throwable ex) {
            operationLog.setStatusCode(500);
            operationLog.setExceptionMsg(truncate(ex.getMessage(), 2000));
            throw ex;
        } finally {
            operationLog.setCostTime(System.currentTimeMillis() - startTime);
            try {
                operationLogMapper.insert(operationLog);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }

        return result;
    }

    /**
     * 从请求路径解析所属模块：优先查菜单表获取标题，找不到则回退为路径
     * /api/system/user/page -> 查 path=/system/user -> "用户管理"
     * /api/library/content  -> 查 path=/library/content -> "内容库"
     */
    private String resolveModule(String uri) {
        if (uri == null) return "";
        // /api/system/user/page -> ["", "api", "system", "user", "page"]
        String[] parts = uri.split("/");
        if (parts.length < 3) return uri;

        // 尝试由长到短匹配菜单路径
        // /api/library/content/page -> 尝试 /library/content/page, /library/content, /library
        for (int i = parts.length; i >= 3; i--) {
            String menuPath = String.join("/", Arrays.copyOfRange(parts, 1, i));
            Menu menu = menuMapper.selectOne(
                    new LambdaQueryWrapper<Menu>().eq(Menu::getPath, menuPath));
            if (menu != null && StringUtils.hasText(menu.getTitle())) {
                return menu.getTitle();
            }
        }

        // 兜底：返回 /system/user 这样的路径
        if (parts.length >= 4) {
            return "/" + parts[2] + "/" + parts[3];
        }
        return "/" + parts[2];
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    private String parseOs(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac OS")) return "macOS";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
        return "Other";
    }

    private String parseBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.contains("Edg/")) return "Edge";
        if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) return "Safari";
        return "Other";
    }

    private String guessOperation(String httpMethod) {
        if (httpMethod == null) return "";
        return switch (httpMethod.toUpperCase()) {
            case "GET" -> "查询";
            case "POST" -> "新增";
            case "PUT" -> "修改";
            case "DELETE" -> "删除";
            default -> httpMethod;
        };
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
}
