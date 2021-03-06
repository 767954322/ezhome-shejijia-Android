package com.autodesk.shejijia.consumer.home.homepage.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.autodesk.shejijia.consumer.R;
import com.autodesk.shejijia.consumer.bidhall.activity.BiddingHallDetailActivity;
import com.autodesk.shejijia.consumer.home.decorationlibrarys.activity.FiltrateActivity;
import com.autodesk.shejijia.consumer.home.decorationlibrarys.entity.FiltrateContentBean;
import com.autodesk.shejijia.consumer.manager.MPServerHttpManager;
import com.autodesk.shejijia.consumer.personalcenter.designer.adapter.BidHallAdapter;
import com.autodesk.shejijia.consumer.personalcenter.designer.entity.BidHallEntity;
import com.autodesk.shejijia.consumer.utils.ApiStatusUtil;
import com.autodesk.shejijia.consumer.utils.AppJsonFileReader;
import com.autodesk.shejijia.shared.components.common.appglobal.Constant;
import com.autodesk.shejijia.shared.components.common.appglobal.MemberEntity;
import com.autodesk.shejijia.shared.components.common.network.OkJsonRequest;
import com.autodesk.shejijia.shared.components.common.uielements.CustomProgress;
import com.autodesk.shejijia.consumer.uielements.pulltorefresh.PullListView;
import com.autodesk.shejijia.consumer.uielements.pulltorefresh.PullToRefreshLayout;
import com.autodesk.shejijia.consumer.base.utils.ConvertUtils;
import com.autodesk.shejijia.shared.components.common.utility.GsonUtil;
import com.autodesk.shejijia.shared.components.common.utility.MPNetworkUtils;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;
import com.autodesk.shejijia.shared.framework.AdskApplication;
import com.autodesk.shejijia.shared.framework.fragment.BaseFragment;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author DongXueQiu .
 * @version 1.0 .
 * @date 2016/3/4 0007 9:42 .
 * @file BidHallFragment  .
 * @brief 应标大厅fragment, 展示可应标的事例 .
 */
public class BidHallFragment extends BaseFragment implements PullToRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {

    public static BidHallFragment getInstance() {
        BidHallFragment uhf = new BidHallFragment();
        return uhf;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_custom_bid;
    }

    @Override
    protected void initView() {
        mPullListView = (PullListView) rootView.findViewById(R.id.lv_custom_bid);
        mPullToRefreshLayout = ((PullToRefreshLayout) rootView.findViewById(R.id.refresh_view));

        mFooterView = View.inflate(getActivity(), R.layout.view_empty_layout, null);
        mRlEmpty = (RelativeLayout) mFooterView.findViewById(R.id.rl_empty);
        mTvEmptyMessage = (TextView) mFooterView.findViewById(R.id.tv_empty_message);
        mIvTemp = ((ImageView) mFooterView.findViewById(R.id.iv_default_empty));
        mPullListView.addFooterView(mFooterView);
    }

    @Override
    protected void initData() {
        registerBoradcastReceiver();
    }

