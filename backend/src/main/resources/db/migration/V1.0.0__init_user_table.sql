CREATE TABLE IF NOT EXISTS `t_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    VARCHAR(100) NOT NULL COMMENT '用户名/邮箱',
    `password`    VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`      VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1=正常 0=禁用',
    `deleted`     INT          NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
