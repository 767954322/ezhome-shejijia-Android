package com.autodesk.shejijia.consumer.codecorationBase.studio.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.autodesk.shejijia.consumer.R;
import com.autodesk.shejijia.consumer.codecorationBase.studio.adapter.WorkRoomDesignerAdapter;
import com.autodesk.shejijia.consumer.codecorationBase.studio.dialog.OrderDialog;
import com.autodesk.shejijia.consumer.codecorationBase.studio.entity.DesignerRetrieveRsp;
import com.autodesk.shejijia.consumer.codecorationBase.studio.entity.WorkRoomDetailsBeen;
import com.autodesk.shejijia.consumer.manager.MPServerHttpManager;
import com.autodesk.shejijia.consumer.utils.ApiStatusUtil;
import com.autodesk.shejijia.consumer.utils.HeightUtils;
import com.autodesk.shejijia.consumer.utils.ToastUtil;
import com.autodesk.shejijia.shared.components.common.appglobal.Constant;
import com.autodesk.shejijia.shared.components.common.appglobal.MemberEntity;
import com.autodesk.shejijia.shared.components.common.network.OkJsonRequest;
import com.autodesk.shejijia.consumer.uielements.AnimationUtils;
import com.autodesk.shejijia.shared.components.common.uielements.CustomProgress;
import com.autodesk.shejijia.consumer.uielements.scrollview.MyScrollView;
import com.autodesk.shejijia.consumer.uielements.scrollview.MyScrollViewListener;
import com.autodesk.shejijia.shared.components.common.utility.GsonUtil;
import com.autodesk.shejijia.shared.components.common.utility.ImageUtils;
import com.autodesk.shejijia.shared.components.common.utility.MPNetworkUtils;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;
import com.autodesk.shejijia.shared.framework.AdskApplication;
import com.autodesk.shejijia.shared.framework.activity.NavigationBarActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author yaoxuehua .
 * @version 1.0 .
 * @date 2016/8/22 0025 14:46 .
 * @file WorkRoomDetailActivity  .
 * @brief 查看工作室详情页面 .
 */
public class WorkRoomDetailActivity extends NavigationBarActivity implements View.OnClickListener, MyScrollViewListener {
    @Override
    protected int getLayoutResId() {
        return R.layout.activity_work_room;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void initView() {

        workRoomDetailHeader = LayoutInflater.from(this).inflate(R.layout.header_work_room_detail, null);
        back = (ImageView) workRoomDetailHeader.findViewById(R.id.work_room_back);
        work_room_detail_content = (LinearLayout) findViewById(R.id.work_room_detail_content);
        work_room_detail_content.addView(workRoomDetailHeader);
        listView = (ListView) findViewById(R.id.listview);
        common_navbar = (RelativeLayout) findViewById(R.id.common_navbar);
        view_navigation_header_view = findViewById(R.id.view_navigation_header_view);
        nav_left_imageButton = (ImageButton) findViewById(R.id.nav_left_imageButton);
        nav_left_imageButton.setImageResource(R.drawable.work_room_back);
        nav_title_textView = (TextView) findViewById(R.id.nav_title_textView);
        nav_title_textView.setTextColor(Color.WHITE);
        common_navbar.setBackgroundColor(Color.BLACK);
        common_navbar.getBackground().setAlpha(0);//让标题栏透明
        view_navigation_header_view.setVisibility(View.GONE);
        header_work_room_name = (TextView) workRoomDetailHeader.findViewById(R.id.header_work_room_name);
        header_work_room_design_year = (TextView) workRoomDetailHeader.findViewById(R.id.header_work_room_design_year);
        work_room_introduce = (TextView) workRoomDetailHeader.findViewById(R.id.work_room_introduce);
        work_room_name_title = (TextView) workRoomDetailHeader.findViewById(R.id.work_room_name_title);
        work_room_detail_six_imageView = (ImageView) workRoomDetailHeader.findViewById(R.id.work_room_detail_six_imageView);
        scrollview_studio = (MyScrollView) findViewById(R.id.scrollview_studio);


        now_order_details = (TextView) findViewById(R.id.now_order_details);
        CustomProgress.show(this, "", false, null);
    }

    @Override
    protected void initExtraBundle() {
        super.initExtraBundle();
        Intent intent = getIntent();
        hs_uid = intent.getStringExtra(Constant.workRoomListFragment.WORK_ROOM_LIST_CONSUMER_HS_UID);

    }

    @Override
    protected void initListener() {
        super.initListener();
        back.setOnClickListener(this);
        now_order_details.setOnClickListener(this);
        scrollview_studio.setMyScrollViewListener(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        getWorkRoomDetailData(acs_member_id, 0, 10, hs_uid);
        isLoginUserJust = isLoginUser();
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {

                    case 0:
                        if (now_order != null) {

                            AnimationUtils.getInstance().clearAnimationControl(now_order);
                            AnimationUtils.getInstance().setAnimationShow(now_order);
                        }
                        break;
                }
            }
        };


        mGestureDetector = new GestureDetectorCompat(this, new MyGestureDetectorImp());
        scrollview_studio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_UP:

                        AnimationUtils.getInstance().clearAnimationControl(now_order_details);
                        AnimationUtils.getInstance().setAnimationShow(now_order_details);
                        break;

                }


