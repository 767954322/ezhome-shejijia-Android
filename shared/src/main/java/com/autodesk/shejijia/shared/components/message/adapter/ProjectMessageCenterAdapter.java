package com.autodesk.shejijia.shared.components.message.adapter;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.autodesk.shejijia.shared.R;
import com.autodesk.shejijia.shared.components.common.utility.ImageUtils;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;
import com.autodesk.shejijia.shared.components.message.entity.MessageInfo;

import java.util.List;

/**
 * Created by luchongbin on 2016/12/5.
 */

public class ProjectMessageCenterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<MessageInfo.DataBean> mData;
    private boolean isUnrea;
    private static HistoricalRecordstListener mHistoricalRecordstListener;
    private int mBottomCount=1;//底部View个数
    public static final int ITEM_TYPE_CONTENT = 0;
    public static final int ITEM_TYPE_BOTTOM = 1;
    private int resId;
    public ProjectMessageCenterAdapter(List<MessageInfo.DataBean> mData,boolean isUnrea,int resId) {
        super();
        this.mData = mData;
        this.resId = resId;
        this.isUnrea = isUnrea;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == ITEM_TYPE_CONTENT){
            View view = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
            return new ProjectMessageCenterVH(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_foot_text, parent, false);
            return new BottomViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProjectMessageCenterAdapter.ProjectMessageCenterVH) {
            initData((ProjectMessageCenterVH)holder,position);
        } else{
            BottomViewHolder mBottomViewHolder = (BottomViewHolder)holder;
            String mHistoricalRecords = UIUtils.getString(R.string.historical_records);
            mBottomViewHolder.mTvMessageHistoricalRecords.setText(Html.fromHtml("<u>"+mHistoricalRecords+"</u>"));
            initListener(mBottomViewHolder);
            mBottomViewHolder.mTvMessageHistoricalRecords.setVisibility(isUnrea?View.VISIBLE:View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return getContentItemCount()+mBottomCount;
    }
    @Override
    public int getItemViewType(int position){
        int dataItemCount = getContentItemCount();
        if (mBottomCount != 0 && position >= dataItemCount){
            return ITEM_TYPE_BOTTOM;
        }else{
            return ITEM_TYPE_CONTENT;
        }
    }
    public int getContentItemCount(){
        return mData != null?mData.size():0;
    }
    private void initListener(BottomViewHolder mBottomViewHolder){
        mBottomViewHolder.mTvMessageHistoricalRecords.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mHistoricalRecordstListener.onHistoricalRecordstClick();
            }
        });
    }
    private void initData(ProjectMessageCenterVH holder,int position){
        String avatar = mData.get(position).getSender_avatar();
        if (TextUtils.isEmpty(avatar)) {
            holder.mImgBtnPersonalHeadPic.setImageDrawable(UIUtils.getDrawable(R.drawable.icon_default_avator));
        } else {
            ImageUtils.loadImageRound(holder.mImgBtnPersonalHeadPic, avatar);
        }

        MessageInfo.DataBean.DisplayMessageBean display_message = mData.get(position).getDisplay_message();
        if (display_message != null && !TextUtils.isEmpty(display_message.getSummary())) {
            String title = display_message.getSummary();
            for (int i = 0; title.contains("*"); i++) {
                title = (i % 2 == 0) ? title.replaceFirst("\\*", "<b><tt>") : title.replaceFirst("\\*", "</b></tt>");
            }
            holder.mTvMeaasgeName.setText(Html.fromHtml(title));
        }
        List<String> detail_items = display_message.getDetail_items();
        if (detail_items != null ) {
            String detail_item="";
            for(String str:detail_items){
                detail_item += "\n"+str;
            }
            detail_item = detail_item.substring(2);
            holder.mTvMeaasgeCantent.setText(detail_item);
        }
        if (!TextUtils.isEmpty(mData.get(position).getSent_time())) {
            holder.mTvMessageTime.setText(mData.get(position).getSent_time());
        }
    }//底部 ViewHolder
    public static class BottomViewHolder extends RecyclerView.ViewHolder {
        private TextView  mTvMessageHistoricalRecords;

        public BottomViewHolder(View itemView) {
            super(itemView);
            mTvMessageHistoricalRecords = (TextView) itemView.findViewById(R.id.tv_message_historical_records);
        }
    }

     public static class ProjectMessageCenterVH extends RecyclerView.ViewHolder {
        private ImageView mImgBtnPersonalHeadPic;
        private TextView  mTvMeaasgeName;
        private TextView  mTvMeaasgeCantent;
        private TextView  mTvMessageTime;

        ProjectMessageCenterVH(View itemView) {
            super(itemView);
            mImgBtnPersonalHeadPic = (ImageView) itemView.findViewById(R.id.imgBtn_personal_headPic);
            mTvMeaasgeName = (TextView) itemView.findViewById(R.id.tv_meaasge_name);
            mTvMeaasgeCantent = (TextView) itemView.findViewById(R.id.tv_meaasge_cantent);
            mTvMessageTime = (TextView) itemView.findViewById(R.id.tv_message_time);
        }
    }
    public static void setHistoricalRecordstListener(HistoricalRecordstListener hr){
        mHistoricalRecordstListener = hr;
    }
    public interface HistoricalRecordstListener {
        void onHistoricalRecordstClick();
    }

}
