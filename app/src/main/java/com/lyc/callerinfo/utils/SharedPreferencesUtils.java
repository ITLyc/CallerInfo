package com.lyc.callerinfo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * SharedPreferencesUtils
 */

public class SharedPreferencesUtils {
    /**
     * 保存信息到SharedPreferences
     *
     * @param context
     * @param shareName
     * @param map
     */
    public static void saveSharedPreferences(Context context, String shareName, Map<String, String> map) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<Map.Entry<String, String>> set = map.entrySet();
        Iterator<Map.Entry<String, String>> iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            editor.putString(entry.getKey(), entry.getValue());
        }
        editor.apply();
    }

    public static void saveSharedPreferences(Context context, String shareName, String key, boolean b) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, b);
        editor.apply();
    }

    public static void saveInt(Context context, String shareName, String key, int b) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, b);
        editor.apply();
    }

    /**
     * 获取SharedPreferences保存的值
     *
     * @param context
     * @param shareName
     * @param key
     * @return
     */
    public static String getSharedPreferences(Context context, String shareName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }

    public static boolean getboobean(Context context, String shareName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, false);
    }

    public static int getInt(Context context, String shareName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, 0);
    }

    /**
     * 删除SharedPreferences文件
     *
     * @param context
     * @param shareName
     */
    public static void delSharedPreferences(Context context, String shareName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear().apply();
    }

    /**
     * 删除SharedPreferences保存的值
     *
     * @param context
     * @param shareName
     * @param key
     */
    public static void delSharedPreferences(Context context, String shareName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.remove(key).apply();
    }

    /**
     * 判断是否包含某个值
     *
     * @param context
     * @param shareName
     * @param key
     * @return
     */
    public static boolean hasValue(Context context, String shareName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
        return sharedPreferences.contains(key);
    }
}
