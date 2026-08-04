# 企微客户与客群管理 - PRD

## 1. 需求背景

用户已绑定企微部门与企微员工，需要通过企微员工的userid获取其名下的客户列表与客户群列表，方便查看和管理。

## 2. 核心功能

### 2.1 客户列表
- 通过员工userid获取该员工名下的客户列表
- 展示客户信息：姓名、头像、性别、添加时间、备注、标签、所属企业、添加方式
- 支持搜索/筛选
- 点击客户可查看客户详情（全屏弹窗）

### 2.2 客群列表
- 通过员工userid获取该员工创建/管理的客户群列表
- 展示群信息：群名、群主、创建时间、成员数、群公告
- 点击群可查看群成员列表

## 3. 技术方案

### 3.1 数据同步策略
- **MVP阶段**：全量定时同步
  - 使用 `getContactList` 获取全组织客户列表
  - 使用 `listGroupChat` 获取全组织客群列表
  - 通过 `followUserid` 字段关联员工
  - 后续迭代：增量同步、首次全量+后续增量

### 3.2 SDK API选择

| 功能 | SDK方法 | 说明 |
|------|---------|------|
| 客户列表 | `getContactList(cursor, limit)` | 全量获取，支持分页 |
| 客户详情 | `getExternalContact(externalUserId)` | 单个获取 |
| 批量客户详情 | `getContactDetailBatch(externalUserIds, cursor, limit)` | 批量获取（最多100个） |
| 客群列表 | `listGroupChat(limit, cursor, statusFilter, userIds)` | 支持按userIds过滤 |
| 客群详情 | `getGroupChat(chatId, needName)` | 包含成员列表 |

### 3.3 数据库设计

#### 客户表 (t_wecom_customer)
```sql
CREATE TABLE t_wecom_customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    external_userid VARCHAR(100) NOT NULL COMMENT '外部用户ID',
    name VARCHAR(100) COMMENT '客户姓名',
    nickname VARCHAR(100) COMMENT '昵称',
    avatar VARCHAR(500) COMMENT '头像URL',
    gender TINYINT COMMENT '性别：0=未知，1=男，2=女',
    corp_name VARCHAR(200) COMMENT '企业名称',
    corp_full_name VARCHAR(200) COMMENT '企业全称',
    position VARCHAR(100) COMMENT '职位',
    union_id VARCHAR(100) COMMENT 'UnionID',
    type TINYINT COMMENT '联系人类型',
    external_profile TEXT COMMENT '扩展属性JSON',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_external_userid (external_userid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企微客户表';
```

#### 客户跟进人表 (t_wecom_customer_follow)
```sql
CREATE TABLE t_wecom_customer_follow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    external_userid VARCHAR(100) NOT NULL COMMENT '外部用户ID',
    userid VARCHAR(100) NOT NULL COMMENT '企业员工userid',
    remark VARCHAR(500) COMMENT '备注',
    description VARCHAR(500) COMMENT '描述',
    create_time_field BIGINT COMMENT '添加时间戳',
    state VARCHAR(100) COMMENT '状态标签',
    remark_company VARCHAR(200) COMMENT '企业备注',
    remark_mobiles VARCHAR(500) COMMENT '手机号备注JSON',
    tag_ids VARCHAR(500) COMMENT '标签ID列表JSON',
    tags TEXT COMMENT '标签详情JSON',
    remark_corp_name VARCHAR(200) COMMENT '企业名称备注',
    add_way VARCHAR(50) COMMENT '添加方式',
    operator_userid VARCHAR(100) COMMENT '操作人userid',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follow (external_userid, userid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户跟进人关系表';
```

#### 客群表 (t_wecom_group_chat)
```sql
CREATE TABLE t_wecom_group_chat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chat_id VARCHAR(100) NOT NULL COMMENT '群聊ID',
    name VARCHAR(200) COMMENT '群名称',
    owner VARCHAR(100) COMMENT '群主userid',
    create_time_field BIGINT COMMENT '创建时间戳',
    notice TEXT COMMENT '群公告',
    member_count INT DEFAULT 0 COMMENT '成员数量',
    status TINYINT DEFAULT 1 COMMENT '状态：0=已解散，1=正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_chat_id (chat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企微客户群表';
```

