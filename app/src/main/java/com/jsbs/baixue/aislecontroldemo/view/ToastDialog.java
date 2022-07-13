package com.jsbs.baixue.aislecontroldemo.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jsbs.baixue.aislecontroldemo.R;
import com.jsbs.baixue.aislecontroldemo.util.ScreenUtil;

public class ToastDialog extends Dialog {
    private Context mContext;
    private int width, height;
    private String hintString;
    private TextView hintText;
    private Handler handler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (ToastDialog.this.isShowing()) {
                ToastDialog.this.dismiss();
            }
        }
    };

    public ToastDialog(@NonNull Context context) {
        super(context);
    }

    public ToastDialog(@NonNull Context context, int themeResId, String hintString) {
        super(context, themeResId);
        this.mContext = context;
        this.hintString = hintString;
    }

    protected ToastDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toast_dialog_layout);
        setCanceledOnTouchOutside(false);

        initDialogSize();

        handler = new Handler();
        ScreenUtil util = new ScreenUtil(mContext);
        width = util.getWidth();
        height = util.getHeight();

        hintText = (TextView) findViewById(R.id.toast_hint);

        LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) hintText.getLayoutParams();
        param.width = width / 5 * 4;
        param.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        hintText.setLayoutParams(param);
        hintText.setTextSize(height / 30);
        hintText.setText(hintString);

        handler.postDelayed(runnable, 2000);
    }

    /**
     * 初始化对话框大小
     */
    private void initDialogSize() {
        //全屏处理(获取屏幕参数)
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT; //设置宽度
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT; //设置高度
        getWindow().setAttributes(lp);
    }
}
