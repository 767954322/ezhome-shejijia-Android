package com.autodesk.shejijia.consumer.personalcenter.recommend.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.autodesk.shejijia.consumer.R;
import com.autodesk.shejijia.consumer.manager.MPServerHttpManager;
import com.autodesk.shejijia.consumer.manager.constants.JsonConstants;
import com.autodesk.shejijia.consumer.personalcenter.recommend.adapter.RecommendExpandableAdapter;
import com.autodesk.shejijia.consumer.personalcenter.recommend.entity.CheckedInformationBean;
import com.autodesk.shejijia.consumer.personalcenter.recommend.entity.MaterialCategoryBean;
import com.autodesk.shejijia.consumer.personalcenter.recommend.entity.RecommendBrandsBean;
import com.autodesk.shejijia.consumer.personalcenter.recommend.entity.RecommendDetailsBean;
import com.autodesk.shejijia.consumer.personalcenter.recommend.entity.RecommendSCFDBean;
import com.autodesk.shejijia.consumer.personalcenter.recommend.entity.RefreshEvent;
import com.autodesk.shejijia.consumer.personalcenter.recommend.view.CustomHeaderExpandableListView;
import com.autodesk.shejijia.consumer.personalcenter.recommend.widget.BrandChangListener;
import com.autodesk.shejijia.shared.components.common.appglobal.Constant;
import com.autodesk.shejijia.shared.components.common.appglobal.MemberEntity;
import com.autodesk.shejijia.shared.components.common.network.OkJsonRequest;
import com.autodesk.shejijia.shared.components.common.uielements.CustomProgress;
import com.autodesk.shejijia.shared.components.common.uielements.alertview.AlertView;
import com.autodesk.shejijia.shared.components.common.uielements.alertview.OnItemClickListener;
import com.autodesk.shejijia.shared.components.common.utility.MPNetworkUtils;
import com.autodesk.shejijia.shared.components.common.utility.StringUtils;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;
import com.autodesk.shejijia.shared.framework.AdskApplication;
import com.autodesk.shejijia.shared.framework.activity.NavigationBarActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.autodesk.shejijia.shared.components.common.utility.GsonUtil.jsonToBean;

/**
 * 清单页面:空白页面及编辑清单页面
 *
 * @author liuhea
 *         created at 16-10-24
 */