    @Override
    protected void initListener() {
        super.initListener();
        /// 跳入应标详情activity.
        mPullListView.setOnItemClickListener(this);
        mPullToRefreshLayout.setOnRefreshListener(this);
        MemberEntity mMemberEntity = AdskApplication.getInstance().getMemberEntity();
        if (null != mMemberEntity) {
            onRefresh(mPullToRefreshLayout);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (adapterView.equals(mPullListView)) {
            if (mNeedsListEntities == null || mNeedsListEntities.size() == 0) {
                return;
            }
            needs_id = String.valueOf(mNeedsListEntities.get(position).getNeeds_id());
            Intent intent = new Intent(activity, BiddingHallDetailActivity.class);
            intent.putExtra(Constant.DemandDetailBundleKey.DEMAND_TYPE, Constant.DemandDetailBundleKey.TYPE_CUSTOMBID_FRAGMENT);
            intent.putExtra(Constant.DemandDetailBundleKey.DEMAND_NEEDS_ID, needs_id);
            startActivity(intent);
        }
    }

    public void handleFilterOption() {
        Intent intent = new Intent(getActivity(), FiltrateActivity.class);
        intent.putExtra(TYPE, 0);
        intent.putExtra(Constant.CaseLibrarySearch.AREA_INDEX, mFiltrateContentBean == null ? 0 : mFiltrateContentBean.getAreaIndex());
        intent.putExtra(Constant.CaseLibrarySearch.HOUSING_INDEX, mFiltrateContentBean == null ? 0 : mFiltrateContentBean.getHouseIndex());
        intent.putExtra(Constant.CaseLibrarySearch.STYLE_INDEX, mFiltrateContentBean == null ? 0 : mFiltrateContentBean.getStyleIndex());
        this.startActivityForResult(intent, REQUEST_CODE);
    }

    /// 获取应标信息.
    private void getShouldHallData(final int state, int offset, int limit,
                                   String custom_string_area, String custom_string_form, String custom_string_type, String custom_string_bedroom,
                                   String custom_string_style, String custom_string_restroom, String asset_taxonomy) {
        CustomProgress.show(getActivity(), "", false, null);
        OkJsonRequest.OKResponseCallback okResponseCallback = new OkJsonRequest.OKResponseCallback() {

            @Override
            public void onResponse(JSONObject jsonObject) {
                CustomProgress.cancelDialog();
                if (jsonObject == null) {
                    return;
                }
                try {
                    String str = GsonUtil.jsonToString(jsonObject);
                    BidHallEntity list = GsonUtil.jsonToBean(str, BidHallEntity.class);
                    if (list != null && list.getNeeds_list() != null) {
                        List<BidHallEntity.NeedsListBean> entitys = getNeedsListEntitys(list.getNeeds_list());
                        switch (state) {
                            case 0:
                                OFFSET = 10;
                                mNeedsListEntities.clear();
                                break;
                            case 1:
                                OFFSET += 10;
                                break;
                            default:
                                break;
                        }

                        mNeedsListEntities.addAll(entitys);
                        mBidHallAdapter = new BidHallAdapter(getActivity(), mNeedsListEntities);
                        mPullListView.setAdapter(mBidHallAdapter);
                        mBidHallAdapter.notifyDataSetChanged();
                    }

                } finally {
                    hideFooterView(mNeedsListEntities);
                    mPullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
                }
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                CustomProgress.cancelDialog();
                MPNetworkUtils.logError(TAG, volleyError);
                mPullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.FAIL);
                if (null != getActivity()) {
                    ApiStatusUtil.getInstance().apiStatuError(volleyError, getActivity());
                }
                hideFooterView(mNeedsListEntities);
            }
        };

        MPServerHttpManager.getInstance().getShouldHallData(offset, custom_string_area, custom_string_form,
                custom_string_type, custom_string_bedroom, limit, custom_string_style, asset_taxonomy, custom_string_restroom, okResponseCallback);
    }

    /**
     * 是否隐藏底部布局
     *
     * @param list 应标集合信息
     */
    private void hideFooterView(List<BidHallEntity.NeedsListBean> list) {
        if (list != null && list.size() > 0) {
            mRlEmpty.setVisibility(View.GONE);
        } else {
            mRlEmpty.setVisibility(View.VISIBLE);
        }
//        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.photopicker_thumbnail_placeholder);
//        mIvTemp.setImageBitmap(bmp);
        WindowManager wm = (WindowManager) getActivity().getSystemService(getActivity().WINDOW_SERVICE);
        int height = wm.getDefaultDisplay().getHeight();
        android.view.ViewGroup.LayoutParams pp = mRlEmpty.getLayoutParams();
        pp.height = height - 100;
        mRlEmpty.setLayoutParams(pp);
        mTvEmptyMessage.setText(UIUtils.getString(R.string.not_found));
    }