                return mGestureDetector.onTouchEvent(event);
            }
        });
    }

    public void upDataForView(WorkRoomDetailsBeen workRoomDetailsBeen) {

        setTitleForNavbar(workRoomDetailsBeen.getNick_name());
        header_work_room_name.setText(workRoomDetailsBeen.getNick_name());
        if (workRoomDetailsBeen.getDesigner() != null && workRoomDetailsBeen.getDesigner().getDesigner_profile_cover() != null) {

            ImageUtils.loadImage(work_room_detail_six_imageView, workRoomDetailsBeen.getDesigner().getDesigner_detail_cover().getPublic_url().replace(" ", ""));
        }
        work_room_introduce.setText("工作室介绍：" + workRoomDetailsBeen.getDesigner().getIntroduction());

        if (workRoomDetailsBeen.getDesigner() != null) {
            //目前没有年限，但是PD还没有确定以后有没有，所以暂时GONE掉，需要时直接解开
            header_work_room_design_year.setText("设计年限 ：" + workRoomDetailsBeen.getDesigner().getExperience() + "年");
            header_work_room_design_year.setVisibility(View.GONE);
        }
        if (workRoomDetailsBeen.getCases_list() != null) {

            WorkRoomDesignerAdapter workRoomAdapter = new WorkRoomDesignerAdapter(this, listMain, workRoomDetailsBeen.getCases_list());
            listView.setAdapter(workRoomAdapter);
            HeightUtils.setListViewHeightBasedOnChildren(listView);
            scrollview_studio.smoothScrollTo(0, 0);
        }

        CustomProgress.cancelDialog();
    }

    //获取主案设计师
    public void getDesignerDataForView(WorkRoomDetailsBeen workRoomDetailsBeen) {

        if (workRoomDetailsBeen.getMain_designers() != null && workRoomDetailsBeen.getMain_designers().size() != 0) {


            int mainLength = workRoomDetailsBeen.getMain_designers().size();//判断主案设计师item数量
            if (mainLength % 3 == 0) {

                mainLength = mainLength / 3;
            } else {

                mainLength = mainLength / 3 + 1;
            }
            WorkRoomDetailsBeen.MainDesignersBean[] mainDesigner;
            for (int i = 0; i < mainLength; i++) {//将主案设计师按数量添加不同的组，方便显示
                int count = 0;
                if (count < workRoomDetailsBeen.getMain_designers().size()) {


                    if (mainLength == 1) {//只有一行的时候判断满不满三个


                        if (workRoomDetailsBeen.getMain_designers().size() == 3) {

                            mainDesigner = new WorkRoomDetailsBeen.MainDesignersBean[3];
                            for (int j = 0; j < 3; j++) {
                                mainDesigner[j] = workRoomDetailsBeen.getMain_designers().get(count);
                                count++;
                            }
                        } else {


                            mainDesigner = new WorkRoomDetailsBeen.MainDesignersBean[workRoomDetailsBeen.getMain_designers().size() % 3];

                            for (int j = 0; j < workRoomDetailsBeen.getMain_designers().size() % 3; j++) {
                                mainDesigner[j] = workRoomDetailsBeen.getMain_designers().get(count);
                                count++;

                            }

                        }


                    } else {//多行的时候判断是否是满行，还是最后一行；

                        if (i + 1 < mainLength) {

                            mainDesigner = new WorkRoomDetailsBeen.MainDesignersBean[3];
                            for (int j = 0; j < 3; j++) {
                                mainDesigner[j] = workRoomDetailsBeen.getMain_designers().get(count);
                                count++;
                            }

                        } else {

                            mainDesigner = new WorkRoomDetailsBeen.MainDesignersBean[workRoomDetailsBeen.getMain_designers().size() % 3];

                            for (int j = 0; j < workRoomDetailsBeen.getMain_designers().size() % 3; j++) {
                                mainDesigner[j] = workRoomDetailsBeen.getMain_designers().get(count);
                                count++;

                            }

                        }


                    }
                    listMain.add(mainDesigner);
                }
            }

            if (workRoomDetailsBeen != null) {

                upDataForView(workRoomDetailsBeen);
            }

        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.work_room_back:

                finish();
                break;
            case R.id.now_order_details:

                //工作室用户信息弹框
                OrderDialog orderDialog = new OrderDialog(WorkRoomDetailActivity.this, R.style.add_dialog);
                //工作室用户信息弹框
                orderDialog.setListenser(new OrderDialog.CommitListenser() {
                    @Override
                    public void commitListener(String name, String phoneNumber) {

                        JSONObject jsonObject = new JSONObject();
                        if (isLoginUserJust) {
                            String member_id = AdskApplication.getInstance().getMemberEntity().getAcs_member_id();
                            String hs_uid = AdskApplication.getInstance().getMemberEntity().getHs_uid();
                            try {
                                jsonObject.put(Constant.workRoomListFragment.WORK_ROOM_LIST_CONSUMER_NAME, name);//姓名
                                jsonObject.put(Constant.workRoomListFragment.WORK_ROOM_LIST_CONSUMER_MOBILE, phoneNumber);//电话
                                jsonObject.put(Constant.workRoomListFragment.WORK_ROOM_LIST_CONSUMER_TYPE, 2);//工作室类型
                                jsonObject.put(Constant.workRoomListFragment.WORK_ROOM_LIST_CONSUMER_ID, member_id);//消费者ID
                                jsonObject.put(Constant.workRoomListFragment.WORK_ROOM_LIST_CONSUMER_UID, hs_uid);
                                jsonObject.put(Constant.workRoomListFragment.WORK_ROOM_NAME, workRoomDetailsBeen.getNick_name());//工作室名字

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {

                            try {
                                jsonObject.put(Constant.workRoomListFragment.WORK_ROOM_LIST_CONSUMER_NAME, name);
                                jsonObject.put(Constant.workRoomListFragment.WORK_ROOM_LIST_CONSUMER_MOBILE, phoneNumber);
                                jsonObject.put(Constant.workRoomListFragment.WORK_ROOM_LIST_CONSUMER_TYPE, 2);
                                jsonObject.put(Constant.workRoomListFragment.WORK_ROOM_NAME, workRoomDetailsBeen.getNick_name());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        CustomProgress.show(WorkRoomDetailActivity.this, "提交中...", false, null);
                        upOrderDataForService(jsonObject);
                    }
                });
                orderDialog.show();
                break;

        }

    }

    /**
     * scrollview 滚动改变调用
     * scrollview 滑动时改变标题栏的状态
     */

    @Override
    public void onScrollChange(MyScrollView scrollView, int x, int y, int oldx, int oldy) {

        int alphaCount = 0;
        if (y > 200 && y < 600) {
            alphaCount = y - 200;

            common_navbar.getBackground().mutate().setAlpha(alphaCount);//让标题栏透明
        } else if (y >= 600) {

            alphaCount = 255;
            common_navbar.getBackground().setAlpha(alphaCount);//让标题栏透明
        } else if (y < 200) {

            common_navbar.getBackground().mutate().setAlpha(0);//让标题栏透明
        }

        if (y == 0) {

            common_navbar.getBackground().mutate().setAlpha(0);//让标题栏透明
        }

        if (isFirstGoIn) {
            isFirstGoIn = false;
            common_navbar.getBackground().mutate().setAlpha(0);//让标题栏透明
        }

        int myScrollViewChildHeight = scrollview_studio.getChildAt(0).getMeasuredHeight();//myScrollView中子类的高度
        int myScrollViewHeight = scrollview_studio.getHeight();//myScrollView的高度


    }


    /**
     * 获取工作室详情信息
     */

    public void getWorkRoomDetailData(int designer_id, int offset, int limit, final String hs_uid) {

        MPServerHttpManager.getInstance().getWorkRoomOrderData(designer_id, offset, limit, hs_uid, new OkJsonRequest.OKResponseCallback() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                MPNetworkUtils.logError(TAG, volleyError);
                ApiStatusUtil.getInstance().apiStatuError(volleyError, WorkRoomDetailActivity.this);
            }

            @Override
            public void onResponse(JSONObject jsonObject) {

                String infoTwo;
                infoTwo = GsonUtil.jsonToString(jsonObject);
                workRoomDetailsBeen = GsonUtil.jsonToBean(infoTwo, WorkRoomDetailsBeen.class);
                main_designers = workRoomDetailsBeen.getMain_designers();
                getDesignerDataForView(workRoomDetailsBeen);
            }
        });


    }

    //判断该用户是否登陆了
    public boolean isLoginUser() {

        MemberEntity memberEntity = AdskApplication.getInstance().getMemberEntity();

        if (memberEntity == null) {

            return false;//未登录
        } else {

            return true;//已登录
        }
    }


    /**
     * 上传立即预约信息
     */
    public void upOrderDataForService(JSONObject jsonObject) {

        MPServerHttpManager.getInstance().upWorkRoomOrderData(jsonObject, new OkJsonRequest.OKResponseCallback() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                CustomProgress.cancelDialog();
                ToastUtil.showCustomToast(WorkRoomDetailActivity.this, UIUtils.getString(R.string.work_room_commit_fail));
//                Toast.makeText(WorkRoomDetailActivity.this, R.string.work_room_commit_fail, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(JSONObject jsonObject) {
                CustomProgress.cancelDialog();
                ToastUtil.showCustomToast(WorkRoomDetailActivity.this, UIUtils.getString(R.string.work_room_commit_successful));
//                Toast.makeText(WorkRoomDetailActivity.this, R.string.work_room_commit_successful, Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * 背景动画效果
     */
    public void setAnimationBackgroud(RelativeLayout view) {

        AnimationSet animationSet = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(2000);
        animationSet.addAnimation(alphaAnimation);
        view.startAnimation(animationSet);
    }


    private View workRoomDetailHeader;
    private LinearLayout work_room_detail_content;
    private RelativeLayout common_navbar;
    private ImageView back;
    private Handler handler;
    private MyScrollView scrollview_studio;
    private View view_navigation_header_view;
    private ImageButton nav_left_imageButton;
    //    private ImageView work_room_design_imageView;
    private ImageView work_room_detail_six_imageView;
    private TextView now_order;
    private TextView nav_title_textView;
    private int acs_member_id;
    private int distance_offset = 0;
    private Timer timer;
    private String hs_uid;
    private TextView header_work_room_name;
    private TextView header_work_room_design_year;
    private TextView work_room_introduce;
    private TextView work_room_name_title;
    private List<WorkRoomDetailsBeen.MainDesignersBean[]> listMain = new ArrayList<WorkRoomDetailsBeen.MainDesignersBean[]>();
    private DesignerRetrieveRsp designerListBean;
    private boolean isLoginUserJust = false;
    private boolean isFirstGoIn = true;//第一次进入该页面将该title设置为透明
    private WorkRoomDetailsBeen workRoomDetailsBeen;
    private List<WorkRoomDetailsBeen.MainDesignersBean> main_designers;
    private GestureDetectorCompat mGestureDetector;

    private ListView listView;
    private TextView now_order_details;
    private GridView gridView;


    private class MyGestureDetectorImp extends GestureDetector.SimpleOnGestureListener {


        @Override
        public boolean onSingleTapUp(MotionEvent e) {
//            controlAnimation();


            return super.onSingleTapUp(e);
        }


        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            AnimationUtils.getInstance().setAnimationDismiss(now_order_details);
            return super.onFling(e1, e2, velocityX, velocityY);
        }


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            switch (e2.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    break;
                //监听滚动类型,时时监听
                case MotionEvent.ACTION_MOVE:
                    AnimationUtils.getInstance().setAnimationDismiss(now_order_details);
                    break;
                case MotionEvent.ACTION_UP:

                    break;
            }


            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    public void controlAnimation() {

        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                Message message = handler.obtainMessage();
                message.what = 0;
                handler.sendMessage(message);
                if (timer != null) {

                    timer.cancel();
                    timer = null;
                }
            }
        };
        if (timer == null) {

            timer = new Timer();
            timer.schedule(timerTask, 1500, 5000);
        }

    }
}
