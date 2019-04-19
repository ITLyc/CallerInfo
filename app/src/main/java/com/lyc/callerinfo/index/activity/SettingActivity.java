package com.lyc.callerinfo.index.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jenzz.materialpreference.SwitchPreference;
import com.lyc.callerinfo.R;
import com.lyc.callerinfo.base.BaseActivity;
import com.lyc.callerinfo.index.MainActivity;
import com.lyc.callerinfo.service.FloatWindow;
import com.lyc.callerinfo.service.IPluginServiceCallback;
import com.lyc.callerinfo.service.PhoneService;
import com.lyc.callerinfo.utils.Utils;
import com.lyc.callerinfo.utils.Window;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import app.minimize.com.seek_bar_compat.SeekBarCompat;

import static com.lyc.callerinfo.utils.Utils.mask;

public class SettingActivity extends AppCompatActivity {
    private final static String TAG = SettingActivity.class.getSimpleName();
    private final static String PLUGIN_SETTING = "com.lyc.callerinfo.action.PLUGIN_SETTING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.action_settings);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, SettingsFragment.newInstance(getIntent()))
                    .commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
        private final static int SUMMARY_FLAG_NORMAL = 0x00000001;
        private final static int SUMMARY_FLAG_MASK = 0x00000002;
        private final static int SUMMARY_FLAG_NULL = 0x00000004;
        private Intent mIntent;
        private Point mPoint;
        private SharedPreferences sharedPrefs;
        private HashMap<String, Integer> keyMap = new HashMap<>();
        private HashMap<String, Preference> prefMap = new HashMap<>();
        private boolean isCheckStorageExport = false;
        private boolean isCheckRingOnce = false;
        private PhoneService mService;

        Window mWindow;

        public static SettingsFragment newInstance(Intent intent) {
            SettingsFragment fragment = new SettingsFragment();
            fragment.setStartIntent(intent);
            return fragment;
        }

        private void setStartIntent(Intent intent) {
            mIntent = intent;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);
            mWindow = new Window();
            sharedPrefs = getPreferenceManager().getSharedPreferences();

            WindowManager windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            mPoint = new Point();
            display.getSize(mPoint);

            bindPreference(R.string.window_text_size_key);
            bindPreference(R.string.window_height_key);
            bindPreferenceList(R.string.window_text_alignment_key, R.array.align_type, 1);
            bindPreference(R.string.window_transparent_key);
            bindPreference(R.string.window_text_padding_key);
            bindPreference(R.string.ignore_known_contact_key);
            bindPreference(R.string.display_on_outgoing_key);
            bindPreference(R.string.ignore_regex_key, false);
            bindPreference(R.string.auto_report_key);
            bindPreference(R.string.enable_marking_key);
            bindPreference(R.string.not_mark_contact_key);
            bindPreference(R.string.outgoing_window_position_key);
            bindPreference(R.string.auto_hangup_key);
            bindPreference(R.string.add_call_log_key);
            bindPreference(R.string.ring_once_and_auto_hangup_key);