    private List<BidHallEntity.NeedsListBean> getNeedsListEntitys(List<BidHallEntity.NeedsListBean> list) {
        List<BidHallEntity.NeedsListBean> needsLis = new ArrayList<>();
        for (BidHallEntity.NeedsListBean needsListEntity : list) {
            if (getActivity() != null) {

                Map<String, String> mapStyle = AppJsonFileReader.getStyle(getActivity());
                Map<String, String> mapLivingRoom = AppJsonFileReader.getLivingRoom(getActivity());
                Map<String, String> mapRoomHall = AppJsonFileReader.getRoomHall(getActivity());
                Map<String, String> mapToilet = AppJsonFileReader.getToilet(getActivity());
                String style = ConvertUtils.getConvert2CN(mapStyle, needsListEntity.getDecoration_style());
                String livingRoom = ConvertUtils.getConvert2CN(mapLivingRoom, needsListEntity.getLiving_room());
                String room = ConvertUtils.getConvert2CN(mapRoomHall, needsListEntity.getRoom());
                String toilet = ConvertUtils.getConvert2CN(mapToilet, needsListEntity.getToilet());

                needsListEntity.setDecoration_style(style);
                needsListEntity.setRoom(room);
                needsListEntity.setLiving_room(livingRoom);
                needsListEntity.setToilet(toilet);

                needsLis.add(needsListEntity);
            }
        }
        return needsLis;
    }


    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        getShouldHallData(0, 0, LIMIT, mFiltrateContentBean == null ? BLANK : mFiltrateContentBean.getArea(), mFiltrateContentBean == null ? BLANK : mFiltrateContentBean.getHousingType(), BLANK, BLANK, mFiltrateContentBean == null ? BLANK : mFiltrateContentBean.getStyle(), BLANK, URL);
    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
        getShouldHallData(1, OFFSET, LIMIT, mFiltrateContentBean == null ? BLANK : mFiltrateContentBean.getArea(), mFiltrateContentBean == null ? BLANK : mFiltrateContentBean.getHousingType(), BLANK, BLANK, mFiltrateContentBean == null ? BLANK : mFiltrateContentBean.getStyle(), BLANK, URL);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null == data) {
            return;
        }
        Bundle bundle = data.getExtras();
        switch (resultCode) {
            case FiltrateActivity.CBF_RESULT_CODE:
                //fix 筛选 不回到初始位置
                mPullListView.clearFocus();
                mPullListView.post(new Runnable() {
                    @Override
                    public void run() {
                        mPullListView.setSelection(0);
                    }
                });
                FiltrateContentBean filtrateContentBean = (FiltrateContentBean) bundle.getSerializable(CONTENT_BEAN);
                updateNotify(filtrateContentBean);
                break;
        }
    }

    @Override
    public void onDestroy() {
        activity.unregisterReceiver(myBroadCastReceivr);
        super.onDestroy();
    }

    public void onFragmentShown() {
        if (!mFlag) {
            mFiltrateContentBean = null;
            mNeedsListEntities.clear();
//            mNeedsListEntities.addAll(mNeedsListEntityArrayList);
//            mBidHallAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    public final static String ACTION_NAME = "REFRESH_BIDHALL";
    /**
     * 01  广播接受者类
     */
    private BroadcastReceiver myBroadCastReceivr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_NAME)) {
                getShouldHallData(0, 0, LIMIT, mFiltrateContentBean == null ? BLANK : mFiltrateContentBean.getArea(), mFiltrateContentBean == null ? BLANK : mFiltrateContentBean.getHousingType(), BLANK, BLANK, mFiltrateContentBean == null ? BLANK : mFiltrateContentBean.getStyle(), BLANK, URL);
            }
        }
    };

    /**
     * 02  注册广播
     */
    public void registerBoradcastReceiver() {
        //注册广播的过滤器
        IntentFilter IntentFilter = new IntentFilter();
        IntentFilter.addAction(ACTION_NAME);
        //注册广播
        getActivity().registerReceiver(myBroadCastReceivr, IntentFilter);
    }


    private void updateNotify(FiltrateContentBean content) {
        this.mFiltrateContentBean = content;
//        mPullToRefreshLayout.autoRefresh();
        getShouldHallData(0, 0, LIMIT, mFiltrateContentBean == null ? BLANK : mFiltrateContentBean.getArea(),
                mFiltrateContentBean == null ? BLANK : mFiltrateContentBean.getHousingType(), BLANK, BLANK,
                mFiltrateContentBean == null ? BLANK : mFiltrateContentBean.getStyle(), BLANK, URL);
    }

    public static final int REQUEST_CODE = 0x9;
    public static final String TYPE = "TYPE";
    public static final String BLANK = "";
    public static final String CONTENT_BEAN = "contentBean";
    public static final String URL = "ezhome/fullflow/audit/success";

    private RelativeLayout mRlEmpty;
    private TextView mTvEmptyMessage;
    private ImageView mIvTemp;
    private View mFooterView;
    private PullListView mPullListView;
    private PullToRefreshLayout mPullToRefreshLayout;

    private String needs_id;
    private int LIMIT = 10;
    private int OFFSET = 0;
    private Boolean bid_status;
    private boolean mFlag = true;

    private FiltrateContentBean mFiltrateContentBean;
    private BidHallAdapter mBidHallAdapter;
    private List<BidHallEntity.NeedsListBean> mNeedsListEntities = new ArrayList<>();
    private List<BidHallEntity.NeedsListBean> mNeedsListEntityArrayList = new ArrayList<>();

}
