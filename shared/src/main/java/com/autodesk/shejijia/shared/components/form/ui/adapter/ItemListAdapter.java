package com.autodesk.shejijia.shared.components.form.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.autodesk.shejijia.shared.R;
import com.autodesk.shejijia.shared.components.form.common.entity.OptionCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by t_aij on 16/12/1.
 */

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemListVH> {
    private List<OptionCell> mOptionCellList = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;

    public ItemListAdapter(List<OptionCell> optionCellList, OnItemClickListener onItemClickListener) {
        mOptionCellList.clear();
        mOptionCellList.addAll(optionCellList);
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public ItemListVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_option_table_cell, parent, false);
        return new ItemListVH(view);
    }

    @Override
    public void onBindViewHolder(ItemListVH holder, int position) {
        OptionCell optionCell = mOptionCellList.get(position);
        holder.mTitleTv.setText(optionCell.getTitle());

        if (optionCell.isShowStandard()) {
            holder.mLineView.setVisibility(View.VISIBLE);
            holder.mStandardTv.setVisibility(View.VISIBLE);
            holder.mActionTypeTv.setVisibility(View.VISIBLE);

            holder.mStandardTv.setText(optionCell.getStandard());
            String actionType = "该项目属于" + getRedText(optionCell.getActionResult()) + "项";
            holder.mActionTypeTv.setText(Html.fromHtml(actionType));
        } else {
            holder.mLineView.setVisibility(View.GONE);
            holder.mStandardTv.setVisibility(View.GONE);
            holder.mActionTypeTv.setVisibility(View.GONE);
        }
// TODO: 16/12/6 错误信息是否显示的问题
        holder.mInformationTv.setVisibility(View.GONE);

        initOptions(holder, optionCell);

        initListener(holder, position, optionCell);

    }

    @Override
    public int getItemCount() {
        return mOptionCellList.size();
    }

    private String getRedText(String string) {
        return String.format("<font color=\"#FF0000\">%s</font>", string);
    }

    private void initOptions(ItemListVH holder, OptionCell optionCell) {

        HashMap<String, List<String>> typeDictMap = optionCell.getTypeDict();  //选择属性
        int checkResult = optionCell.getCheckResult();  //选择

        switch (checkResult) {
            case 0:
                initRight(holder, typeDictMap);
                break;
            case 1:
                initCenter(holder, typeDictMap);
                break;
            case 2:
                initLeft(holder, typeDictMap);
                break;
            default:
                init(holder, typeDictMap);
                break;
        }

        initWord(holder, typeDictMap);

    }

    private void init(ItemListVH holder, HashMap<String, List<String>> typeDictMap) {
        if (typeDictMap.containsKey("acceptance_options")) {
            holder.mLeftLayout.setVisibility(View.VISIBLE);
            holder.mLeftIv.setBackgroundResource(R.drawable.ic_option_none_normal_svg);
            holder.mCenterIv.setBackgroundResource(R.drawable.ic_option_unqualified_normal_svg);
            holder.mRightIv.setBackgroundResource(R.drawable.ic_option_qualified_normal_svg);
        } else if (typeDictMap.containsKey("level_options")) {
            holder.mLeftLayout.setVisibility(View.GONE);
            holder.mCenterIv.setBackgroundResource(R.drawable.ic_option_not_level_normal_svg);
            holder.mRightIv.setBackgroundResource(R.drawable.ic_option_level_normal_svg);
        } else if (typeDictMap.containsKey("notification_options")) {
            holder.mLeftLayout.setVisibility(View.GONE);
            holder.mCenterIv.setBackgroundResource(R.drawable.ic_option_not_notification_normal_svg);
            holder.mRightIv.setBackgroundResource(R.drawable.ic_option_notification_normal_svg);
        }
    }

    private void initWord(ItemListVH holder, HashMap<String, List<String>> typeDictMap) {
        if (typeDictMap.containsKey("acceptance_options")) {
            List<String> list = typeDictMap.get("acceptance_options");
            holder.mRightTv.setText(list.get(0));
            holder.mCenterTv.setText(list.get(1));
            holder.mLeftTv.setText(list.get(2));
        } else if (typeDictMap.containsKey("level_options")) {
            List<String> list = typeDictMap.get("level_options");
            holder.mRightTv.setText(list.get(0));
            holder.mCenterTv.setText(list.get(1));
        } else if (typeDictMap.containsKey("notification_options")) {
            List<String> list = typeDictMap.get("notification_options");
            holder.mRightTv.setText(list.get(0));
            holder.mCenterTv.setText(list.get(1));
        }
    }

    private void initRight(ItemListVH holder, HashMap<String, List<String>> typeDictMap) {
        if (typeDictMap.containsKey("acceptance_options")) {
            holder.mLeftLayout.setVisibility(View.VISIBLE);
            holder.mLeftIv.setBackgroundResource(R.drawable.ic_option_none_normal_svg);
            holder.mCenterIv.setBackgroundResource(R.drawable.ic_option_unqualified_normal_svg);
            holder.mRightIv.setBackgroundResource(R.drawable.ic_option_qualified_check_svg);
        } else if (typeDictMap.containsKey("level_options")) {
            holder.mLeftLayout.setVisibility(View.GONE);
            holder.mCenterIv.setBackgroundResource(R.drawable.ic_option_not_level_normal_svg);
            holder.mRightIv.setBackgroundResource(R.drawable.ic_option_level_check_svg);
        } else if (typeDictMap.containsKey("notification_options")) {
            holder.mLeftLayout.setVisibility(View.GONE);
            holder.mCenterIv.setBackgroundResource(R.drawable.ic_option_not_notification_normal_svg);
            holder.mRightIv.setBackgroundResource(R.drawable.ic_option_notification_check_svg);
        }
    }

    private void initCenter(ItemListVH holder, HashMap<String, List<String>> typeDictMap) {
        if (typeDictMap.containsKey("acceptance_options")) {
            holder.mLeftLayout.setVisibility(View.VISIBLE);
            holder.mLeftIv.setBackgroundResource(R.drawable.ic_option_none_normal_svg);
            holder.mCenterIv.setBackgroundResource(R.drawable.ic_option_unqualified_check_svg);
            holder.mRightIv.setBackgroundResource(R.drawable.ic_option_qualified_normal_svg);
        } else if (typeDictMap.containsKey("level_options")) {
            holder.mLeftLayout.setVisibility(View.GONE);
            holder.mCenterIv.setBackgroundResource(R.drawable.ic_option_not_level_check_svg);
            holder.mRightIv.setBackgroundResource(R.drawable.ic_option_level_normal_svg);
        } else if (typeDictMap.containsKey("notification_options")) {
            holder.mLeftLayout.setVisibility(View.GONE);
            holder.mCenterIv.setBackgroundResource(R.drawable.ic_option_not_notification_check_svg);
            holder.mRightIv.setBackgroundResource(R.drawable.ic_option_notification_normal_svg);
        }
    }

    private void initLeft(ItemListVH holder, HashMap<String, List<String>> typeDictMap) {
        if (typeDictMap.containsKey("acceptance_options")) {
            holder.mLeftLayout.setVisibility(View.VISIBLE);
            holder.mLeftIv.setBackgroundResource(R.drawable.ic_option_none_check_svg);
            holder.mCenterIv.setBackgroundResource(R.drawable.ic_option_unqualified_normal_svg);
            holder.mRightIv.setBackgroundResource(R.drawable.ic_option_qualified_normal_svg);
        }
    }

    private void initListener(final ItemListVH holder, final int position, OptionCell optionCell) {
        if (mOnItemClickListener != null) {
            final HashMap<String, List<String>> typeDictMap = optionCell.getTypeDict();  //选择属性

            holder.mLeftIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onLeftItemClick(2, position);
                    initLeft(holder, typeDictMap);
                }
            });
            holder.mCenterIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onCenterItemClick(1, position);
                    initCenter(holder, typeDictMap);
                }
            });
            holder.mRightIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onRightItemClick(0, position);
                    initRight(holder, typeDictMap);
                }
            });


        }
    }

    static class ItemListVH extends RecyclerView.ViewHolder {

        private TextView mTitleTv;
        private TextView mStandardTv;
        private TextView mActionTypeTv;
        private LinearLayout mLeftLayout;   //左边选项的布局
        private ImageView mLeftIv;
        private TextView mLeftTv;
        private ImageView mCenterIv;
        private TextView mCenterTv;
        private ImageView mRightIv;
        private TextView mRightTv;
        private View mLineView;            //分隔线
        private TextView mInformationTv;  //显示错误信息的表示

        ItemListVH(View itemView) {
            super(itemView);
            mTitleTv = (TextView) itemView.findViewById(R.id.tv_title);
            mStandardTv = (TextView) itemView.findViewById(R.id.tv_standard);
            mActionTypeTv = (TextView) itemView.findViewById(R.id.tv_action_type);

            mLeftLayout = (LinearLayout) itemView.findViewById(R.id.ll_left);
            mLineView = itemView.findViewById(R.id.view_line);
            mInformationTv = (TextView) itemView.findViewById(R.id.tv_information);

            mLeftIv = (ImageView) itemView.findViewById(R.id.iv_left);
            mLeftTv = (TextView) itemView.findViewById(R.id.tv_left);

            mCenterIv = (ImageView) itemView.findViewById(R.id.iv_center);
            mCenterTv = (TextView) itemView.findViewById(R.id.tv_center);

            mRightIv = (ImageView) itemView.findViewById(R.id.iv_right);
            mRightTv = (TextView) itemView.findViewById(R.id.tv_right);
        }
    }

    public interface OnItemClickListener {

        void onLeftItemClick(int type, int position);

        void onCenterItemClick(int type, int position);

        void onRightItemClick(int type, int position);

    }
}
