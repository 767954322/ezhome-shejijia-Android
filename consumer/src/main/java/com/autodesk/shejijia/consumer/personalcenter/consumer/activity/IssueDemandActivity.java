package com.autodesk.shejijia.consumer.personalcenter.consumer.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.autodesk.shejijia.consumer.R;
import com.autodesk.shejijia.consumer.manager.MPServerHttpManager;
import com.autodesk.shejijia.consumer.manager.constants.JsonConstants;
import com.autodesk.shejijia.consumer.personalcenter.consumer.entity.ConsumerEssentialInfoEntity;
import com.autodesk.shejijia.consumer.personalcenter.consumer.entity.IssueDemandBean;
import com.autodesk.shejijia.consumer.utils.ApiStatusUtil;
import com.autodesk.shejijia.consumer.utils.AppJsonFileReader;
import com.autodesk.shejijia.shared.components.common.appglobal.MemberEntity;
import com.autodesk.shejijia.shared.components.common.network.OkJsonRequest;
import com.autodesk.shejijia.shared.components.common.uielements.AddressDialog;
import com.autodesk.shejijia.shared.components.common.uielements.CustomProgress;
import com.autodesk.shejijia.consumer.uielements.HomeTypeDialog;
import com.autodesk.shejijia.shared.components.common.uielements.alertview.AlertView;
import com.autodesk.shejijia.shared.components.common.uielements.alertview.OnItemClickListener;
import com.autodesk.shejijia.shared.components.common.uielements.reusewheel.utils.OptionsPickerView;
import com.autodesk.shejijia.consumer.base.utils.ConvertUtils;
import com.autodesk.shejijia.shared.components.common.utility.GsonUtil;
import com.autodesk.shejijia.shared.components.common.utility.LogUtils;
import com.autodesk.shejijia.shared.components.common.utility.MPNetworkUtils;
import com.autodesk.shejijia.shared.components.common.utility.RegexUtil;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;
import com.autodesk.shejijia.shared.framework.AdskApplication;
import com.autodesk.shejijia.shared.framework.activity.NavigationBarActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author he.liu
 * @version v1.0 .
 * @date 2016-6-6 .
 * @file IssueDemandActivity.java .
 * @brief 消费者发布需求.
 */
