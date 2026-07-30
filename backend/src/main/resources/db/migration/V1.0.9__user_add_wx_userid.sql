-- t_user 表新增 wx_user_id 和 wx_dept_id 字段，绑定企微成员
ALTER TABLE `t_user` ADD COLUMN `wx_user_id` VARCHAR(64) DEFAULT NULL COMMENT '企微成员userId' AFTER `phone`;
ALTER TABLE `t_user` ADD COLUMN `wx_dept_id` BIGINT DEFAULT NULL COMMENT '企微部门id' AFTER `wx_user_id`;
