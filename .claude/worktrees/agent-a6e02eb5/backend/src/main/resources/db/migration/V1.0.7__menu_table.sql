CREATE TABLE IF NOT EXISTS `t_menu` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `parent_id`   BIGINT       DEFAULT 0         COMMENT '父菜单ID',
    `path`        VARCHAR(200) NOT NULL           COMMENT '路由路径',
    `name`        VARCHAR(100) DEFAULT NULL       COMMENT '路由名称',
    `component`   VARCHAR(255) DEFAULT NULL       COMMENT '组件路径',
    `title`       VARCHAR(64)  NOT NULL           COMMENT '菜单标题',
    `icon`        VARCHAR(100) DEFAULT NULL       COMMENT '菜单图标',
    `rank`        INT          DEFAULT 0          COMMENT '排序',
    `roles`       VARCHAR(255) DEFAULT NULL       COMMENT '可访问角色(JSON数组)',
    `auths`       VARCHAR(500) DEFAULT NULL       COMMENT '按钮权限(JSON数组)',
    `show_link`   TINYINT      DEFAULT 1          COMMENT '是否显示菜单 1=是 0=否',
    `status`      TINYINT      DEFAULT 1          COMMENT '状态 1=启用 0=禁用',
    `remark`      VARCHAR(500) DEFAULT NULL       COMMENT '备注',
    `deleted`     INT          NOT NULL DEFAULT 0,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单管理表';