public class IssueDemandActivity extends NavigationBarActivity implements View.OnClickListener, OnItemClickListener,TextWatcher {


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            nick_name = (String) msg.obj;
            et_issue_demand_name.setText(nick_name);
        }
    };
    private HomeTypeDialog homeTypeDialog;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_demand;
    }

    @Override
    protected void initView() {
        super.initView();
        ll_issue_house_type = (LinearLayout) findViewById(R.id.ll_issue_house_type);
        ll_issue_style = (LinearLayout) findViewById(R.id.ll_issue_style);
        et_issue_demand_name = (EditText) findViewById(R.id.et_issue_demand_name);
        nav_title_textView = (TextView) findViewById(R.id.nav_title_textView);
        et_issue_demand_mobile = (EditText) findViewById(R.id.et_issue_demand_mobile);
        et_issue_demand_area = (EditText) findViewById(R.id.et_issue_demand_area);
        btn_send_demand = (Button) findViewById(R.id.btn_send_demand);
        tv_issue_house_type = (TextView) findViewById(R.id.tv_issue_house_type);
        tv_issue_demand_design_budget = (TextView) findViewById(R.id.tv_issue_demand_design_budget);
        tv_issue_demand_budget = (TextView) findViewById(R.id.tv_issue_demand_budget);
        tv_issue_room = (TextView) findViewById(R.id.tv_issue_room);
        tv_issue_style = (TextView) findViewById(R.id.tv_issue_style);
        tv_issue_address = (TextView) findViewById(R.id.tv_issue_address);
        tv_issue_demand_detail_address = (EditText) findViewById(R.id.tv_issue_demand_detail_address);
        ll_house_type_demand = (LinearLayout) findViewById(R.id.ll_house_type_demand);
        ll_line = (LinearLayout) findViewById(R.id.ll_line);


        et_issue_demand_area.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String area = et_issue_demand_area.getText().toString().trim();
                    if (TextUtils.isEmpty(area)) {
                        area = "0";
                    }
                    area = String.format("%.2f", Double.valueOf(area));
                    et_issue_demand_area.setText(area);
                }
            }
        });

    }

    @Override
    protected void initExtraBundle() {
        super.initExtraBundle();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);

        nav_title_textView.setTextColor(UIUtils.getColor(R.color.comment_gray));
        setTitleForNavbar(UIUtils.getString(R.string.requirements));
        MemberEntity mMemberEntity = AdskApplication.getInstance().getMemberEntity();
        member_id = mMemberEntity.getAcs_member_id();
        if (!TextUtils.isEmpty(member_id)) {
            getConsumerInfoData(member_id);
        }
        /// 房屋类型.
        setHouseType();
        /// 房屋风格.
        setStyleType();
        /// 室、厅、卫.
        setRoomType();
        /// 设计预算.
        setDesignBudget();
        /// 装修预算 .
        setDecorationBudget();
        ///提示框
        initAlertView();

    }

    @Override
    protected void initListener() {
        super.initListener();
        btn_send_demand.setOnClickListener(this);
        ll_issue_house_type.setOnClickListener(this);
        tv_issue_room.setOnClickListener(this);
        ll_issue_style.setOnClickListener(this);
        tv_issue_demand_budget.setOnClickListener(this);
        tv_issue_demand_design_budget.setOnClickListener(this);
        tv_issue_address.setOnClickListener(this);

        et_issue_demand_area.addTextChangedListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ll_issue_house_type: /// 房屋类型 .
                pvHouseTypeOptions.show();
                et_issue_demand_area.clearFocus();
                et_issue_demand_mobile.clearFocus();
                break;

            case R.id.tv_issue_room: /// 请选择户型：室 厅 卫 .
                homeTypeDialog.show(getFragmentManager(), null);
                et_issue_demand_area.clearFocus();
                et_issue_demand_mobile.clearFocus();
                break;

            case R.id.ll_issue_style: /// 风格 .
                pvStyleOptions.show();
                et_issue_demand_area.clearFocus();
                et_issue_demand_mobile.clearFocus();
                break;

            case R.id.tv_issue_demand_budget: /// 请选择装修预算 .
                pvDecorationBudgetOptions.show();
                et_issue_demand_area.clearFocus();
                et_issue_demand_mobile.clearFocus();
                break;

            case R.id.tv_issue_demand_design_budget: /// 请选择设计预算 .
                pvDesignBudgetOptions.show();
                et_issue_demand_area.clearFocus();
                et_issue_demand_mobile.clearFocus();
                break;

            case R.id.tv_issue_address: /// 请选择地址：省 市 区 .
                getPCDAddress();
                et_issue_demand_area.clearFocus();
                et_issue_demand_mobile.clearFocus();
                break;

            case R.id.btn_send_demand: /// 提交 .
                if (!isSendState) {
                    return;
                }
                et_issue_demand_area.clearFocus();
                et_issue_demand_mobile.clearFocus();
                String area = et_issue_demand_area.getText().toString();


//                if(area.equals("0.00")||area.equals("0.0")||area.equals("0000.00")||area.equals("000.00")||area.equals("00.00")){
//                    showAlertView(R.string.please_input_correct_area);
//                    return;
//                }

                String mobile = et_issue_demand_mobile.getText().toString();
                String detail_address = tv_issue_demand_detail_address.getText().toString();
                String consumer_name = et_issue_demand_name.getText().toString();

                // ----------------以下code by zjl------------------
                // 增加姓名校验
                boolean matches = consumer_name.matches(RegexUtil.NICK_NAME_REGEX);
                if (!matches || TextUtils.isEmpty(consumer_name)) {
                    new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(R.string.check_name_tip),
                            null, null, new String[]{UIUtils.getString(R.string.sure)}, IssueDemandActivity.this, AlertView.Style.Alert, null).show();
                    return;
                }
                // ----------------以上code by zjl------------------

                boolean regex_area_right = area.matches(RegexUtil.AREA_REGEX);
                boolean phoneRight = mobile.matches(RegexUtil.PHONE_REGEX);
                boolean regex_address_right = detail_address.matches(RegexUtil.ADDRESS_REGEX);

                if (TextUtils.isEmpty(mobile) || !phoneRight) {
                    showAlertView(R.string.please_enter_correct_phone_number);
                    return;
                }

                if (TextUtils.isEmpty(house_type)) {
                    showAlertView(R.string.demand_please_project_types);
                    return;
                }
