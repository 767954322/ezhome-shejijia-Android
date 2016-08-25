package com.autodesk.shejijia.consumer.utils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.autodesk.shejijia.consumer.R;
import com.autodesk.shejijia.consumer.home.homepage.activity.MPConsumerHomeActivity;
import com.autodesk.shejijia.consumer.manager.MPServerHttpManager;
import com.autodesk.shejijia.consumer.personalcenter.consumer.entity.ConsumerEssentialInfoEntity;
import com.autodesk.shejijia.consumer.personalcenter.designer.entity.DesignerInfoDetails;
import com.autodesk.shejijia.shared.components.common.appglobal.Constant;
import com.autodesk.shejijia.shared.components.common.appglobal.MemberEntity;
import com.autodesk.shejijia.shared.components.common.network.OkJsonRequest;
import com.autodesk.shejijia.shared.components.common.uielements.alertview.AlertView;
import com.autodesk.shejijia.shared.components.common.utility.GsonUtil;
import com.autodesk.shejijia.shared.components.common.utility.ImageUtils;
import com.autodesk.shejijia.shared.components.common.utility.MPNetworkUtils;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;
import com.autodesk.shejijia.shared.framework.AdskApplication;
import com.autodesk.shejijia.shared.framework.activity.NavigationBarActivity;

import org.json.JSONObject;

/**
 */
public class UserPictureUtil {

    private static ConsumerEssentialInfoEntity mConsumerEssentialInfoEntity;
    private static DesignerInfoDetails designerInfoDetails;

    //设置头像
    public static void setConsumerOrDesignerPicture(Context context,ImageView iv) {
        MemberEntity mMemberEntity = AdskApplication.getInstance().getMemberEntity();
        if (mMemberEntity != null &&
                Constant.UerInfoKey.CONSUMER_TYPE.equals(mMemberEntity.getMember_type())) {
            getConsumerInfoData(mMemberEntity.getAcs_member_id(),context,iv);

        }else if (mMemberEntity != null && Constant.UerInfoKey.DESIGNER_TYPE.equals(mMemberEntity.getMember_type())) {
            getDesignerInfoData(mMemberEntity.getAcs_member_id(), mMemberEntity.getHs_uid(),context,iv);

        }else {
            iv.setImageResource(R.drawable.icon_default_avator);
        }
    }

    /**
     * 获取个人基本信息
     *
     * @param member_id
     * @brief For details on consumers .
     */
    private static void getConsumerInfoData(String member_id, final Context context, final ImageView iv) {
        MPServerHttpManager.getInstance().getConsumerInfoData(member_id, new OkJsonRequest.OKResponseCallback() {

            @Override
            public void onResponse(JSONObject jsonObject) {
                String jsonString = GsonUtil.jsonToString(jsonObject);
                mConsumerEssentialInfoEntity = GsonUtil.jsonToBean(jsonString, ConsumerEssentialInfoEntity.class);
                ImageUtils.displayAvatarImage(mConsumerEssentialInfoEntity.getAvatar(), iv);
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (context != null) {
                    new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(R.string.network_error), null, new String[]{UIUtils.getString(R.string.sure)}, null, context,
                            AlertView.Style.Alert, null).show();
                }
            }
        });
    }


    /**
     * 设计师个人信息
     *
     * @param designer_id
     * @param hs_uid
     */
    private static void getDesignerInfoData(String designer_id, String hs_uid, final Context context, final ImageView iv) {
        MPServerHttpManager.getInstance().getDesignerInfoData(designer_id, hs_uid, new OkJsonRequest.OKResponseCallback() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                String jsonString = GsonUtil.jsonToString(jsonObject);
                designerInfoDetails = GsonUtil.jsonToBean(jsonString, DesignerInfoDetails.class);
                ImageUtils.displayAvatarImage(designerInfoDetails.getAvatar(), iv);

            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(R.string.network_error), null, new String[]{UIUtils.getString(R.string.sure)}, null, context,
                        AlertView.Style.Alert, null).show();
            }
        });
    }
}
