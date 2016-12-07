package com.autodesk.shejijia.shared.components.issue.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.autodesk.shejijia.shared.R;
import com.autodesk.shejijia.shared.components.common.appglobal.ConstructionConstants;
import com.autodesk.shejijia.shared.components.common.entity.ResponseError;
import com.autodesk.shejijia.shared.components.common.entity.microbean.Task;
import com.autodesk.shejijia.shared.components.common.utility.StringUtils;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;
import com.autodesk.shejijia.shared.components.issue.common.entity.IssueDescription;
import com.autodesk.shejijia.shared.components.issue.contract.AddIssueDescriptionContract;
import com.autodesk.shejijia.shared.components.issue.presenter.AddIssueDescriptionPresent;
import com.autodesk.shejijia.shared.components.issue.ui.fragment.AddIssueListFragment;
import com.autodesk.shejijia.shared.framework.activity.BaseActivity;

/**
 * 添加问题追踪列表页
 * Created by Menghao.Gu on 2016/12/6.
 */

public class AddIssueListActivity extends BaseActivity implements AddIssueDescriptionContract.View {
    private AddIssueDescriptionPresent mTrackingPresent;
    private AddIssueListFragment addIssueTrackingFragment;
    private IssueDescription mIssueDescription;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_add_issuetracking;
    }

    @Override
    protected void initView() {


    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            initToolbar();
            addIssueTrackingFragment = AddIssueListFragment.getInstance();
            mTrackingPresent = new AddIssueDescriptionPresent(this);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_add_issuetracking, addIssueTrackingFragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.issuetraction_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            return true;
        } else if (i == R.id.add_traction) {
            // TODO 上传图片语音文字
//            mTrackingPresent.putIssueTracking(mIssueDescription.getmDescription(), mIssueDescription.getmAudioPath()
//                    , mIssueDescription.getmImagePath());
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void initToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setTitle(UIUtils.getString(R.string.activity_addissue_tital));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onShowStatus(boolean status) {

    }

    @Override
    public void showNetError(ResponseError error) {

    }

    @Override
    public void showError(String errorMsg) {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }


}
