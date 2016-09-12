package com.autodesk.shejijia.consumer.personalcenter.workflow.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.autodesk.shejijia.consumer.R;
import com.autodesk.shejijia.consumer.manager.MPServerHttpManager;
import com.autodesk.shejijia.consumer.personalcenter.designer.entity.DesignerInfoDetails;
import com.autodesk.shejijia.consumer.personalcenter.workflow.entity.MPContractDataBean;
import com.autodesk.shejijia.consumer.personalcenter.workflow.entity.MPContractNoBean;
import com.autodesk.shejijia.consumer.personalcenter.workflow.entity.MPDesignContractBean;
import com.autodesk.shejijia.consumer.utils.AppDataFormatValidator.MPDesignFormatValidator;
import com.autodesk.shejijia.consumer.utils.MPStatusMachine;
import com.autodesk.shejijia.shared.components.common.appglobal.Constant;
import com.autodesk.shejijia.shared.components.common.network.OkJsonRequest;
import com.autodesk.shejijia.shared.components.common.uielements.CustomProgress;
import com.autodesk.shejijia.shared.components.common.uielements.TextViewContent;
import com.autodesk.shejijia.shared.components.common.uielements.alertview.AlertView;
import com.autodesk.shejijia.shared.components.common.uielements.alertview.OnDismissListener;
import com.autodesk.shejijia.shared.components.common.uielements.alertview.OnItemClickListener;
import com.autodesk.shejijia.shared.components.common.utility.GsonUtil;
import com.autodesk.shejijia.shared.components.common.utility.MPNetworkUtils;
import com.autodesk.shejijia.shared.components.common.utility.RegexUtil;
import com.autodesk.shejijia.shared.components.common.utility.StringUtils;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;
import com.google.gson.Gson;
import com.socks.library.KLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * @author Malidong .
 * @version 1.0 .
 * @date 16-6-7
 * @file FlowEstablishContractActivity.java  .
 * @brief 全流程设计合同.
 */