            bindPreference(R.string.hangup_keyword_key, R.string.hangup_keyword_summary);
            bindPreference(R.string.hangup_geo_keyword_key,
                    R.string.hangup_geo_keyword_summary);
            bindPreference(R.string.hangup_number_keyword_key,
                    R.string.hangup_number_keyword_summary);
            Intent intent = new Intent(getActivity(), PhoneService.class);
            getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
        }

        ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected: " + name.toString());
                mService = ((PhoneService.MyBindel) service).getservice();
                mService.registerCallback(new IPluginServiceCallback() {
                    @Override
                    public void onCallPermissionResult(final boolean success) {
                        Log.d(TAG, "onCallPermissionResult: " + success);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setChecked(R.string.auto_hangup_key, success);
                            }
                        });
                    }

                    @Override
                    public void onCallLogPermissionResult(final boolean success) {
                        Log.d(TAG, "onCallLogPermissionResult: " + success);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isCheckRingOnce) {
                                    setChecked(R.string.ring_once_and_auto_hangup_key, success);
                                } else {
                                    setChecked(R.string.add_call_log_key, success);
                                }
                            }
                        });
                    }

                    @Override
                    public void onStoragePermissionResult(boolean success) {

                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected: " + name.toString());
                mService = null;
            }
        };


        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            if (mIntent != null && mIntent.getAction() != null) {
                String action = mIntent.getAction();

                switch (action) {
                    case PLUGIN_SETTING:
                        view.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                openPreference(getString(R.string.plugin_key));
                            }
                        }, 500);
                        break;
                }
            }
        }

        private void openPreference(String key) {
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            final ListAdapter listAdapter = preferenceScreen.getRootAdapter();

            final int itemsCount = listAdapter.getCount();
            int itemNumber;
            for (itemNumber = 0; itemNumber < itemsCount; ++itemNumber) {
                if (listAdapter.getItem(itemNumber).equals(findPreference(key))) {
                    preferenceScreen.onItemClick(null, null, itemNumber, 0);
                    break;
                }
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                             Preference preference) {
            super.onPreferenceTreeClick(preferenceScreen, preference);
            if (preference instanceof PreferenceScreen) {
                setUpNestedScreen((PreferenceScreen) preference);
            }
            return false;
        }

        private void setUpNestedScreen(PreferenceScreen preference) {
            final Dialog dialog = preference.getDialog();

            AppBarLayout appBarLayout;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                    || Build.VERSION.RELEASE.equals("7.0") || Build.VERSION.RELEASE.equals("N")) {
                ListView listView = dialog.findViewById(android.R.id.list);
                ViewGroup root = (ViewGroup) listView.getParent();

                appBarLayout = (AppBarLayout) LayoutInflater.from(getActivity()).inflate(
                        R.layout.settings_toolbar, root, false);

                int height;
                TypedValue tv = new TypedValue();
                if (getActivity().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                    height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                } else {
                    height = appBarLayout.getHeight();
                }
                listView.setPadding(0, height, 0, 0);
                root.addView(appBarLayout, 0);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                LinearLayout root = (LinearLayout) dialog.findViewById(android.R.id.list).getParent();
                appBarLayout = (AppBarLayout) LayoutInflater.from(getActivity()).inflate(R.layout.settings_toolbar, root, false);
                root.addView(appBarLayout, 0);
            } else {
                ViewGroup root = dialog.findViewById(android.R.id.content);
                ListView content = (ListView) root.getChildAt(0);

                root.removeAllViews();

                appBarLayout = (AppBarLayout) LayoutInflater.from(getActivity()).inflate(R.layout.settings_toolbar, root, false);

                int height;
                TypedValue tv = new TypedValue();
                if (getActivity().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                    height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                } else {
                    height = appBarLayout.getHeight();
                }

                content.setPadding((int) dpToPx(16), height, (int) dpToPx(16), 0);

                root.addView(content);
                root.addView(appBarLayout);
            }

            Toolbar toolbar = appBarLayout.findViewById(R.id.toolbar);
            toolbar.setTitle(preference.getTitle());
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

        private float dpToPx(float dp) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                    getActivity().getResources().getDisplayMetrics());
        }

        @Override
        public Preference findPreference(CharSequence key) {
            Preference pref = prefMap.get(key.toString());
            if (pref == null) {
                pref = super.findPreference(key);
            }
            return pref;
        }

        private void bindPreference(int keyId) {
            bindPreference(keyId, SUMMARY_FLAG_NULL, 0);
        }

        private void bindPreference(int keyId, boolean mask) {
            if (mask) {
                bindPreference(keyId, SUMMARY_FLAG_NORMAL | SUMMARY_FLAG_MASK, 0);
            } else {
                bindPreference(keyId, SUMMARY_FLAG_NORMAL, 0);
            }
        }

        private void bindPreference(int keyId, int summaryId) {
            bindPreference(keyId, SUMMARY_FLAG_NORMAL, summaryId);
        }

        private void bindPreferenceList(int keyId, int arrayId, int index) {
            bindPreferenceList(keyId, arrayId, index, 0);
        }

        private void bindPreferenceList(int keyId, int arrayId, int defValue, int offset) {
            String key = getString(keyId);
            Preference preference = findPreference(key);
            List<String> apiList = Arrays.asList(getResources().getStringArray(arrayId));
            preference.setOnPreferenceClickListener(this);
            preference.setSummary(apiList.get(sharedPrefs.getInt(key, defValue) - offset));
            keyMap.put(key, keyId);
            prefMap.put(key, preference);
        }

        private void bindPreference(int keyId, int summaryFlags, int summaryId) {
            String key = getString(keyId);
            Preference preference = findPreference(key);
            preference.setOnPreferenceClickListener(this);

            if ((summaryFlags & SUMMARY_FLAG_NORMAL) == SUMMARY_FLAG_NORMAL) {
                String defaultSummary = summaryId == 0 ? "" : getString(summaryId);
                String summary = sharedPrefs.getString(key, defaultSummary);

                if (summary.isEmpty() && !defaultSummary.isEmpty()) {
                    summary = defaultSummary;
                }

                boolean mask = ((summaryFlags & SUMMARY_FLAG_MASK) == SUMMARY_FLAG_MASK);
                preference.setSummary(mask ? mask(summary) : summary);
            }
            keyMap.put(key, keyId);
            prefMap.put(key, preference);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode,
                                               @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode) {
                case R.string.ignore_known_contact_key:
                case R.string.not_mark_contact_key:
                case R.string.display_on_outgoing_key:
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        setChecked(requestCode, false);
                    }
                    break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }

        private void setChecked(int key, boolean checked) {
            SwitchPreference preference = (SwitchPreference) findPreference(getString(key));
            preference.setChecked(checked);
        }

        private int getKeyId(String key) {
            return keyMap.get(key);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            int keyId = getKeyId(preference.getKey());
            switch (keyId) {
//                文字大小
                case R.string.window_text_size_key:
                    showSeekBarDialog(R.string.window_text_size_key, FloatWindow.TEXT_SIZE, 20, 60,
                            R.string.window_text_size, R.string.text_size);
                    break;
//                    窗口高度
                case R.string.window_height_key:
                    showSeekBarDialog(R.string.window_height_key, FloatWindow.WINDOW_HEIGHT, mPoint.y / 8, mPoint.y / 4,
                            R.string.window_height, R.string.window_height_message);
                    break;
//                    文字对齐
                case R.string.window_text_alignment_key:
                    showRadioDialog(R.string.window_text_alignment_key,
                            R.string.window_text_alignment, R.array.align_type, 1);
                    break;
//                    透明度
                case R.string.window_transparent_key:
                    showSeekBarDialog(R.string.window_transparent_key, FloatWindow.WINDOW_TRANS, 80,
                            100, R.string.window_transparent, R.string.text_transparent);
                    break;
//                    文字缩进
                case R.string.window_text_padding_key:
                    showSeekBarDialog(R.string.window_text_padding_key, FloatWindow.TEXT_PADDING, 0,
                            mPoint.x / 2, R.string.window_text_padding, R.string.text_padding);
                    break;
//                    忽略已存在的联系人号码
                case R.string.ignore_known_contact_key:

//                    去电时显示
                case R.string.display_on_outgoing_key:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        int res = getActivity().checkSelfPermission(Manifest.permission.PROCESS_OUTGOING_CALLS);
                        if (res != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.PROCESS_OUTGOING_CALLS}, keyId);
                            return true;
                        }
                    }
                    break;
