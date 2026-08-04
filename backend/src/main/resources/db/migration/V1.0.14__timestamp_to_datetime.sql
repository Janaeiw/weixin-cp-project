-- 将时间戳字段从bigint转为datetime（保留已有数据）
-- 客群表: create_time_field
UPDATE t_wecom_group_chat SET create_time_field = NULL WHERE create_time_field <= 0;
UPDATE t_wecom_group_chat SET create_time_field = FROM_UNIXTIME(create_time_field) WHERE create_time_field IS NOT NULL;
ALTER TABLE t_wecom_group_chat MODIFY COLUMN create_time_field DATETIME DEFAULT NULL COMMENT '群创建时间';

-- 客群成员表: join_time
UPDATE t_wecom_group_chat_member SET join_time = NULL WHERE join_time <= 0;
UPDATE t_wecom_group_chat_member SET join_time = FROM_UNIXTIME(join_time) WHERE join_time IS NOT NULL;
ALTER TABLE t_wecom_group_chat_member MODIFY COLUMN join_time DATETIME DEFAULT NULL COMMENT '入群时间';

-- 客户跟进表: follow_create_time
UPDATE t_wecom_customer_follow SET follow_create_time = NULL WHERE follow_create_time <= 0;
UPDATE t_wecom_customer_follow SET follow_create_time = FROM_UNIXTIME(follow_create_time) WHERE follow_create_time IS NOT NULL;
ALTER TABLE t_wecom_customer_follow MODIFY COLUMN follow_create_time DATETIME DEFAULT NULL COMMENT '添加时间';
