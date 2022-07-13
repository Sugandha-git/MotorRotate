package com.jsbs.baixue.aislecontroldemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.jsbs.baixue.aislecontroldemo.constant.Keys;

/**
 * Created by baixue on 2018/7/28.
 */
public class USBDiskReceiver extends BroadcastReceiver {
    private static final String TAG = "USBDiskReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = context.getSharedPreferences(Keys.SP_USB_DISK_PATH, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String action = intent.getAction();
        String path = intent.getData().getPath();
        if (!TextUtils.isEmpty(path)) {
            if ("android.intent.action.MEDIA_REMOVED".equals(action)) {
                editor.clear();
            }
            if ("android.intent.action.MEDIA_EJECT".equals(action)) {
                editor.clear();
            }
            if ("android.intent.action.MEDIA_MOUNTED".equals(action)) {
                editor.clear();
                editor.putString(Keys.KEYS_SAVE_PATH, path);
            }
        }
        editor.apply();
    }
}
