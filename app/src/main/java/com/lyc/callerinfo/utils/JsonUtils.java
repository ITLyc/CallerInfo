package com.lyc.callerinfo.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lyc.callerinfo.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Map;

public class JsonUtils {
    /**
     * 判断list为非字符串时是否返回成功字符串
     * 1:返回成功数据：服务器返回请求成功的数据
     * 2:返回失败数据：由于未知原因返回请求失败的数据
     * 3:返回的数据不是固定格式；
     * 4:异常
     *
     * @param jsonString
     * @return
     */
    private static int getJsonResultCode(String jsonString) {
        try {
            JSONObject mJsonObject = new JSONObject(jsonString);
            if (mJsonObject.has("error_code")) {
                if (mJsonObject.getInt("error_code") == 0) {
                    return 1;
                } else {
                    return 2;
                }
            } else {
                return 3;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return 4;
        }
    }


    /**
     * 用于非基本model的json
     * 将给定的 {@code JSON} 字符串转换成指定的类型对象。
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            switch (getJsonResultCode(json)) {
                case 1:
                case 3:
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    return gson.fromJson(json, clazz);
                case 2:
                    return null;
                case 4:
                    return null;
                default:
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            Log.e("JsonException","报错实体类："+ clazz.getName());
            Log.e("JsonException","报错信息："+ ex.getMessage().toString());
            Log.e("JsonException","json数据："+ json);
//            Log.e(json + " 无法转换为 " + clazz.getName() + " 对象!", ex.getMessage().toString());
            return null;
        }
        return null;
    }



    /**
     * 将Map转化为Json
     *
     * @param map
     * @return String
     */
    public static <T> String mapToJson(Map<String, T> map) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(map);
        return jsonStr;
    }

}
