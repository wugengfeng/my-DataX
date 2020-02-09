package com.alibaba.datax.plugin.writer.mysqlwriterarchive;

import com.alibaba.datax.common.constant.DeleteDbEnum;
import com.alibaba.datax.common.constant.LogDbEnum;
import com.alibaba.datax.common.exception.CheckDBException;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.common.util.ParseConfigUtil;
import com.alibaba.datax.plugin.rdbms.util.DBUtil;
import com.alibaba.datax.plugin.rdbms.util.DataBaseType;
import com.alibaba.datax.plugin.rdbms.writer.CommonRdbmsWriter;
import com.alibaba.datax.plugin.rdbms.writer.Key;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: wgf
 * @create: 2020-01-21 10:36
 * @description: DataX Mysql数据归档 核心插件
 **/
public class MysqlWriterArchive {
    private static final DataBaseType DATABASE_TYPE = DataBaseType.MySql;

    public static class Job extends Writer.Job {
        private Configuration originalConfig = null;
        private CommonRdbmsWriter.Job commonRdbmsWriterJob;
        private Map<String, String> deleteDbConfig;
        private Map<String, String> logDbConfig;

        @Override
        public void preCheck() {
            this.init();
            this.commonRdbmsWriterJob.writerPreCheck(this.originalConfig, DATABASE_TYPE);
        }

        @Override
        public void init() {
            this.originalConfig = super.getPluginJobConf();
            this.commonRdbmsWriterJob = new CommonRdbmsWriter.Job(DATABASE_TYPE);
            this.commonRdbmsWriterJob.init(this.originalConfig);
            this.deleteDbConfig = ParseConfigUtil.parse(this.originalConfig, DeleteDbEnum.toMap());
            this.logDbConfig = ParseConfigUtil.parse(this.originalConfig, LogDbEnum.toMap());
        }

        // 一般来说，是需要推迟到 task 中进行pre 的执行（单表情况例外）
        @Override
        public void prepare() {
            //实跑先不支持 权限 检验
            //this.commonRdbmsWriterJob.privilegeValid(this.originalConfig, DATABASE_TYPE);
            this.commonRdbmsWriterJob.prepare(this.originalConfig);

            // 源数据库删除权限校验
            if (!DBUtil.checkDeletePrivilege(deleteDbConfig)) {
                throw new CheckDBException(String.format("请检查delete模块配置，当前配置没有删除权限"));
            }

            // 日志库插入权限校验
            if (!DBUtil.checkInsertPrivilege(logDbConfig)) {
                throw new CheckDBException(String.format("请检查log模块配置，当前配置没有新增数据权限"));
            }
        }

        @Override
        public List<Configuration> split(int mandatoryNumber) {
            return this.commonRdbmsWriterJob.split(this.originalConfig, mandatoryNumber);
        }

        // 一般来说，是需要推迟到 task 中进行post 的执行（单表情况例外）
        @Override
        public void post() {
            this.commonRdbmsWriterJob.post(this.originalConfig);
        }

        @Override
        public void destroy() {
            this.commonRdbmsWriterJob.destroy(this.originalConfig);
        }

    }

    public static class Task extends Writer.Task {
        private static final Logger LOG = LoggerFactory.getLogger(Task.class);

        private Configuration writerSliceConfig;
        private CommonRdbmsWriter.Task commonRdbmsWriterTask;
        private Map<String, String> deleteDbConfig;
        private Map<String, String> logDbConfig;
        private Date startDate;
        private AtomicInteger countNum = new AtomicInteger(0);
        private AtomicInteger successNum = new AtomicInteger(0);

        @Override
        public void init() {
            startDate = new Date();
            this.writerSliceConfig = super.getPluginJobConf();
            this.commonRdbmsWriterTask = new CommonRdbmsWriter.Task(DATABASE_TYPE);
            this.commonRdbmsWriterTask.init(this.writerSliceConfig);
            this.deleteDbConfig = ParseConfigUtil.parse(this.writerSliceConfig, DeleteDbEnum.toMap());
            this.logDbConfig = ParseConfigUtil.parse(this.writerSliceConfig, LogDbEnum.toMap());
        }

        @Override
        public void prepare() {
            this.commonRdbmsWriterTask.prepare(this.writerSliceConfig);
        }

        //TODO 改用连接池，确保每次获取的连接都是可用的（注意：连接可能需要每次都初始化其 session）
        public void startWrite(RecordReceiver recordReceiver) {

            if (MapUtils.isNotEmpty(deleteDbConfig)) {
                LOG.info("删除源数据信息 url：[{}]", deleteDbConfig.get(DeleteDbEnum.JDBC_URL.name()));
                LOG.info("删除源数据表：[{}]", deleteDbConfig.get(DeleteDbEnum.TABLE.name()));
            }

            this.commonRdbmsWriterTask.startWrite(recordReceiver, this.writerSliceConfig,
                    super.getTaskPluginCollector(), deleteDbConfig, countNum, successNum);

            // 记录日志
            this.commonRdbmsWriterTask.addLog(logDbConfig, deleteDbConfig, countNum, successNum, startDate);
        }

        @Override
        public void post() {
            this.commonRdbmsWriterTask.post(this.writerSliceConfig);
        }

        @Override
        public void destroy() {
            this.commonRdbmsWriterTask.destroy(this.writerSliceConfig);
        }

        @Override
        public boolean supportFailOver() {
            String writeMode = writerSliceConfig.getString(Key.WRITE_MODE);
            return "replace".equalsIgnoreCase(writeMode);
        }

    }
}