public class FlowEstablishContractActivity extends BaseWorkFlowActivity implements View.OnClickListener, OnItemClickListener, OnDismissListener {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_flow_design_contract;
    }

    /**
     * @param savedInstanceState
     * @brief 初始化信息 .
     */
    @Override
    protected void initData(Bundle savedInstanceState) {
        CustomProgress.show(this, "", false, null);

        super.initData(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();


    }
    @Override
    protected void initView() {
        super.initView();

        setTitleForNavbar(getResources().getString(R.string.design_contract)); /// 设置标题 .

        initViewVariables();
        initWebViewConfig();

        tvc_designer_name.setEnabled(false);
        tvc_designer_phone.setEnabled(false);
        tvc_designer_email.setEnabled(false);
        tvc_designer_decorate_address.setEnabled(false);

        /* To prevent the pop-up keyboard */
        TextView textView = (TextView) findViewById(R.id.tv_prevent_edit_text);
        textView.requestFocus();
    }

    protected void initViewVariables() {

         /* init content for designer form */
        llbtn_see_contract_detail = (LinearLayout) findViewById(R.id.ll_flow_examine_contract); //設計師合同詳情點擊條
        ll_designer_action_layout = (LinearLayout) findViewById(R.id.ll_flow_designer_send_contract); //設計師發送合同的layout
       //Variables
        ll_contract_input_form_layout = (LinearLayout) findViewById(R.id.design_contract_content_designer);//合同表單Layout
        ll_contract_webview_layout = (LinearLayout) findViewById(R.id.design_contract_content_consumer);//web view 合同表單

        btn_designer_submit_button = (Button) findViewById(R.id.btn_send_establish_contract_designer); //設計師發送合同按鍵
        btn_consumer_submit_button = (Button) findViewById(R.id.btn_send_establish_contract);                  //消費者支付的按鍵
        tvc_consumer_name = (TextViewContent) findViewById(R.id.flow_establish_contract_consumer_name);
        tvc_consumer_phone = (TextViewContent) findViewById(R.id.flow_establish_contract_consumer_phone);
        tvc_consumer_email = (TextViewContent) findViewById(R.id.flow_establish_contract_consumer_email);
        tvc_designer_name = (TextViewContent) findViewById(R.id.flow_establish_contract_designer_name);
        tvc_designer_phone = (TextViewContent) findViewById(R.id.flow_establish_contract_designer_phone);
        tvc_designer_email = (TextViewContent) findViewById(R.id.flow_establish_contract_designer_email);
        tvc_designer_decorate_address = (TextViewContent) findViewById(R.id.flow_establish_contract_designer_decorate_address);
        tvc_total_cost = (TextViewContent) findViewById(R.id.flow_establish_contract_consumer_total_cost);
        tvc_first_cost = (TextViewContent) findViewById(R.id.flow_establish_contract_consumer_first_cost);
        tvc_last_cost = (TextViewContent) findViewById(R.id.tvc_flow_establish_contract_consumer_last_cost);
        img_agree_establish_contract = (ImageView) findViewById(R.id.img_agree_establish_contract);
        tvc_treeD_render_count = (TextViewContent) findViewById(R.id.tvc_flow_establish_contract_designer_render_map);
        tvc_consumer_local_area = (TextView) findViewById(R.id.flow_establish_contract_consumer_decorate_address);


        /* init content for consumer form */
        twvc_contract_content_webview = (WebView) findViewById(R.id.contract_sub_content_webview);//webview 物件
        ll_consumer_action_layout= (LinearLayout) findViewById(R.id.ll_send_establish_contract);//消費者支付動作Layout
        ll_consumer_agree_content_layout = (LinearLayout) findViewById(R.id.ll_agree_establish_contract);//消費者己閱讀的提示Layout


    }

    private void initWebViewConfig() {

        WebSettings webSettings = twvc_contract_content_webview.getSettings();
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDefaultTextEncodingName(Constant.NetBundleKey.UTF_8);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        twvc_contract_content_webview.setWebViewClient(new webViewClient());
        twvc_contract_content_webview.addJavascriptInterface(new WebViewJavaScriptCallback(), ANDROID);
    }

    private void UpdateUIlayoutContract() {
        UpdateUIlayoutContractContent();

        if (!bShowModeContentWebView) { /// 设计师　.
            UpdateUIcontractContentInputForm();
        } else  { /// 消费者 .
            UpdateUIcontractContentWebView();
        }
        UpdateUIActionLayout();

    }

    private void UpdateUIlayoutContractContent() {

        /* 填充设计师个人信息 */
        tvc_designer_phone.setText(designer_mobile);
        tvc_designer_name.setText(designer_name);
        tvc_designer_email.setText(designer_mail);

        if (bShowModeContentWebView) {
            ll_contract_input_form_layout.setVisibility(View.GONE);
            ll_contract_webview_layout.setVisibility(View.VISIBLE);
        }else {
            ll_contract_webview_layout.setVisibility(View.GONE);
            ll_contract_input_form_layout.setVisibility(View.VISIBLE);
        }


    }

    private void UpdateUIcontractContentInputForm() {
        ll_consumer_agree_content_layout.setVisibility(View.GONE);
        ll_consumer_action_layout.setVisibility(View.GONE);


        tvc_last_cost.setEnabled(false);

        if (!bDesignerContractCreated) {
            AsyncGetContractNumber(new commonJsonResponseCallback() {
                @Override
                public void onJsonResponse(String jsonResponse) {
                    contractNo = new Gson().fromJson(jsonResponse.toString(), MPContractNoBean.class);
                    contract_no = contractNo.getContractNO(); /// 获取合同编号 .

                }
                @Override
                public void onError(VolleyError volleyError) {
                    MPNetworkUtils.logError(TAG, volleyError);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UIAlert = new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(R.string.error_on_get_contract_number), null, null, new String[]{UIUtils.getString(R.string.sure)}, FlowEstablishContractActivity.this, AlertView.Style.Alert, FlowEstablishContractActivity.this).setOnDismissListener(new OnDismissListener() {
                                @Override
                                public void onDismiss(Object o) {
                                    finish();
                                }
                            });
                            UIAlert.show();
                        }
                    });

                }
            });

            tvc_consumer_local_area.setText(requirement.getProvince_name() + requirement.getCity_name() + requirement.getDistrict_name());
            tvc_designer_decorate_address.setText(requirement.getProvince_name() + requirement.getCity_name() + requirement.getDistrict_name());


        } else {

            if (!bAllowUserInput) {
                UpdateUIdisableFieldsInput();
            }
            MPDesignContractBean designContractEntity = mBidders.get(0).getDesign_contract();
            if (null == designContractEntity) {
                return;
            }
            String contractData = designContractEntity.getContract_data();
            String str = contractData.toString().replace("@jr@", "\"");
            MPContractDataBean designContractBean = GsonUtil.jsonToBean(str, MPContractDataBean.class);

            contract_no=designContractEntity.getContract_no();
            tvc_consumer_name.setText(designContractBean.getName());
            tvc_consumer_phone.setText(designContractBean.getMobile());
            tvc_total_cost.setText(designContractEntity.getContract_charge());
            tvc_first_cost.setText(designContractEntity.getContract_first_charge());
            tvc_consumer_local_area.setText(designContractBean.getAddr());
            tvc_designer_decorate_address.setText(designContractBean.getAddr());

            if (designContractBean.getEmail().equals("null") || designContractBean == null) {
                tvc_consumer_email.setText("");
            } else {
                tvc_consumer_email.setText(designContractBean.getEmail());
            }

            tvc_treeD_render_count.setText(designContractBean.getRender_map());
            Double totalCost = Double.parseDouble(designContractEntity.getContract_charge());
            Double firstCost = Double.parseDouble(designContractEntity.getContract_first_charge());
            DecimalFormat df = new DecimalFormat("#.##"); /// 保留小数点后两位 .
            tvc_last_cost.setText(df.format(totalCost - firstCost));


        }


    }

    private void UIsetDesignerSendButtonActive(boolean bVivible, String buttonText) {

        if (bVivible) {
            ll_designer_action_layout.setVisibility(View.VISIBLE);

            if (buttonText != null)
                btn_designer_submit_button.setText(buttonText);
        } else {
            ll_designer_action_layout.setVisibility(View.GONE);
        }


    }

    private void UpdateUIcontractContentWebView() {

        if (getContractDataEntityFromFirstBidder() == null) {
            UIAlert = new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(R.string.please_wait_designer_send_contract), null, null, new String[]{UIUtils.getString(R.string.sure)}, FlowEstablishContractActivity.this, AlertView.Style.Alert, FlowEstablishContractActivity.this).setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(Object o) {
                    finish();
                }
            });
            UIAlert.show();
            return;
        }

        img_agree_establish_contract.setBackgroundResource(R.drawable.icon_selected_unchecked);
        btn_consumer_submit_button.setEnabled(false);
        btn_consumer_submit_button.setBackgroundResource(R.drawable.bg_common_btn_pressed);

        UpdateUIsetContentViewConsumer();

    }

    private void UpdateUIsetContentViewConsumer() {

        try {
            //Return an AssetManager instance for your application's package
            InputStream is = getAssets().open("contract_content_consumer.txt");
            int size = is.available();

            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String text = new String(buffer, Constant.NetBundleKey.UTF_8);

            while (true) {
                text = text.replace("#val(designer_name)", designer_name);
                text = text.replace("#val(designer_mobile)", designer_mobile);
                text = text.replace("#val(designer_mail)", designer_mail);

                if (contract_data_entity == null)
                    break;

                MPContractDataBean contract_detail = getContractDetailData(contract_data_entity);
                if (contract_detail == null)
                    break;

                contract_number = contract_data_entity.getContract_no();
                contract_date = contract_data_entity.getContract_create_date();
                design_amount = contract_data_entity.getContract_charge();
                design_amount_first = contract_data_entity.getContract_first_charge();

                contract_date = Validator.getStrDateToString(contract_date);


                Double totalCost = Double.parseDouble(design_amount);
                Double firstCost = Double.parseDouble(design_amount_first);

                design_amount = String.format("%.2f", totalCost);
                design_amount_first = String.format("%.2f", firstCost);
                design_amount_balance = String.format("%.2f", totalCost - firstCost);

                String consumer_name = contract_detail.getName();
                String consumer_mobile = contract_detail.getMobile();
                String consumer_addr = contract_detail.getAddr();
                String consumer_mail = contract_detail.getEmail();
                String tree_d_renderimage = contract_detail.getRender_map();
                String designer_addr = contract_detail.getAddr();

                text = text.replace("#val(contract_number)", Validator.getStringWithNullDefaultString(contract_number,""));
                text = text.replace("#val(contract_date)", Validator.getStringWithNullDefaultString(contract_date, UIUtils.getString(R.string.data_null)));
                text = text.replace("#val(design_amount)", Validator.getStringWithNullDefaultString(design_amount, UIUtils.getString(R.string.data_null)));
                text = text.replace("#val(design_amount_first)", Validator.getStringWithNullDefaultString(design_amount_first, UIUtils.getString(R.string.data_null)));
                text = text.replace("#val(design_amount_balance)", Validator.getStringWithNullDefaultString(design_amount_balance, UIUtils.getString(R.string.data_null)));
                text = text.replace("#val(consumer_name)", Validator.getStringWithNullDefaultString(consumer_name, UIUtils.getString(R.string.data_null)));
                text = text.replace("#val(consumer_mobile)", Validator.getStringWithNullDefaultString(consumer_mobile, UIUtils.getString(R.string.data_null)));
                text = text.replace("#val(consumer_addr)", Validator.getStringWithNullDefaultString(consumer_addr, UIUtils.getString(R.string.data_null)));
                text = text.replace("#val(consumer_mail)", Validator.getStringWithNullDefaultString(consumer_mail, ""));
                text = text.replace("#val(tree_d_renderimage)", Validator.getStringWithNullDefaultString(tree_d_renderimage, UIUtils.getString(R.string.data_null)));
                text = text.replace("#val(designer_addr)", Validator.getStringWithNullDefaultString(designer_addr, UIUtils.getString(R.string.data_null)));
                break;
            }
            ;

            twvc_contract_content_webview.loadDataWithBaseURL(null, text, Constant.NetBundleKey.MIME_TYPE_TEXT_HTML, Constant.NetBundleKey.UTF_8, "");

        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }

    private void UpdateUIActionLayout() {

        if (bDesignerActionShow){

            if (bDesignerContractCreated)
                UIsetDesignerSendButtonActive(true, getResources().getString(R.string.sure_modify_send));
            else
                UIsetDesignerSendButtonActive(true, getResources().getString(R.string.send_design_contract));
        }


        if (bconsumerActionShow){
            ll_consumer_action_layout.setVisibility(View.VISIBLE);
            ll_consumer_agree_content_layout.setVisibility(View.VISIBLE);

            btn_consumer_submit_button.setText(R.string.confirmation_and_payment);
            btn_consumer_submit_button.setEnabled(false);
            btn_consumer_submit_button.setBackgroundResource(R.drawable.bg_common_btn_pressed);

        }

        img_agree_establish_contract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAgree) { // 判断是否我已阅读（我已阅读）
                    img_agree_establish_contract.setBackgroundResource(R.drawable.icon_selected_unchecked);
                    btn_consumer_submit_button.setEnabled(false);
                    btn_consumer_submit_button.setBackgroundResource(R.drawable.bg_common_btn_pressed);
                } else { // 判断是否我已阅读（我未阅读）
                    img_agree_establish_contract.setBackgroundResource(R.drawable.icon_selected_checked);
                    btn_consumer_submit_button.setEnabled(true);
                    btn_consumer_submit_button.setBackgroundResource(R.drawable.bg_common_btn_blue);
                    btn_consumer_submit_button.setOnClickListener(new View.OnClickListener() { // 跳转到支付首款页面                              @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(FlowEstablishContractActivity.this, FlowFirstDesignActivity.class);
                            intent.putExtra(Constant.SeekDesignerDetailKey.DESIGNER_ID, designer_id);
                            intent.putExtra(Constant.SeekDesignerDetailKey.NEEDS_ID, needs_id);
                            intent.putExtra(Constant.SeekDesignerDetailKey.CONTRACT_NO, contract_number);
                            intent.putExtra(Constant.BundleKey.TEMPDATE_ID, MPStatusMachine.NODE__DESIGN_FIRST_PAY);
//                                    intent.putExtra(Constant.WorkFlowStateKey.JUMP_FROM_STATE, Constant.WorkFlowStateKey.STEP_FLOW);
                            startActivityForResult(intent, ContractForFirst);
                        }
                    });
                }

                isAgree = !isAgree;
            }
        });

    }




    /**
     * 设置输入框不可点击输入且无边框
     */
    private void UpdateUIdisableFieldsInput() {
        tvc_consumer_name.setEnabled(false);
        tvc_consumer_phone.setEnabled(false);
        tvc_consumer_email.setEnabled(false);
        tvc_first_cost.setEnabled(false);
        tvc_total_cost.setEnabled(false);
        tvc_last_cost.setEnabled(false);
        tvc_consumer_name.setBackgroundResource(0);
        tvc_consumer_phone.setBackgroundResource(0);
        tvc_consumer_email.setBackgroundResource(0);
        tvc_first_cost.setBackgroundResource(0);
        tvc_total_cost.setBackgroundResource(0);
        tvc_last_cost.setBackgroundResource(0);
        tvc_designer_name.setBackgroundResource(0);
        tvc_designer_decorate_address.setBackgroundResource(0);
        tvc_designer_email.setBackgroundResource(0);
        tvc_designer_phone.setBackgroundResource(0);
    }

    // work flow related
    @Override
    protected void onWorkFlowData() {
        super.onWorkFlowData();
        /**
         * 如果超过了33节点，就隐藏上传合同按钮
         */
        CustomProgress.cancelDialog();

        //Here we can determine if we already have a contract
        contract_data_entity = getContractDataEntityFromFirstBidder();
        if (contract_data_entity == null) {
            if (isRoleCustomer()) {

            }
        }

        if (StringUtils.isNumeric(wk_cur_sub_node_id) && Integer.valueOf(wk_cur_sub_node_id) >= 33) {
            btn_consumer_submit_button.setVisibility(View.GONE);

            ll_consumer_agree_content_layout.setVisibility(View.GONE);
        }

        //get designer infomation
        restgetDesignerInfoData(designer_id, hs_uid, new commonJsonResponseCallback() {
            @Override
            public void onJsonResponse(String jsonResponse) {
                designer_detail_entity = GsonUtil.jsonToBean(jsonResponse, DesignerInfoDetails.class);

                tvc_consumer_local_area.setText(designer_detail_entity.getProvince_name() + designer_detail_entity.getCity_name() + designer_detail_entity.getDistrict_name());
                tvc_designer_decorate_address.setText(designer_detail_entity.getProvince_name() + designer_detail_entity.getCity_name() + designer_detail_entity.getDistrict_name());

                designer_name = designer_detail_entity.getReal_name().getReal_name();
                designer_mobile = designer_detail_entity.getReal_name().getMobile_number().toString();
                designer_mail = designer_detail_entity.getEmail();

                UpdateUIlayoutContract();
            }

            @Override
            public void onError(VolleyError volleyError) {

            }
        });
        UpdateUIlayoutContract();
    }

    @Override
    protected void onPreCheckWorkFlowStep(int template_id,int sub_node_id){

        bDesignerActionShow = false;
        bconsumerActionShow = false;
        bAllowUserInput = true;

        contract_data_entity = getContractDataEntityFromFirstBidder();
        bDesignerContractCreated =(contract_data_entity != null)? (true):(false);

        if (!bDesignerContractCreated && isRoleCustomer()) {
            ShowAlertViewAndFinishActivity (UIUtils.getString(R.string.please_wait_designer_send_contract));
            return;
        }

        if (state == Constant.WorkFlowStateKey.STEP_MATERIAL ||(WorkFlowSubNodeStep() >= 33)) {// 从项目资料跳转.
            bAllowUserInput=false; /// 如果是已有的合同设置所有得按键都不可点击 .
        }


        bShowModeContentWebView=true;

        if(isRoleCustomer() )
        {
            if (WorkFlowSubNodeStep() == 31 || WorkFlowSubNodeStep() == 32)  /// 设计师发完合同后可以继续发送按钮显示 .
                bconsumerActionShow=true;
        }

        if(isRoleDesigner() )
        {
            if (WorkFlowTemplateStep() == 4 && WorkFlowSubNodeStep() == 11 )  /// 设计师发完合同后可以继续发送按钮显示 .
            {
                bDesignerActionShow = true;
                bShowModeContentWebView= false;
            }

            if (WorkFlowSubNodeStep() <= 31 || WorkFlowSubNodeStep() == 32 )  /// 设计师发完合同后可以继续发送按钮显示 .
            {
                bDesignerActionShow = true;
                bShowModeContentWebView= false;
            }

        }


    }

    private void ShowAlertViewAndFinishActivity(String msg){
        UIAlert = new AlertView(UIUtils.getString(R.string.tip),msg , null, null, new String[]{UIUtils.getString(R.string.sure)}, FlowEstablishContractActivity.this, AlertView.Style.Alert, FlowEstablishContractActivity.this).setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(Object o) {
                finish();
            }
        });
        UIAlert.show();
    }


    /**
     * @brief 监听方法 .
     */
    @Override
    protected void initListener() {
        super.initListener();
        llbtn_see_contract_detail.setOnClickListener(this);
        btn_designer_submit_button.setOnClickListener(this);
        tvc_consumer_local_area.setOnClickListener(this);
        btn_consumer_submit_button.setOnClickListener(this);
        tvc_first_cost.addTextChangedListener(new EditTextWatcher(tvc_first_cost));
        tvc_total_cost.addTextChangedListener(new EditTextWatcher(tvc_total_cost));
        tvc_consumer_local_area.addTextChangedListener(new EditTextWatcher(tvc_consumer_local_area));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_flow_examine_contract:
                doClickOpenContractDetail();

                break;
            case R.id.btn_send_establish_contract:

                break;
            case R.id.btn_send_establish_contract_designer:
                if (checkValidFormInputWithErrorAlertView())
                    submitDesignContract();
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == FirstForContract) {
            finish();
        }
        if (resultCode == ContractDetail || resultCode == ContractForFirst) {
            fetchWorkFlowData();
        }

    }

    @Override
    public void onItemClick(Object o, int position) {
        CustomProgress.cancelDialog();
    }

    private void doClickOpenContractDetail() {

        Intent intent = new Intent(FlowEstablishContractActivity.this, FlowWebContractDetailActivity.class);
        intent.putExtra(Constant.SeekDesignerDetailKey.DESIGNER_ID, designer_id);
        intent.putExtra(Constant.SeekDesignerDetailKey.NEEDS_ID, needs_id);
        intent.putExtra(Constant.SeekDesignerDetailKey.CONTRACT_NO, contract_number);
        intent.putExtra("CONSUMER_ACTION_SHOW", bconsumerActionShow);
        startActivityForResult(intent, ContractDetail);


    }


    //contract data method
    private MPDesignContractBean getContractDataEntityFromFirstBidder() {
        MPDesignContractBean designContractEntity = mBidders.get(0).getDesign_contract();
        if (null == designContractEntity) // 如果设计师没有发送设计合同 .
            return null;

        return designContractEntity;
    }

    private MPContractDataBean getContractDetailData(MPDesignContractBean contractEntity) {
        String contractData = contractEntity.getContract_data();
        final String str = contractData.toString().replace("@jr@", "\""); /// 将@jr@转换成引号格式，以便读取 .
        MPContractDataBean designContractDetail = GsonUtil.jsonToBean(str, MPContractDataBean.class);

        return designContractDetail;
    }

    //Async method
    public void AsyncGetContractNumber(final commonJsonResponseCallback callBack) {
        MPServerHttpManager.getInstance().getContractNumber(new OkJsonRequest.OKResponseCallback() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                String jsonString = GsonUtil.jsonToString(jsonObject);
                callBack.onJsonResponse(jsonString);

            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                MPNetworkUtils.logError(TAG, volleyError, true);
                callBack.onError(volleyError);
            }
        });
    }
    /**
     * 发送设计合同
     */
    public void sendEstablishContract(String needs_id, String Member_Type, JSONObject jsonObject) {
        OkJsonRequest.OKResponseCallback callback = new OkJsonRequest.OKResponseCallback() {

            @Override
            public void onResponse(JSONObject jsonObject) {
                KLog.d(TAG, jsonObject.toString());
                ContractState = 0;
                CustomProgress.cancelDialog();
                UIAlert = new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(R.string.the_contract_sent_successfully), null, null, new String[]{UIUtils.getString(R.string.sure)}, FlowEstablishContractActivity.this, AlertView.Style.Alert, FlowEstablishContractActivity.this).setOnDismissListener(FlowEstablishContractActivity.this);
                UIAlert.show();
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                MPNetworkUtils.logError(TAG, volleyError);
                CustomProgress.cancelDialog();
                UIAlert = new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(R.string.the_contract_sent_failure), null, null, new String[]{UIUtils.getString(R.string.sure)}, FlowEstablishContractActivity.this, AlertView.Style.Alert, FlowEstablishContractActivity.this).setOnDismissListener(FlowEstablishContractActivity.this);
                UIAlert.show();
            }
        };
        MPServerHttpManager.getInstance().sendEstablishContract(needs_id, Member_Type, jsonObject, callback);
    }

    /**
     * 修改:         consumer/src/main/res/layout/activity_flow_establish_contract.xml
     *
     * @brief 发送设计合同内容 .
     */
    private boolean checkValidFormInputWithErrorAlertView() {

        String total_cost = tvc_total_cost.getText().toString();
        String first_cost = tvc_first_cost.getText().toString();

        String consumerName = tvc_consumer_name.getText().toString();
        String consumerPhone = tvc_consumer_phone.getText().toString();
        String consumerEmail = tvc_consumer_email.getText().toString();
        String renderCount = tvc_treeD_render_count.getText().toString();
        String location_area = tvc_consumer_local_area.getText().toString();
        boolean bValid = true;

        while (true) {
            if (!Validator.isContractNameValid(consumerName)) {
                showAlertView(R.string.no_input_name);
                bValid = false;
                break;
            }

            if (!Validator.isMobileValid(consumerPhone)) {
                showAlertView(R.string.please_fill_in_the_right_phone_number);
                bValid = false;
                break;
            }
            //REFACTOR
            if (!consumerEmail.equals("")) {
                if (!consumerEmail.matches(RegexUtil.EMAIL_REGEX)) {
                    showAlertView(R.string.please_fill_in_the_right_phone_email);
                    bValid = false;
                    break;
                }

            }

            if (!Validator.isAddressValid(location_area)) {
                showAlertView(R.string.demand_please_project_address);
                bValid = false;
                break;
            }

            if (!Validator.isStringPositiveNumberValid(renderCount)) {
                showAlertView(R.string.please_fill_in_the_number_of_rendering);
                bValid = false;
                break;
            }

            if (!Validator.isStringValid(total_cost)) {
                new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(R.string.please_fill_in_the_total_project_design), null, new String[]{UIUtils.getString(R.string.sure)}, null, FlowEstablishContractActivity.this, AlertView.Style.Alert, null).show();
                bValid = false;
                break;
            }

            if (!Validator.isStringValid(first_cost)) {
                new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(R.string.please_fill_out_the_design_first), null, new String[]{UIUtils.getString(R.string.sure)}, null, FlowEstablishContractActivity.this, AlertView.Style.Alert, null).show();
                bValid = false;
                break;
            }

            if (!checkAmontContractValidwithResult(mBiddersEntity.getMeasurement_fee(), total_cost, first_cost)) {
                bValid = false;
                break;
            }
            break;
        }
        return bValid;
    }

    public boolean checkAmontContractValidwithResult(String meansurePrice, String total, String downpay) {

        BigDecimal loanAmount = new BigDecimal(total); //
        BigDecimal interestRate = new BigDecimal("0.8"); //
        BigDecimal interest = loanAmount.multiply(interestRate); //

        BigDecimal decimal = new BigDecimal(interest.toString());
        decimal = decimal.setScale(3, RoundingMode.HALF_UP); // 保留小数点后三位

        double totalCost = Double.parseDouble(total);
        double discountCost = Double.parseDouble(decimal.toString());
        double firstCost = Double.parseDouble(downpay);

        boolean bValid = true;

        while (true) {
            if (!Validator.isStringValid(downpay)) {
                new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(R.string.please_fill_out_the_design_first), null, new String[]{UIUtils.getString(R.string.sure)}, null, FlowEstablishContractActivity.this, AlertView.Style.Alert, null).show();
                bValid = false;
                break;
            }

            if (!Validator.isStringValid(downpay)) {
                new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(R.string.costs_cannot_be_negative), null, new String[]{UIUtils.getString(R.string.sure)}, null, FlowEstablishContractActivity.this, AlertView.Style.Alert, null).show();
                bValid = false;
                break;
            }

            if (firstCost < discountCost) {
                new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(R.string.room_first_less_than_eighty_percent_of_the_total_amount_of_design), null, new String[]{UIUtils.getString(R.string.sure)}, null, FlowEstablishContractActivity.this,
                        AlertView.Style.Alert, null).show();
                bValid = false;
                break;
            }

            if (firstCost > totalCost) {
                new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(R.string.total_quantity_room_first_is_not_greater_than_design), null, new String[]{UIUtils.getString(R.string.sure)}, null, FlowEstablishContractActivity.this,
                        AlertView.Style.Alert, null).show();
                bValid = false;
                break;
            }

            if (firstCost <= Double.valueOf(meansurePrice)) {
                new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(R.string.room_first_less_than_eighty_percent_of_the_total_amount_measure_fee), null, new String[]{UIUtils.getString(R.string.sure)}, null, FlowEstablishContractActivity.this,
                        AlertView.Style.Alert, null).show();
                bValid = false;
                break;
            }
            break;
        }


        return bValid;
    }

    /// 发送设计合同方法 .
    private void submitDesignContract() {

        JSONObject jsonO = new JSONObject();
        String total_cost = tvc_total_cost.getText().toString();
        String first_cost = tvc_first_cost.getText().toString();

        String consumerName = tvc_consumer_name.getText().toString();
        String consumerPhone = tvc_consumer_phone.getText().toString();
        String consumerEmail = tvc_consumer_email.getText().toString();
        String renderCount = tvc_treeD_render_count.getText().toString();
        String location_area = tvc_consumer_local_area.getText().toString();

        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put(Constant.EstablishContractKey.NAME, consumerName);
            jsonObj.put(Constant.EstablishContractKey.ADDR, location_area);
            jsonObj.put(Constant.EstablishContractKey.MOBILE, consumerPhone);
            jsonObj.put(Constant.EstablishContractKey.EMAIL, consumerEmail);
            jsonObj.put(Constant.EstablishContractKey.RENDER_MAP, renderCount);
            jsonO.put(Constant.EstablishContractKey.CONTRACT_NO, contract_no); // 合同编号
            jsonO.put(Constant.EstablishContractKey.CONTRACT_CHARGE, total_cost); // 设计总额
            jsonO.put(Constant.EstablishContractKey.CONTRACT_FIRST_CHARGE, first_cost); // 设计首款
            jsonO.put(Constant.EstablishContractKey.DESIGNER_ID, designer_id);
            jsonO.put(Constant.EstablishContractKey.CONTRACT_TYPE, 0);
            jsonO.put(Constant.EstablishContractKey.CONTRACT_TEMPLATE_URL, "www.baidu.com");
            jsonO.put(Constant.EstablishContractKey.CONTRACT_DATA, jsonObj.toString().replace("\"", "@jr@"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomProgress.show(FlowEstablishContractActivity.this, UIUtils.getString(R.string.in_contract_sent), false, null);
        sendEstablishContract(needs_id, ((memberEntity != null) ? memberEntity.getMember_type() : null), jsonO);

    }

    //Web view client
    private class webViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private final class WebViewJavaScriptCallback {

        @JavascriptInterface
        public void onClickContractDetail() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doClickOpenContractDetail();
                
                }
            });
        }
    }


    /**
     * 监听EditText内容变化的内部类 .
     */
    public class EditTextWatcher implements TextWatcher {
        private View view;

        public EditTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (view == tvc_total_cost) { /// 监听总款 .
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(0, s.toString().indexOf(".") + 3);
                        tvc_total_cost.setText(s);
                        tvc_total_cost.setSelection(s.length());
                    }
                }

                if (s.toString().trim().substring(0).equals(".")) {
                    s = "0" + s;
                    tvc_total_cost.setText(s);
                    tvc_total_cost.setSelection(2);
                }

                if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        tvc_total_cost.setText(s.subSequence(0, 1));
                        tvc_total_cost.setSelection(1);
                        return;
                    }
                }
                //REFACTOR
                if (jusnFrist) {

                    String firstCost;
                    String totalCost;
                    if (tvc_total_cost.getText().toString().isEmpty()) {
                        totalCost = "0";
                    } else {
                        totalCost = tvc_total_cost.getText().toString();
                    }
                    if (tvc_first_cost.getText().toString().isEmpty()) {
                        firstCost = "0";
                    } else {
                        firstCost = tvc_first_cost.getText().toString();
                    }


                    Double dTotalCost = Double.parseDouble(totalCost);
                    DecimalFormat df = new DecimalFormat("#.##"); // 保留小数点后两位
                    tvc_last_cost.setText(df.format((dTotalCost - Double.parseDouble(firstCost))));

                }

                jusnFrist = true;

            }
            if (view == tvc_first_cost) { /// 监听首款 .
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + 3);
                        tvc_first_cost.setText(s);
                        tvc_first_cost.setSelection(s.length());
                    }
                }

                if (s.toString().trim().substring(0).equals(".")) {
                    s = "0" + s;
                    tvc_first_cost.setText(s);
                    tvc_first_cost.setSelection(2);
                }

                if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        tvc_first_cost.setText(s.subSequence(0, 1));
                        tvc_first_cost.setSelection(1);
                        return;
                    }
                }

                String firstCost;
                String totalCost;
                if (tvc_total_cost.getText().toString().isEmpty()) {
                    totalCost = "0";
                } else {
                    totalCost = tvc_total_cost.getText().toString();
                }
                if (tvc_first_cost.getText().toString().isEmpty()) {
                    firstCost = "0";
                } else {
                    firstCost = s.toString();
                }
                Double dTotalCost = Double.parseDouble(totalCost);
                DecimalFormat df = new DecimalFormat("#.##"); // 保留小数点后两位
                tvc_last_cost.setText(df.format((dTotalCost - Double.parseDouble(firstCost))));
            }

            if (view == tvc_consumer_local_area) { /// 监听项目地址，服务地址跟项目地址同步 .
                tvc_designer_decorate_address.setText(s.toString());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    @Override
    public void onDismiss(Object o) {
        if (ContractState == 0) {
            finish();
        }
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }

    /**
     * 隐藏软键盘 .
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }

        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    private void showAlertView(int content) {
        new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(content), null, new String[]{UIUtils.getString(R.string.sure)}, null, FlowEstablishContractActivity.this,
                AlertView.Style.Alert, null).show();
    }

    MPDesignFormatValidator Validator = new MPDesignFormatValidator();

    private static final String ANDROID = "android";
    private TextViewContent tvc_consumer_name;
    private TextViewContent tvc_consumer_phone;
    private TextViewContent tvc_consumer_email;
    private TextViewContent tvc_treeD_render_count;
    private TextViewContent tvc_designer_name;
    private TextViewContent tvc_designer_phone;
    private TextViewContent tvc_designer_email;
    private TextViewContent tvc_designer_decorate_address;
    private TextView tvc_consumer_local_area;
    private TextViewContent tvc_total_cost;
    private TextViewContent tvc_first_cost;
    private TextViewContent tvc_last_cost;
    private ImageView img_agree_establish_contract;
    private Button btn_consumer_submit_button;
    private Button btn_designer_submit_button;




    private LinearLayout llbtn_see_contract_detail;
    private LinearLayout ll_consumer_action_layout;
    private LinearLayout ll_designer_action_layout;
    private LinearLayout ll_consumer_agree_content_layout;
    private LinearLayout ll_contract_input_form_layout;
    private LinearLayout ll_contract_webview_layout;

    private MPDesignContractBean contract_data_entity;
    private DesignerInfoDetails designer_detail_entity;
    private MPContractNoBean contractNo; // 设计合同编号对象
    private AlertView UIAlert;
    private WebView twvc_contract_content_webview;

    private boolean jusnFrist = false;
    private int ContractState = -1; // 判断合同是否发送成功弹出框的点击事件
    private int ContractForFirst = 0; //　从合同跳转到设计首款
    private int FirstForContract = 1; // 首款调到设计合同
    private int ContractDetail = 2;
    private boolean isAgree = false;

    private String contract_no; // 设计合同编号
    private String designer_name = "";
    private String designer_mobile = "";
    private String designer_mail = "";

    private String contract_number; //　合同编号
    private String contract_date;
    private String design_amount;
    private String design_amount_first;
    private String design_amount_balance;

    private boolean bDesignerActionShow = false;
    private boolean bconsumerActionShow = false;
    private boolean bDesignerContractCreated = false;
    private boolean bAllowUserInput = false;
    private boolean bShowModeContentWebView = false;

}
