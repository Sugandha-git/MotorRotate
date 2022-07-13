package com.jsbs.baixue.aislecontroldemo.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.alibaba.fastjson.JSON;
import com.jsbs.baixue.aislecontroldemo.R;
import com.jsbs.baixue.aislecontroldemo.constant.EventBusOrderVariety;
import com.jsbs.baixue.aislecontroldemo.mode.EventMsg;
import com.jsbs.baixue.aislecontroldemo.util.ScreenUtil;
import com.jsbs.baixue.aislecontrollib.CallBack;
import com.jsbs.baixue.aislecontrollib.constant.OrderVariety;
import com.jsbs.baixue.aislecontrollib.mode.ResponseOrder;
import com.jsbs.baixue.aislecontrollib.mode.request.LightParam;
import com.jsbs.baixue.aislecontrollib.mode.request.TemperatureParam;
import com.jsbs.baixue.aislecontrollib.mode.response.MachineState;
import com.yinglan.keyboard.HideUtil;

import org.greenrobot.eventbus.EventBus;

import java.math.BigDecimal;

public class StateActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private SerialDataReceiver sReceiver = null;
    private MyHandler mHandler;
    private int width, height;
    private Button setTemperatureButton, setLightButton, back;
    private EditText setTemperatureContent, refrigerationTimeContent, defrostTimeContent;
    private TextView lightControlState, doorStateHint, doorStateContent, dropDetectionStateHint, dropDetectionStateContent, temperatureControlStateHint, temperatureControlStateContent, refrigerationWorkStateHint, refrigerationWorkStateContent, compressorWorkStateHint, compressWorkStateContent, evaporationFanWorkStateHint, evaporationFanWorkStateContent, lightStateHint, lightStateContent, glassHeatingStateHint, glassHeatingStateContent, cabinetTemperatureValueHint, cabinetTemperatureValueContent, setTemperatureValueHint, setTemperatureValueContent, refrigerationRunTimeHint, refrigerationRunTimeContent, defrostRunTimeHint, defrostRunTimeContent;
    private ToggleButton lightControlButton;
    private NestedScrollView paramScroll;
    String TAG = "sugandha";
    private String saveStateValue = "";//当前保存的状态值 // currently saved state value
    private final static int MSG_SHOW_STATE = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state);
        mHandler = new MyHandler();
        HideUtil.init(this);
        ScreenUtil util = new ScreenUtil(StateActivity.this);
        width = util.getWidth();
        height = util.getHeight();

        initView();
        initViewSize();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressWarnings("RedundantCast")
    private void initView() {
        setTemperatureContent = (EditText) findViewById(R.id.set_temperature_value);
        refrigerationTimeContent = (EditText) findViewById(R.id.refrigeration_time_value);
        defrostTimeContent = (EditText) findViewById(R.id.defrost_time_value);
        setTemperatureButton = (Button) findViewById(R.id.set_temperature_button);
        setTemperatureButton.setOnClickListener(this);
        lightControlButton = (ToggleButton) findViewById(R.id.light_control_button);
        lightControlButton.setOnCheckedChangeListener(this);
        lightControlState = (TextView) findViewById(R.id.light_control_state);
        setLightButton = (Button) findViewById(R.id.set_light_button);
        lightControlButton.setChecked(false);
        lightControlState.setText("Close");
        setLightButton.setOnClickListener(this);
        back = (Button) findViewById(R.id.back);
        back.setOnClickListener(this);
        paramScroll = (NestedScrollView) findViewById(R.id.state_param_scroll);
        doorStateHint = (TextView) findViewById(R.id.door_state_hint);
        doorStateContent = (TextView) findViewById(R.id.door_state_content);
        dropDetectionStateHint = (TextView) findViewById(R.id.drop_detection_state_hint);
        dropDetectionStateContent = (TextView) findViewById(R.id.drop_detection_state_content);
        temperatureControlStateHint = (TextView) findViewById(R.id.temperature_control_state_hint);
        temperatureControlStateContent = (TextView) findViewById(R.id.temperature_control_state_content);
        refrigerationWorkStateHint = (TextView) findViewById(R.id.refrigeration_work_state_hint);
        refrigerationWorkStateContent = (TextView) findViewById(R.id.refrigeration_work_state_content);
        compressorWorkStateHint = (TextView) findViewById(R.id.compressor_work_state_hint);
        compressWorkStateContent = (TextView) findViewById(R.id.compressor_work_state_content);
        evaporationFanWorkStateHint = (TextView) findViewById(R.id.evaporation_fan_work_state_hint);
        evaporationFanWorkStateContent = (TextView) findViewById(R.id.evaporation_fan_work_state_content);
        lightStateHint = (TextView) findViewById(R.id.light_state_hint);
        lightStateContent = (TextView) findViewById(R.id.light_state_content);
        glassHeatingStateHint = (TextView) findViewById(R.id.glass_heating_state_hint);
        glassHeatingStateContent = (TextView) findViewById(R.id.glass_heating_state_content);
        cabinetTemperatureValueHint = (TextView) findViewById(R.id.cabinet_temperature_hint);
        cabinetTemperatureValueContent = (TextView) findViewById(R.id.cabinet_temperature_content);
        setTemperatureValueHint = (TextView) findViewById(R.id.set_temperature_hint);
        setTemperatureValueContent = (TextView) findViewById(R.id.set_temperature_content);
        refrigerationRunTimeHint = (TextView) findViewById(R.id.refrigeration_run_time_hint);
        refrigerationRunTimeContent = (TextView) findViewById(R.id.refrigeration_run_time_content);
        defrostRunTimeHint = (TextView) findViewById(R.id.defrost_run_time_hint);
        defrostRunTimeContent = (TextView) findViewById(R.id.defrost_run_time_content);
    }

    private void initViewSize() {
        RelativeLayout.LayoutParams param1 = (RelativeLayout.LayoutParams) paramScroll.getLayoutParams();
        param1.width = width / 8 * 7;
        param1.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        paramScroll.setLayoutParams(param1);

        RelativeLayout.LayoutParams param2 = (RelativeLayout.LayoutParams) doorStateHint.getLayoutParams();
        param2.width = width / 10 * 3;
        param2.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        doorStateHint.setLayoutParams(param2);

        RelativeLayout.LayoutParams param3 = (RelativeLayout.LayoutParams) dropDetectionStateHint.getLayoutParams();
        param3.width = width / 10 * 3;
        param3.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        dropDetectionStateHint.setLayoutParams(param3);

        RelativeLayout.LayoutParams param4 = (RelativeLayout.LayoutParams) temperatureControlStateHint.getLayoutParams();
        param4.width = width / 10 * 3;
        param4.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        temperatureControlStateHint.setLayoutParams(param4);

        RelativeLayout.LayoutParams param5 = (RelativeLayout.LayoutParams) refrigerationWorkStateHint.getLayoutParams();
        param5.width = width / 10 * 3;
        param5.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        refrigerationWorkStateHint.setLayoutParams(param5);

        RelativeLayout.LayoutParams param6 = (RelativeLayout.LayoutParams) compressorWorkStateHint.getLayoutParams();
        param6.width = width / 10 * 3;
        param6.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        compressorWorkStateHint.setLayoutParams(param6);

        RelativeLayout.LayoutParams param7 = (RelativeLayout.LayoutParams) evaporationFanWorkStateHint.getLayoutParams();
        param7.width = width / 10 * 3;
        param7.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        evaporationFanWorkStateHint.setLayoutParams(param7);

        RelativeLayout.LayoutParams param8 = (RelativeLayout.LayoutParams) lightStateHint.getLayoutParams();
        param8.width = width / 10 * 3;
        param8.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        lightStateHint.setLayoutParams(param8);

        RelativeLayout.LayoutParams param9 = (RelativeLayout.LayoutParams) glassHeatingStateHint.getLayoutParams();
        param9.width = width / 10 * 3;
        param9.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        glassHeatingStateHint.setLayoutParams(param9);

        RelativeLayout.LayoutParams param10 = (RelativeLayout.LayoutParams) cabinetTemperatureValueHint.getLayoutParams();
        param10.width = width / 10 * 3;
        param10.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        cabinetTemperatureValueHint.setLayoutParams(param10);

        RelativeLayout.LayoutParams param11 = (RelativeLayout.LayoutParams) setTemperatureValueHint.getLayoutParams();
        param11.width = width / 10 * 3;
        param11.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        setTemperatureValueHint.setLayoutParams(param11);

        RelativeLayout.LayoutParams param12 = (RelativeLayout.LayoutParams) refrigerationRunTimeHint.getLayoutParams();
        param12.width = width / 10 * 3;
        param12.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        refrigerationRunTimeHint.setLayoutParams(param12);

        RelativeLayout.LayoutParams param13 = (RelativeLayout.LayoutParams) defrostRunTimeHint.getLayoutParams();
        param13.width = width / 10 * 3;
        param13.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        defrostRunTimeHint.setLayoutParams(param13);
    }

    /**
     * set temperature operation
     */
    private void setTemperatureOperation() {
        Log.d(TAG,"setTemperatureOperation");
        String setTemperature = setTemperatureContent.getText().toString().trim();
        String refrigerationValue = refrigerationTimeContent.getText().toString().trim();
        String defrostValue = defrostTimeContent.getText().toString().trim();
        if (TextUtils.isEmpty(setTemperature)) {
            Log.i("aisle control", "set temperature is empty.");
            return;
        }
        TemperatureParam param = new TemperatureParam();
        param.setTemperatureMode(0x00);//refrigeration
        param.setSetTemperature(new BigDecimal(setTemperature).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue());
        param.setRefrigerationStillTime(TextUtils.isEmpty(refrigerationValue) ? 45 : new BigDecimal(refrigerationValue).intValue());
        param.setDefrostRunTime(TextUtils.isEmpty(defrostValue) ? 8 : new BigDecimal(defrostValue).intValue());

        EventMsg event = new EventMsg();
        event.setMsgVariety(EventBusOrderVariety.EVENT_SET_TEMPERATURE_PARAM);
        event.setMsgValue(JSON.toJSONString(param));
        EventBus.getDefault().post(event);//notify set temperature param.
    }

    /**
     * set light state operation
     */
    private void setLightOperation() {
        LightParam param = new LightParam();
        param.setLightControlValue(lightControlButton.isChecked());

        EventMsg event = new EventMsg();
        event.setMsgVariety(EventBusOrderVariety.EVENT_SET_LIGHT_PARAM);
        event.setMsgValue(JSON.toJSONString(param));
        EventBus.getDefault().post(event);//notify set light param.
    }

    /**
     * back operation
     */
    private void backOperation() {
        StateActivity.this.finish();
    }

    /**
     * show current the state of machine
     *
     * @param data the value of data
     */
    @SuppressLint("SetTextI18n")
    private void showState(String data) {
        if (TextUtils.isEmpty(data))
            return;
        MachineState state = JSON.parseObject(data, MachineState.class);
        if (state == null)
            return;
        doorStateContent.setText(state.isDoorState() ? "Door Open" : "Door Close");
        dropDetectionStateContent.setText(state.isSaleDetectionState() ? "Normal" : "Abnormal");
        temperatureControlStateContent.setText(state.isTemperatureControlState() ? "Temperature Normal" : "Temperature Abnormal");
        refrigerationWorkStateContent.setText(state.isRefrigerationWorkState() ? "Refrigeration Open" : "Refrigeration Close");
        compressWorkStateContent.setText(state.isCompressorWorkState() ? "Compressor Open" : "Compressor Close");
        evaporationFanWorkStateContent.setText(state.isEvaporationFanWorkState() ? "Evaporation Fan Open" : "Evaporation Fan Close");
        lightStateContent.setText(state.isLightState() ? "Light Open" : "Light Close");
        glassHeatingStateContent.setText(state.isGlassHeatingState() ? "Glass Heating Open" : "Glass Heating Close");
        cabinetTemperatureValueContent.setText(BigDecimal.valueOf(state.getCabinetTemperature()).setScale(0, BigDecimal.ROUND_HALF_UP).toString() + getString(R.string.degree_celsius));
        setTemperatureValueContent.setText(BigDecimal.valueOf(state.getSetTemperature()).setScale(0, BigDecimal.ROUND_HALF_UP).toString() + getString(R.string.degree_celsius));
        refrigerationRunTimeContent.setText(BigDecimal.valueOf(state.getSetRefrigerationStillRunTime()).setScale(0, BigDecimal.ROUND_HALF_UP).toString() + "Min");
        defrostRunTimeContent.setText(BigDecimal.valueOf(state.getSetDefrostStillRunTime()).setScale(0, BigDecimal.ROUND_HALF_UP).toString() + "Min");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_temperature_button:
                //set temperature
                setTemperatureOperation();
                break;
            case R.id.set_light_button:
                //set light
                setLightOperation();
                break;
            case R.id.back:
                //back operation
                backOperation();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.light_control_button:
                //switch light state
                if (isChecked)
                    lightControlState.setText("Open");
                else
                    lightControlState.setText("Close");
                break;
            default:
                break;
        }
    }

    class SerialDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String variety = intent.getStringExtra("order_response_variety");
                String value = intent.getStringExtra("order_response_value");
                if (!TextUtils.isEmpty(variety) && !TextUtils.isEmpty(value)) {
                    if (variety.equals(OrderVariety.MACHINE_STATE)) {
                        //反馈当前机器状态
                        if (!value.equals(saveStateValue)) {
                            //当前保存值不一样
                            saveStateValue = value;
                            Message msg = mHandler.obtainMessage();
                            msg.what = MSG_SHOW_STATE;
                            msg.obj = value;
                            mHandler.sendMessage(msg);
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("HandlerLeak")
    class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_SHOW_STATE) {
                //显示机器状态信息
                showState((String) msg.obj);
            }
        }
    }
}