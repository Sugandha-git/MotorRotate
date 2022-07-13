package com.jsbs.baixue.aislecontroldemo.constant;

public class AisleStateConstant {
    public static final int AISLE_NORMAL = 0;//电机正常 //The motor is normal
    public static final int AISLE_TIME_OUT = 1;//电机超时 //Motor timed out
    public static final int AISLE_BLOCK = 2;//电机堵转 // Motor blocked
    public static final int AISLE_EMPTY = 3;//电机空缺 //Motor vacancy
    public static final int AISLE_CONNECT_LOST = 4;//电机无响应 //Motor not responding
    public static final int AISLE_SALE_SENSOR_LOST = 5;//当前货道售货检测传感器丢失  //The current goods aisle sales detection sensor is missing
}
