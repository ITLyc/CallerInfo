package com.lyc.callerinfo.permission;

import android.content.Context;

public interface Permission {

    boolean canDrawOverlays();

    void requestDrawOverlays(Context context, int requestCode);

    int checkPermission(String permission);

    void requestPermissions(Context context, String[] permission, int requestCode);

    boolean canReadPhoneState();

    boolean canReadContact();
}
