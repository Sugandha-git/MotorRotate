package com.jsbs.baixue.aislecontroldemo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.jsbs.baixue.aislecontroldemo.constant.Keys;
import com.jsbs.baixue.aislecontroldemo.mode.AisleStateInfo;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    /**
     * @return 所有识别到的串口号
     * @throws Exception 异常
     */
    public static List<String> getCanUsedComPortName() throws Exception {
        List<String> deviceRoot = new ArrayList<>();
        LineNumberReader r = new LineNumberReader(new FileReader("/proc/tty/drivers"));
        String l;
        while ((l = r.readLine()) != null) {
            String[] w = l.split(" +");
            if ((w.length == 5) && (w[4].equals("serial"))) {
                Log.d("serialDevice", "Found new driver: " + w[1]);
                deviceRoot.add(w[1]);
            }
        }
        r.close();

        List<String> result = new ArrayList<>();
        File dev = new File("/dev");
        File[] files = dev.listFiles();
        for (int j = 0; j < deviceRoot.size(); j++) {
            int i;
            for (i = 0; i < files.length; i++) {
                if (files[i].getAbsolutePath().startsWith(deviceRoot.get(j))) {
                    Log.d("serialName", "Found new name: " + files[i]);
                    result.add(files[i].getAbsolutePath());
                }
            }
        }
        return result;
    }

    /**
     * 获取所有货道信息
     *
     * @param context 容器
     */
    public static List<AisleStateInfo> getSaveAisleStateInfo(Context context) {
        if (context == null)
            return null;
        SharedPreferences sp = context.getSharedPreferences(Keys.SP_AISLE_STATE, Context.MODE_PRIVATE);
        String value = sp.getString(Keys.KEYS_AISLE_STATE_INFO, "");
        if (!TextUtils.isEmpty(value))
            return JSON.parseArray(value, AisleStateInfo.class);
        else
            return null;
    }

    /**
     * 保存当前货道状态信息
     * @param context 容器
     * @param infoList 数据
     */
    public static void saveAisleStateInfo(Context context ,List<AisleStateInfo> infoList){
        if (context== null || infoList == null || infoList.size() ==0)
            return;
        SharedPreferences sp = context.getSharedPreferences(Keys.SP_AISLE_STATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Keys.KEYS_AISLE_STATE_INFO , JSON.toJSONString(infoList));
        editor.apply();
    }
}
