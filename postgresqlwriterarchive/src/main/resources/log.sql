-- 建表语句
CREATE TABLE job_log (
  id int NOT NULL PRIMARY KEY,
  job_name varchar(50) DEFAULT NULL,
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