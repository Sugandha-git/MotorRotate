package com.jsbs.baixue.aislecontroldemo.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.lang.reflect.Field;

public class ScreenUtil {
    private Context context;
    private float density;
    private int densityDpi;
    private int width;
    private int height;

    public ScreenUtil(Context context) {
        super();
        this.context = context;

        getRealScreenRelateInformation(context);

    }

    private void getScreenSize() {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        density = dm.density;
        densityDpi = dm.densityDpi;
        width = dm.widthPixels;
        height = dm.heightPixels;
    }

    public int getDensityDpi() {
        return densityDpi;
    }

    public float getDensity() {
        return density;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
////        映翰通系统
//		return height;
//        //四信系统
//        return height + getNavigationHeight(context);
        return height;
    }


    /**
     * 得到导航栏高度
     *
     * @param context 句柄
     * @return 值
     */
    private static int getNavigationHeight(Context context) {
        int result = 0;
        int resourceId = 0;
        int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        if (rid != 0) {
            resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            return context.getResources().getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    /**
     * 获取状态栏高度参数
     *
     * @param context 容器
     * @return 数据
     */
    private static int getStatusBarHeight(Context context) {
        if (context == null)
            return 0;
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * 获取实际屏幕尺寸参数
     *
     * @param context 容器
     */
    private void getRealScreenRelateInformation(Context context) {
        if (context == null)
            return;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            DisplayMetrics outMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getRealMetrics(outMetrics);
            int widthPixels = outMetrics.widthPixels;//可用显示大小的绝对宽度（以像素为单位）
            int heightPixels = outMetrics.heightPixels;//可用显示大小的绝对高度（以像素为单位）
            int densityDpi = outMetrics.densityDpi;//屏幕密度表示为每英寸点数
            float density = outMetrics.density;//显示器的逻辑密度
            float scaledDensity = outMetrics.scaledDensity;//显示屏上显示的字体缩放系数
            Log.i("display", "widthPixels = " + widthPixels + " ;heightPixels = " + heightPixels);
            //赋值屏幕参数
            this.width = widthPixels;
            this.height = heightPixels;
        }
    }
}