//                    忽略号码段白名单
                case R.string.ignore_regex_key:
                    showEditDialog(R.string.ignore_regex_key, R.string.ignore_regex,
                            R.string.empty_string,
                            R.string.ignore_regex_hint, R.string.example, R.string.regex_example);
                    break;
//                    挂断关键字
                case R.string.hangup_keyword_key:
                    showEditDialog(R.string.hangup_keyword_key, R.string.hangup_keyword,
                            R.string.hangup_keyword_default, R.string.hangup_keyword_hint);
                    break;
//                    挂断号码关键字
                case R.string.hangup_number_keyword_key:
                    showEditDialog(R.string.hangup_number_keyword_key,
                            R.string.hangup_number_keyword,
                            R.string.empty_string, R.string.hangup_keyword_hint);
                    break;
//                挂断归属地关键字
                case R.string.hangup_geo_keyword_key:
                    showEditDialog(R.string.hangup_geo_keyword_key, R.string.hangup_geo_keyword,
                            R.string.empty_string, R.string.hangup_keyword_hint);
                    break;
//                临时禁用自动挂断
                case R.string.temporary_disable_blacklist_key:
                    if (sharedPrefs.getBoolean(getString(R.string.temporary_disable_blacklist_key),
                            false)) {
                        showRadioDialog(R.string.repeated_incoming_count_key,
                                R.string.temporary_disable_blacklist, R.array.repeated_incoming_count, 1);
                    }
                    break;
