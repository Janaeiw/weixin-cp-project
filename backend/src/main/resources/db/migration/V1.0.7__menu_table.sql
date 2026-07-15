CREATE TABLE IF NOT EXISTS `t_menu` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `parent_id`   BIGINT       DEFAULT 0         COMMENT '父菜单ID',
    `path`        VARCHAR(200) NOT NULL           COMMENT '路由路径',
    `name`        VARCHAR(100) DEFAULT NULL       COMMENT '路由名称',
    `component`   VARCHAR(255) DEFAULT NULL       COMMENT '组件路径',
    `title`       VARCHAR(64)  NOT NULL           COMMENT '菜单标题',
    `icon`        VARCHAR(100) DEFAULT NULL       COMMENT '菜单图标',
    `rank`        INT          DEFAULT 0          COMMENT '排序',
    `menu_type`   TINYINT      DEFAULT 0          COMMENT '菜单类型：0=目录/菜单，1=按钮',
    `permission`  VARCHAR(100) DEFAULT NULL       COMMENT '权限标识，如 system:user:add',
    `show_link`   TINYINT      DEFAULT 1          COMMENT '是否显示菜单 1=是 0=否',
    `status`      TINYINT      DEFAULT 1          COMMENT '状态 1=启用 0=禁用',
    `remark`      VARCHAR(500) DEFAULT NULL       COMMENT '备注',
    `deleted`     INT          NOT NULL DEFAULT 0,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单管理表';

CREATE TABLE IF NOT EXISTS `t_role_menu` (
    `role_id`     BIGINT   NOT NULL COMMENT '角色ID',
    `menu_id`     BIGINT   NOT NULL COMMENT '菜单ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单关联表';
