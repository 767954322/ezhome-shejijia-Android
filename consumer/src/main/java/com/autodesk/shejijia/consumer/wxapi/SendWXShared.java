package com.autodesk.shejijia.consumer.wxapi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.TextUtils;
import android.view.View;

import com.autodesk.shejijia.consumer.ConsumerApplication;
import com.autodesk.shejijia.consumer.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author allengu .
 * @version v1.0 .
 * @date 2016-7-19 .
 * @file AliPayService.java .
 * @brief 微信分享工具类 .
 */
public class SendWXShared {
    public static void sendProjectToWX(final Context context, final String webUrl, final String title, final String description, final boolean ifIsSharedToFriends, final String imgUrl) throws IOException {
        ImageLoader.getInstance().loadImage(imgUrl, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {


                WXWebpageObject webpage = new WXWebpageObject();
                if (!TextUtils.isEmpty(webUrl)) {
                    webpage.webpageUrl = webUrl;
                }
                WXMediaMessage msg = new WXMediaMessage(webpage);
                msg.title = title;
                if (description != null && !description.trim().equals("null")) {
                    msg.description = description;
                } else {
                    msg.description = "";
                }
                Bitmap thumbBmp = null;
                if (loadedImage != null) {
                    thumbBmp = createBitmapThumbnail(loadedImage);
                } else {
                    thumbBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.shuoming_ico);
                }
//                Bitmap thumbBmp = Bitmap.createScaledBitmap(loadedImage, 150, 150, true);
                byte[] bytes = bmpToByteArray(thumbBmp, true);
                msg.thumbData = bytes;
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("webpage");
                req.message = msg;
                req.scene = ifIsSharedToFriends ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
                ConsumerApplication.api.sendReq(req);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
    }


    public static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Bitmap createBitmapThumbnail(Bitmap bitMap) {
        int width = bitMap.getWidth();
        int height = bitMap.getHeight();
        // 设置想要的大小
        int newWidth = 99;
        int newHeight = 99;
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newBitMap = Bitmap.createBitmap(bitMap, 0, 0, width, height,
                matrix, true);
        return newBitMap;
    }

}