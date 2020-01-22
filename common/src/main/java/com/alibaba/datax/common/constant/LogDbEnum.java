package com.alibaba.datax.common.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: wgf
 * @create: 2020-01-21 11:17
 * @description: 日志数据库配置Key 枚举
 **/
public enum LogDbEnum {

    TYPE(Prefix.PREFIX + "type"),
    USERNAME(Prefix.PREFIX + "username"),
    PASSWORD(Prefix.PREFIX + "password"),
    JDBC_URL(Prefix.PREFIX + "jdbcUrl"),
    TABLE(Prefix.PREFIX + "table"),
    JOB_NAME(SourceDbEnum.Prefix.PREFIX + "jobName");

    LogDbEnum(String key) {
        this.key = key;
    }

    private String key;

    public String getKey() {
        return key;
    }

    /**
     * 将当前枚举信息转化为Map
     *
     * @return
     */
    public static Map<String, String> toMap() {
        return Arrays.stream(SourceDbEnum.values())
                .collect(Collectors.toMap(SourceDbEnum::name, SourceDbEnum::getKey));
    }

    interface Prefix {
        // path前缀
        String PREFIX = "delete.";
    }
}
