package com.alibaba.datax.common.util;

import com.alibaba.datax.common.exception.ParseConfigException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author: wgf
 * @create: 2020-01-21 11:08
 * @description: 解析自定义组件工具类
 **/
public class ParseConfigUtil {
    private ParseConfigUtil() {
    }

    public static Map<String, String> parse(Configuration configuration, Map<String, String> enumMap) {
        if (MapUtils.isEmpty(enumMap) || Objects.isNull(configuration)) {
            return null;
        }

        Map<String, String> configMap = new HashMap<>();
        enumMap.forEach((k, v) -> {
            String value = configuration.getString(v);
            if (StringUtils.isNotEmpty(value)) {
                configMap.put(k, value);
            } else {
                throw new ParseConfigException(String.format("请检查配置文件 %s 没有配置", v));
            }
        });

        return configMap;
    }
}
