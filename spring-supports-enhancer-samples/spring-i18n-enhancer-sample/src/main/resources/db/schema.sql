-- ----------------------------
-- Table structure for i18n_article
-- ----------------------------
DROP TABLE IF EXISTS `i18n_article`;
CREATE TABLE `i18n_article` (
  `id` int(11) NOT NULL COMMENT '主键',
  `locale` varchar(20) NOT NULL COMMENT '语言信息',
  `title` varchar(255) DEFAULT NULL COMMENT '标题',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `a` varchar(100) DEFAULT NULL COMMENT '字段a',
  `b` varchar(100) DEFAULT NULL COMMENT '字段b',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用',
  PRIMARY KEY (`id`,`locale`)
) ;

-- ----------------------------
-- Table structure for i18n_message
-- ----------------------------
DROP TABLE IF EXISTS `i18n_message`;
CREATE TABLE `i18n_message` (
  `code` varchar(250) NOT NULL COMMENT 'mapping code',
  `locale` varchar(100) NOT NULL COMMENT 'language tag',
  `type` varchar(100) DEFAULT NULL COMMENT 'type for group',
  `message` text NOT NULL COMMENT 'message content',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modify time',
  PRIMARY KEY (`code`,`locale`)
) ;