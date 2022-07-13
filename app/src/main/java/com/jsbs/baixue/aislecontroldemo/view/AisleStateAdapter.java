package com.jsbs.baixue.aislecontroldemo.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jsbs.baixue.aislecontroldemo.R;
import com.jsbs.baixue.aislecontroldemo.constant.AisleStateConstant;
import com.jsbs.baixue.aislecontroldemo.mode.AisleStateInfo;

import java.util.ArrayList;
import java.util.List;

public class AisleStateAdapter extends RecyclerView.Adapter<AisleStateAdapter.BodyViewHolder> {
    private Context mContext;
    private int width, height;
    //传入数据
    private List<AisleStateInfo> aisleStateInfo;

    private OnItemClickerListener listener;

    public void SetOnItemClickerListener(OnItemClickerListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickerListener {
        void onItemClick(View v, int position);
    }

    public AisleStateAdapter(Context context, int width, int height, List<AisleStateInfo> infoList) {
        this.mContext = context;
        this.width = width;
        this.height = height;
        this.aisleStateInfo = new ArrayList<>();
        if (infoList != null && infoList.size() > 0) {
            this.aisleStateInfo = infoList;
        }
    }

    @NonNull
    @Override
    public BodyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.aisle_state_item_adapter_layout, null);
        v.setLayoutParams(new ViewGroup.LayoutParams(width, height));

        return new BodyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final BodyViewHolder holder, int position) {
        if (mContext != null && aisleStateInfo != null && aisleStateInfo.size() > 0) {
            AisleStateInfo param = aisleStateInfo.get(holder.getAdapterPosition());
            holder.getAisleNum().setText(mContext.getString(R.string.aisle_adapter_aisle) + param.getAisleNum());
            holder.getAisleStateValue().setText(getAisleValue(mContext, param.getAisleState()));
            if (param.getAisleState() == 0) {
                holder.getItemLayout().setBackgroundResource(R.color.green);
            } else {
                holder.getItemLayout().setBackgroundResource(R.color.red);
            }
            holder.getItemLayout().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(v, holder.getAdapterPosition());
                    }
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BodyViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            //局部刷新
            String tmpPayload = (String) payloads.get(0);
            if (!TextUtils.isEmpty(tmpPayload) && tmpPayload.equals("update")) {
                AisleStateInfo param = aisleStateInfo.get(holder.getAdapterPosition());
                holder.getAisleStateValue().setText(getAisleValue(mContext, param.getAisleState()));
                if (param.getAisleState() == 0) {
                    holder.getItemLayout().setBackgroundResource(R.color.green);
                } else {
                    holder.getItemLayout().setBackgroundResource(R.color.red);
                }
            } else if (!TextUtils.isEmpty(tmpPayload) && tmpPayload.equals("running")) {
                holder.getAisleStateValue().setText(mContext.getString(R.string.aisle_run));
                holder.getItemLayout().setBackgroundResource(R.color.orange);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (aisleStateInfo != null && aisleStateInfo.size() > 0) {
            return aisleStateInfo.size();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    /**
     * 更新当前货道状态信息
     *
     * @param state 状态
     */
    public void updateAisleState(AisleStateInfo state) {
        if (aisleStateInfo != null && aisleStateInfo.size() > 0 && state != null) {
            int position = -1;
            int i = 0;
            for (i = 0; i < aisleStateInfo.size(); i++) {
                if (state.getAisleNum().equals(aisleStateInfo.get(i).getAisleNum())) {
                    position = i;
                    aisleStateInfo.set(i, state);
                    break;
                }
            }

            if (position >= 0)
                notifyItemChanged(position, "update");
        }
    }

    /**
     * 更新测试货道提示
     *
     * @param aisleNum 测试货道信息
     */
    public void updateAisleTesting(String aisleNum) {
        if (aisleStateInfo != null && aisleStateInfo.size() > 0 && !TextUtils.isEmpty(aisleNum)) {
            int position = -1;
            int i = 0;
            for (i = 0; i < aisleStateInfo.size(); i++) {
                if (aisleNum.equals(aisleStateInfo.get(i).getAisleNum())) {
                    position = i;
                    break;
                }
            }

            if (position >= 0)
                notifyItemChanged(position, "running");//提示测试中
        }
    }

    /**
     * 清空所有数据
     */
    public void emptyItem() {
        aisleStateInfo = new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * 获取货道状态值
     *
     * @param context   容器
     * @param stateCode 状态编码
     * @return 状态值
     */
    public static String getAisleValue(Context context, int stateCode) {
        String result = context.getString(R.string.aisle_normal);
        if (stateCode == AisleStateConstant.AISLE_NORMAL) {
            result = context.getString(R.string.aisle_normal);
        } else if (stateCode == AisleStateConstant.AISLE_TIME_OUT) {
            result = context.getString(R.string.aisle_time_out);
        } else if (stateCode == AisleStateConstant.AISLE_BLOCK) {
            result = context.getString(R.string.aisle_block);
        } else if (stateCode == AisleStateConstant.AISLE_EMPTY) {
            result = context.getString(R.string.aisle_empty);
        } else if (stateCode == AisleStateConstant.AISLE_CONNECT_LOST) {
            result = context.getString(R.string.aisle_connect_lost);
        }
        return result;
    }

    /**
     * 设置控件大小
     *
     * @param aisleNum   货道名称
     * @param aisleState 货道状态
     */
    private void setViewSize(TextView aisleNum, TextView aisleState) {
        RelativeLayout.LayoutParams param1 = (RelativeLayout.LayoutParams) aisleNum.getLayoutParams();
        param1.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        param1.height = height / 2 - 5;
        param1.setMargins(0, 10, 0, 0);
        aisleNum.setLayoutParams(param1);

        RelativeLayout.LayoutParams param2 = (RelativeLayout.LayoutParams) aisleState.getLayoutParams();
        param2.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        param2.height = height / 2 - 5;
        param2.setMargins(0, 0, 0, 10);
        aisleState.setLayoutParams(param2);
    }

    public class BodyViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout itemLayout;
        TextView aisleNum, aisleStateValue;

        public BodyViewHolder(View itemView) {
            super(itemView);
            //初始化控件
            itemLayout = (RelativeLayout) itemView.findViewById(R.id.aisle_state_item_layout);
            aisleNum = (TextView) itemView.findViewById(R.id.aisle_state_name);
            aisleStateValue = (TextView) itemView.findViewById(R.id.aisle_state_value);

            LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) itemLayout.getLayoutParams();
            param.width = width - 10;
            param.height = height - 10;
            itemLayout.setLayoutParams(param);
            setViewSize(aisleNum, aisleStateValue);
        }

        public RelativeLayout getItemLayout() {
            return itemLayout;
        }

        public TextView getAisleNum() {
            return aisleNum;
        }

        public TextView getAisleStateValue() {
            return aisleStateValue;
        }
    }
}
