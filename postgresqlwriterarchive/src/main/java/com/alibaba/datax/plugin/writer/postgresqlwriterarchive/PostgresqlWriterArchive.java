package com.alibaba.datax.plugin.writer.postgresqlwriterarchive;

import com.alibaba.datax.common.constant.DeleteDbEnum;
import com.alibaba.datax.common.constant.LogDbEnum;
import com.alibaba.datax.common.exception.CheckDBException;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.common.util.ParseConfigUtil;
import com.alibaba.datax.plugin.rdbms.util.DBUtil;
import com.alibaba.datax.plugin.rdbms.util.DBUtilErrorCode;
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

public class PostgresqlWriterArchive extends Writer {
	private static final DataBaseType DATABASE_TYPE = DataBaseType.PostgreSQL;

	public static class Job extends Writer.Job {
		private Configuration originalConfig = null;
		private CommonRdbmsWriter.Job commonRdbmsWriterMaster;
		private Map<String, String> deleteDbConfig;
		private Map<String, String> logDbConfig;

		@Override
		public void init() {
			this.originalConfig = super.getPluginJobConf();

			// warn：not like mysql, PostgreSQL only support insert mode, don't use
			String writeMode = this.originalConfig.getString(Key.WRITE_MODE);
			if (null != writeMode) {
				throw DataXException.asDataXException(DBUtilErrorCode.CONF_ERROR,
					String.format("写入模式(writeMode)配置有误. 因为PostgreSQL不支持配置参数项 writeMode: %s, PostgreSQL仅使用insert sql 插入数据. 请检查您的配置并作出修改.", writeMode));
			}

			this.commonRdbmsWriterMaster = new CommonRdbmsWriter.Job(DATABASE_TYPE);
			this.commonRdbmsWriterMaster.init(this.originalConfig);
			this.deleteDbConfig = ParseConfigUtil.parse(this.originalConfig, DeleteDbEnum.toMap());
			this.logDbConfig = ParseConfigUtil.parse(this.originalConfig, LogDbEnum.toMap());
		}

		@Override
		public void prepare() {
			this.commonRdbmsWriterMaster.prepare(this.originalConfig);

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
			return this.commonRdbmsWriterMaster.split(this.originalConfig, mandatoryNumber);
		}

		@Override
		public void post() {
			this.commonRdbmsWriterMaster.post(this.originalConfig);
		}

		@Override
		public void destroy() {
			this.commonRdbmsWriterMaster.destroy(this.originalConfig);
		}

	}

	public static class Task extends Writer.Task {
		private static final Logger LOG = LoggerFactory.getLogger(Task.class);

		private Configuration writerSliceConfig;
		private CommonRdbmsWriter.Task commonRdbmsWriterSlave;
		private Map<String, String> deleteDbConfig;
		private Map<String, String> logDbConfig;
		private Date startDate;
		private AtomicInteger countNum = new AtomicInteger(0);
		private AtomicInteger successNum = new AtomicInteger(0);

		@Override
		public void init() {
			startDate = new Date();
			this.writerSliceConfig = super.getPluginJobConf();
			this.commonRdbmsWriterSlave = new CommonRdbmsWriter.Task(DATABASE_TYPE){
				@Override
				public String calcValueHolder(String columnType){
					if("serial".equalsIgnoreCase(columnType)){
						return "?::int";
					}else if("bit".equalsIgnoreCase(columnType)){
						return "?::bit varying";
					}
					return "?::" + columnType;
				}
			};
			this.commonRdbmsWriterSlave.init(this.writerSliceConfig);
			this.deleteDbConfig = ParseConfigUtil.parse(this.writerSliceConfig, DeleteDbEnum.toMap());
			this.logDbConfig = ParseConfigUtil.parse(this.writerSliceConfig, LogDbEnum.toMap());
		}

		@Override
		public void prepare() {
			this.commonRdbmsWriterSlave.prepare(this.writerSliceConfig);
		}

		public void startWrite(RecordReceiver recordReceiver) {

			if (MapUtils.isNotEmpty(deleteDbConfig)) {
				LOG.info("删除源数据信息 url：[{}]", deleteDbConfig.get(DeleteDbEnum.JDBC_URL.name()));
				LOG.info("删除源数据表：[{}]", deleteDbConfig.get(DeleteDbEnum.TABLE.name()));
			}

			this.commonRdbmsWriterSlave.startWrite(recordReceiver, this.writerSliceConfig,
					super.getTaskPluginCollector(), deleteDbConfig, countNum, successNum);

			// 记录日志
			this.commonRdbmsWriterSlave.addLog(logDbConfig, deleteDbConfig, countNum, successNum, startDate);
		}

		@Override
		public void post() {
			this.commonRdbmsWriterSlave.post(this.writerSliceConfig);
		}

		@Override
		public void destroy() {
			this.commonRdbmsWriterSlave.destroy(this.writerSliceConfig);
		}

	}

}