//                if (TextUtils.isEmpty(area) || area.equals("0") || !regex_area_right) {
//                    showAlertView(R.string.please_input_correct_area);
//                    return;
//                }

                //..................................
                if (TextUtils.isEmpty(area)) {
                    area = "0";
                }
                area = String.format("%.2f", Double.valueOf(area));
                if (Double.valueOf(area) < 1 || Double.valueOf(area) > 9999) {
                    showAlertView(R.string.alert_msg_area);
                    return;
                }

                et_issue_demand_area.setText(area);
                String subNum = "0";
                if (area.contains(".")) {
                    subNum = area.substring(0, area.indexOf("."));
                }
                if (TextUtils.isEmpty(area) || Float.valueOf(area) == 0) {
                    showAlertView(R.string.please_input_correct_area);
                    return;
                } else {
                    if ((subNum.length() > 1 && subNum.startsWith("0")) || subNum.length() > 4) {
                        showAlertView(R.string.please_input_correct_area);
                        return;
                    } else {
                        if (!area.matches("^[0-9]{1,4}+(.[0-9]{1,2})?$") || subNum.length() > 4) {
                            showAlertView(R.string.please_input_correct_area);
                            return;
                        }
                    }
                }

                if (TextUtils.isEmpty(mDesignBudget)) {
                    showAlertView(R.string.please_select_design_budget);
                    return;
                }
                if (TextUtils.isEmpty(mDecorationBudget)) {
                    showAlertView(R.string.please_select_decorate_budget);
                    return;
                }

                if (tv_issue_house_type.getText().toString().equals("住宅空间")){

                    if (TextUtils.isEmpty(mRoom)) {
                        showAlertView(R.string.please_select_form);
                        return;
                    }
                }else {

                    mLivingRoom = null;
                    mRoom = "other";
                    mToilet = null;
                }
                if (TextUtils.isEmpty(style)) {
                    showAlertView(R.string.please_select_style);
                    return;
                }
                if (TextUtils.isEmpty(mCurrentDistrictCode)) {
                    showAlertView(R.string.please_select_addresses);
                    return;
                }
                if (TextUtils.isEmpty(detail_address) || !regex_address_right) {
                    showAlertView(R.string.please_enter_correct_address);
                    return;
                }
                success = "SUCCESS";
                JSONObject jsonObject = new JSONObject();
                try {
                    String click_number = "0";
                    jsonObject.put(JsonConstants.JSON_SEND_DESIGN_REQUIREMENTS_CITY, mCurrentCityCode);/// city = 110100; .
                    jsonObject.put(JsonConstants.JSON_SEND_DESIGN_REQUIREMENTS_CITY_NAME, mCurrentCity);/// "city_name" = "\U5317\U4eac\U5e02"; .
                    jsonObject.put(JsonConstants.JSON_SEND_DESIGN_REQUIREMENTS_CLICK_NUMBER, click_number);/// "click_number" = 0; .
                    jsonObject.put(JsonConstants.JSON_SEND_DESIGN_REQUIREMENTS_COMMUNITY_NAME, detail_address);/// "community_name" = "\U9ece\U6d1b\U7fbd";.
                    jsonObject.put(JsonConstants.JSON_SEND_DESIGN_REQUIREMENTS_CONSUMER_MOBILE, mobile);/// "consumer_mobile" = 11012011900; .
                    jsonObject.put(JsonConstants.JSON_SEND_DESIGN_REQUIREMENTS_CONSUMER_NAME, consumer_name);/// "consumer_name" = "APP\U7aef\U53d1\U5e03\U9700\U6c42-\U6b64\U5b57\U6bb5\U4e0d\U7528"; .
                    jsonObject.put(JsonConstants.JSON_MODIFY_DESIGNER_REQUIREMENT_CONTACTS_MOBILE, mobile);/// "contacts_mobile" = 15234948734; .
                    jsonObject.put(JsonConstants.JSON_MODIFY_DESIGNER_REQUIREMENT_CONTACTS_NAME, consumer_name);/// "contacts_name" = "\U63a5\U4f60"; .
                    jsonObject.put(JsonConstants.JSON_SEND_DESIGN_REQUIREMENTS_DECORATION_BUDGET, mDecorationBudget);/// "decoration_budget" = "5\U4e07\U4ee5\U4e0b"; .
                    jsonObject.put(JsonConstants.JSON_SEND_DESIGN_REQUIREMENTS_DECORATION_STYLE, style);/// "decoration_style" = Japan; .
                    jsonObject.put(JsonConstants.JSON_MEASURE_FORM_DESIGN_BUDGET, mDesignBudget);/// "design_budget" = "3000\U4ee5\U4e0b"; .
                    jsonObject.put(JsonConstants.JSON_SEND_DESIGN_REQUIREMENTS_DETAIL_DESC, "desc");/// "detail_desc" = desc; .
                    jsonObject.put(JsonConstants.JSON_SEND_DESIGN_REQUIREMENTS_DISTRICT, mCurrentDistrictCode);/// district_name = 110101; .
                    jsonObject.put(JsonConstants.JSON_SEND_DESIGN_REQUIREMENTS_DISTRICT_NAME, mCurrentDistrict);/// "district_name_name" = "\U4e1c\U57ce\U533a"; .
                    jsonObject.put(JsonConstants.JSON_MODIFY_DESIGNER_REQUIREMENT_HOUSE_AREA, area);/// "house_area" = 36; .
                    jsonObject.put(JsonConstants.JSON_MODIFY_DESIGNER_REQUIREMENT_HOUSE_TYPE, house_type);/// "house_type" = house; .
                    jsonObject.put(JsonConstants.JSON_MODIFY_DESIGNER_REQUIREMENT_LIVING_ROOM, mLivingRoom);/// "living_room" = one; .
                    jsonObject.put(JsonConstants.JSON_SEND_DESIGN_REQUIREMENTS_PROVINCE, mCurrentProvinceCode);/// province = 110000; .
                    jsonObject.put(JsonConstants.JSON_SEND_DESIGN_REQUIREMENTS_PROVINCE_NAME, mCurrentProvince);/// "province_name" = "\U5317\U4eac"; .
                    jsonObject.put(JsonConstants.JSON_MODIFY_DESIGNER_REQUIREMENT_ROOM, mRoom);///  room = one; .
                    jsonObject.put(JsonConstants.JSON_SEND_DESIGN_REQUIREMENTS_TOILET, mToilet);///  toilet = one; .
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                isSendState = false;
                CustomProgress.show(this, UIUtils.getString(R.string.data_submission), false, null);
                sendDesignRequirements(jsonObject);
                break;
        }
    }

    /**
     * @brief 打开AlertView对话框
     */
    private void showAlertView(int content) {
        new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(content), null, null, new String[]{UIUtils.getString(R.string.sure)}, IssueDemandActivity.this, AlertView.Style.Alert, null).show();
    }

    /**
     * @brief 把String数组转成List集合
     */
    private List<String> filledData(String[] date) {
        List<String> mSortList = new ArrayList<String>();
        for (String str : date) {
            mSortList.add(str);
        }
        return mSortList;
    }

    /**
     * @brief 获取省市区地址
     */
    private void getPCDAddress() {
        mChangeAddressDialog = new AddressDialog();
        mChangeAddressDialog.show(getFragmentManager(), "mChangeAddressDialog");
        mChangeAddressDialog
                .setAddressListener(new AddressDialog.OnAddressCListener() {
                    @Override
                    public void onClick(String province, String provinceCode, String city, String cityCode, String area, String areaCode) {
                        mCurrentProvince = province;
                        mCurrentProvinceCode = provinceCode;
                        mCurrentCity = city;
                        mCurrentCityCode = cityCode;
                        // 由于有些地区没有区这个字段，将含有区域得字段name改为none，code改为0
                        mCurrentDistrict = area;
                        mCurrentDistrictCode = areaCode;

                        area = UIUtils.getNoStringIfEmpty(area);

                        tv_issue_address.setText(province + " " + city + " " + area);
                        mChangeAddressDialog.dismiss();
                    }

                });
    }


    /**
     * 发布需求
     *
     * @param jsonObject
     */
    private void sendDesignRequirements(JSONObject jsonObject) {
        MPServerHttpManager.getInstance().sendDesignRequirements(jsonObject, false, new OkJsonRequest.OKResponseCallback() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                CustomProgress.cancelDialog();
                String str = GsonUtil.jsonToString(jsonObject);
                LogUtils.i(TAG, str);
                IssueDemandBean issueDemandBean = GsonUtil.jsonToBean(str, IssueDemandBean.class);
                if (issueDemandBean != null && issueDemandBean.getNeeds_id() != null && issueDemandBean.getNeeds_id().length() > 0) {
                    mSendDesignRequirementSuccessAlertView.show();
                }
                isSendState = true;
                Intent intent = new Intent();
                intent.putExtra("SUCCESS", success);
                setResult(RESULT_CODE, intent);
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                MPNetworkUtils.logError(TAG, volleyError);
                isSendState = true;
                CustomProgress.cancelDialog();
                ApiStatusUtil.getInstance().apiStatuError(volleyError, IssueDemandActivity.this);
            }
        });
    }

    /**
     * 获取个人基本信息
     *
     * @param member_id
     * @brief For details on consumers .
     */
    public void getConsumerInfoData(String member_id) {
        CustomProgress.show(this, "", false, null);

        MPServerHttpManager.getInstance().getConsumerInfoData(member_id, new OkJsonRequest.OKResponseCallback() {

            @Override
            public void onResponse(JSONObject jsonObject) {
                String jsonString = GsonUtil.jsonToString(jsonObject);
                ConsumerEssentialInfoEntity mConsumerEssentialInfoEntity = GsonUtil.jsonToBean(jsonString, ConsumerEssentialInfoEntity.class);
                CustomProgress.cancelDialog();

                // 使用手机注册的手机号码
                String mobile_number = mConsumerEssentialInfoEntity.getMobile_number();
                if (!TextUtils.isEmpty(mobile_number)){

                    et_issue_demand_mobile.setText(mobile_number);
                }

                Message msg = new Message();
                msg.obj = mConsumerEssentialInfoEntity.getNick_name();
                handler.sendMessage(msg);
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                CustomProgress.cancelDialog();

                MPNetworkUtils.logError(TAG, volleyError);
            }
        });
    }


    /**
     * @brief 设置室 厅 卫
     */
    private void setRoomType() {
        homeTypeDialog = HomeTypeDialog.getInstance(this);
        homeTypeDialog.setOnAddressCListener(new HomeTypeDialog.OnAddressCListener() {
            @Override
            public void onClick(String roomName, String livingRoom, String toilet) {
                String roomType = roomName + livingRoom + toilet;
                tv_issue_room.setText(roomType);

                /// convet .
                Map<String, String> livingRoomMap = AppJsonFileReader.getLivingRoom(IssueDemandActivity.this);
                Map<String, String> roomHall = AppJsonFileReader.getRoomHall(IssueDemandActivity.this);
                Map<String, String> toiletMap = AppJsonFileReader.getToilet(IssueDemandActivity.this);

                mLivingRoom = ConvertUtils.getKeyByValue(livingRoomMap, livingRoom);
                mRoom = ConvertUtils.getKeyByValue(roomHall, roomName);
                mToilet = ConvertUtils.getKeyByValue(toiletMap, toilet);

                homeTypeDialog.dismiss();
            }
        });
    }

    /**
     * @brief 风格
     */
    private void setStyleType() {
        final ArrayList<String> styleItems = new ArrayList<>();
        List<String> styles = filledData(UIUtils.getStringArray(R.array.style));

        pvStyleOptions = new OptionsPickerView(this);
        for (String item : styles) {
            styleItems.add(item);
        }
        pvStyleOptions.setPicker(styleItems);
        pvStyleOptions.setSelectOptions(0);
        pvStyleOptions.setCyclic(false);
        pvStyleOptions.setTitle(UIUtils.getString(R.string.demand_please_style_title));
        pvStyleOptions.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                style = styleItems.get(options1);
                tv_issue_style.setText(style);
                Map<String, String> space = AppJsonFileReader.getStyle(IssueDemandActivity.this);
                style = ConvertUtils.getKeyByValue(space, style);
            }
        });
    }

    /**
     * 设置房屋类型
     */
    private void setHouseType() {
        final ArrayList<String> houseTypeItems = new ArrayList<>();
        List<String> houseType = filledData(UIUtils.getStringArray(R.array.hType));

        pvHouseTypeOptions = new OptionsPickerView(this);
        for (String item : houseType) {
            houseTypeItems.add(item);
        }
        pvHouseTypeOptions.setPicker(houseTypeItems);
        pvHouseTypeOptions.setSelectOptions(0);
        pvHouseTypeOptions.setCyclic(false);
        pvHouseTypeOptions.setTitle(UIUtils.getString(R.string.demand_project_types_title));
        pvHouseTypeOptions.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                house_type = houseTypeItems.get(options1);
                tv_issue_house_type.setText(house_type);

                if (house_type.equals("住宅空间")){

                    ll_house_type_demand.setVisibility(View.VISIBLE);
                    ll_line.setVisibility(View.VISIBLE);
                    tv_issue_room.setHint("请选择户型");
                }else {
                    ll_house_type_demand.setVisibility(View.GONE);
                    ll_line.setVisibility(View.GONE);
                }

                Map<String, String> space = AppJsonFileReader.getSpace(IssueDemandActivity.this);
                house_type = ConvertUtils.getKeyByValue(space, house_type);
            }
        });
    }

    /**
     * 设置设计预算
     */
    private void setDesignBudget() {
        final ArrayList<String> designBudgetItems = new ArrayList<>();
        List<String> design_budgets = filledData(getResources().getStringArray(R.array.design_budget));
        pvDesignBudgetOptions = new OptionsPickerView(this);

        for (String item : design_budgets) {
            designBudgetItems.add(item);
        }
        pvDesignBudgetOptions.setPicker(designBudgetItems);
        pvDesignBudgetOptions.setSelectOptions(2);
        pvDesignBudgetOptions.setCyclic(false);
        pvDesignBudgetOptions.setTitle(UIUtils.getString(R.string.demand_planning_budget_title));
        pvDesignBudgetOptions.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                mDesignBudget = designBudgetItems.get(options1);
                tv_issue_demand_design_budget.setText(mDesignBudget);
            }
        });
    }

    /**
     * 设置装修预算
     */
    private void setDecorationBudget() {
        final ArrayList<String> decorationBudgetItems = new ArrayList<>();
        List<String> decoration_budgets = filledData(getResources().getStringArray(R.array.decoration_budget));
        pvDecorationBudgetOptions = new OptionsPickerView(this);
        for (String item : decoration_budgets) {
            decorationBudgetItems.add(item);
        }
        pvDecorationBudgetOptions.setPicker(decorationBudgetItems);
        pvDecorationBudgetOptions.setSelectOptions(2);
        pvDecorationBudgetOptions.setCyclic(false);
        pvDecorationBudgetOptions.setTitle(UIUtils.getString(R.string.demand_project_budget_title));
        pvDecorationBudgetOptions.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                mDecorationBudget = decorationBudgetItems.get(options1);
                tv_issue_demand_budget.setText(mDecorationBudget);
            }
        });
    }

    /**
     * 提示框
     */
    private void initAlertView() {
        mSendDesignRequirementSuccessAlertView = new AlertView(UIUtils.getString(R.string.send_design_requirement_save_success_alert_view), UIUtils.getString(R.string.send_design_requirement_success_alert_view_1), null, null, new String[]{UIUtils.getString(R.string.sure)}, this, AlertView.Style.Alert, this).setCancelable(false);
    }

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
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    /**
     * @brief 是否隐藏View
     */
    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
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

    @Override
    public void onItemClick(Object obj, int position) {
        if (obj == mSendDesignRequirementSuccessAlertView && position != AlertView.CANCELPOSITION) {
            finish();
        }
    }

    /// 控件.
    private LinearLayout ll_issue_house_type;
    private LinearLayout ll_issue_style;
    private LinearLayout ll_house_type_demand;
    private LinearLayout ll_line;
    private EditText et_issue_demand_name;
    private TextView tv_issue_demand_budget;
    private TextView tv_issue_demand_design_budget;
    private TextView tv_issue_house_type;
    private TextView tv_issue_room;
    private TextView tv_issue_style;
    private TextView tv_issue_address;
    private TextView nav_title_textView;
    private EditText tv_issue_demand_detail_address;
    private EditText et_issue_demand_mobile;
    private EditText et_issue_demand_area;
    private Button btn_send_demand;
    private AlertView mSendDesignRequirementSuccessAlertView;
    private AddressDialog mChangeAddressDialog;
    private OptionsPickerView pvDesignBudgetOptions;
    private OptionsPickerView pvDecorationBudgetOptions, pvStyleOptions,  pvHouseTypeOptions;

    /// 变量.
    private String mCurrentProvince, mCurrentCity, mCurrentDistrict;
    private String mCurrentProvinceCode, mCurrentCityCode, mCurrentDistrictCode;
    private String house_type; /// 房屋类型 .
    private String style;   /// 风格 .
    private String mDesignBudget;
    private String mDecorationBudget;
    private String nick_name;
    private String mRoom, mLivingRoom, mToilet;
    private boolean isSendState = true;
    private String success = "";
    public static final int RESULT_CODE = 101;
    private String member_id;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().contains(".")) {
                if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                    s = s.toString().subSequence(0, s.toString().indexOf(".") + 3);
                    et_issue_demand_area.setText(s);
                    et_issue_demand_area.setSelection(s.length());
                }
            }

            if (s.toString().trim().substring(0).equals(".")) {
                s = "0" + s;
                et_issue_demand_area.setText(s);
                et_issue_demand_area.setSelection(2);
            }

            if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                if (!s.toString().substring(1, 2).equals(".")) {
                    et_issue_demand_area.setText(s.subSequence(0, 1));
                    et_issue_demand_area.setSelection(1);
                    return;
                }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
