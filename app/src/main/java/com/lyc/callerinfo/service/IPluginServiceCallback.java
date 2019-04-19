package com.lyc.callerinfo.service;

public interface IPluginServiceCallback {
    void onCallPermissionResult(boolean success);
    void onCallLogPermissionResult(boolean success);
    void onStoragePermissionResult(boolean success);
}
