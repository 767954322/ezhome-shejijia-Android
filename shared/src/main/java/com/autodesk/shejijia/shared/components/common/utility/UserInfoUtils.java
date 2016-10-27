package com.autodesk.shejijia.shared.components.common.utility;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.autodesk.shejijia.shared.components.common.appglobal.Constant;
import com.autodesk.shejijia.shared.components.common.appglobal.MemberEntity;
import com.autodesk.shejijia.shared.components.common.utility.LogUtils;
import com.autodesk.shejijia.shared.components.common.utility.SharedPreferencesUtils;

/**
 * Created by t_xuz on 10/17/16.
 * 账号信息管理类
 */
public final class UserInfoUtils {

    private UserInfoUtils(){
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static String getToken(@NonNull Context context){
        MemberEntity entity = (MemberEntity) SharedPreferencesUtils.getObject(context, Constant.UerInfoKey.USER_INFO);
        if (entity != null && !TextUtils.isEmpty(entity.getHs_accesstoken())) {
            return entity.getHs_accesstoken();
        }
        return null;
    }
}