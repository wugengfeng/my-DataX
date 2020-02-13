# DataX PostgresqlWriterArchive


---


## 1 快速介绍

PostgresqlWriterArchive插件实现了写入数据到 PostgreSQL主库目的表的功能。在底层实现上，PostgresqlWriterArchive通过JDBC连接远程 PostgreSQL 数据库，并执行相应的 insert into ... sql 语句将数据写入 PostgreSQL，内部会分批次提交入库。

PostgresqlWriterArchive面向ETL开发工程师，他们使用PostgresqlWriterArchive从数仓导入数据到PostgreSQL。同时 PostgresqlWriterArchive亦可以作为数据迁移工具为DBA等用户提供服务。


## 2 实现原理

PostgresqlWriterArchive通过 DataX 框架获取 Reader 生成的协议数据，根据你配置生成相应的SQL插入语句


* `insert into...`(当主键/唯一性索引冲突时会写不进去冲突的行)

<br />

    注意：
    1. 目的表所在数据库必须是主库才能写入数据；整个任务至少需具备 insert into...的权限，是否需要其他权限，取决于你任务配置中在 preSql 和 postSql 中指定的语句。
    2. PostgresqlWriterArchive和MysqlWriter不同，不支持配置writeMode参数。


## 3 功能说明

### 3.1 配置样例

* 这里使用一份从MySql产生到 PostgresqlWriterArchive导入的数据。

```json
{
    "job": {
        "setting": {
            "speed": {
                "channel": 3
            }
        },
        "content": [
            {
             "reader": {
                 "name": "mysqlreader",
                 "parameter": {
                     "username": "xx",
                     "password": "xx",
                     "splitPk": "id",
                     "connection": [
                         {
                             "querySql": [
                                 "SELECT id, crawl_id, create_date FROM test"
                             ],
                             "jdbcUrl": [
                                 "jdbc:mysql://127.0.0.1:3306/datax?useUnicode=true&characterEncoding=utf8&useSSL=true&allowMultiQueries=true&autoReconnect=true"
                             ]
                         }
                     ]
                 },
                "writer": {
                    "name": "postgresqlwriterarchive",
                    "parameter": {
                        "username": "xx",
                        "password": "xx",
                        "column": [
                            "id",
                            "name",
                            "create_date"
                        ],
                        "preSql": [],
                        "connection": [
                            {
                                "jdbcUrl": "jdbc:postgresql://127.0.0.1:3002/datax",
                                "table": [
                                    "test"
                                ]
                            }
                        ],
                        "delete": {
                            "type": "MySql",
                            "username": "xx",
                            "password": "xx",
                            "jdbcUrl": "jdbc:mysql://127.0.0.1:3306/datax?useUnicode=true&characterEncoding=utf8&useSSL=true&allowMultiQueries=true&autoReconnect=true",
                            "table": "listing_snapshot",
                            "column": "id",
                            "columnIndex": "0"
                        },
                        "log": {
                            "type": "PostgreSQL",
                            "username": "xx",
                            "password": "xx",
                            "jdbcUrl": "jdbc:postgresql://127.0.0.1:5432/datax",
                            "table": "job_log",
                            "jobName": "pg_test"
                        }
                    }
                }
            }
        ]
    }
}

```


### 3.2 参数说明 (writer部分)

* **jdbcUrl**

    * 描述：目的数据库的 JDBC 连接信息 ,jdbcUrl必须包含在connection配置单元中。

      注意：1、在一个数据库上只能配置一个值。
      2、jdbcUrl按照PostgreSQL官方规范，并可以填写连接附加参数信息。具体请参看PostgreSQL官方文档或者咨询对应 DBA。


  * 必选：是 <br />

  * 默认值：无 <br />

* **username**

  * 描述：目的数据库的用户名 <br />

  * 必选：是 <br />

  * 默认值：无 <br />

* **password**

  * 描述：目的数据库的密码 <br />

  * 必选：是 <br />

  * 默认值：无 <br />

* **table**

  * 描述：目的表的表名称。支持写入一个或者多个表。当配置为多张表时，必须确保所有表结构保持一致。

               注意：table 和 jdbcUrl 必须包含在 connection 配置单元中

  * 必选：是 <br />

  * 默认值：无 <br />

* **column**

  * 描述：目的表需要写入数据的字段,字段之间用英文逗号分隔。例如: "column": ["id","name","age"]。如果要依次写入全部列，使用*表示, 例如: "column": ["*"]

               注意：1、我们强烈不推荐你这样配置，因为当你目的表字段个数、类型等有改动时，你的任务可能运行不正确或者失败
                    2、此处 column 不能配置任何常量值

  * 必选：是 <br />

  * 默认值：否 <br />

