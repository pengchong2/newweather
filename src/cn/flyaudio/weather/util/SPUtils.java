package cn.flyaudio.weather.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.flyaudio.weather.data.WeatherWidgetApplication;


public class SPUtils {
    private static Map<String, SPUtils> sSPMap = new HashMap<>();
    private SharedPreferences sp;

    /**
     * 获取SP实例 * * @return {@link SPUtils}
     */
    public static SPUtils getInstance() {
        return getInstance("");
    }

    /**
     * 获取SP实例 * * @param spName sp名 * @return {@link SPUtils}
     */
    public static SPUtils getInstance(String spName) {
        if (isSpace(spName))
            spName = "weather";
            SPUtils sp = sSPMap.get(spName);
        if (sp == null) {
            sp = new SPUtils(spName);
            sSPMap.put(spName, sp);
        }
        return sp;
    }

    private SPUtils(String spName) {
        sp = WeatherWidgetApplication.getContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    /**
     * SP中写入String * * @param key 键 * @param value 值
     */
    public void put(String key, String value) {
        sp.edit().putString(key, value).apply();
    }

    /**
     * SP中读取String * * @param key 键 * @return 存在返回对应值，不存在返回默认值{@code null}
     */
    public String getString(String key) {
        return getString(key, "");
    }

    /**
     * SP中读取String * * @param key 键 * @param defaultValue 默认值 * @return 存在返回对应值，不存在返回默认值{@code defaultValue}
     */
    public String getString(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    /**
     * SP中写入int * * @param key 键 * @param value 值
     */
    public void put(String key, int value) {
        sp.edit().putInt(key, value).apply();
    }

    /**
     * SP中读取int * * @param key 键 * @return 存在返回对应值，不存在返回默认值-1
     */
    public int getInt(String key) {
        return getInt(key, -1);
    }

    /**
     * SP中读取int * * @param key 键 * @param defaultValue 默认值 * @return 存在返回对应值，不存在返回默认值{@code defaultValue}
     */
    public int getInt(String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    /**
     * SP中写入long * * @param key 键 * @param value 值
     */
    public void put(String key, long value) {
        sp.edit().putLong(key, value).apply();
    }

    /**
     * SP中读取long * * @param key 键 * @return 存在返回对应值，不存在返回默认值-1
     */
    public long getLong(String key) {
        return getLong(key, -1L);
    }

    /**
     * SP中读取long * * @param key 键 * @param defaultValue 默认值 * @return 存在返回对应值，不存在返回默认值{@code defaultValue}
     */
    public long getLong(String key, long defaultValue) {
        return sp.getLong(key, defaultValue);
    }

    /**
     * SP中写入float * * @param key 键 * @param value 值
     */
    public void put(String key, float value) {
        sp.edit().putFloat(key, value).apply();
    }

    /**
     * SP中读取float * * @param key 键 * @return 存在返回对应值，不存在返回默认值-1
     */
    public float getFloat(String key) {
        return getFloat(key, -1f);
    }

    /**
     * SP中读取float * * @param key 键 * @param defaultValue 默认值 * @return 存在返回对应值，不存在返回默认值{@code defaultValue}
     */
    public float getFloat(String key, float defaultValue) {
        return sp.getFloat(key, defaultValue);
    }

    /**
     * SP中写入boolean * * @param key 键 * @param value 值
     */
    public void put(String key, boolean value) {
        sp.edit().putBoolean(key, value).apply();
    }

    /**
     * SP中读取boolean * * @param key 键 * @return 存在返回对应值，不存在返回默认值{@code false}
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * SP中读取boolean * * @param key 键 * @param defaultValue 默认值 * @return 存在返回对应值，不存在返回默认值{@code defaultValue}
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    /**
     * SP中写入String集合 * * @param key 键 * @param values 值
     */
    public void put(String key, Set<String> values) {
        sp.edit().putStringSet(key, values).apply();
    }

    /**
     * SP中读取StringSet * * @param key 键 * @return 存在返回对应值，不存在返回默认值{@code null}
     */
    public Set<String> getStringSet(String key) {
        return getStringSet(key, Collections.<String>emptySet());
    }

    /**
     * SP中读取StringSet * * @param key 键 * @param defaultValue 默认值 * @return 存在返回对应值，不存在返回默认值{@code defaultValue}
     */
    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        return sp.getStringSet(key, defaultValue);
    }

    /**
     * SP中获取所有键值对 * * @return Map对象
     */
    public Map<String, ?> getAll() {
        return sp.getAll();
    }

    /**
     * SP中移除该key * * @param key 键
     */
    public void remove(String key) {
        sp.edit().remove(key).apply();
    }

    /**
     * SP中是否存在该key * * @param key 键 * @return {@code true}: 存在<br>{@code false}: 不存在
     */
    public boolean contains(String key) {
        return sp.contains(key);
    }

    /**
     * SP中清除所有数据
     */
    public void clear() {
        sp.edit().clear().apply();
    }

    private static boolean isSpace(String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}


