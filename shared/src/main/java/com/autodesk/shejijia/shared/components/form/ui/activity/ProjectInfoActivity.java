package com.autodesk.shejijia.shared.components.form.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.autodesk.shejijia.shared.R;
import com.autodesk.shejijia.shared.components.common.entity.ResponseError;
import com.autodesk.shejijia.shared.components.common.entity.microbean.Building;
import com.autodesk.shejijia.shared.components.common.entity.microbean.Member;
import com.autodesk.shejijia.shared.components.common.entity.microbean.Task;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;
import com.autodesk.shejijia.shared.components.form.contract.ProjectInfoContract;
import com.autodesk.shejijia.shared.components.form.presenter.ProjectInfoPresenter;
import com.autodesk.shejijia.shared.framework.activity.BaseActivity;

/**
 * Created by t_aij on 16/10/21.
 */
public class ProjectInfoActivity extends BaseActivity implements ProjectInfoContract.View, View.OnClickListener {

    private TextView mUsernameTv;
    private TextView mTelephoneTv;
    private TextView mAddressTv;
    private TextView mCommunityTv;
    private ProjectInfoPresenter mPresenter;
    private Task mTask;
    private Member mMember;
    private Building mBuilding;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_project_info;
    }

    @Override
    protected void initView() {
        initToolbar();

        mUsernameTv = (TextView) findViewById(R.id.tv_username);
        mTelephoneTv = (TextView) findViewById(R.id.tv_telephone);
        mAddressTv = (TextView) findViewById(R.id.tv_address);
        mCommunityTv = (TextView) findViewById(R.id.tv_community);

    }

    @Override
    protected void initExtraBundle() {
        Intent intent = getIntent();
        mTask = (Task) intent.getSerializableExtra("task");
        mMember = (Member) intent.getSerializableExtra("member");
        mBuilding = (Building) intent.getSerializableExtra("building");
        mPresenter = new ProjectInfoPresenter(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mUsernameTv.setText(UIUtils.getString(R.string.form_username) + mMember.getProfile().getName());
        mTelephoneTv.setText(UIUtils.getString(R.string.form_telephone) + mMember.getProfile().getMobile());
        String address = mBuilding.getCityName() + mBuilding.getDistrictName() + mBuilding.getDistrict();
        mAddressTv.setText(UIUtils.getString(R.string.form_address) + address);
        mCommunityTv.setText(UIUtils.getString(R.string.form_community) + mBuilding.getCommunityName());
    }

    @Override
    protected void initListener() {
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
    }

    @Override
    public void enterPrecheck(Task task) {
        Intent intent = new Intent(this, PrecheckActivity.class);
        intent.putExtra("task", task);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_confirm) {
            mPresenter.submit(mTask);
        } else if (i == R.id.btn_cancel) {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showNetError(ResponseError error) {

    }

    @Override
    public void showError(String msg) {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    private void initToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setTitle(R.string.app_name);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }
}
