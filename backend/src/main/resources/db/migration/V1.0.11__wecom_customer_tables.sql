-- 企微客户表
CREATE TABLE IF NOT EXISTS `t_wecom_customer` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT,
    `external_userid`   VARCHAR(100) NOT NULL COMMENT '外部用户ID',
    `name`              VARCHAR(100) DEFAULT NULL COMMENT '客户姓名',
    `nickname`          VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    `avatar`            VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `gender`            TINYINT      DEFAULT 0 COMMENT '性别：0=未知，1=男，2=女',
    `type`              TINYINT      DEFAULT 0 COMMENT '联系人类型',
    `corp_name`         VARCHAR(200) DEFAULT NULL COMMENT '企业名称',
    `corp_full_name`    VARCHAR(200) DEFAULT NULL COMMENT '企业全称',
    `position`          VARCHAR(100) DEFAULT NULL COMMENT '职位',
    `union_id`          VARCHAR(100) DEFAULT NULL COMMENT 'UnionID',
    `external_profile`  TEXT         DEFAULT NULL COMMENT '扩展属性JSON',
    `deleted`           INT          NOT NULL DEFAULT 0,
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_external_userid` (`external_userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企微客户表';

-- 客户跟进人关系表
CREATE TABLE IF NOT EXISTS `t_wecom_customer_follow` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT,
    `external_userid`   VARCHAR(100) NOT NULL COMMENT '外部用户ID',
    `userid`            VARCHAR(100) NOT NULL COMMENT '企业员工userid',
    `remark`            VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `description`       VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `follow_create_time` BIGINT     DEFAULT NULL COMMENT '添加时间戳',
    `state`             VARCHAR(100) DEFAULT NULL COMMENT '状态标签',
    `remark_company`    VARCHAR(200) DEFAULT NULL COMMENT '企业备注',
    `remark_mobiles`    VARCHAR(500) DEFAULT NULL COMMENT '手机号备注JSON',
    `tag_ids`           VARCHAR(500) DEFAULT NULL COMMENT '标签ID列表JSON',
    `tags`              TEXT         DEFAULT NULL COMMENT '标签详情JSON',
    `remark_corp_name`  VARCHAR(200) DEFAULT NULL COMMENT '企业名称备注',
    `add_way`           VARCHAR(50)  DEFAULT NULL COMMENT '添加方式',
    `operator_userid`   VARCHAR(100) DEFAULT NULL COMMENT '操作人userid',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follow` (`external_userid`, `userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户跟进人关系表';

-- 企微客户群表
CREATE TABLE IF NOT EXISTS `t_wecom_group_chat` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT,
    `chat_id`           VARCHAR(100) NOT NULL COMMENT '群聊ID',
    `name`              VARCHAR(200) DEFAULT NULL COMMENT '群名称',
    `owner`             VARCHAR(100) DEFAULT NULL COMMENT '群主userid',
    `create_time_field` BIGINT       DEFAULT NULL COMMENT '创建时间戳',
    `notice`            TEXT         DEFAULT NULL COMMENT '群公告',
    `member_count`      INT          DEFAULT 0 COMMENT '成员数量',
    `status`            TINYINT      DEFAULT 1 COMMENT '状态：0=已解散，1=正常',
    `deleted`           INT          NOT NULL DEFAULT 0,
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chat_id` (`chat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企微客户群表';

-- 客群成员表
CREATE TABLE IF NOT EXISTS `t_wecom_group_chat_member` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT,
    `chat_id`           VARCHAR(100) NOT NULL COMMENT '群聊ID',
    `user_id`           VARCHAR(100) NOT NULL COMMENT '成员userid或external_userid',
    `member_type`       TINYINT      DEFAULT NULL COMMENT '成员类型：1=企业成员，2=外部联系人',
    `join_time`         BIGINT       DEFAULT NULL COMMENT '入群时间戳',
    `join_scene`        TINYINT      DEFAULT NULL COMMENT '入群方式：1=成员邀请，2=管理员邀请，3=扫描二维码',
    `group_nickname`    VARCHAR(100) DEFAULT NULL COMMENT '群昵称',
    `name`              VARCHAR(100) DEFAULT NULL COMMENT '真实姓名',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member` (`chat_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客群成员表';