public class RecommendListDetailActivity extends NavigationBarActivity implements View.OnClickListener, BrandChangListener,
        ExpandableListView.OnGroupClickListener {

    private CustomHeaderExpandableListView mExpandListView;
    private AppCompatButton mBtnListSend;
    private Activity mActivity;
    private String mAssetId;
    private LinearLayout mLlEmptyContentView;
    private RecommendExpandableAdapter mRecommendExpandableAdapter;
    private List<RecommendSCFDBean> mRecommendSCFDList;
    private TextView mTvNavTitle;
    private String mRecommendScfdTag = "";
    private String mProjectName;
    private int mBrandCountLimit;

    public static void actionStartActivity(Context context, String asset_id) {
        Intent intent = new Intent(context, RecommendListDetailActivity.class);
        intent.putExtra(JsonConstants.RECOMMEND_ASSET_ID, asset_id);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_recommend_list_detail;
    }

    @Override
    protected void initView() {
        super.initView();
        mExpandListView = (CustomHeaderExpandableListView) findViewById(R.id.rcy_recommend_detail);
        mBtnListSend = (AppCompatButton) findViewById(R.id.btn_list_send);
        mLlEmptyContentView = (LinearLayout) findViewById(R.id.empty_view);
        mTvNavTitle = (TextView) findViewById(R.id.nav_title_textView);
    }

    @Override
    protected void initExtraBundle() {
        super.initExtraBundle();
        Intent intent = getIntent();
        mAssetId = intent.getStringExtra(JsonConstants.RECOMMEND_ASSET_ID);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mActivity = this;
        mTvNavTitle.setMaxEms(6);
        mRecommendSCFDList = new ArrayList<>();
        mExpandListView.setHeaderView(getLayoutInflater().inflate(R.layout.item_group_indicator,
                mExpandListView, false));

        mRecommendExpandableAdapter = new RecommendExpandableAdapter(this, mRecommendSCFDList, mExpandListView);
        mExpandListView.setAdapter(mRecommendExpandableAdapter);
        setEmptyListDefaultButtn();
        getRecommendDraftDetail();
    }

    @Override
    protected void initListener() {
        super.initListener();
        mBtnListSend.setOnClickListener(this);
        mExpandListView.setGroupIndicator(null); //去掉箭头
        //点击不可收缩
        mExpandListView.setOnGroupClickListener(this);
        mRecommendExpandableAdapter.setBrandChangListener(this);
    }

    /**
     * 从推荐草稿中获取推荐清单信息
     */
    private void getRecommendDraftDetail() {
        CustomProgress.showDefaultProgress(mActivity);
        OkJsonRequest.OKResponseCallback callback = new OkJsonRequest.OKResponseCallback() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                CustomProgress.cancelDialog();
                String jsonString = jsonObject.toString();
                RecommendDetailsBean recommendListDetailBean = jsonToBean(jsonString, RecommendDetailsBean.class);
                mProjectName = recommendListDetailBean.getCommunity_name();
                mBrandCountLimit = recommendListDetailBean.getBrand_count_limit();

                updateUI(recommendListDetailBean);
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                CustomProgress.cancelDialog();
                MPNetworkUtils.logError(TAG, volleyError);
            }
        };
        MPServerHttpManager.getInstance().getRecommendDraftDetail(mAssetId, callback);
    }

    /**
     * 保存推荐清单详情页面
     */
    private void saveOrSendRecommendDetail(final boolean isSendInterface) {

        MemberEntity mMemberEntity = AdskApplication.getInstance().getMemberEntity();
        String design_id = mMemberEntity.getAcs_member_id();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JsonConstants.RECOMMEND_ASSET_ID, mAssetId);
            jsonObject.put(JsonConstants.RECOMMEND_SCFD, mRecommendSCFDList.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CustomProgress.showDefaultProgress(mActivity);
        OkJsonRequest.OKResponseCallback callback = new OkJsonRequest.OKResponseCallback() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                CustomProgress.cancelDialog();
                if (isSendInterface) {
                    new AlertView(UIUtils.getString(R.string.common_tip), UIUtils.getString(R.string.recommend_form_success),
                            null, null, new String[]{UIUtils.getString(R.string.sure)}, RecommendListDetailActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object object, int position) {
                            if (position != AlertView.CANCELPOSITION) {
                                EventBus.getDefault().post(new RefreshEvent());
                                finish();
                            }
                        }
                    }).show();
                } else {
                    new AlertView(UIUtils.getString(R.string.common_tip), UIUtils.getString(R.string.recommend_form_save_success),
                            null, null, new String[]{UIUtils.getString(R.string.sure)}, RecommendListDetailActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object object, int position) {
                            if (position != AlertView.CANCELPOSITION) {
                                EventBus.getDefault().post(new RefreshEvent());
                                finish();
                            }
                        }
                    }).show();
                }
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                CustomProgress.cancelDialog();
                MPNetworkUtils.logError(TAG, volleyError);
            }
        };
        MPServerHttpManager.getInstance().saveOrSendRecommendDetail(isSendInterface, design_id, mAssetId, jsonObject, callback);
    }

    private void updateUI(RecommendDetailsBean recommendListDetailBean) {
        setTitle(recommendListDetailBean);

        String scfd = recommendListDetailBean.getScfd();
        updateItemView(scfd);
    }

    private void updateItemView(String scfd) {
        // 更新适配器
        List<RecommendSCFDBean> recommendScfd = new Gson()
                .fromJson(scfd, new TypeToken<List<RecommendSCFDBean>>() {
                }.getType());

        if (null == recommendScfd || recommendScfd.size() <= 0) {
            mLlEmptyContentView.setVisibility(View.VISIBLE);
            setEmptyListDefaultButtn();
            return;
        }

        setSendButtn();
        mRecommendScfdTag = recommendScfd.toString();
        mRecommendSCFDList.addAll(recommendScfd);

        for (RecommendSCFDBean recommendSCFDBean : mRecommendSCFDList) {
            recommendSCFDBean.setBrand_count_limit(mBrandCountLimit);
        }

        mLlEmptyContentView.setVisibility(View.GONE);
        mRecommendExpandableAdapter.notifyDataSetChanged();
        for (int i = 0; i < mRecommendSCFDList.size(); i++) {
            mExpandListView.expandGroup(i);
        }
    }

    /**
     * 设置清单详情页面为空时候
     */
    private void setEmptyListDefaultButtn() {
        mBtnListSend.setClickable(false);
        mBtnListSend.setBackgroundColor(UIUtils.getColor(R.color.gray));
    }

    /**
     * 设置清单详情页面有数据时可点击发送
     */
    private void setSendButtn() {
        mBtnListSend.setClickable(true);
        mBtnListSend.setBackgroundColor(UIUtils.getColor(R.color.bg_0084ff));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_list_send:
                new AlertView(UIUtils.getString(R.string.common_tip), UIUtils.getString(R.string.recommend_form_send),
                        UIUtils.getString(R.string.common_cancel), null, new String[]{UIUtils.getString(R.string.sure)}, RecommendListDetailActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object object, int position) {
                        if (position != AlertView.CANCELPOSITION) {
                            saveOrSendRecommendDetail(true);
                        }
                    }
                }).show();
                break;
        }
    }

    /**
     * 空实现  不能删除
     */
    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        ViewCategoryActivity.jumpTo(mActivity, mRecommendSCFDList, groupPosition);
        return true;
    }

    @Override
    protected void rightNavButtonClicked(View view) {
        super.rightNavButtonClicked(view);
        //TODO @xuehua.yao
        Intent intent = new Intent(RecommendListDetailActivity.this, AddMaterialActivity.class);
        intent.putExtra(JsonConstants.RECOMMENDBRANDSCFDBEAN, (Serializable) mRecommendSCFDList);
        intent.putExtra(JsonConstants.JSON_PROJECT_NAME, mProjectName);
        intent.putExtra(JsonConstants.BRAND_COUNT_LIMIT, mBrandCountLimit);
        startActivityForResult(intent, 24);
    }

    @Override
    public void onBackPressed() {
        onBackEvent();
    }

    @Override
    protected void leftNavButtonClicked(View view) {
        onBackEvent();
    }

    /**
     * 处理退出，未保存逻辑
     */
    private void onBackEvent() {
        boolean isNotEdited = mRecommendScfdTag.equals(mRecommendSCFDList.toString());
        // [1]初次创建，没有内容，不算修改．
        // [2]删除主材，没有内容，修改了．
        // [3]空白清单，添加主材后，这两个值不相同，并且tag为null, 需要另外给tag赋值, 让其走保存操作．
        if (isNotEdited || StringUtils.isEmpty(mRecommendScfdTag)) {
            EventBus.getDefault().post(new RefreshEvent());
            finish();
        } else {
            new AlertView(UIUtils.getString(R.string.common_tip), UIUtils.getString(R.string.recommend_form_save),
                    UIUtils.getString(R.string.common_no), null, new String[]{UIUtils.getString(R.string.common_right)}, RecommendListDetailActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                @Override
                public void onItemClick(Object object, int position) {
                    if (position != AlertView.CANCELPOSITION) {
                        saveOrSendRecommendDetail(false);
                    } else {
                        RecommendListDetailActivity.this.finish();
                    }
                }
            }).show();
        }
    }


    @Override
    public void onBrandChangListener(RecommendSCFDBean recommendSCFDBean, RecommendBrandsBean recommendBrandsBean) {
        Intent intent = new Intent(mActivity, ChangeBrandActivity.class);
        intent.putExtra(JsonConstants.RECOMMENDBRANDSCFDBEAN, recommendSCFDBean);
        intent.putExtra(Constant.BundleKey.BRANDCODE, recommendBrandsBean);
        mActivity.startActivityForResult(intent, 21);
    }

    @Override
    public void onBrandAddListener(RecommendSCFDBean recommendSCFDBean) {
        Intent intent = new Intent(mActivity, AddBrandActivity.class);
        intent.putExtra(JsonConstants.RECOMMENDBRANDSCFDBEAN, recommendSCFDBean);
        intent.putExtra(JsonConstants.BRAND_COUNT_LIMIT, mBrandCountLimit);
        startActivityForResult(intent, 22);
    }

    @Override
    public void onBrandDeleteListener(final int currentParentPosition, final int childPosition) {
        new AlertView(UIUtils.getString(R.string.common_tip), UIUtils.getString(R.string.recommend_brand_delete),
                UIUtils.getString(R.string.common_cancel), null, new String[]{UIUtils.getString(R.string.sure)}, mActivity, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object object, int position) {
                if (position != AlertView.CANCELPOSITION) {
                    List<RecommendBrandsBean> rb = mRecommendSCFDList.get(currentParentPosition).getBrands();
                    rb.remove(childPosition);
                    if (rb == null || rb.size() <= 0) {
                        mRecommendSCFDList.remove(currentParentPosition);
                    }
                    mRecommendExpandableAdapter.notifyDataSetChanged();
                    setSendButtonState();
                    setEmptyContentView();
                }
            }
        }).show();
    }

    @Override
    public void onSubCategoryDeleteListener(final int groupPosition) {
        new AlertView(UIUtils.getString(R.string.common_tip), UIUtils.getString(R.string.recommend_material_delete),
                UIUtils.getString(R.string.following_cancel), null, new String[]{UIUtils.getString(R.string.sure)}, mActivity, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object object, int position) {
                if (position != AlertView.CANCELPOSITION) {
                    mRecommendSCFDList.remove(groupPosition);
                    mRecommendExpandableAdapter.notifyDataSetChanged();
                    setSendButtonState();
                    setEmptyContentView();
                }
            }
        }).show();
    }

    private void setEmptyContentView() {
        if (mRecommendSCFDList == null || mRecommendSCFDList.size() <= 0) {
            mLlEmptyContentView.setVisibility(View.VISIBLE);
        } else {
            mLlEmptyContentView.setVisibility(View.GONE);
        }
    }

    private void setTitle(RecommendDetailsBean recommendListDetailBean) {
        setTitleForNavbar(recommendListDetailBean.getCommunity_name());
        setTitleForNavButton(ButtonType.RIGHT, UIUtils.getString(R.string.recommend_material_add));
        setTextColorForRightNavButton(UIUtils.getColor(R.color.search_text_color));
    }

    /**
     * 获取添加主材后的更新
     */
    private RecommendSCFDBean getMaterialRecommendSCFDBean(CheckedInformationBean checkedInformationBean) {
        RecommendSCFDBean recommendSCFDBean1 = new RecommendSCFDBean();
        MaterialCategoryBean.Categories3dBean categories3dBean = checkedInformationBean.getCategories3dBean();
        MaterialCategoryBean.Categories3dBean.SubCategoryBean subCategoryBean = checkedInformationBean.getSubCategoryBean();
        List<RecommendBrandsBean> checkedBrandsInformationBean = checkedInformationBean.getCheckedBrandsInformationBean();
        recommendSCFDBean1.setSub_category_3d_name(subCategoryBean.getSub_category_3d_name());
        recommendSCFDBean1.setSub_category_3d_id(subCategoryBean.getSub_category_3d_id());
        recommendSCFDBean1.setCategory_3d_id(categories3dBean.getCategory_3d_id());
        recommendSCFDBean1.setCategory_3d_name(categories3dBean.getCategory_3d_name());
        recommendSCFDBean1.setBrands(checkedBrandsInformationBean);
        return recommendSCFDBean1;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 21: // 品牌变更．
                    changeBrand(data);
                    break;
                case 22:// 添加品牌．
                    addBrands(data);
                    break;
                case 23:// 定位二级品类．
                    int intExtra = data.getIntExtra(JsonConstants.LOCATION, 0);
                    mExpandListView.setSelection(intExtra);
                    break;
                case 24: // 添加主材．
                    addMaterialRecommendSCFD(data);
                    break;
                default:
                    break;
            }

            setSendButtonState();
        }
    }

    /**
     * 根据
     */
    private void setSendButtonState() {
        if (null != mRecommendSCFDList && mRecommendSCFDList.size() > 0) {
            setSendButtn();
        } else {
            setEmptyListDefaultButtn();
        }
    }

    private void changeBrand(Intent intent) {
        RecommendSCFDBean mRecommendSCFDBean = (RecommendSCFDBean) intent.getSerializableExtra(JsonConstants.RECOMMENDBRANDSCFDBEAN);
        for (RecommendSCFDBean recommendSCFDBean : mRecommendSCFDList) {
            if (recommendSCFDBean.getSub_category_3d_id().equals(mRecommendSCFDBean.getSub_category_3d_id())) {
                int post = mRecommendSCFDList.indexOf(recommendSCFDBean);
                mRecommendSCFDList.remove(recommendSCFDBean);
                mRecommendSCFDList.add(post, mRecommendSCFDBean);
                break;
            }
        }
        mRecommendExpandableAdapter.notifyDataSetChanged();

    }

    private void addBrands(Intent intent) {
        List<RecommendBrandsBean> brandAddList = (List<RecommendBrandsBean>) intent.getSerializableExtra(JsonConstants.RECOMMENDBRANDBEAN);//接
        String sub_category_3d_id = intent.getStringExtra(Constant.JsonLocationKey.SUB_CATEGORY_3D_ID);
        for (RecommendSCFDBean recommendSCFDBean : mRecommendSCFDList) {
            if (recommendSCFDBean.getSub_category_3d_id().equals(sub_category_3d_id)) {
                int post = mRecommendSCFDList.indexOf(recommendSCFDBean);
                mRecommendSCFDList.remove(recommendSCFDBean);
                recommendSCFDBean.getBrands().addAll(brandAddList);
                mRecommendSCFDList.add(post, recommendSCFDBean);
                break;
            }
        }
        mRecommendExpandableAdapter.notifyDataSetChanged();
    }

    private void addMaterialRecommendSCFD(Intent intent) {
        Bundle bundle = intent.getExtras();
        List<CheckedInformationBean> checkedInformationBeanList = (List<CheckedInformationBean>) bundle.get(JsonConstants.JSON_BACK_TOTAL_LIST);
        List<RecommendSCFDBean> recommendSCFDList = new ArrayList<>();
        recommendSCFDList.addAll(mRecommendSCFDList);

        mRecommendSCFDList.clear();

        for (CheckedInformationBean checkedInformationBean : checkedInformationBeanList) {
            mRecommendSCFDList.add(getMaterialRecommendSCFDBean(checkedInformationBean));
        }

        if (recommendSCFDList.toString().equals(mRecommendSCFDList.toString())) {
            mRecommendScfdTag = mRecommendSCFDList.toString();
        } else {
            mRecommendScfdTag = "data_modify";
        }

        for (RecommendSCFDBean recommendSCFDBean : mRecommendSCFDList) {
            recommendSCFDBean.setBrand_count_limit(mBrandCountLimit);
        }
        mLlEmptyContentView.setVisibility(mRecommendSCFDList.size() > 0 ? View.GONE : View.VISIBLE);
        mRecommendExpandableAdapter.notifyDataSetChanged();
        for (int i = 0; i < mRecommendSCFDList.size(); i++) {
            mExpandListView.expandGroup(i);
        }

    }
}