//                自动挂断
                case R.string.auto_hangup_key:
                    try {
                        mService.checkCallPermission();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
//                    添加号码信息到通话记录
                case R.string.add_call_log_key:
                    try {
                        isCheckRingOnce = false;
                        mService.checkCallLogPermission();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
//                    添加响一声和自动挂断提示到通话记录
                case R.string.ring_once_and_auto_hangup_key:
                    try {
                        isCheckRingOnce = true;
                        mService.checkCallLogPermission();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
//                    自动上报
                case R.string.auto_report_key:
                    if (sharedPrefs.getBoolean(getString(R.string.auto_report_key), false)) {
                        showConfirmDialog(R.string.auto_report, R.string.auto_report_confirm,
                                R.string.auto_report_key);
                    }
                    break;
//                    启动主动标记功能
                case R.string.enable_marking_key:
                    if (sharedPrefs.getBoolean(getString(R.string.enable_marking_key), false)) {
                        showConfirmDialog(R.string.enable_marking, R.string.mark_confirm,
                                R.string.enable_marking_key);
                    }
                    break;

                //                    不标记联系人号码
                case R.string.not_mark_contact_key:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        int res = getActivity().checkSelfPermission(
                                Manifest.permission.READ_CONTACTS);
                        if (res != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                                    keyId);
                            return true;
                        }
                    }
                    break;
//                    去电时位置调整
                case R.string.outgoing_window_position_key:
                    if (sharedPrefs.getBoolean(getString(R.string.outgoing_window_position_key),
                            false)) {
                        showTextDialog(R.string.outgoing_window_position,
                                R.string.outgoing_window_position_message);
                    }
                    break;
            }
            return false;
        }

        private void showSeekBarDialog(int keyId, final String bundleKey, int defaultValue,
                                       int max, int title, int textRes) {
            final String key = getString(keyId);
            int value = sharedPrefs.getInt(key, defaultValue);
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(title));
            View layout = View.inflate(getActivity(), R.layout.dialog_seek, null);
            builder.setView(layout);

            final SeekBarCompat seekBar = (SeekBarCompat) layout.findViewById(R.id.seek_bar);
            seekBar.setMax(max);
            seekBar.setProgress(value);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress == 0) {
                        progress = 1;
                    }
                    mWindow.sendData(bundleKey, progress, Window.Type.SETTING);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int value = seekBar.getProgress();
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putInt(key, value);
                    editor.apply();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mWindow.closeWindow();
                }
            });
            builder.show();

            mWindow.showTextWindow(textRes, Window.Type.SETTING);
        }

        private void showRadioDialog(int keyId, int title, int listId, int defValue) {
            showRadioDialog(keyId, title, listId, defValue, 0);
        }

        private void showRadioDialog(int keyId, int title, int listId, int defValue,
                                     final int offset) {
            final String key = getString(keyId);
            final List<String> list = Arrays.asList(getResources().getStringArray(listId));
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(title));
            View layout = View.inflate(getActivity(), R.layout.dialog_radio, null);
            builder.setView(layout);
            final AlertDialog dialog = builder.create();

            final RadioGroup radioGroup = (RadioGroup) layout.findViewById(R.id.radio);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            for (String s : list) {
                RadioButton radioButton = new RadioButton(getActivity());
                radioButton.setText(s);
                radioGroup.addView(radioButton, layoutParams);
            }

            RadioButton button =
                    ((RadioButton) radioGroup.getChildAt(
                            sharedPrefs.getInt(key, defValue) - offset));
            button.setChecked(true);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    int index = group.indexOfChild(group.findViewById(checkedId));
                    Preference preference = findPreference(key);
                    if (preference != null) {
                        preference.setSummary(list.get(index));
                    }
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putInt(key, index + offset);
                    editor.apply();
                    dialog.dismiss();
                }
            });
            dialog.show();
        }

        private void showTextDialog(int title, int text) {
            showTextDialog(title, getString(text));
        }

        private void showTextDialog(int title, String text) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(title));
            View layout = View.inflate(getActivity(), R.layout.dialog_text, null);
            builder.setView(layout);

            TextView textView = layout.findViewById(R.id.text);
            textView.setText(text);

            builder.setPositiveButton(R.string.ok, null);
            builder.show();
        }

        private void showEditDialog(int keyId, int title, final int defaultText, int hint) {
            showEditDialog(keyId, title, defaultText, hint, 0, 0);
        }

        private void showEditDialog(int keyId, int title, final int defaultText, int hint,
                                    final int help, final int helpText) {
            final String key = getString(keyId);
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(title));
            View layout = View.inflate(getActivity(), R.layout.dialog_edit, null);
            builder.setView(layout);

            final EditText editText = (EditText) layout.findViewById(R.id.text);
            editText.setText(sharedPrefs.getString(key, getString(defaultText)));
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            if (hint > 0) {
                editText.setHint(hint);
            }

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String value = editText.getText().toString();
                    if (value.isEmpty()) {
                        value = getString(defaultText);
                    }
                    findPreference(key).setSummary(value);
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(key, value);
                    editor.apply();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);

            if (help != 0) {
                builder.setNeutralButton(help, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showTextDialog(help, helpText);
                    }
                });
            }

            builder.setCancelable(true);
            builder.show();
        }

        private void showConfirmDialog(int title, int text, final int key) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(title));
            View layout = View.inflate(getActivity(), R.layout.dialog_text, null);
            builder.setView(layout);

            TextView textView = layout.findViewById(R.id.text);
            textView.setText(getText(text));
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onCanfirmCanceled(key);
                }
            });
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onConfirmed(key);
                }
            });
            builder.show();
        }

        private void onCanfirmCanceled(int key) {
            switch (key) {
                case R.string.auto_report_key:
                    ((SwitchPreference) findPreference(getString(R.string.auto_report_key))).setChecked(false);
                    break;
                case R.string.enable_marking_key:
                    ((SwitchPreference) findPreference(getString(R.string.enable_marking_key))).setChecked(false);
            }
        }

        private void onConfirmed(int key) {
            switch (key) {
                case R.string.import_key:
                    isCheckStorageExport = false;
                    try {
                        mService.checkStoragePermission();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case R.string.export_key:
                    isCheckStorageExport = true;
                    try {
                        mService.checkStoragePermission();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }


        @Override
        public void onDestroy() {
            super.onDestroy();
            getActivity().unbindService(conn);
        }

    }
}
