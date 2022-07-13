package com.jsbs.baixue.aislecontroldemo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import com.jsbs.baixue.aislecontroldemo.constant.Keys;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by baixue on 2018/7/28.
 */
public class USBDiskUtil {
    public static final String ANDROID_APK_FILE_PATH = "baixue" + File.separator + "apk";
    public static final String ANDROID_APK_NAME = "touch.apk";

    /**
     * 获取U盘路径（失败-未知原因）
     *
     * @return U盘路径
     */
    public static List<String> getAllExternalSdcardPath() {
        List<String> PathList = new ArrayList<String>();

        String firstPath = Environment.getExternalStorageDirectory().getPath();
        Log.d("UdiskPath:", "getAllExternalSdcardPath , firstPath = " + firstPath);

        try {
            // 运行mount命令，获取命令的输出，得到系统中挂载的所有目录
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                // 将常见的linux分区过滤掉
                if (line.contains("proc") || line.contains("tmpfs") || line.contains("media") || line.contains("asec") || line.contains("secure") || line.contains("system") || line.contains("cache") || line.contains("sys") || line.contains("data") || line.contains("shell") || line.contains("root") || line.contains("acct") || line.contains("misc") || line.contains("obb")) {
                    continue;
                }

                // 下面这些分区是我们需要的
                if (line.contains("fat") || line.contains("fuse") || (line.contains("ntfs"))) {
                    // 将mount命令获取的列表分割，items[0]为设备名，items[1]为挂载路径
                    String items[] = line.split(" ");
                    if (items != null && items.length > 1) {
                        String path = items[1].toLowerCase(Locale.getDefault());
                        // 添加一些判断，确保是sd卡，如果是otg等挂载方式，可以具体分析并添加判断条件
                        if (path != null && !PathList.contains(path) && path.contains("sd"))
                            PathList.add(items[1]);
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (!PathList.contains(firstPath))
            PathList.add(firstPath);

        return PathList;
    }

    /**
     * 得到u盘路径
     *
     * @return 路径
     */
    public static String getUSBDiskPath(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Keys.SP_USB_DISK_PATH, Context.MODE_PRIVATE);
        return sp.getString(Keys.KEYS_SAVE_PATH, "");
    }

    /**
     * 获取U盘路径
     */
    public static String getUSBPath(Context context) {
        String result = "";

        List<String> usbPath = getUSBPathList(context);
        // TODO: 2019/11/27 目前是Inhand的U盘路径是默认的
//        List<String> usbPath = getUSBPathWithInhandSevenInch(true);
        if (usbPath != null && usbPath.size() > 0) {
            //目前只有USB路径
            result = usbPath.get(0);
            Log.e("UDiskPath", result);
        } else {
            result = getUSBDiskPath(context);
        }
        return result;
    }

    /**
     * 获取U盘路径
     *
     * @param context 上下文
     * @return 各个路径的集合
     */
    public static List<String> getUSBPathList(Context context) {
        if (context == null)
            return null;
        List<String> usbPaths = new ArrayList<>();
        try {
            StorageManager srgMgr = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Class<StorageManager> srgMgrClass = StorageManager.class;
            String[] paths = (String[]) srgMgrClass.getMethod("getVolumePaths").invoke(srgMgr);
            for (String path : paths) {
                Object volumeState = srgMgrClass.getMethod("getVolumeState", String.class).invoke(srgMgr, path);
                if (!path.contains("emulated") && Environment.MEDIA_MOUNTED.equals(volumeState))
                    usbPaths.add(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usbPaths;
    }
}