#### 客群成员表 (t_wecom_group_chat_member)
```sql
CREATE TABLE t_wecom_group_chat_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chat_id VARCHAR(100) NOT NULL COMMENT '群聊ID',
    user_id VARCHAR(100) NOT NULL COMMENT '成员userid或external_userid',
    member_type TINYINT COMMENT '成员类型：1=企业成员，2=外部联系人',
    join_time BIGINT COMMENT '入群时间戳',
    join_scene TINYINT COMMENT '入群方式：1=成员邀请，2=管理员邀请，3=扫描二维码',
    group_nickname VARCHAR(100) COMMENT '群昵称',
    name VARCHAR(100) COMMENT '真实姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_member (chat_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客群成员表';
```

## 4. 接口设计

### 4.1 客户相关接口

#### 获取客户列表
- **URL**: `GET /api/wx/customer/list`
- **参数**:
  - `userId`: 企微员工userid（必填）
  - `keyword`: 搜索关键词（可选）
  - `page`: 页码（默认1）
  - `pageSize`: 每页数量（默认20）
- **返回**: 分页客户列表

#### 获取客户详情
- **URL**: `GET /api/wx/customer/{externalUserId}`
- **返回**: 客户详细信息，包含所有跟进人

### 4.2 客群相关接口

#### 获取客群列表
- **URL**: `GET /api/wx/group-chat/list`
- **参数**:
  - `userId`: 企微员工userid（必填）
  - `keyword`: 搜索关键词（可选）
  - `page`: 页码（默认1）
  - `pageSize`: 每页数量（默认20）
- **返回**: 分页客群列表

#### 获取客群详情
- **URL**: `GET /api/wx/group-chat/{chatId}`
- **返回**: 客群详细信息，包含成员列表

### 4.3 数据同步接口

#### 触发全量同步
- **URL**: `POST /api/wx/customer/sync`
- **说明**: 手动触发全量同步（定时任务也会自动执行）

## 5. 菜单结构

```
客户管理 (目录，path: /customer)
├── 客户列表 (菜单，path: /customer/list，component: wx/customer/list)
└── 客群列表 (菜单，path: /customer/group，component: wx/customer-group/list)
```

## 6. 页面设计

### 6.1 客户列表页
- 左侧：企微部门树 + 员工列表
- 右侧：客户列表表格
  - 列：头像、姓名、性别、所属企业、添加时间、添加方式、操作
  - 操作：查看详情
- 搜索框：按姓名/企业搜索

### 6.2 客户详情弹窗（全屏）
- 默认全屏状态
- 展示客户所有信息
- 展示所有跟进人列表
- 展示标签信息

### 6.3 客群列表页
- 左侧：企微部门树 + 员工列表
- 右侧：客群列表表格
  - 列：群名、群主、成员数、创建时间、操作
  - 操作：查看详情
- 搜索框：按群名搜索

### 6.4 客群详情弹窗（全屏）
- 默认全屏状态
- 展示群基本信息
- 展示群成员列表（区分企业成员和外部联系人）

## 7. MVP范围

### 7.1 包含
- 全量数据同步（定时任务）
- 客户列表展示
- 客户详情查看（全屏弹窗）
- 客群列表展示
- 客群详情查看（含成员列表）
- 基础搜索功能

### 7.2 不包含（后续迭代）
- 增量同步
- 数据导出
- 客户标签管理
- 客户转移

## 8. 开发任务拆解

### 8.1 后端任务
1. 创建数据库表和Migration
2. 创建Entity和Mapper
3. 创建Service层（数据同步 + 查询）
4. 创建Controller层（接口）
5. 配置定时任务

### 8.2 前端任务
1. 创建API层
2. 创建客户列表页面
3. 创建客户详情弹窗组件
4. 创建客群列表页面
5. 创建客群详情弹窗组件
6. 新增菜单配置（通过接口或SQL）
