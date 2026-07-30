-- t_user 表新增企微绑定字段
ALTER TABLE `t_user` ADD COLUMN `wx_user_id` VARCHAR(64) DEFAULT NULL COMMENT '企微成员userId' AFTER `phone`;
ALTER TABLE `t_user` ADD COLUMN `wx_user_name` VARCHAR(64) DEFAULT NULL COMMENT '企微成员名称' AFTER `wx_user_id`;
ALTER TABLE `t_user` ADD COLUMN `wx_dept_id` BIGINT DEFAULT NULL COMMENT '企微部门id' AFTER `wx_user_name`;
ALTER TABLE `t_user` ADD COLUMN `wx_dept_name` VARCHAR(64) DEFAULT NULL COMMENT '企微部门名称' AFTER `wx_dept_id`;
