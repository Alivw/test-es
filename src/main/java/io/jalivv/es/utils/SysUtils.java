package io.jalivv.es.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @description: 系统工具类
 * @author: Jalivv
 * @create: 2022-11-10 13:14
 **/
public class SysUtils {
    public static final ObjectMapper OBJECTMAPPER = new ObjectMapper();

    /**
     * 获取系统环境变量
     * @param key UpperCase if not will be converted to UpperCase
     * @param defaultValue Return intact
     * @return
     */
    public static String getSystemEnv(String key, String defaultValue) {
        key = key.toUpperCase();
        String value = System.getenv(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
