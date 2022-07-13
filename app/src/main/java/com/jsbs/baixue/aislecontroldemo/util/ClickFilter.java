package com.jsbs.baixue.aislecontroldemo.util;

/**
 * Created on 2017/8/18 15:37.
 * Author: WangJun
 */

public class ClickFilter {
    private static final long INTERVAL = 1000L;//防止连续点击的时间间隔
    private static long lastClickTime = 0L;//上一次点击的时间

    private static final long INTERVAL_FAST = 500L;//防止连续点击的时间间隔(快速)
    private static long lastClickTime_FAST = 0L;//上一次点击的时间(快速)

    public static void initLastClickTime() {
        lastClickTime = 0;
    }

    public synchronized static boolean filter() {
        long time = System.currentTimeMillis();
        boolean isDoubleClick;
        if (time - lastClickTime > INTERVAL) {
            isDoubleClick = false;
        } else {
            isDoubleClick = true;
        }
        lastClickTime = time;
        return isDoubleClick;
    }

    public synchronized static boolean filterFast() {
        long time = System.currentTimeMillis();
        boolean isDoubleClick;
        if (time - lastClickTime_FAST > INTERVAL_FAST) {
            isDoubleClick = false;
        } else {
            isDoubleClick = true;
        }
        lastClickTime_FAST = time;
        return isDoubleClick;
    }
}
