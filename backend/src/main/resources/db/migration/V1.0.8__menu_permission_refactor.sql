-- 从旧版本迁移：添加 menu_type 和 permission 字段
-- 新建库已有这些字段（在 V1.0.7 中），以下语句会报错但 continue-on-error 会忽略
ALTER TABLE t_menu ADD COLUMN menu_type TINYINT DEFAULT 0 COMMENT '菜单类型：0=目录/菜单，1=按钮';
ALTER TABLE t_menu ADD COLUMN permission VARCHAR(100) DEFAULT NULL COMMENT '权限标识，如 system:user:add';

-- 移除已废弃字段（旧版本才有，新版本无此列，报错忽略）
ALTER TABLE t_menu DROP COLUMN roles;
ALTER TABLE t_menu DROP COLUMN auths;

-- 创建角色-菜单关联表（IF NOT EXISTS 保证幂等）
CREATE TABLE IF NOT EXISTS t_role_menu (
    role_id     BIGINT   NOT NULL COMMENT '角色ID',
    menu_id     BIGINT   NOT NULL COMMENT '菜单ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单关联表';
