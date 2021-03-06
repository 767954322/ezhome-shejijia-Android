package com.autodesk.shejijia.consumer.personalcenter.consumer.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.autodesk.shejijia.consumer.R;
import com.autodesk.shejijia.consumer.home.decorationlibrarys.entity.CaseLibraryBean;
import com.autodesk.shejijia.consumer.utils.AppJsonFileReader;
import com.autodesk.shejijia.shared.components.common.appglobal.Constant;
import com.autodesk.shejijia.shared.components.common.appglobal.MemberEntity;
import com.autodesk.shejijia.consumer.uielements.viewgraph.PolygonImageView;
import com.autodesk.shejijia.shared.components.common.utility.ImageUtils;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;
import com.autodesk.shejijia.shared.framework.AdskApplication;
import com.autodesk.shejijia.consumer.base.adapter.BaseAdapter;

import java.util.List;
import java.util.Map;

/**
 * @author DongXueQiu .
 * @version 1.0 .
 * @date 16-6-7 上午11:17
 * @file UserHomeCaseAdapter.java  .
 * @brief 首页案例和搜索案例适配器 .
 */
public class UserHomeCaseAdapter extends BaseAdapter<CaseLibraryBean.CasesEntity> {

    public void setOnItemImageHeadClickListener(OnItemImageHeadClickListener mOnItemImageHeadClickListener, OnItemImageHeadClickListener mOnItemCaseClickListener, OnItemImageHeadClickListener OnItemHomechatclickListener) {
        this.mOnItemImageHeadClickListener = mOnItemImageHeadClickListener;
        this.mOnItemCaseClickListener = mOnItemCaseClickListener;
        this.OnItemHomechatclickListener = OnItemHomechatclickListener;
    }

    public UserHomeCaseAdapter(Context context, List<CaseLibraryBean.CasesEntity> datas, Activity activity, int screenWidth, int screenHeight) {
        super(context, datas);
        this.context = context;
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        roomMap = AppJsonFileReader.getRoomHall(activity);
        style = AppJsonFileReader.getStyle(activity);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_lv_consume_home;
    }

    @Override
    public Holder initHolder(View container) {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.ivCase = (ImageView) container.findViewById(R.id.img_customer_home_case);
        viewHolder.llContent = (LinearLayout) container.findViewById(R.id.ll_item_content);
        viewHolder.ivHeadIcon = (PolygonImageView) container.findViewById(R.id.piv_img_customer_home_header);
        viewHolder.tvRoom = (TextView) container.findViewById(R.id.tv_customer_home_room);
        viewHolder.tvStyle = (TextView) container.findViewById(R.id.tv_customer_home_style);
        viewHolder.tvArea = (TextView) container.findViewById(R.id.tv_customer_home_area);
        viewHolder.tvAddress = (TextView) container.findViewById(R.id.iv_consume_home_designer_address);
        viewHolder.tvThumbUp = (TextView) container.findViewById(R.id.tv_thumb_up);
        viewHolder.imgConsumeChat = (ImageView) container.findViewById(R.id.img_consume_home_chat);
        viewHolder.mLine = (TextView) container.findViewById(R.id.view_consume_home_line);
        return viewHolder;
    }

