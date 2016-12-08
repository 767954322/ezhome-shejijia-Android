package com.autodesk.shejijia.shared.components.message.datamodel;

import android.os.Bundle;

import com.android.volley.VolleyError;
import com.autodesk.shejijia.shared.components.common.entity.ResponseError;
import com.autodesk.shejijia.shared.components.common.network.OkJsonRequest;
import com.autodesk.shejijia.shared.components.common.utility.ResponseErrorUtil;
import com.autodesk.shejijia.shared.components.message.network.MessageCenterHttpManager;
import com.autodesk.shejijia.shared.components.message.network.MessageCenterHttpManagerImpl;

import org.json.JSONObject;

/**
 * Created by luchongbin on 2016/12/6.
 */

public class MessageCenterRemoteDataSource implements MessageCenterDataSource{
    private  static CallBackMessageCenterDataSource mCallback;
    private MessageCenterHttpManager mMessageCenterHttpManager;
    public MessageCenterRemoteDataSource(CallBackMessageCenterDataSource mCallBackMessageCenterDataSource) {
        mCallback = mCallBackMessageCenterDataSource;
        mMessageCenterHttpManager = MessageCenterHttpManagerImpl.getInstance();
    }
    @Override
    public void getUnreadCount(String project_ids, String requestTag) {
        mMessageCenterHttpManager.getUnreadCount(project_ids,requestTag,new OkJsonRequest.OKResponseCallback(){
            @Override
            public void onResponse(JSONObject jsonObject) {
                mCallback.onResponse(jsonObject);
            }
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ResponseError responseError =  ResponseErrorUtil.checkVolleyError(volleyError);
                mCallback.onErrorResponse(responseError);
            }
        });
    }
    @Override
    public void listMessageCenterInfo(Bundle requestParams, String requestTag) {
        mMessageCenterHttpManager.listMessageCenterInfo(requestParams,requestTag,new OkJsonRequest.OKResponseCallback(){
            @Override
            public void onResponse(JSONObject jsonObject) {
                mCallback.onResponse(jsonObject);
            }
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ResponseError responseError =  ResponseErrorUtil.checkVolleyError(volleyError);
                mCallback.onErrorResponse(responseError);
            }
        });
    }
}
