# Research: 企微外部联系人/客户回调事件

- **Query**: WxJava SDK (weixin-java-cp 4.8.0) 中与企微客户/外部联系人相关的回调事件支持
- **Scope**: mixed (SDK jar 反编译 + 项目内部代码)
- **Date**: 2026-08-04

## Findings

### 1. SDK 支持的外部联系人相关回调事件

SDK 在 `me.chanjar.weixin.cp.constant.WxCpConsts` 中定义了三大类外部联系人相关事件常量：

#### 1.1 事件类型 (EventType)

| 常量名 | 字符串值 | 说明 |
|--------|---------|------|
| `CHANGE_EXTERNAL_CONTACT` | `change_external_contact` | 外部联系人变更（客户添加/删除/编辑等） |
| `CHANGE_EXTERNAL_CHAT` | `change_external_chat` | 客户群变更（群创建/更新/解散） |
| `CHANGE_EXTERNAL_TAG` | `change_external_tag` | 客户标签变更（标签增删改） |
| `CUSTOMER_ACQUISITION` | `customer_acquisition` | 客户获客链接事件 |

#### 1.2 ExternalContactChangeType (change_external_contact 的子类型)

类路径: `me.chanjar.weixin.cp.constant.WxCpConsts$ExternalContactChangeType`

| 常量名 | 字符串值 | 触发时机 |
|--------|---------|---------|
| `ADD_EXTERNAL_CONTACT` | `add_external_contact` | 企业成员添加外部联系人（客户主动添加或成员主动添加，且已完成好友验证） |
| `EDIT_EXTERNAL_CONTACT` | `edit_external_contact` | 外部联系人编辑（客户修改昵称/头像等） |
| `DEL_EXTERNAL_CONTACT` | `del_external_contact` | 企业成员删除外部联系人（成员主动删除客户） |
| `ADD_HALF_EXTERNAL_CONTACT` | `add_half_external_contact` | 外部联系人半添加（客户主动添加成员但成员未通过验证，处于待确认状态） |
| `DEL_FOLLOW_USER` | `del_follow_user` | 客户删除企业成员（客户主动删除/拉黑成员） |
| `TRANSFER_FAIL` | `transfer_fail` | 客户转接失败 |

**TransferFailReason (transfer_fail 的失败原因)**:
- `CUSTOMER_REFUSED` = `customer_refused` — 客户拒绝
- `CUSTOMER_LIMIT_EXCEED` = `customer_limit_exceed` — 客户好友数超限

#### 1.3 ExternalChatChangeType (change_external_chat 的子类型)

类路径: `me.chanjar.weixin.cp.constant.WxCpConsts$ExternalChatChangeType`

| 常量名 | 字符串值 | 触发时机 |
|--------|---------|---------|
| `CREATE` | `create` | 客户群被创建 |
| `UPDATE` | `update` | 客户群被修改（群名/群公告/群成员变化等） |
| `DISMISS` | `dismiss` | 客户群被解散 |

#### 1.4 ExternalTagChangeType (change_external_tag 的子类型)

类路径: `me.chanjar.weixin.cp.constant.WxCpConsts$ExternalTagChangeType`

| 常量名 | 字符串值 | 触发时机 |
|--------|---------|---------|
| `CREATE` | `create` | 企业客户标签被创建 |
| `UPDATE` | `update` | 企业客户标签被修改 |
| `DELETE` | `delete` | 企业客户标签被删除 |
| `SHUFFLE` | `shuffle` | 企业客户标签排序被调整 |

---

### 2. WxCpXmlMessage 中的外部联系人相关字段

类路径: `me.chanjar.weixin.cp.bean.message.WxCpXmlMessage`

当事件类型为 `change_external_contact` / `change_external_chat` / `change_external_tag` 时，以下字段会被填充：

