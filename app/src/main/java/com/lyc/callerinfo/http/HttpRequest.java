package com.lyc.callerinfo.http;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.lyc.callerinfo.base.UrlConstants;
import com.lyc.callerinfo.utils.JsonUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private Context context;

    public static HttpRequest get() {
        return SingletonHelper.INSTANCE;
    }

    public HttpRequest() {
    }

    public void init(Context context) {
        context = context.getApplicationContext();
    }


    /**
     * 执行普通的get请求
     *
     * @param url
     * @param params
     * @param clazz
     * @param mCallBack
     * @param <T>
     */

    public <T> void doGet(final String url, String params, final Class<T> clazz, final HttpStringCallBack mCallBack) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("id", "51");
            map.put("key", UrlConstants.key);
            map.put("tel", params);
            OkGo.<String>get(url).tag(this).params(map).execute(new StringCallback() {
                @Override
                public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                    String body = response.body();
                    Log.e(url, body);
                    Object obj = JsonUtils.fromJson(body, clazz);
                    if (obj != null) {
                        mCallBack.onSuccess(obj);
                    }

                }

                @Override
                public void onError(com.lzy.okgo.model.Response<String> response) {
                    super.onError(response);
                    mCallBack.onFailure(1, "接口异常" + response.code());
                }
            });
        } catch (Exception e) {
            mCallBack.onFailure(-101, "接口异常");
            e.printStackTrace();
        }
    }

    private final static class SingletonHelper {
        private final static HttpRequest INSTANCE = new HttpRequest();
    }
}
