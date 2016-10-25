package com.autodesk.shejijia.consumer.personalcenter.recommend.activity;

import android.content.Context;
import android.content.Intent;

import com.autodesk.shejijia.consumer.R;
import com.autodesk.shejijia.shared.framework.activity.NavigationBarActivity;

/**
 * @User: 蜡笔小新
 * @date: 16-10-24
 * @GitHub: https://github.com/meikoz
 * des:消费者详情页面
 */

public class ConsumerRecomDetailsActivity extends NavigationBarActivity {

    public static void jumpTo(Context context) {
        Intent intent = new Intent(context, ConsumerRecomDetailsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void initView() {
        super.initView();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_recommend_details;
    }
}