| 字段 | 类型 | 说明 |
|------|------|------|
| `event` | String | 事件类型，值为 `change_external_contact` 等 |
| `changeType` | String | 变更类型，值为 `add_external_contact` 等 |
| `externalUserId` | String | 外部联系人的 external_userid |
| `userId` | String | 企业成员的 userid（触发事件的员工） |
| `state` | String | 添加渠道的 state 参数 |
| `welcomeCode` | String | 欢迎码（add_external_contact 时有值） |
| `chatId` | String | 客户群的 chat_id（change_external_chat 时有值） |
| `tagId` | String | 标签 ID（change_external_tag 时有值） |
| `tagType` | String | 标签类型 |
| `failReason` | String | 转接失败原因（transfer_fail 时有值） |
| `source` | String | 添加来源 |

---

### 3. 回调处理机制 (WxCpMessageRouter)

SDK 提供 `WxCpMessageRouter` 机制来注册事件处理器，核心类：

| 类 | 路径 | 说明 |
|----|------|------|
| `WxCpMessageRouter` | `me.chanjar.weixin.cp.message.WxCpMessageRouter` | 消息路由器，管理规则列表 |
| `WxCpMessageRouterRule` | `me.chanjar.weixin.cp.message.WxCpMessageRouterRule` | 路由规则，支持按 msgType/event/eventKey 等条件匹配 |
| `WxCpMessageHandler` | `me.chanjar.weixin.cp.message.WxCpMessageHandler` | 消息处理器接口 |

**WxCpMessageHandler 接口**:
```java
public interface WxCpMessageHandler {
    WxCpXmlOutMessage handle(
        WxCpXmlMessage wxMessage,
        Map<String, Object> context,
        WxCpService wxCpService,
        WxSessionManager sessionManager
    ) throws WxErrorException;
}
```

**WxCpMessageRouterRule 匹配条件**:
- `msgType(String)` — 消息类型匹配
- `event(String)` — 事件类型匹配（如 `change_external_contact`）
- `eventKey(String)` / `eventKeyRegex(String)` — 事件 Key 匹配
- `agentId(Integer)` — 应用 ID 匹配
- `fromUser(String)` — 发送者匹配
- `matcher(WxCpMessageMatcher)` — 自定义匹配器

**WxCpMessageRouterRule 配置方法**:
- `handler(WxCpMessageHandler, WxCpMessageHandler...)` — 设置处理器
- `interceptor(WxCpMessageInterceptor)` — 设置拦截器
- `async(boolean)` — 是否异步执行
- `end()` — 结束规则配置
- `next()` — 开始下一条规则

---

### 4. 项目中已有的回调处理代码

#### 4.1 WxCallbackController (已实现)

文件: `backend/src/main/java/com/wecorp/controller/WxCallbackController.java`

**现状**:
- 已有 GET 验证和 POST 接收端点 (`/api/wx/callback`)
- POST 端点中使用 `WxCpMessageRouter.route(inMessage)` 路由消息
- **但 Router 是每次请求 new 出来的，没有注册任何规则或处理器**
- 目前只是把消息路由出去，没有实际处理逻辑

**关键代码** (第 101-103 行):
```java
WxCpMessageRouter router = new WxCpMessageRouter(wxCpService);
WxCpXmlOutMessage outMessage = router.route(inMessage);
```

#### 4.2 WecomCustomerSyncTask (已实现)

文件: `backend/src/main/java/com/wecorp/task/WecomCustomerSyncTask.java`

**现状**:
- 启动时全量同步 + 每天凌晨 1 点定时全量同步
- 注释明确写了: "增量同步：待接入企微回调事件后实现"
- 目前没有回调驱动的增量同步机制

#### 4.3 WxCpConfig (配置)

文件: `backend/src/main/java/com/wecorp/config/WxCpConfig.java`

**现状**:
- 配置了 `WxCpService` Bean
- 没有配置 `WxCpMessageRouter` Bean
- 没有注册任何 `WxCpMessageHandler`

---

### 5. 推荐的实现方式

基于 SDK 已有的机制，实现回调事件处理的标准模式：

