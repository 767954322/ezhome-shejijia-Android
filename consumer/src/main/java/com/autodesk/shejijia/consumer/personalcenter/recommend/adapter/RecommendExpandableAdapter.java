package com.autodesk.shejijia.consumer.personalcenter.recommend.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.autodesk.shejijia.consumer.R;
import com.autodesk.shejijia.consumer.personalcenter.recommend.entity.RecommendBrandsBean;
import com.autodesk.shejijia.consumer.personalcenter.recommend.entity.RecommendMallsBean;
import com.autodesk.shejijia.consumer.personalcenter.recommend.entity.RecommendSCFDBean;
import com.autodesk.shejijia.consumer.personalcenter.recommend.view.CustomHeaderExpandableListView;
import com.autodesk.shejijia.consumer.personalcenter.recommend.view.customspinner.MaterialSpinner;
import com.autodesk.shejijia.consumer.personalcenter.recommend.widget.BrandChangListener;
import com.autodesk.shejijia.consumer.personalcenter.recommend.widget.ExpandListHeaderInterface;
import com.autodesk.shejijia.shared.components.common.uielements.alertview.AlertView;
import com.autodesk.shejijia.shared.components.common.utility.StringUtils;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;

import java.util.Arrays;
import java.util.List;

public class RecommendExpandableAdapter extends BaseExpandableListAdapter implements ExpandListHeaderInterface {


    private static final String ALL_NUM = "^[0-9]{1,4}+(.[0-9]{1,2})?$";// 是否为合法数量（1-9999 ）．
    private static final String NUM_AND_CHINESE = "^([0-9]{1,4}+(.[0-9]{1,2})?)[\u4e00-\u9fa5]{1,2}$";// （1-9999 + 2个汉字）．

    private ViewGroupHolder viewGroupHolder;
    private ViewHolder mViewHolder;
    private LayoutInflater inflater;
    private List<RecommendSCFDBean> mRecommendSCFDList;
    private CustomHeaderExpandableListView listView;
    private Activity mActivity;
    private BrandChangListener mBrandChangListener;
    private int mTouchItemPosition = -1;
    private SparseIntArray groupStatusMap = new SparseIntArray();

