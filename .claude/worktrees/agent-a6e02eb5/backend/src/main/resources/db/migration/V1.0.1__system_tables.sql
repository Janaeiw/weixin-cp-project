-- 角色表
CREATE TABLE IF NOT EXISTS `t_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_name`   VARCHAR(64)  NOT NULL COMMENT '角色名称',
    `role_key`    VARCHAR(64)  NOT NULL COMMENT '角色标识',
    `sort`        INT          DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1=启用 0=禁用',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted`     INT          NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `t_permission` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id`       BIGINT       NOT NULL DEFAULT 0 COMMENT '父权限ID',
    `name`            VARCHAR(64)  NOT NULL COMMENT '权限名称',
    `permission_key`  VARCHAR(128) NOT NULL COMMENT '权限标识',
    `type`            VARCHAR(16)  NOT NULL COMMENT '类型: menu/button/api',
    `sort`            INT          DEFAULT 0 COMMENT '排序',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1=启用 0=禁用',
    `remark`          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted`         INT          NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS `t_user_role` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS `t_role_permission` (
    `role_id`       BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- t_user 表新增字段（首次启动执行，后续启动如果字段已存在会报错可忽略，或改用 Flyway）
ALTER TABLE `t_user` ADD COLUMN `email`   VARCHAR(128) DEFAULT NULL COMMENT '邮箱'   AFTER `avatar`;
ALTER TABLE `t_user` ADD COLUMN `phone`   VARCHAR(32)  DEFAULT NULL COMMENT '手机号' AFTER `email`;
ALTER TABLE `t_user` ADD COLUMN `dept_id` BIGINT       DEFAULT NULL COMMENT '部门ID' AFTER `phone`;

-- 内容库素材表
CREATE TABLE IF NOT EXISTS `t_content` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type`         VARCHAR(32)  NOT NULL COMMENT '素材类型: post=推文, article=文章',
    `title`        VARCHAR(200) NOT NULL COMMENT '标题',
    `summary`      VARCHAR(500) DEFAULT NULL COMMENT '摘要',
    `cover_image`  VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
    `content`      LONGTEXT     DEFAULT NULL COMMENT '正文内容(富文本)',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1=上架 0=下架',
    `deleted`      INT          NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_type_status` (`type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容库素材表';
