package com.autodesk.shejijia.consumer.personalcenter.workflow.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.autodesk.shejijia.consumer.R;
import com.autodesk.shejijia.shared.components.common.appglobal.Constant;
import com.autodesk.shejijia.shared.components.common.uielements.CustomProgress;
import com.autodesk.shejijia.shared.components.common.uielements.alertview.AlertView;
import com.autodesk.shejijia.shared.components.common.uielements.alertview.OnItemClickListener;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;

/**
 * @author Malidong .
 * @version 1.0 .
 * @date 16-6-7
 * @file ProjectMaterialActivity.java  .
 * @brief 项目资料类 .
 */
public class ProjectMaterialActivity extends BaseWorkFlowActivity implements View.OnClickListener {

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.ll_project_material_measure_house:
                Intent mIntent = new Intent(ProjectMaterialActivity.this, FlowMeasureFormActivity.class);
                mIntent.putExtra(Constant.SeekDesignerDetailKey.NEEDS_ID, needs_id);
                mIntent.putExtra(Constant.SeekDesignerDetailKey.DESIGNER_ID, designer_id);
                startActivity(mIntent);
                break;

            case R.id.ll_project_material_measure_house_delivery:
                Intent dIntent = new Intent(ProjectMaterialActivity.this, FlowUploadDeliveryActivity.class);
                dIntent.putExtra(Constant.SeekDesignerDetailKey.NEEDS_ID, needs_id);
                dIntent.putExtra(Constant.SeekDesignerDetailKey.DESIGNER_ID, designer_id);
                startActivity(dIntent);
                break;

            case R.id.ll_project_material_contract:
                Intent cIntent = new Intent(ProjectMaterialActivity.this, FlowEstablishContractActivity.class);
                cIntent.putExtra(Constant.SeekDesignerDetailKey.NEEDS_ID, needs_id);
                cIntent.putExtra(Constant.SeekDesignerDetailKey.DESIGNER_ID, designer_id);
                startActivity(cIntent);
                break;

            case R.id.ll_project_material_project:
                Intent pIntent = new Intent(ProjectMaterialActivity.this, FlowUploadDeliveryActivity.class);
                pIntent.putExtra(Constant.SeekDesignerDetailKey.NEEDS_ID, needs_id);
                pIntent.putExtra(Constant.SeekDesignerDetailKey.DESIGNER_ID, designer_id);
                startActivity(pIntent);
                break;
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_project_material;
    }

    @Override
    protected void initView() {
        super.initView();
        ll_project_material_measure_house = (LinearLayout) findViewById(R.id.ll_project_material_measure_house);
        ll_project_material_measure_house_delivery = (LinearLayout) findViewById(R.id.ll_project_material_measure_house_delivery);
        ll_project_material_contract = (LinearLayout) findViewById(R.id.ll_project_material_contract);
        ll_project_material_project = (LinearLayout) findViewById(R.id.ll_project_material_project);

        ll_project_material_measure_house.setOnClickListener(this);
        ll_project_material_measure_house_delivery.setOnClickListener(this);
        ll_project_material_contract.setOnClickListener(this);
        ll_project_material_project.setOnClickListener(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        CustomProgress.show(this, "", false, null);
        super.initData(savedInstanceState);

        setTitleForNavbar(getResources().getString(R.string.flow_project_data));
    }

    @Override
    protected void onWorkFlowData() { // 判断项目资料显示节点
        super.onWorkFlowData();
        CustomProgress.cancelDialog();

        wk_cur_sub_node_idi = Integer.valueOf(wk_cur_sub_node_id);
        isShowAlertView(wk_cur_sub_node_idi);
        if (wk_cur_sub_node_idi >= 11) {
            ll_project_material_measure_house.setVisibility(View.VISIBLE);
        }
        if (wk_cur_sub_node_idi == 33) {
            ll_project_material_measure_house_delivery.setVisibility(View.VISIBLE);
        }
        if (wk_cur_sub_node_idi >= 31) {
            ll_project_material_contract.setVisibility(View.VISIBLE);
        }
        if (wk_cur_sub_node_idi >= 51) {
            ll_project_material_project.setVisibility(View.VISIBLE);
        }
    }
    private void isShowAlertView(int sub_node_id){
        if (isElite(wk_cur_template_id)) {
            if (sub_node_id < 11) {
                new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(R.string.please_wait_consumer__quantity_room), null,
                        new String[]{UIUtils.getString(R.string.sure)}, null, this, AlertView.Style.Alert, new OnItemClickListener(){
                    @Override
                    public void onItemClick(Object object, int position) {
                        setResult(RESULT_OK, null);
                        finish();
                    }
                }).show();
            }
        }
    }

    private LinearLayout ll_project_material_measure_house;
    private LinearLayout ll_project_material_measure_house_delivery;
    private LinearLayout ll_project_material_contract;
    private LinearLayout ll_project_material_project;
    private int wk_cur_sub_node_idi;
}
