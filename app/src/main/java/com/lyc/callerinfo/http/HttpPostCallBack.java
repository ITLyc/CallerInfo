package com.lyc.callerinfo.http;

/**
 * Created by Administrator on 2017/2/27.
 */

public  interface HttpPostCallBack {

    /**
     * Post成功的回调
     */
    public void onSuccess(Object result);

    /**
     * Post失败的回调
     */
    public void onFailure(int code, String msg);

}