* **preSql**

  * 描述：写入数据到目的表前，会先执行这里的标准语句。如果 Sql 中有你需要操作到的表名称，请使用 `@table` 表示，这样在实际执行 Sql 语句时，会对变量按照实际表名称进行替换。比如你的任务是要写入到目的端的100个同构分表(表名称为:datax_00,datax01, ... datax_98,datax_99)，并且你希望导入数据前，先对表中数据进行删除操作，那么你可以这样配置：`"preSql":["delete from @table"]`，效果是：在执行到每个表写入数据前，会先执行对应的 delete from 对应表名称 <br />

  * 必选：否 <br />

  * 默认值：无 <br />

* **postSql**

  * 描述：写入数据到目的表后，会执行这里的标准语句。（原理同 preSql ） <br />

  * 必选：否 <br />

  * 默认值：无 <br />

* **batchSize**

	* 描述：一次性批量提交的记录数大小，该值可以极大减少DataX与PostgreSql的网络交互次数，并提升整体吞吐量。但是该值设置过大可能会造成DataX运行进程OOM情况。<br />

	* 必选：否 <br />

	* 默认值：1024 <br />
	
### delete模块
```
    * 描述：配置后数据迁移完成会删除源库数据，不配则不删除源数据
    
    * 必选：按需
    
    * 默认值：无
    
type

    * 描述：数据库类型，目前有MySql、PostgreSQL配置，扩展插件后可以继续添加数据库类型

    * 必选：否

    * 默认值：无 
    
username

    * 描述：源数据库用户名
    
    * 必选：配置delete模块则必填
    
    * 默认值：无  
    
password

    * 描述：源数据库密码
    
    * 必选：配置delete模块则必填
    
    * 默认值：无    
    
jdbcUrl

    * 描述：源数据库连接
    
    * 必选：配置delete模块则必填
    
    * 默认值：无   
    
table

    * 描述：源数据表，归档完成删除的源数据表名，一定要和reader模块的表名一致
    
    * 必选：配置delete模块则必填
    
    * 默认值：无                
    
column
     
    * 描述：归档完成后删除源数据的条件列名，必须在reader模块的querySql中存在，推荐使用id或其他唯一值，删除源数据底层实现 delete from [配置table] where [配置colunm] in [data.get[配置columnIndex]] 
    
    * 必选：配置delete模块则必填
    
    * 默认值：无    
    
columnIndex
    
    * 描述：column在querySql中查询字段的下表位置，开始值为0
    
    * 必选：配置delete模块则必填
    
    * 默认值：无            
```

### log模块
```
    * 描述：数据完成后归档记录日志的数据库，配置此模块需要在日志库中新建log表，建表语句参考根目录log.sql
    
    * 必选：按需
    
    * 默认值：无
    
type

    * 描述：数据库类型，目前有MySql、PostgreSQL配置，扩展插件后可以继续添加数据库类型

    * 必选：配置log模块则必填

    * 默认值：无 
    
username

    * 描述：日志据库用户名
    
    * 必选：配置log模块则必填
    
    * 默认值：无  
    
password

    * 描述：日志据库密码
    
    * 必选：配置log模块则必填
    
    * 默认值：无    
    
jdbcUrl

    * 描述：日志数据库连接
    
    * 必选：配置log模块则必填
    
    * 默认值：无   
    
table

    * 描述：日志表名，归档完成后记录日志的表名称
    
    * 必选：配置log模块则必填
    
    * 默认值：log.sql里默认建表名称为 job_log，可以修改
    
jobName

    * 描述：这次归档的任务名称，一般取 归档表名_archive，方便在log表中查看
    
    * 必选：配置log模块则必填
    
    * 默认值：无                   
```


### 3.3 类型转换

目前 PostgresqlWriterArchive支持大部分 PostgreSQL类型，但也存在部分没有支持的情况，请注意检查你的类型。

下面列出 PostgresqlWriterArchive针对 PostgreSQL类型转换列表:

| DataX 内部类型| PostgreSQL 数据类型    |
| -------- | -----  |
| Long     |bigint, bigserial, integer, smallint, serial |
| Double   |double precision, money, numeric, real |
| String   |varchar, char, text, bit|
| Date     |date, time, timestamp |
| Boolean  |bool|
| Bytes    |bytea|

