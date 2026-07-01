# Backend Quality Guidelines

## 技术栈
- Java 17, Spring Boot 3.x, MyBatis-Plus, Maven, MySQL
- 单体架构，必须支持多实例部署

---

## 核心约束（违反即阻断）

### 多实例部署约束

```java
// ❌ 禁止：本地文件存储
File file = new File("/tmp/upload/" + filename);

// ✅ 正确：使用 OSS
ossClient.putObject(bucket, key, inputStream);

// ❌ 禁止：本地缓存
Map<String, Object> localCache = new HashMap<>();

// ✅ 正确：使用 Redis
redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);

// ❌ 禁止：本地 Session
HttpSession session = request.getSession();

// ✅ 正确：Spring Session + Redis
// 配置 spring.session.store-type=redis
```

---

## 代码规范

### Lombok 使用

```java
// Entity 类
@Data
@TableName("t_xxx")
public class XxxEntity extends BaseEntity { ... }

// Service 实现类 — 用 @Slf4j
@Slf4j
@Service
public class XxxServiceImpl implements XxxService { ... }

// DTO / VO — 用 @Data
@Data
public class XxxQueryDTO {
    private String keyword;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
```

### 依赖注入规范

**必须使用构造器注入**（`@RequiredArgsConstructor`），禁止 `@Autowired` 字段注入：

```java
@Service
@RequiredArgsConstructor  // ✅
public class XxxServiceImpl implements XxxService {
    private final XxxMapper xxxMapper;
    private final RedisTemplate<String, Object> redisTemplate;
}

// ❌ 禁止字段注入
@Autowired
private XxxMapper xxxMapper;
```

---

## 接口设计规范

- RESTful 风格，路径小写，中划线分隔
- 统一返回 `R<T>` 格式
- 分页参数：`pageNum`（从 1 开始）、`pageSize`
- 分页返回：`IPage<T>` 或 `PageResult<T>` 包装

---

## 测试要求

| 层级 | 覆盖范围 | 测试方式 |
|------|---------|---------|
| Service | 核心业务逻辑 | `@SpringBootTest` + 内嵌数据库或测试库 |
| Mapper | 复杂 SQL | `@MybatisTest` |
| Controller | 接口参数校验 | `@WebMvcTest` + MockMvc |

---

## 代码风格

- 代码格式：IDEA 默认格式化或 Spotless Maven 插件
- 静态分析：建议配置 SonarLint / SpotBugs
