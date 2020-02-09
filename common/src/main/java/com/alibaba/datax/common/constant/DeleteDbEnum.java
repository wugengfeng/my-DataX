package com.alibaba.datax.common.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: wgf
 * @create: 2020-01-21 11:18
 * @description: DELETE据库配置Key 枚举
 **/
public enum DeleteDbEnum {
    TYPE(Prefix.PREFIX + "type"),
    USERNAME(Prefix.PREFIX + "username"),
    PASSWORD(Prefix.PREFIX + "password"),
    JDBC_URL(Prefix.PREFIX + "jdbcUrl"),
    TABLE(Prefix.PREFIX + "table"),
    COLUMN(Prefix.PREFIX + "column"),
    COLUMN_INDEX(Prefix.PREFIX + "columnIndex");


    DeleteDbEnum(String key) {
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
        return Arrays.stream(DeleteDbEnum.values())
                .collect(Collectors.toMap(DeleteDbEnum::name, DeleteDbEnum::getKey));
    }

    interface Prefix {
        // path前缀
        String PREFIX = "delete.";
    }
}