    public RecommendExpandableAdapter(Activity activity, List<RecommendSCFDBean> recommendSCFDList, CustomHeaderExpandableListView recyclerViewList) {
        this.mActivity = activity;
        this.mRecommendSCFDList = recommendSCFDList;
        this.listView = recyclerViewList;
        inflater = LayoutInflater.from(this.mActivity);
    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (mRecommendSCFDList.size() > 0 && mRecommendSCFDList.get(groupPosition).getBrands() != null) {
            return mRecommendSCFDList.get(groupPosition).getBrands().get(childPosition);
        }
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, final ViewGroup parent) {
        View view = null;
        if (convertView != null) {
            view = convertView;
            mViewHolder = (ViewHolder) convertView.getTag();
            //动态更新TextWathcer的position
            mViewHolder.updatePosition(groupPosition, childPosition);
        } else {
            view = createChildrenView();
            mViewHolder = new ViewHolder();
            mViewHolder.etBrandNum = (EditText) view.findViewById(R.id.et_brand_num);
            mViewHolder.etBranDimension = (EditText) view.findViewById(R.id.et_brand_dimension);    // 规格．
            mViewHolder.etBranRemarks = (EditText) view.findViewById(R.id.et_brand_remarks);
            mViewHolder.tvBrandName = (TextView) view.findViewById(R.id.tv_brand_name);
            mViewHolder.tvBrandMallName = (TextView) view.findViewById(R.id.tv_brand_mall_name);
            mViewHolder.spinnerApartment = (MaterialSpinner) view.findViewById(R.id.spinner_brand_apartment);
            mViewHolder.rlRecommendFooter = (RelativeLayout) view.findViewById(R.id.rl_recommend_footer);
            mViewHolder.tvBrandChange = (TextView) view.findViewById(R.id.tv_brand_change);
            mViewHolder.tvBrandAdd = (TextView) view.findViewById(R.id.tv_create_brand);
            mViewHolder.tvCategoryDelete = (TextView) view.findViewById(R.id.tv_delete_brand);
            mViewHolder.tvBrandDelete = (TextView) view.findViewById(R.id.tv_brand_delete);

            setOnTouchListenerForEditText(mViewHolder.etBrandNum, R.id.et_brand_num);
            setOnTouchListenerForEditText(mViewHolder.etBranDimension, R.id.et_brand_dimension);
            setOnTouchListenerForEditText(mViewHolder.etBranRemarks, R.id.et_brand_remarks);

            mViewHolder.mTextWatcherNum = new ExpandListTextWatcher(0, mViewHolder.etBrandNum);
            mViewHolder.mTextWatcherDimension = new ExpandListTextWatcher(1, mViewHolder.etBranDimension);
            mViewHolder.mTextWatcherRemarks = new ExpandListTextWatcher(2, mViewHolder.etBranRemarks);

            mViewHolder.etBrandNum.addTextChangedListener(mViewHolder.mTextWatcherNum);
            mViewHolder.etBranDimension.addTextChangedListener(mViewHolder.mTextWatcherDimension);
            mViewHolder.etBranRemarks.addTextChangedListener(mViewHolder.mTextWatcherRemarks);

            mViewHolder.updatePosition(groupPosition, childPosition);

            view.setTag(mViewHolder);
        }

        if (mRecommendSCFDList.size() > 0 && childPosition == mRecommendSCFDList.get(groupPosition).getBrands().size() - 1) {
            mViewHolder.rlRecommendFooter.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.rlRecommendFooter.setVisibility(View.GONE);
        }
        if (mRecommendSCFDList.get(groupPosition).getBrands().size() < mRecommendSCFDList.get(groupPosition).getBrand_count_limit()) {
            mViewHolder.tvBrandAdd.setEnabled(true);
            mViewHolder.tvBrandAdd.setBackgroundResource(R.drawable.recommend_btn_bag_style);
            mViewHolder.tvBrandAdd.setTextColor(UIUtils.getColor(R.color.color_blue_0084ff));
        } else {
            mViewHolder.tvBrandAdd.setEnabled(false);
            mViewHolder.tvBrandAdd.setBackgroundResource(R.drawable.bg_btn_filtrate_normal);
            mViewHolder.tvBrandAdd.setTextColor(UIUtils.getColor(R.color.comment_gray));
        }

        final RecommendBrandsBean recommendBrandsBean = mRecommendSCFDList.get(groupPosition).getBrands().get(childPosition);

        // 数量,规格,备注．
        String amountAndUnit = recommendBrandsBean.getAmountAndUnit();
        String dimension = recommendBrandsBean.getDimension();
        String remarks = recommendBrandsBean.getRemarks();

        mViewHolder.etBrandNum.setText(UIUtils.getNoStringIfEmpty(amountAndUnit));
        mViewHolder.etBrandNum.setTag(groupPosition * 100 + childPosition * 10 + 1);

        mViewHolder.etBranDimension.setText(UIUtils.getNoStringIfEmpty(dimension));
        mViewHolder.etBranDimension.setTag(groupPosition * 100 + childPosition * 10 + 2);

        mViewHolder.etBranRemarks.setText(UIUtils.getNoStringIfEmpty(remarks));
        mViewHolder.etBranRemarks.setTag(groupPosition * 100 + childPosition * 10 + 3);

        if ((mTouchItemPosition / 100 == groupPosition) && (mTouchItemPosition / 10 == childPosition) && (mTouchItemPosition % 10 == 1)) {
            mViewHolder.etBrandNum.requestFocus();
            mViewHolder.etBrandNum.setSelection(mViewHolder.etBrandNum.getText().length());
        } else if ((mTouchItemPosition / 100 == groupPosition) && (mTouchItemPosition / 10 == childPosition) && (mTouchItemPosition % 10 == 2)) {
            mViewHolder.etBranDimension.requestFocus();
            mViewHolder.etBranDimension.setSelection(mViewHolder.etBranDimension.getText().length());
        } else if ((mTouchItemPosition / 100 == groupPosition) && (mTouchItemPosition / 10 == childPosition) && (mTouchItemPosition % 10 == 3)) {
            mViewHolder.etBranRemarks.requestFocus();
            mViewHolder.etBranRemarks.setSelection(mViewHolder.etBranRemarks.getText().length());
        } else {
            mViewHolder.etBrandNum.clearFocus();
            mViewHolder.etBranDimension.clearFocus();
            mViewHolder.etBranRemarks.clearFocus();
        }

        // 店铺地址．
        StringBuffer mallName = new StringBuffer();
        for (RecommendMallsBean mallsBean : recommendBrandsBean.getMalls()) {
            mallName.append(mallsBean.getMall_name() + "、");
        }
        mViewHolder.tvBrandMallName.setText(mallName.substring(0, mallName.length() - 1));
        // 品牌名称．
        mViewHolder.tvBrandName.setText(recommendBrandsBean.getName());
        // 空间．
        String[] apartmentArray = UIUtils.getStringArray(R.array.recommend_apartments);

        final List<String> apartmentList = Arrays.asList(apartmentArray);
        mViewHolder.spinnerApartment.setItems(apartmentList);

        mViewHolder.spinnerApartment.setSelectedIndex(StringUtils.isEmpty(recommendBrandsBean.getApartment())
                ? 0 : Integer.valueOf(recommendBrandsBean.getApartment()));
        boolean falg = mViewHolder.spinnerApartment.getText().toString().equals(UIUtils.getString(R.string.select));
        mViewHolder.spinnerApartment.setTextColor(falg ? mActivity.getResources().getColor(R.color.bg_99) : Color.BLACK);

        final int currentParentPosition = groupPosition;
        final int currentChildPosition = childPosition;
        mViewHolder.spinnerApartment.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                hideSoftKeywords(mViewHolder.spinnerApartment);
                onClearFocus(mViewHolder.etBrandNum, mViewHolder.etBranDimension, mViewHolder.etBranDimension);
                boolean falg = (position == 0) ? true : false;
                view.setTextColor(falg ? mActivity.getResources().getColor(R.color.bg_99) : Color.BLACK);
                String index = "";
                if (position != 0) {
                    index = position < 10 ? "0" + position : position + "";
                }
                mRecommendSCFDList.get(currentParentPosition).getBrands().get(currentChildPosition).setApartment(index);
            }
        });


        mViewHolder.tvBrandChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mBrandChangListener) {
                    mBrandChangListener.onBrandChangListener(mRecommendSCFDList.get(currentParentPosition), recommendBrandsBean);
                }
            }
        });

        mViewHolder.tvBrandAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mBrandChangListener) {
                    mBrandChangListener.onBrandAddListener(mRecommendSCFDList.get(currentParentPosition));
                }
            }
        });

        mViewHolder.tvCategoryDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mBrandChangListener) {
                    mBrandChangListener.onSubCategoryDeleteListener(currentParentPosition);
                }
            }
        });

        mViewHolder.tvBrandDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mBrandChangListener) {
                    mBrandChangListener.onBrandDeleteListener(currentParentPosition, currentChildPosition);
                }
            }
        });
        return view;
    }

    private void onClearFocus(EditText... editview) {
        for (EditText v : editview) {
            if (v.hasFocus()) {
                v.clearFocus();
            }
        }
    }

