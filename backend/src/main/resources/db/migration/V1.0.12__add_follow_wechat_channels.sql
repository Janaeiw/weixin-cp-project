ALTER TABLE `t_wecom_customer_follow`
    ADD COLUMN `wechat_channels_nickname` VARCHAR(100) DEFAULT NULL COMMENT '视频号名称' AFTER `operator_userid`,
    ADD COLUMN `wechat_channels_source` TINYINT DEFAULT NULL COMMENT '视频号添加场景：0=未知 1=主页 2=直播间 3=留资服务' AFTER `wechat_channels_nickname`;
