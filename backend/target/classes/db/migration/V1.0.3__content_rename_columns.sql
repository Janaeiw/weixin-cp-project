-- 内容库字段改名：summary → description，cover_image → image
ALTER TABLE `t_content` CHANGE COLUMN `summary` `description` VARCHAR(500) DEFAULT NULL COMMENT '素材描述';
ALTER TABLE `t_content` CHANGE COLUMN `cover_image` `image` VARCHAR(500) DEFAULT NULL COMMENT '素材图片URL';
