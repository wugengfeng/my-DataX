-- mysql
CREATE TABLE `job_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `job_name` varchar(50) DEFAULT NULL COMMENT '归档任务名称',
  `start_date` datetime DEFAULT NULL COMMENT '开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '结束时间',
  `period` date DEFAULT NULL COMMENT '归档日期',
  `count_num` int(11) DEFAULT NULL COMMENT '归档数据总条数',
  `success_num` int(11) DEFAULT NULL COMMENT '归档数据成功条数',
  `error_num` int(11) DEFAULT NULL COMMENT '归档数据失败条数',
  `reader_tab` varchar(50) DEFAULT NULL COMMENT '源数据表',
  `reader_url` varchar(200) DEFAULT NULL COMMENT '源数据库配置信息',
  `writer_tab` varchar(50) DEFAULT NULL COMMENT '迁移数据表',
  `writer_url` varchar(200) DEFAULT NULL COMMENT '迁移数据库配置信息',
  `ip_address` varchar(20) DEFAULT NULL COMMENT '服务的ip地址',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- PostgreSql
CREATE TABLE job_log (
  id int NOT NULL PRIMARY KEY,
  job_name varchar(50),
  start_date timestamp DEFAULT NULL,
  end_date timestamp DEFAULT NULL,
  period date DEFAULT NULL,
  count_num int DEFAULT NULL,
  success_num int DEFAULT NULL,
  error_num int DEFAULT NULL,
  reader_tab varchar(50) ,
  reader_url varchar(200) ,
  writer_tab varchar(50) ,
  writer_url varchar(200) ,
  ip_address varchar(20)
);

-- 创建序列用于id生成
CREATE SEQUENCE job_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 使用序列
alter table job_log alter column id set default nextval('job_log_id_seq');