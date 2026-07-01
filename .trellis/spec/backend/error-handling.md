# Backend Error Handling

## 统一响应格式

所有接口返回 `R<T>` 统一包装：

```java
@Data
public class R<T> {
    private int code;       // 0=成功, 非0=失败
    private String msg;     // 错误信息（用户友好）
    private T data;         // 业务数据

    public static <T> R<T> ok(T data) { ... }
    public static <T> R<T> fail(String msg) { ... }
    public static <T> R<T> fail(int code, String msg) { ... }
}
```

---

## 异常分层

### 业务异常 — 用户可感知

```java
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        this(ErrorCodes.BUSINESS_ERROR, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
```

抛出场景：参数校验失败、业务规则不满足、权限不足

### 系统异常 — 程序员需要修复的

- `NullPointerException`、`IllegalArgumentException` 等
- 数据库连接失败、Redis 连接超时
- 由全局异常处理器兜底，返回通用错误信息

---

## 全局异常处理器

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public R<?> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return R.fail(ErrorCodes.PARAM_ERROR, msg);
    }

    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(ErrorCodes.SYSTEM_ERROR, "系统繁忙，请稍后重试");
    }
}
```

---

## 错误码规范

```java
public final class ErrorCodes {
    public static final int SUCCESS          = 0;
    public static final int SYSTEM_ERROR     = 10000;
    public static final int PARAM_ERROR      = 10001;
    public static final int BUSINESS_ERROR   = 10002;
    public static final int AUTH_ERROR       = 10003;

    // 企业微信相关 2xxxx
    public static final int WX_API_ERROR      = 20001;
    public static final int WX_CALLBACK_ERROR = 20002;
}
```

---

## 规则

1. **Controller 层不 try-catch** — 交给全局异常处理器
2. **Service 层抛 BusinessException** — 不返回 null 或特殊值表示失败
3. **日志记录** — 业务异常用 `warn`，系统异常用 `error` 并带堆栈
4. **错误信息面向用户** — 不暴露 SQL、堆栈、配置路径等技术细节
5. **WxJava SDK 异常** — catch 后包装为 `BusinessException(WX_API_ERROR, e.getMessage())`
