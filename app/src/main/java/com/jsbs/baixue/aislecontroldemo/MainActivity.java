package com.jsbs.baixue.aislecontroldemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jsbs.baixue.aislecontroldemo.activity.StateActivity;
import com.jsbs.baixue.aislecontroldemo.constant.AisleStateConstant;
import com.jsbs.baixue.aislecontroldemo.constant.EventBusOrderVariety;
import com.jsbs.baixue.aislecontroldemo.mode.AisleStateInfo;
import com.jsbs.baixue.aislecontroldemo.mode.EventMsg;
import com.jsbs.baixue.aislecontroldemo.util.ClickFilter;
import com.jsbs.baixue.aislecontroldemo.util.FileUtil;
import com.jsbs.baixue.aislecontroldemo.util.ScreenUtil;
import com.jsbs.baixue.aislecontroldemo.util.USBDiskUtil;
import com.jsbs.baixue.aislecontroldemo.view.AisleStateAdapter;
import com.jsbs.baixue.aislecontroldemo.view.NiceSpinner;
import com.jsbs.baixue.aislecontroldemo.view.ToastDialog;
import com.jsbs.baixue.aislecontrollib.AisleControl;
import com.jsbs.baixue.aislecontrollib.CallBack;
import com.jsbs.baixue.aislecontrollib.constant.OrderVariety;
import com.jsbs.baixue.aislecontrollib.mode.ResponseOrder;
import com.jsbs.baixue.aislecontrollib.mode.request.AisleInspectionParam;
import com.jsbs.baixue.aislecontrollib.mode.request.LightParam;
import com.jsbs.baixue.aislecontrollib.mode.request.TemperatureParam;
import com.jsbs.baixue.aislecontrollib.mode.request.UpgradeParam;
import com.jsbs.baixue.aislecontrollib.mode.response.AisleInspectionResult;
import com.yinglan.keyboard.HideUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private MyHandler mHandler;
    private int width, height;
    private AisleControl aisleControl = null;
    private SerialDataReceiver sReceiver = null;
    private NiceSpinner serialSpinner;
    private Button serialButton, upgradeButton, exitButton, startTestButton, stopTestButton, queryStateButton;
    private TextView response;
    private NestedScrollView responseScrollView;
    private RecyclerView aisleShowView;
    String TAG = "Sugandha";

    private AisleStateAdapter aAdapter = null;
    private ToastDialog tDialog;

    private boolean isOpenSerial = false;//whether to open the serial port.  true:open;false:close
    private boolean canStartTest = false;//whether to start testing.  true:start;false:stop
    private boolean isCheckProgress = false;//the state of check.  true:start;false:stop
    private int checkCount = 0;//check count

    private String selectSerialName = "";//the aisle that is selected
    private List<String> allSerialNameList = null;//all serial info
    private List<AisleStateInfo> stateList = null;//all aisle state info
    private List<AisleStateInfo> needCheckAisleList = null;//the aisle that need to inspection

    private final static int MSG_SHOW_RESPONSE = 0x01;
    private final static int MSG_CHECK_PREPARE = 0x02;
    private final static int MSG_SEND_CHECK_AISLE = 0x03;
    private final static int MSG_CHECK_FINISH = 0x04;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //request permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestSimPermission(this);
        }
        mHandler = new MyHandler();
        ScreenUtil util = new ScreenUtil(MainActivity.this);
        width = util.getWidth();
        height = util.getHeight();
        HideUtil.init(this);
        EventBus.getDefault().register(this);

        isOpenSerial = false;
        canStartTest = false;
        isCheckProgress = false;
        selectSerialName = "";
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (sReceiver == null) {
            sReceiver = new SerialDataReceiver();
            registerReceiver(sReceiver, new IntentFilter("com.jsbs.aislecontrol.response"));//register serial response data
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (sReceiver != null) {
            unregisterReceiver(sReceiver);//unregister serial response data
            sReceiver = null;
        }
        dismissToastDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (aisleControl != null) {
            aisleControl.closeControl();//close aisle control
            aisleControl = null;
        }
    }

    /**
     * request permission
     *
     * @param activity activity
     */
    private void requestSimPermission(Activity activity) {
        String[] needPermissions = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            needPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_NUMBERS};
        } else {
            needPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_SMS};
        }
        ActivityCompat.requestPermissions(activity, needPermissions, 1);
    }

    /**
     * init view
     */
    @SuppressWarnings("RedundantCast")
    private void initView() {
        serialSpinner = (NiceSpinner) findViewById(R.id.serial_name_spinner);
        serialButton = (Button) findViewById(R.id.serial_control_button);
        serialButton.setText("Open Serial");
        serialButton.setOnClickListener(this);
        upgradeButton = (Button) findViewById(R.id.upgrade_button);
        upgradeButton.setOnClickListener(this);
        exitButton = (Button) findViewById(R.id.exit);
        exitButton.setOnClickListener(this);
        startTestButton = (Button) findViewById(R.id.start_all_aisle_test_button);
        startTestButton.setOnClickListener(this);
        stopTestButton = (Button) findViewById(R.id.stop_all_aisle_test_button);
        stopTestButton.setOnClickListener(this);
        response = (TextView) findViewById(R.id.response);
        initSerialNameSpinner();
        queryStateButton = (Button) findViewById(R.id.query_state_button);
        queryStateButton.setOnClickListener(this);
        responseScrollView = (NestedScrollView) findViewById(R.id.response_scroll_view);
        RelativeLayout.LayoutParams param1 = (RelativeLayout.LayoutParams) responseScrollView.getLayoutParams();
        param1.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        param1.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        responseScrollView.setLayoutParams(param1);
        aisleShowView = (RecyclerView) findViewById(R.id.aisle_show_view);
        RelativeLayout.LayoutParams param2 = (RelativeLayout.LayoutParams) aisleShowView.getLayoutParams();
        param2.width = width / 5 * 4;
        param2.height = height / 5 * 2;
        aisleShowView.setLayoutParams(param2);
        initAisleShowView();//show all test aisle
    }

    /**
     * init serial spinner
     */
    private void initSerialNameSpinner() {
        try {
            allSerialNameList = FileUtil.getCanUsedComPortName();
        } catch (Exception e) {
            Log.e("aisle control", e.getMessage());
            allSerialNameList = null;
        }
        if (allSerialNameList != null && allSerialNameList.size() > 0) {
            serialButton.setClickable(true);//open clickable
            selectSerialName = allSerialNameList.get(0);
            serialSpinner.setTextColor(getResources().getColor(R.color.material_blue_grey_95));
            serialSpinner.attachDataSource(allSerialNameList);
            serialSpinner.setSelectedIndex(0);
            serialSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.w("select serial", allSerialNameList.get(position));
                    selectSerialName = allSerialNameList.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } else {
            serialButton.setClickable(false);//close clickable
            selectSerialName = "";
        }
    }

    /**
     * 升级操作
     */
    private void upgradeOperation() {
        String usbPath = USBDiskUtil.getUSBPath(MainActivity.this);
        if (TextUtils.isEmpty(usbPath)) {
            Log.i("aisle control", "Can't find usb path.");
            return;
        }
        File file = new File(usbPath + File.separator + "upgrade.bin");
        if (file.exists()) {
            //system start upgrade
            UpgradeParam param = new UpgradeParam();
            param.setUpgradeFile(file);
            param.setNeedUpgrade(true);
            if (aisleControl != null) {
                aisleControl.sendControlOrder(OrderVariety.VERSION_UPGRADE, 302, param, new CallBack() {
                    @Override
                    public void onSuccess(ResponseOrder responseOrder) {
                        Log.w("aisle control", "success response = " + (responseOrder != null ? JSON.toJSONString(responseOrder) : "null"));
                        Message msg = mHandler.obtainMessage();
                        msg.what = MSG_SHOW_RESPONSE;
                        msg.obj = "upgrade set success response = " + (responseOrder != null ? JSON.toJSONString(responseOrder) : "null");
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onFailure(String s) {
                        Log.w("aisle control", "fail response = " + (s != null ? s : "null"));
                        Message msg = mHandler.obtainMessage();
                        msg.what = MSG_SHOW_RESPONSE;
                        msg.obj = "upgrade set fail response = " + (s != null ? s : "null");
                        mHandler.sendMessage(msg);
                    }
                });
            }
        }
    }

    /**
     * init aisle info
     */
    private void initAisleShowView() {
        stateList = FileUtil.getSaveAisleStateInfo(MainActivity.this);
        if (stateList == null || stateList.size() == 0) {
            //init aisle info
            stateList = new ArrayList<>();
            for (int i = 0; i < 60; i++) {
                AisleStateInfo info = new AisleStateInfo();
                info.setCabinetId(0);
                info.setAisleNum(Integer.toString(10 + i));
                info.setAisleState(AisleStateConstant.AISLE_NORMAL);
                stateList.add(info);
            }
        }
        //show aisle info
        if (aisleShowView != null)
            aisleShowView.removeAllViews();
        GridLayoutManager gm = new GridLayoutManager(MainActivity.this, 4);
        gm.setOrientation(GridLayoutManager.VERTICAL);
        aisleShowView.setLayoutManager(gm);
        aAdapter = new AisleStateAdapter(MainActivity.this, width / 5, height / 15, stateList);
        aisleShowView.setAdapter(aAdapter);
        aAdapter.SetOnItemClickerListener(new AisleStateAdapter.OnItemClickerListener() {
            @Override
            public void onItemClick(View v, int position) {
                if (!ClickFilter.filterFast()) {
                    if (!canStartTest) {
                        showToastDialog(MainActivity.this, getString(R.string.cannot_start_test));
                        return;
                    }
                    if (isCheckProgress) {
                        showToastDialog(MainActivity.this, getString(R.string.aisle_check_progress));
                        return;
                    }
                    if (stateList != null && stateList.size() > 0 && position < stateList.size()) {
                        needCheckAisleList = new ArrayList<>();
                        needCheckAisleList.add(stateList.get(position));
                        mHandler.sendEmptyMessage(MSG_CHECK_PREPARE);
                    }
                }
            }
        });
    }

    /**
     * send aisle run order
     *
     * @param aisleNum aisle number
     */
    private void sendCheckOrder(String aisleNum) {
        if (!TextUtils.isEmpty(aisleNum)) {
            AisleInspectionParam param = new AisleInspectionParam();
            param.setAisleVariety(0x00);
            param.setAisleRowNum(Integer.parseInt(aisleNum.substring(0, 1), 16));
            param.setAisleColumnNum(Integer.parseInt(aisleNum.substring(1, 2), 16));
            if (aisleControl != null) {
                aisleControl.sendControlOrder(OrderVariety.AISLE_INSPECTION, Long.parseLong(aisleNum, 16), param, new CallBack() {
                    @Override
                    public void onSuccess(ResponseOrder responseOrder) {
                        Log.w("aisle control", "success response = " + (responseOrder != null ? JSON.toJSONString(responseOrder) : "null"));
                        Message msg = mHandler.obtainMessage();
                        msg.what = MSG_SHOW_RESPONSE;
                        msg.obj = "aisle inspection success response = " + (responseOrder != null ? JSON.toJSONString(responseOrder) : "null");
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onFailure(String s) {
                        Log.w("aisle control", "fail response = " + (s != null ? s : "null"));
                        Message msg = mHandler.obtainMessage();
                        msg.what = MSG_SHOW_RESPONSE;
                        msg.obj = "aisle inspection fail response = " + (s != null ? s : "null");
                        mHandler.sendMessage(msg);
                    }
                });
            } else {
                mHandler.sendEmptyMessage(MSG_CHECK_FINISH);
            }
        }
    }

    /**
     * parse aisle inspection result
     *
     * @param value result
     */
    private void parseAisleInspectionResult(String value) {
        if (!TextUtils.isEmpty(value)) {
            AisleInspectionResult param = JSON.parseObject(value, AisleInspectionResult.class);
            if (param != null) {
                String aisleNum = Integer.toHexString(param.getAisleRowNum()) + Integer.toHexString(param.getAisleColumnNum());
                AisleStateInfo info = new AisleStateInfo();
                info.setCabinetId(0);
                info.setAisleState(param.getAisleState());
                info.setAisleNum(aisleNum);
                //upgrade aisle state
                if (aAdapter != null)
                    aAdapter.updateAisleState(info);
            }
        }
        //check next inspection aisle
        if (checkCount == -1) {
            //stop aisle inspection
            mHandler.sendEmptyMessage(MSG_CHECK_FINISH);
        } else if (checkCount >= 0 && (checkCount + 1) < needCheckAisleList.size()) {
            //continue testing
            checkCount++;
            Message startMsg = mHandler.obtainMessage();
            startMsg.what = MSG_SEND_CHECK_AISLE;
            startMsg.arg1 = checkCount;
            mHandler.sendMessageDelayed(startMsg, 2000);//Test the next aisle at an interval of two seconds.
        } else if (checkCount >= 0) {
            //stop aisle inspection
            mHandler.sendEmptyMessage(MSG_CHECK_FINISH);
        }
    }

    /**
     * query drive version
     */
    private void queryDriveVersion() {
        aisleControl.sendControlOrder(OrderVariety.DRIVE_VERSION, 100, null, new CallBack() {
            @Override
            public void onSuccess(ResponseOrder responseOrder) {
                Log.i("aisleVersion", "success response = " + JSON.toJSONString(responseOrder));
            }

            @Override
            public void onFailure(String s) {
                Log.i("aisleVersion", "fail response = " + s);
            }
        });
    }

    /**
     * open StateActivity
     */
    private void startStateActivity() {
        if (!ClickFilter.filter()) {
            Intent intent = new Intent(MainActivity.this, StateActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    /**
     * show hint content dialog
     *
     * @param mContext context
     * @param hint     hint
     */
    private void showToastDialog(Context mContext, String hint) {
        if (tDialog == null) {
            tDialog = new ToastDialog(mContext, R.style.CardDialogTransparent, hint);
            tDialog.show();
            tDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dismissToastDialog();
                }
            });
        }
    }

    /**
     * dismiss hint content dialog
     */
    private void dismissToastDialog() {
        if (tDialog != null) {
            if (tDialog.isShowing()) {
                tDialog.dismiss();
            }
            tDialog = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMsg(EventMsg event) {
        if (event != null) {
            String variety = event.getMsgVariety();
            String value = event.getMsgValue();
            if (!TextUtils.isEmpty(variety) && !TextUtils.isEmpty(value)) {
                if (variety.equals(EventBusOrderVariety.EVENT_SET_TEMPERATURE_PARAM)) {
                    //设置温度控制参数
                    TemperatureParam param = JSON.parseObject(value, TemperatureParam.class);
                    if (param != null && aisleControl != null) {
                        aisleControl.sendControlOrder(OrderVariety.SET_TEMPERATURE, 303, param, new CallBack() {
                            @Override
                            public void onSuccess(ResponseOrder responseOrder) {
                                Log.d(TAG, "SetTemp");
                                Log.w("aisle control", "success response = " + (responseOrder != null ? JSON.toJSONString(responseOrder) : "null"));
                                Message msg = mHandler.obtainMessage();
                                msg.what = MSG_SHOW_RESPONSE;
                                msg.obj = "set temperature success response = " + (responseOrder != null ? JSON.toJSONString(responseOrder) : "null");
                                mHandler.sendMessage(msg);
                            }

                            @Override
                            public void onFailure(String s) {
                                Log.w("aisle control", "fail response = " + (s != null ? s : "null"));
                                Message msg = mHandler.obtainMessage();
                                msg.what = MSG_SHOW_RESPONSE;
                                msg.obj = "set temperature fail response = " + (s != null ? s : "null");
                                mHandler.sendMessage(msg);
                            }
                        });
                    }
                } else if (variety.equals(EventBusOrderVariety.EVENT_SET_LIGHT_PARAM)) {
                    //设置照明控制参数
                    LightParam param = JSON.parseObject(value, LightParam.class);
                    if (param != null && aisleControl != null) {
                        aisleControl.sendControlOrder(OrderVariety.SET_LIGHT, 304, param, new CallBack() {
                            @Override
                            public void onSuccess(ResponseOrder responseOrder) {
                                Log.w("aisle control", "success response = " + (responseOrder != null ? JSON.toJSONString(responseOrder) : "null"));
                                Message msg = mHandler.obtainMessage();
                                msg.what = MSG_SHOW_RESPONSE;
                                msg.obj = "set light success response = " + (responseOrder != null ? JSON.toJSONString(responseOrder) : "null");
                                mHandler.sendMessage(msg);
                            }

                            @Override
                            public void onFailure(String s) {
                                Log.w("aisle control", "fail response = " + (s != null ? s : "null"));
                                Message msg = mHandler.obtainMessage();
                                msg.what = MSG_SHOW_RESPONSE;
                                msg.obj = "set light fail response = " + (s != null ? s : "null");
                                mHandler.sendMessage(msg);
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exit:
                //exit
                MainActivity.this.finish();
                break;
            case R.id.serial_control_button:
                //serial control
                if (!isOpenSerial) {
                    //open serial
                    isOpenSerial = true;
                    serialButton.setText("Close Serial");
                    if (aisleControl == null && !TextUtils.isEmpty(selectSerialName)) {
                        aisleControl = AisleControl.getDefault(MainActivity.this, selectSerialName, 9600, 0);
                        aisleControl.openControl();//start control
                    }
                } else {
                    //close serial
                    isOpenSerial = false;
                    serialButton.setText("Open Serial");
                    if (aisleControl != null) {
                        aisleControl.closeControl();//stop control
                        aisleControl = null;
                    }
                }
                break;
            case R.id.upgrade_button:
                //upgrade driver software
                upgradeOperation();
                break;
            case R.id.start_all_aisle_test_button:
                //start all aisle test
                if (!ClickFilter.filter()) {
                    if (!canStartTest) {
                        showToastDialog(MainActivity.this, getString(R.string.cannot_start_test));
                        return;
                    }
                    if (isCheckProgress) {
                        showToastDialog(MainActivity.this, getString(R.string.aisle_check_progress));
                        return;
                    }
                    if (stateList != null && stateList.size() > 0) {
                        needCheckAisleList = new ArrayList<>();
                        needCheckAisleList.addAll(stateList);
                        mHandler.sendEmptyMessage(MSG_CHECK_PREPARE);
                    }
                }
                break;
            case R.id.stop_all_aisle_test_button:
                //stop all aisle test
                checkCount = -1;
                break;
            case R.id.query_state_button:
                //query current machine state
                startStateActivity();
                break;
            default:
                break;
        }
    }

    /**
     * serial response data
     */
    class SerialDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String variety = intent.getStringExtra("order_response_variety");
                String value = intent.getStringExtra("order_response_value");
                Log.w("serialData", "variety = " + (TextUtils.isEmpty(variety) ? "null" : variety) + "@@@" + "value = " + (TextUtils.isEmpty(value) ? "null" : value));

                String receiveValue = "receiver <-- " + "variety = " + (TextUtils.isEmpty(variety) ? "null" : variety) + "@@@" + "value = " + (TextUtils.isEmpty(value) ? "null" : value);
                Message msg = mHandler.obtainMessage();
                msg.what = MSG_SHOW_RESPONSE;
                msg.obj = receiveValue;
                mHandler.sendMessage(msg);//show response data

                if (variety.equals(OrderVariety.AISLE_INSPECTION_RESULT)) {
                    //the result of aisle inspection
                    parseAisleInspectionResult(value);
                } else if (variety.equals(OrderVariety.AISLE_DRIVE_CONNECT_STATE)) {
                    //serial connect state
                    if (!TextUtils.isEmpty(value)) {
                        JSONObject object = JSON.parseObject(value);
                        if (object != null && object.getString("state").equals("connect"))
                            canStartTest = true;
                        else
                            canStartTest = false;
                    } else
                        canStartTest = false;
                }
            }
        }
    }

    @SuppressLint("HandlerLeak")
    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_SHOW_RESPONSE) {
                //show content
                String value = (String) msg.obj;
                if (!TextUtils.isEmpty(value))
                    response.append(value + "\n");
            } else if (msg.what == MSG_CHECK_PREPARE) {
                //prepare aisle test
                if (needCheckAisleList != null && needCheckAisleList.size() > 0) {
                    isCheckProgress = true;
                    checkCount = 0;

                    Message startMsg = obtainMessage();
                    startMsg.what = MSG_SEND_CHECK_AISLE;
                    startMsg.arg1 = checkCount;
                    sendMessage(startMsg);
                } else {
                    sendEmptyMessage(MSG_CHECK_FINISH);
                }
            } else if (msg.what == MSG_SEND_CHECK_AISLE) {
                //notify aisle run
                if (needCheckAisleList != null && needCheckAisleList.size() > 0 && msg.arg1 >= 0) {
                    //update aisle state
                    if (aAdapter != null)
                        aAdapter.updateAisleTesting(needCheckAisleList.get(msg.arg1).getAisleNum());

                    sendCheckOrder(needCheckAisleList.get(msg.arg1).getAisleNum());
                }
            } else if (msg.what == MSG_CHECK_FINISH) {
                //stop test
                isCheckProgress = false;
                checkCount = 0;
                needCheckAisleList = new ArrayList<>();
                //save aisle state
                FileUtil.saveAisleStateInfo(MainActivity.this, stateList);
            }
        }
    }
}
