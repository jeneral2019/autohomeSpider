create database if not exists autohome_spider;

drop table if exists `autohome_spider`.`autohome_spider`;
CREATE TABLE `autohome_spider`.`autohome_spider` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `autohome_id` int COMMENT '汽车之家id',
  `level` smallint DEFAULT NULL COMMENT '层级',
  `parent_id` int DEFAULT NULL COMMENT '父级ID',
  `first_letter` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '首字母',
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '品牌',
  `series` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '系列',
  `year` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  `logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '车型数据logo',
  `car_manufacturer` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '厂商',
  `car_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '车辆类型（C-小轿车，T-卡车，CT-轿车或卡车）',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '车型详情路径',
  `engine` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '发动机',
  `precursor` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '前置四驱',
  `gear` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '离合档位',
  `spec_id` int DEFAULT NULL COMMENT 'specId',
  `status`int DEFAULT NULL COMMENT '爬虫状态 0表示未爬取，1表示已爬取',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_first_char` (`first_letter`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

insert into `autohome_spider`.`autohome_spider` value (1,1,null,'A','brand','series','year','name','logo','car_manufacturer','car_type','url','engine','precursor','gear',0,0);