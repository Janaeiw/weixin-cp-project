-- 客群表新增字段
ALTER TABLE t_wecom_group_chat
    ADD COLUMN admin_list VARCHAR(1024) DEFAULT NULL COMMENT '群管理员userid列表(JSON数组)' AFTER status,
    ADD COLUMN member_version VARCHAR(64) DEFAULT NULL COMMENT '群成员版本号' AFTER admin_list;

-- 客群成员表新增字段
ALTER TABLE t_wecom_group_chat_member
    ADD COLUMN invitor VARCHAR(128) DEFAULT NULL COMMENT '邀请者userid' AFTER name,
    ADD COLUMN union_id VARCHAR(128) DEFAULT NULL COMMENT '微信开放平台unionid' AFTER invitor;
