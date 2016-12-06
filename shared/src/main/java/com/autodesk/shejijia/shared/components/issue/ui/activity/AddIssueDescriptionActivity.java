package com.autodesk.shejijia.shared.components.issue.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.autodesk.shejijia.shared.R;
import com.autodesk.shejijia.shared.components.common.appglobal.ConstructionConstants;
import com.autodesk.shejijia.shared.components.common.entity.ResponseError;
import com.autodesk.shejijia.shared.components.common.uielements.commoncomment.CommentConfig;
import com.autodesk.shejijia.shared.components.common.uielements.commoncomment.comment.CommentFragment;
import com.autodesk.shejijia.shared.components.common.uielements.commoncomment.comment.CommentPresenter;
import com.autodesk.shejijia.shared.components.common.utility.ToastUtils;
import com.autodesk.shejijia.shared.components.issue.common.entity.IssueDescription;
import com.autodesk.shejijia.shared.components.issue.contract.AddIssueDescriptionContract;
import com.autodesk.shejijia.shared.components.issue.presenter.AddIssueDescriptionPresent;
import com.autodesk.shejijia.shared.framework.activity.BaseActivity;

import java.util.List;

/**
 * 添加问题追踪描述
 * Created by Menghao.Gu on 2016/12/5.
 */

public class AddIssueDescriptionActivity extends BaseActivity {
    private FrameLayout mAddIssueTracking;
    private CommentFragment addIssueTrackingFragment;
    private CommentPresenter mPresenter;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_add_issuetracking;
    }

    @Override
    protected void initView() {

        mAddIssueTracking = (FrameLayout) findViewById(R.id.fl_add_issuetracking);

    }

    @Override
    protected void initData(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            addIssueTrackingFragment = CommentFragment.getInstance(new CommentConfig());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_add_issuetracking, addIssueTrackingFragment)
                    .commit();
            mPresenter = new CommentPresenter(addIssueTrackingFragment, null);
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
            backAddIssueList();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void backAddIssueList() {
        String mDescription = mPresenter.getCommentContent();
        String mAudioPath = mPresenter.getAudioPath();
        List<String> mImagePath = mPresenter.getImageData();
        boolean hasImage = (mImagePath == null || mImagePath.size() == 0) ? false : true;
        if (!TextUtils.isEmpty(mDescription) || !TextUtils.isEmpty(mDescription) || hasImage) {
            IssueDescription mEntity = new IssueDescription(mDescription, mAudioPath, mImagePath);
            Intent intent = new Intent();
            intent.putExtra(ConstructionConstants.IssueTracking.ADD_ISSUE_DESCRIPTION_RESULT_KEY, mEntity);
            setResult(RESULT_OK, intent);
            finish();
        }
    }


}