    @Override
    public void initItem(View view, Holder holder, int position) {
        if (null != mDatas && mDatas.size() > 0) {
            CaseLibraryBean.CasesEntity casesEntity = mDatas.get(position);
            ((ViewHolder) holder).tvThumbUp.setText(casesEntity.getFavorite_count() + "");
            List<CaseLibraryBean.CasesEntity.ImagesEntity> images = casesEntity.getImages();
            if (images != null && images.size() > 0) {
                for (int i = 0; i < casesEntity.getImages().size(); i++) {
                    CaseLibraryBean.CasesEntity.ImagesEntity imagesEntity = casesEntity.getImages().get(i);
                    if (imagesEntity.isIs_primary()) {
                        imageOneUrl = imagesEntity.getFile_url();
                    }
                }
                if (TextUtils.isEmpty(imageOneUrl)) {
                    imageOneUrl = casesEntity.getImages().get(0).getFile_url();
                }
                ImageUtils.loadImageIcon(((ViewHolder) holder).ivCase, imageOneUrl + "HD.jpg");
            }
            MemberEntity mMemberEntity = AdskApplication.getInstance().getMemberEntity();
            if (mMemberEntity != null && Constant.UerInfoKey.DESIGNER_TYPE.equals(mMemberEntity.getMember_type())) {
                ((ViewHolder) holder).imgConsumeChat.setVisibility(View.INVISIBLE);
            } else {
                ((ViewHolder) holder).imgConsumeChat.setVisibility(View.VISIBLE);
            }

            if (null != casesEntity) {
                if (casesEntity.getTitle() == null) {
                    ((ViewHolder) holder).tvAddress.setText(UIUtils.getString(R.string.str_others));
                } else {
                    ((ViewHolder) holder).tvAddress.setText(casesEntity.getTitle());
                }
                if (casesEntity.getRoom_area() == null) {
                    ((ViewHolder) holder).tvAddress.setText(UIUtils.getString(R.string.str_others));
                } else {
                    ((ViewHolder) holder).tvArea.setText(casesEntity.getRoom_area() + UIUtils.getString(R.string.m2));
                }
                if (casesEntity.getRoom_type() == null) {
                    ((ViewHolder) holder).tvRoom.setText(UIUtils.getString(R.string.str_others));
                } else {
                    String room_type = casesEntity.getRoom_type();
                    if (roomMap.containsKey(room_type)) {
                        ((ViewHolder) holder).tvRoom.setText(roomMap.get(room_type));
                    } else {
                        ((ViewHolder) holder).tvRoom.setText(casesEntity.getRoom_type());
                    }
                }
                if (casesEntity.getProject_style() == null) {
                    ((ViewHolder) holder).tvStyle.setText(UIUtils.getString(R.string.str_others));
                } else {
                    String project_style = casesEntity.getProject_style();
                    if (style.containsKey(project_style)) {
                        ((ViewHolder) holder).tvStyle.setText(style.get(project_style));
                    } else {
                        ((ViewHolder) holder).tvStyle.setText(casesEntity.getProject_style());
                    }
                }
                if (null != casesEntity.getDesigner_info() && !TextUtils.isEmpty(casesEntity.getDesigner_info().getAvatar())) {
                    ImageUtils.displayAvatarImage(casesEntity.getDesigner_info().getAvatar(), ((ViewHolder) holder).ivHeadIcon);
                }else {
                    ImageUtils.displayAvatarImage("", ((ViewHolder) holder).ivHeadIcon);
                }
            }
        } else {
            ((ViewHolder) holder).ivCase.setImageResource(R.drawable.common_case_icon);
            ((ViewHolder) holder).tvAddress.setText(UIUtils.getString(R.string.str_others));
            ((ViewHolder) holder).tvRoom.setText(UIUtils.getString(R.string.str_others));
            ((ViewHolder) holder).tvStyle.setText(UIUtils.getString(R.string.str_others));
            ((ViewHolder) holder).tvArea.setText(UIUtils.getString(R.string.str_others));
        }


        ((ViewHolder) holder).ivHeadIcon.setOnClickListener(new MyOnClickListener(position, ((ViewHolder) holder)));
        ((ViewHolder) holder).llContent.setOnClickListener(new MyOnClickListener(position, ((ViewHolder) holder)));
        ((ViewHolder) holder).imgConsumeChat.setOnClickListener(new MyOnClickListener(position, ((ViewHolder) holder)));
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ((ViewHolder) holder).ivCase.getLayoutParams();
        lp.width = screenWidth;
        lp.height = 197 * screenWidth / 320;
        ((ViewHolder) holder).ivCase.setLayoutParams(lp);

    }

    public class ViewHolder extends BaseAdapter.Holder {
        public ImageView ivCase;
        public LinearLayout llContent;
        public PolygonImageView ivHeadIcon;
        public TextView tvRoom;
        public TextView tvStyle;
        public TextView tvArea;
        public TextView tvAddress;
        public TextView tvThumbUp;
        public ImageView imgConsumeChat;
        public TextView mLine;
    }

    class MyOnClickListener implements View.OnClickListener {
        private int position;
        private ViewHolder holder;

        private MyOnClickListener(int position, ViewHolder holder) {
            this.position = position;
            this.holder = holder;
        }

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.piv_img_customer_home_header:
                    if (mOnItemImageHeadClickListener != null) {
                        mOnItemImageHeadClickListener.OnItemImageHeadClick(position);
                    }
                    break;
                case R.id.ll_item_content:
                    if (mOnItemCaseClickListener != null) {
                        mOnItemCaseClickListener.OnItemCaseClick(position);
                    }
                    break;
                case R.id.img_consume_home_chat:
                    if (mOnItemCaseClickListener != null) {
                        OnItemHomechatclickListener.OnItemHomeChatClick(position);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public interface OnItemImageHeadClickListener {
        void OnItemImageHeadClick(int position);

        void OnItemCaseClick(int position);

        void OnItemHomeChatClick(int position);
    }

    private Map<String, String> roomMap;
    private Map<String, String> style;
    private OnItemImageHeadClickListener mOnItemImageHeadClickListener;
    private OnItemImageHeadClickListener mOnItemCaseClickListener;
    private OnItemImageHeadClickListener OnItemHomechatclickListener;
    private int screenWidth;
    private int screenHeight;
    private Context context;
    private String imageOneUrl;
}