//    private void setSpinnerTextColor(MaterialSpinner spinnerApartment) {
//        boolean falg = spinnerApartment.getText().toString().equals(UIUtils.getString(R.string.select));
//        spinnerApartment.setTextColor(falg ? mActivity.getResources().getColor(R.color.bg_99) : Color.BLACK);
//    }

    public void setBrandChangListener(BrandChangListener brandChangListener) {
        mBrandChangListener = brandChangListener;
    }

    private boolean canVerticalScroll(EditText editText) {
        //滚动的距离
        int scrollY = editText.getScrollY();
        //控件内容的总高度
        int scrollRange = editText.getLayout().getHeight();
        //控件实际显示的高度
        int scrollExtent = editText.getHeight() - editText.getCompoundPaddingTop() - editText.getCompoundPaddingBottom();
        //控件内容总高度与实际显示高度的差值
        int scrollDifference = scrollRange - scrollExtent;

        if (scrollDifference == 0) {
            return false;
        }
        return (scrollY > 0) || (scrollY < scrollDifference - 1);
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        if (mRecommendSCFDList.size() > 0 && mRecommendSCFDList.get(groupPosition).getBrands() != null) {
            return mRecommendSCFDList.get(groupPosition).getBrands().size();
        }
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mRecommendSCFDList.size() <= 0 ? null : mRecommendSCFDList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return null == mRecommendSCFDList ? 0 : (mRecommendSCFDList.size());
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        View view = null;
        if (convertView != null) {
            view = convertView;
        } else {
            view = createGroupView();
            viewGroupHolder = new ViewGroupHolder();
        }
        // isExpanded 判断是否展开．
//        if (isExpanded) {
//            iv.setImageResource(R.drawable.btn_browser2);
//        } else {
//            iv.setImageResource(R.drawable.btn_browser);
//        }
        TextView text = (TextView) view.findViewById(R.id.tv_category_name);
        text.setText(mRecommendSCFDList.get(groupPosition).getSub_category_3d_name());
        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private View createChildrenView() {
        return inflater.inflate(R.layout.item_brand_edit_view, null);
    }

    private View createGroupView() {
        return inflater.inflate(R.layout.item_group_indicator, null);
    }

    @Override
    public int getHeaderState(int groupPosition, int childPosition) {
        final int childCount = getChildrenCount(groupPosition);
        // TODO 设置显示悬浮重叠效果．
//        if (childPosition == childCount - 1) {
//            return PINNED_HEADER_PUSHED_UP;
//        } else if (childPosition == -1
//                && !listView.isGroupExpanded(groupPosition)) {
        return PINNED_HEADER_GONE;
//        } else {
//            return PINNED_HEADER_VISIBLE;
//        }
    }

    @Override
    public void configureHeader(View header, int groupPosition,
                                int childPosition, int alpha) {
        String groupData = this.mRecommendSCFDList.get(groupPosition).getSub_category_3d_name();
        ((TextView) header.findViewById(R.id.tv_category_name)).setText(groupData);
    }

    @Override
    public void setGroupClickStatus(int groupPosition, int status) {
        groupStatusMap.put(groupPosition, status);
    }

    @Override
    public int getGroupClickStatus(int groupPosition) {
        if (groupStatusMap.keyAt(groupPosition) >= 0) {
            return groupStatusMap.get(groupPosition);
        } else {
            return 0;
        }
    }

    public void setOnTouchListenerForEditText(EditText editText, final int id) {
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                //注意，此处必须使用getTag的方式，不能将position定义为final，写成mTouchItemPosition = position
                mTouchItemPosition = (Integer) view.getTag();
                //触摸的是EditText并且当前EditText可以滚动则将事件交给EditText处理；否则将事件交由其父类处理
                if ((view.getId() == id && canVerticalScroll((EditText) view))) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
                return false;
            }
        });
    }

    static class ViewGroupHolder {
    }

    static class ViewHolder {
        EditText etBrandNum;
        EditText etBranDimension;
        EditText etBranRemarks;
        TextView tvBrandName;
        TextView tvBrandMallName;
        MaterialSpinner spinnerApartment;
        RelativeLayout rlRecommendFooter;
        TextView tvBrandChange;
        TextView tvBrandAdd;
        TextView tvCategoryDelete;
        TextView tvBrandDelete;


        ExpandListTextWatcher mTextWatcherNum;
        ExpandListTextWatcher mTextWatcherDimension;
        ExpandListTextWatcher mTextWatcherRemarks;

        //动态更新TextWathcer的position
        public void updatePosition(int groupPosition, int position) {
            mTextWatcherNum.updatePosition(groupPosition, position);
            mTextWatcherDimension.updatePosition(groupPosition, position);
            mTextWatcherRemarks.updatePosition(groupPosition, position);
        }
    }

    class ExpandListTextWatcher implements TextWatcher {

        //由于TextWatcher的afterTextChanged中拿不到对应的position值，所以自己创建一个子类
        private int ChildPosition;
        private int parentPosition;
        private int type;
        EditText mEditText;

        public ExpandListTextWatcher(int type, EditText editText) {
            this.type = type;
            mEditText = editText;

        }

        public void updatePosition(int groupPosition, int position) {
            this.ChildPosition = position;
            this.parentPosition = groupPosition;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mRecommendSCFDList.size() <= 0 ||
                    null == mRecommendSCFDList.get(parentPosition)
                    || mRecommendSCFDList.get(parentPosition).getBrands() == null
                    || mRecommendSCFDList.get(parentPosition).getBrands().size() <= 0) {
                return;
            }
            if (type == 0) {
                if (!isValidAmountAndUnit(s.toString())) {
                    showAlertView(UIUtils.getString(R.string.recommend_form_number));
                    hideSoftKeywords(mEditText);
                    mEditText.setText("");
                } else {
                    mRecommendSCFDList.get(parentPosition).getBrands().get(ChildPosition).setAmountAndUnit(s.toString());
                }
            } else if (type == 1) {
                if (!isValidDimension(s.toString())) {
                    showAlertView(UIUtils.getString(R.string.recommend_form_dimension));
                    mEditText.setText(s.toString().substring(0, 32));
                    hideSoftKeywords(mEditText);
                } else {
                    mRecommendSCFDList.get(parentPosition).getBrands().get(ChildPosition).setDimension(s.toString());
                }
            } else if (type == 2) {
                mRecommendSCFDList.get(parentPosition).getBrands().get(ChildPosition).setRemarks(s.toString());
            } else {
            }
        }
    }

    private boolean isValidDimension(String text) {
        if (StringUtils.isEmpty(text)) {
            return true;
        }
        if (text.length() > 32) {
            return false;
        }
        return true;
    }


    // 检验数量（1-9999 + 2个汉字）．
    private boolean isValidAmountAndUnit(String text) {
        if (StringUtils.isEmpty(text)) {
            return true;
        }
        boolean isThanZero = StringUtils.isNumeric(text) && Float.valueOf(text) > 0;
        boolean isAllNum = text.matches(ALL_NUM);
        boolean isNumAndChines = text.matches(NUM_AND_CHINESE);
        if (isThanZero && isAllNum || isNumAndChines) {
            return true;
        }
        return false;
    }

    //打开AlertView对话框
    private void showAlertView(String content) {
        new AlertView(UIUtils.getString(R.string.tip),
                content, null, null, new String[]{UIUtils.getString(R.string.sure)}, mActivity, AlertView.Style.Alert, null).show();
    }

    // 强制隐藏软件盘．
    private void hideSoftKeywords(TextView editText) {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

//    public void setCallBackListener(CallBack callBack) {
//        this.callBack = callBack;
//    }
//
//    public interface CallBack {
//        public void callBackRecommendSCFDs(List<RecommendSCFDBean> recommendSCFDList);
//
//    }
}