**方式一：注册为 Spring Bean（推荐）**
1. 在 `WxCpConfig` 中创建 `WxCpMessageRouter` Bean
2. 在 Router Bean 中通过 `rule().event(...).handler(...).end()` 链式注册规则
3. 注入 Router 到 `WxCpCallbackController`，替换当前的 `new WxCpMessageRouter()`

**方式二：在 Controller 中构建 Router**
1. 在 Controller 的 `@PostConstruct` 或构造方法中构建 Router 并注册规则
2. 将 Router 存为 Controller 的成员变量

两种方式都需要：
1. 实现 `WxCpMessageHandler` 接口来处理具体的外部联系人事件
2. 在 handler 的 `handle` 方法中，通过 `wxMessage.getChangeType()` 判断具体子事件类型
3. 根据 `changeType` 执行不同的业务逻辑（如增量同步客户数据）

---

### 6. 外部联系人事件回调 XML 结构示例

企微推送的 `change_external_contact` 事件 XML 格式：
```xml
<xml>
   <ToUserName><![CDATA[corpid]]></ToUserName>
   <FromUserName><![CDATA[UserID]]></FromUserName>
   <CreateTime>1348831860</CreateTime>
   <MsgType><![CDATA[event]]></MsgType>
   <Event><![CDATA[change_external_contact]]></Event>
   <ChangeType><![CDATA[add_external_contact]]></ChangeType>
   <UserID><![CDATA[zhangsan]]></UserID>
   <ExternalUserID><![CDATA[woAAAAAAAAAAAAAAAAAAAAAAAA]]></ExternalUserID>
   <State><![CDATA[test_state]]></State>
   <WelcomeCode><![CDATA[WELCOME_CODE]]></WelcomeCode>
</xml>
```

这些字段对应 `WxCpXmlMessage` 的属性：`event` -> `Event`, `changeType` -> `ChangeType`, `userId` -> `UserID`, `externalUserId` -> `ExternalUserID`, `state` -> `State`, `welcomeCode` -> `WelcomeCode`.

---

### 7. 涉及的 SDK 关键类汇总

| 类 | 包路径 | 用途 |
|----|--------|------|
| `WxCpConsts.EventType` | `me.chanjar.weixin.cp.constant` | 事件类型常量 |
| `WxCpConsts.ExternalContactChangeType` | `me.chanjar.weixin.cp.constant` | 外部联系人变更子类型 |
| `WxCpConsts.ExternalChatChangeType` | `me.chanjar.weixin.cp.constant` | 客户群变更子类型 |
| `WxCpConsts.ExternalTagChangeType` | `me.chanjar.weixin.cp.constant` | 客户标签变更子类型 |
| `WxCpXmlMessage` | `me.chanjar.weixin.cp.bean.message` | 回调消息解析实体 |
| `WxCpMessageRouter` | `me.chanjar.weixin.cp.message` | 消息路由器 |
| `WxCpMessageRouterRule` | `me.chanjar.weixin.cp.message` | 路由规则 |
| `WxCpMessageHandler` | `me.chanjar.weixin.cp.message` | 消息处理器接口 |
| `WxCpExternalContactService` | `me.chanjar.weixin.cp.api` | 外部联系人 API 服务 |

---

## Caveats / Not Found

1. **WxCpMessageInterceptor** — SDK 中存在此接口但反编译失败，未能确认其方法签名。推测是标准的拦截器模式（pre/post handle）。
2. **回调事件的详细 XML 字段** — 以上信息基于 SDK 类结构反编译。企微官方文档中的完整回调字段列表可能有细微差异，建议参考[企微官方文档](https://developer.work.weixin.qq.com/document/path/90968)做交叉验证。
3. **change_external_tag 事件中的 shuffle 类型** — 企微官方文档中可能标记为"调整客户标签排序"，SDK 中确实有此常量，但实际触发频率可能较低。
4. **项目当前状态** — `WxCallbackController` 已经可以接收回调，但没有注册任何处理器。`WecomCustomerSyncTask` 的 TODO 注释明确表示需要接入回调事件实现增量同步。
