简单介绍下这个同步中间件
---

- [DataX](https://github.com/alibaba/DataX)是阿里离线数据迁移中间件，这份源码是扩展DataX的

- DataX是一个数据迁移框架，以插件形式组织数据迁移有 read, write 插件，想了解点上面传送门去Git


关于这个中间件
---

- 目前只保留 mysqlreader 的读插件，和扩展postgresqlwriter而来的postgresqlwriterarchive 归档插件

- 除了原有的数据迁移能力，扩展了数据由源库迁移到目标库同时删除源库迁移完成的数据，还能在迁移完成后记录日志，详情使用看扩展的postgresqlwriterarchive的说明文件


如何扩展插件
---

- 常用的数据库 reader、writer 操作DataX都有提供，去git下载插件加入本项目，并在datax-all pom文件的<modules>模块添加新插件

- 自定义扩展参考 [DataX 二次开发](https://blog.csdn.net/lw277232240/article/details/90903251)


打包部署
---

- 通过maven打包
```
cd  {DataX_source_code_home}
mvn -U clean package assembly:assembly -Dmaven.test.skip=true
```

- 打包成功，日志显示如下
```
[INFO] BUILD SUCCESS
[INFO] -----------------------------------------------------------------
[INFO] Total time: 08:12 min
[INFO] Finished at: 2015-12-13T16:26:48+08:00
[INFO] Final Memory: 133M/960M
[INFO] -----------------------------------------------------------------
```

- 包成功后的DataX包位于 {DataX_source_code_home}/target/datax/datax/ ，结构如下
```
$ cd  {DataX_source_code_home}
$ ls ./target/datax/datax/
bin		conf		job		lib		log		log_perf	plugin		script		tmp
```

- 启动DataX
```
$ cd {YOUR_DATAX_DIR_BIN}
$ python datax.py /{YOUR_FILE_PATH}/your.json 
```

- json 文件统一放在core项目下的script目录下 dev为测试环境，uat为正式环境

- json 配置文件参考 [Datax Quick Start](https://github.com/alibaba/DataX/blob/master/userGuid.md) 或 postgresqlwriterarchive项目的说明文件

- 正式环境部署目前是通过linux的crontab定时执行脚本命令