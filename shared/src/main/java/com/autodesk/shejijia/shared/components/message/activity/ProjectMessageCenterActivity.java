package com.autodesk.shejijia.shared.components.message.activity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import com.autodesk.shejijia.shared.R;
import com.autodesk.shejijia.shared.components.common.appglobal.ConstructionConstants;
import com.autodesk.shejijia.shared.components.common.entity.ResponseError;
import com.autodesk.shejijia.shared.components.common.utility.GsonUtil;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;
import com.autodesk.shejijia.shared.components.message.adapter.ProjectMessageCenterAdapter;
import com.autodesk.shejijia.shared.components.message.datamodel.CallBackMessageCenterDataSource;
import com.autodesk.shejijia.shared.components.message.datamodel.MessageCenterDataSource;
import com.autodesk.shejijia.shared.components.message.datamodel.MessageCenterRemoteDataSource;
import com.autodesk.shejijia.shared.components.message.entity.MessageInfo;
import com.autodesk.shejijia.shared.framework.activity.BaseActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luchongbin on 2016/12/5.
 */
public class ProjectMessageCenterActivity extends BaseActivity implements CallBackMessageCenterDataSource,ProjectMessageCenterAdapter.HistoricalRecordstListener {

    private MessageCenterDataSource mMessageCenterDataSource;
    private List<MessageInfo.DataBean> mData;
    private String mProjectId;
    private boolean isUnrea;
    private RecyclerView mRvProjectMessagCenterView;
    private ProjectMessageCenterAdapter mProjectMessageCenterAdapter;
    @Override
    protected int getLayoutResId() {
        return R.layout.activity_project_message_center;
    }
    @Override
    protected void initView() {
        mRvProjectMessagCenterView = (RecyclerView)findViewById(R.id.rv_project_message_center_view);
    }
    @Override
    protected void initExtraBundle() {
        super.initExtraBundle();
        if(getIntent().hasExtra(ConstructionConstants.BUNDLE_KEY_PROJECT_NAME)){
            isUnrea = getIntent().getBooleanExtra(ConstructionConstants.UNREAD,false);
            mProjectId = getIntent().getStringExtra(ConstructionConstants.BUNDLE_KEY_PROJECT_ID);
        }
    }
    @Override
    protected void initData(Bundle savedInstanceState) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(UIUtils.getString(R.string.update_priject_details));
        mData = new ArrayList<>();
        mMessageCenterDataSource = new MessageCenterRemoteDataSource(this);
        mProjectMessageCenterAdapter = new ProjectMessageCenterAdapter(mData,isUnrea,R.layout.item_messagecenter);
        initRecyclerView();
        getListMessageCenterInfo();
    }

    private void initRecyclerView(){
        mRvProjectMessagCenterView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRvProjectMessagCenterView.setLayoutManager(layoutManager);
        mRvProjectMessagCenterView.setAdapter(mProjectMessageCenterAdapter);
    }
    private void getListMessageCenterInfo(){
        mMessageCenterDataSource.listMessageCenterInfo(getRequestBundle(),TAG);
    }
    private Bundle getRequestBundle(){
        Bundle requestParams = new Bundle();
        requestParams.putString(ConstructionConstants.BUNDLE_KEY_PROJECT_ID,mProjectId);//"1642677"
        requestParams.putInt(ConstructionConstants.OFFSET,0);
        requestParams.putBoolean(ConstructionConstants.UNREAD,isUnrea);
        requestParams.putInt(ConstructionConstants.LIMIT,50);
        return requestParams;
    }
    @Override
    protected void initListener() {
        super.initListener();
        ProjectMessageCenterAdapter.setHistoricalRecordstListener(this);
    }
    @Override
    public void onErrorResponse(ResponseError responseError) {

    }
    @Override
    public void onResponse(JSONObject jsonObject) {
        String result = jsonObject.toString();
        isUnrea = false;
        MessageInfo messageInfo = GsonUtil.jsonToBean(result, MessageInfo.class);
        if(messageInfo.getData() != null){
            mData.addAll(messageInfo.getData());
        }
        mProjectMessageCenterAdapter.notifyDataSetChanged();
    }

    @Override
    public void onHistoricalRecordstClick() {
        getListMessageCenterInfo();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
