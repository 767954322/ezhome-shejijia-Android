package com.autodesk.shejijia.consumer.personalcenter.recommend.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.autodesk.shejijia.consumer.R;
import com.autodesk.shejijia.consumer.personalcenter.recommend.adapter.RecommendAdapter;
import com.autodesk.shejijia.consumer.personalcenter.recommend.entity.RecommendEntity;
import com.autodesk.shejijia.consumer.personalcenter.recommend.view.RecommendView;
import com.autodesk.shejijia.shared.components.common.uielements.CustomProgress;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;
import com.autodesk.shejijia.shared.framework.activity.NavigationBarActivity;

import java.util.ArrayList;
import java.util.List;

import cn.finalteam.loadingviewfinal.ListViewFinal;
import cn.finalteam.loadingviewfinal.OnDefaultRefreshListener;
import cn.finalteam.loadingviewfinal.OnLoadMoreListener;
import cn.finalteam.loadingviewfinal.PtrClassicFrameLayout;
import cn.finalteam.loadingviewfinal.PtrFrameLayout;

/**
 * @User: 蜡笔小新
 * @date: 16-10-21
 * @GitHub: https://github.com/meikoz
 */

public class CsRecommendActivity extends NavigationBarActivity implements RecommendView, OnLoadMoreListener, AdapterView.OnItemClickListener {

    private LinearLayout mEmptyView;
    private RecommendLogicImpl mRecommendLogic;

    public static void jumpTo(Context context, int status) {
        Intent intent = new Intent(context, CsRecommendActivity.class);
        context.startActivity(intent);
    }

    private List<RecommendEntity.ItemsBean> mRecommends = new ArrayList<>();
    private ListViewFinal mListView;
    private RecommendAdapter mAdapter;
    private PtrClassicFrameLayout mFrameLayout;
    private static int OFFSET = 0;
    private static final int LIMIT = 10;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_recommend_list;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RecommendEntity.ItemsBean item = (RecommendEntity.ItemsBean) parent.getAdapter().getItem(position);
        CsRecommendDetailsActivity.jumpTo(this, item.getDesign_project_id() + "", item.getCommunity_name());
    }

    @Override
    protected void initListener() {
        super.initListener();
        mListView.setOnLoadMoreListener(this);
        mListView.setOnItemClickListener(this);
        mFrameLayout.setOnRefreshListener(new OnDefaultRefreshListener() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mRecommendLogic.onLoadRecommendListData(false, 0, LIMIT, 0);
                mFrameLayout.onRefreshComplete();
            }
        });
    }

    @Override
    protected void initView() {
        setTitleForNavbar(UIUtils.getString(R.string.recommend_listing));
        mListView = (ListViewFinal) findViewById(R.id.lv_recommend);
        mFrameLayout = (PtrClassicFrameLayout) findViewById(R.id.ptr_layout);
        mEmptyView = (LinearLayout) findViewById(R.id.empty_view);
        mAdapter = new RecommendAdapter(this, mRecommends, R.layout.item_recommend, false);
        mListView.setAdapter(mAdapter);
        mRecommendLogic = new RecommendLogicImpl(this);
        mRecommendLogic.onLoadRecommendListData(false, 0, LIMIT, 0);
        CustomProgress.show(this, "", false, null);
    }

    @Override
    public void loadMore() {
        OFFSET += mRecommends.size();
        mRecommendLogic.onLoadRecommendListData(false, OFFSET, LIMIT, 0);
    }

    private void updateViewFromApi(int offset, List<RecommendEntity.ItemsBean> items) {
        if (items != null && items.size() > 0) {
            if (offset == 0) {
                mRecommends.clear();
                mFrameLayout.onRefreshComplete();
            } else
                mListView.onLoadMoreComplete();
            mEmptyView.setVisibility(View.GONE);
            mRecommends.addAll(items);
            mAdapter.notifyDataSetChanged();
        } else {
            if (offset == 0)
                mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoadDataSuccess(int offset, RecommendEntity entity) {
        CustomProgress.cancelDialog();
        updateViewFromApi(offset, entity.getItems());
    }

    @Override
    public void onLoadFailer() {
        CustomProgress.cancelDialog();
        mFrameLayout.onRefreshComplete();
        mListView.onLoadMoreComplete();
    }
}