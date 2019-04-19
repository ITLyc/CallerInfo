package com.lyc.callerinfo.index.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ListView;

import com.lyc.callerinfo.R;
import com.lyc.callerinfo.application.MyApplication;
import com.lyc.callerinfo.base.BaseActivity;
import com.lyc.callerinfo.data.CallerRepository;
import com.lyc.callerinfo.http.HttpRequest;
import com.lyc.callerinfo.model.db.MarkedRecord;
import com.lyc.callerinfo.model.setting.Setting;
import com.lyc.callerinfo.model.setting.SettingImpl;
import com.lyc.callerinfo.permission.PermissionImpl;
import com.lyc.callerinfo.utils.Alarm;
import com.lyc.callerinfo.utils.Contact;
import com.lyc.callerinfo.utils.Window;
import com.lyc.greendao.DaoSession;
import com.lyc.greendao.MarkedRecordDao;

import java.util.ArrayList;
import java.util.List;

public class MarkActivity extends BaseActivity implements DialogInterface.OnDismissListener {
    public static final String NUMBER = "number";
    private static final String TAG = MarkActivity.class.getSimpleName();
    boolean isPaused = false;

    private static Setting mSetting;
    private static DaoSession mDaoSession;
    private static Context sContext;
    private static Alarm mAlarm;

    private AlertDialog mAlertDialog;
    private ArrayList<String> mNumberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        init(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public static void init(Context context) {
        sContext = context.getApplicationContext();
        mDaoSession = MyApplication.getDaoSession();
        mSetting = SettingImpl.getInstance();
        mAlarm = new Alarm(sContext);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
        Intent intent = getIntent();
        String number = intent.getStringExtra(NUMBER);
        if (!TextUtils.isEmpty(number)) {
            showAlertDialog(number);
        } else {
            mNumberList = mSetting.getPaddingMarks();
            if (mNumberList.size() > 0) {
                showAlertDialog(mNumberList.get(0));
            } else {
                Log.e(TAG, "number为null或空! " + number);
                finish();
            }
        }
    }

    private void showAlertDialog(final String number) {
        String title = getString(R.string.mark_number) + "(" + number + ")";
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MarkDialogStyle);
        builder.setTitle(title);
        builder.setOnDismissListener(this);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setCancelable(false);
        builder.setSingleChoiceItems(R.array.mark_type, -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                                .setEnabled(true);
                    }
                });
        builder.setNegativeButton(R.string.ignore, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                MarkedRecord markedRecord = new MarkedRecord();
//                markedRecord.setUid(mSetting.getUid());
//                markedRecord.setNumber(number);
//                markedRecord.setType(MarkedRecord.TYPE_IGNORE);
//                markedRecord.setTypeName(getString(R.string.ignore_number));
//                markedRecord.setReported(true);
//                saveMarked(markedRecord);
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView lv = ((AlertDialog) dialog).getListView();
                String type = (String) lv.getAdapter().getItem(lv.getCheckedItemPosition());
                MarkedRecord markedRecord = new MarkedRecord();
                markedRecord.setType(lv.getCheckedItemPosition());
                markedRecord.setTypeName(type);
                markedRecord.setUid(mSetting.getUid());
                markedRecord.setNumber(number);
                saveMarked(markedRecord);
                mAlarm.alarm();
            }
        });
        mAlertDialog = builder.create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            mAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        mAlertDialog.show();
        mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
        }
        finish();
    }

    private void saveMarked(MarkedRecord markedRecord) {
        mDaoSession.getMarkedRecordDao().insertOrReplace(markedRecord);
    }

    private void updateMarked(MarkedRecord markedRecord) {

    }

    @Override
    protected int getTitleId() {
        return R.string.app_name;
    }

    @Override
    protected int getlayoutId() {
        return R.layout.activity_mark;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mNumberList != null && mNumberList.size() > 0) {
            mSetting.removePaddingMark(mNumberList.get(0));
            mNumberList.remove(0);
            if (mNumberList.size() > 0) {
                showAlertDialog(mNumberList.get(0));
                return;
            }
        }

        if (!isPaused && (mNumberList == null || mNumberList.size() == 0)) {
            finish();
        }
    }
}
