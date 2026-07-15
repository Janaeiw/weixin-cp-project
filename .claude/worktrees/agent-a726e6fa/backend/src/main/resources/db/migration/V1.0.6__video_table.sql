CREATE TABLE IF NOT EXISTS `t_video` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(255) NOT NULL COMMENT '文件名',
    `type`        VARCHAR(64)  DEFAULT NULL COMMENT 'MIME类型',
    `data`        LONGBLOB     NOT NULL COMMENT '视频二进制数据',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频存储表';
