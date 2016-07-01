package com.autodesk.shejijia.consumer.home.decorationlibrarys.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.autodesk.shejijia.shared.framework.AdskApplication;
import com.autodesk.shejijia.consumer.R;
import com.autodesk.shejijia.consumer.home.decorationdesigners.activity.SeekDesignerDetailActivity;
import com.autodesk.shejijia.consumer.home.decorationlibrarys.adapter.FuzzySearchAdapter;
import com.autodesk.shejijia.consumer.home.decorationlibrarys.entity.CaseLibraryBean;
import com.autodesk.shejijia.consumer.home.decorationlibrarys.entity.FiltrateContentBean;
import com.autodesk.shejijia.consumer.home.decorationlibrarys.entity.SearchHoverCaseBean;
import com.autodesk.shejijia.consumer.manager.MPServerHttpManager;
import com.autodesk.shejijia.shared.components.common.appglobal.Constant;
import com.autodesk.shejijia.shared.components.common.network.OkJsonRequest;
import com.autodesk.shejijia.shared.components.common.network.OkStringRequest;
import com.autodesk.shejijia.consumer.personalcenter.consumer.adapter.UserHomeCaseAdapter;
import com.autodesk.shejijia.shared.components.common.appglobal.MemberEntity;
import com.autodesk.shejijia.shared.components.im.activity.ChatRoomActivity;
import com.autodesk.shejijia.shared.framework.activity.NavigationBarActivity;
import com.autodesk.shejijia.shared.components.im.datamodel.MPChatThread;
import com.autodesk.shejijia.shared.components.im.datamodel.MPChatThreads;
import com.autodesk.shejijia.shared.components.im.datamodel.MPChatUtility;
import com.autodesk.shejijia.shared.components.im.manager.MPChatHttpManager;
import com.autodesk.shejijia.consumer.utils.AppJsonFileReader;
import com.autodesk.shejijia.consumer.utils.CharacterParser;
import com.autodesk.shejijia.shared.components.common.utility.GsonUtil;
import com.autodesk.shejijia.shared.components.common.utility.MPNetworkUtils;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;
import com.autodesk.shejijia.shared.components.common.uielements.ClearEditText;
import com.autodesk.shejijia.shared.components.common.uielements.alertview.AlertView;
import com.autodesk.shejijia.shared.components.common.uielements.pulltorefresh.PinnedHeaderListView;
import com.autodesk.shejijia.shared.components.common.uielements.pulltorefresh.PullToRefreshLayout;
import com.socks.library.KLog;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Malidong .
 * @version 1.0 .
 * @date 2015/12/25 0025 15:57 .
 * @file SearchActivity  .
 * @brief 搜索页面 .
 */
public class SearchActivity extends NavigationBarActivity implements PullToRefreshLayout.OnRefreshListener, View.OnClickListener, UserHomeCaseAdapter.OnItemImageHeadClickListener {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_search;
    }

    @Override
    protected void initView() {
        mClearEditText1 = (ClearEditText) findViewById(R.id.et_search);
        mPullToRefreshLayout = ((PullToRefreshLayout) findViewById(R.id.refresh_view));
        mFooterView = View.inflate(this, R.layout.view_empty_layout, null);
        mRlEmpty = (RelativeLayout) mFooterView.findViewById(R.id.rl_empty);
        mTvEmptyMessage = (TextView) mFooterView.findViewById(R.id.tv_empty_message);
        mIvTemp = ((ImageView) mFooterView.findViewById(R.id.iv_default_empty));
        mListView = (ListView) findViewById(R.id.content_view);
        mSearchBack = (ImageView) findViewById(R.id.searchc_back);

        mListView.addFooterView(mFooterView);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
        mCharacterParser = CharacterParser.getInstance();

        custom_string_keywords = "";
        getSearchData(0, AppJsonFileReader.getRoomHall(this));

        getSearchData(1, AppJsonFileReader.getStyle(this));
        getSearchData(2, AppJsonFileReader.getArea(this));

        if (mUserHomeCaseAdapter == null) {
            mUserHomeCaseAdapter = new UserHomeCaseAdapter(this, mCasesEntities, this, screenWidth, screenHeight);
        }
        mListView.setAdapter(mUserHomeCaseAdapter);
        mUserHomeCaseAdapter.setOnItemImageHeadClickListener(this, this, this);
    }

    @Override
    protected void initListener() {
        mPullToRefreshLayout.setOnRefreshListener(this);
        mClearEditText1.setOnClickListener(this); /// cancel search.
        mSearchBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.et_search:/// 搜索.
                openPopupWindow(mClearEditText1.getText().toString());
                break;
            case R.id.searchc_back:/// 返回.
                finish();
                break;
            default:
                break;
        }
    }

    /// 查看设计师详情页面.
    @Override
    public void OnItemImageHeadClick(int position) {
        String designer_id;
        String hsUid;
        Intent intent = new Intent(this, SeekDesignerDetailActivity.class);
        CaseLibraryBean.CasesEntity.DesignerInfoEntity designerInfoEntity = mCasesEntities.get(position).getDesigner_info();
        if (designerInfoEntity != null) {
            CaseLibraryBean.CasesEntity.DesignerInfoEntity.DesignerEntity designerEntity = designerInfoEntity.getDesigner();
            if (designerEntity != null) {
                designer_id = designerEntity.getAcs_member_id();
                intent.putExtra(Constant.ConsumerDecorationFragment.designer_id, designer_id);
            }
        }
        hsUid = mCasesEntities.get(position).getHs_designer_uid();
        intent.putExtra(Constant.ConsumerDecorationFragment.hs_uid, hsUid);
        startActivity(intent);
    }

    /// 进入案例详情页面.
    @Override
    public void OnItemCaseClick(int position) {
        String case_id = mCasesEntities.get(position).getId();
        mIntent = new Intent(this, CaseLibraryDetailActivity.class);
        mIntent.putExtra(Constant.CaseLibraryDetail.CASE_ID, case_id);
        startActivity(mIntent);
    }

    /// 聊天.
    @Override
    public void OnItemHomeChatClick(final int position) {
        MemberEntity mMemberEntity = AdskApplication.getInstance().getMemberEntity();
        final String member_id = mMemberEntity.getAcs_member_id();
        if (mMemberEntity != null) {
            final String designer_id = mCasesEntities.get(position).getDesigner_id();
            final String hs_uid = mCasesEntities.get(position).getHs_designer_uid();
            final String receiver_name = mCasesEntities.get(position).getDesigner_info().getNick_name();
            final String mMemberType = mMemberEntity.getMember_type();
            final String recipient_ids = member_id + "," + designer_id + "," + "20730165";

            MPChatHttpManager.getInstance().retrieveMultipleMemberThreads(recipient_ids, 0, 10, new OkStringRequest.OKResponseCallback() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    MPNetworkUtils.logError(TAG, volleyError);
                }

                @Override
                public void onResponse(String s) {
                    MPChatThreads mpChatThreads = MPChatThreads.fromJSONString(s);
                    if (mpChatThreads != null && mpChatThreads.threads.size() > 0) {
                        MPChatThread mpChatThread = mpChatThreads.threads.get(0);

                        int assetId = MPChatUtility.getAssetIdFromThread(mpChatThread);
                        Intent intent = new Intent(SearchActivity.this, ChatRoomActivity.class);
                        intent.putExtra(ChatRoomActivity.THREAD_ID, mpChatThread.thread_id);
                        intent.putExtra(ChatRoomActivity.RECIEVER_USER_ID, designer_id);
                        intent.putExtra(ChatRoomActivity.RECIEVER_USER_NAME, receiver_name);
                        intent.putExtra(ChatRoomActivity.ASSET_ID, assetId+"");
                        intent.putExtra(ChatRoomActivity.MEMBER_TYPE, mMemberType);
                        intent.putExtra(ChatRoomActivity.ACS_MEMBER_ID, member_id);
                        SearchActivity.this.startActivity(intent);
                    } else {
                        Intent intent = new Intent(SearchActivity.this, ChatRoomActivity.class);
                        intent.putExtra(ChatRoomActivity.RECIEVER_HS_UID, hs_uid);
                        intent.putExtra(ChatRoomActivity.RECIEVER_USER_NAME, receiver_name);
                        intent.putExtra(ChatRoomActivity.RECIEVER_USER_ID, designer_id);
                        intent.putExtra(ChatRoomActivity.ACS_MEMBER_ID, member_id);
                        intent.putExtra(ChatRoomActivity.MEMBER_TYPE, mMemberType);
                        intent.putExtra(ChatRoomActivity.ASSET_ID, "");
                        SearchActivity.this.startActivity(intent);
                    }
                }
            });
        } else {
            AdskApplication.getInstance().doLogin(SearchActivity.this);
        }
    }

    /**
     * 点击搜索表格行的Click事件
     *
     * @param pinnedHeaderListView
     */
    private void setOnItemClickListenerForTvEctSearchListView(PinnedHeaderListView pinnedHeaderListView) {
        pinnedHeaderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchHoverCaseBean selectedHoverCaseBean = mSearchHoverCaseBeanArrayList.get(position - 1);
                mClearEditText1.setText(selectedHoverCaseBean.getValue());
                setSelection(mClearEditText1);
                mFiltrateContentBean = new FiltrateContentBean();
                custom_string_keywords = "";
                switch (selectedHoverCaseBean.getType()) {
                    case 0:
                        mFiltrateContentBean.setHousingType(selectedHoverCaseBean.getKey());
                        break;
                    case 1:
                        mFiltrateContentBean.setStyle(selectedHoverCaseBean.getKey());
                        break;
                    case 2:
                        mFiltrateContentBean.setArea(selectedHoverCaseBean.getKey());
                        break;
                    default:
                        break;
                }
                isFirstIn = true;
                cancelPopupWindowAndClearEditText();
            }
        });
    }

    /**
     * 请求访问网络的公用方法
     *
     * @param offset
     * @param state
     */
    private void requestManage(int offset, int state) {
        String area = (mFiltrateContentBean != null && mFiltrateContentBean.getArea() != null) ? mFiltrateContentBean.getArea() : BLANK;
        String string_form = (mFiltrateContentBean != null && mFiltrateContentBean.getHousingType() != null) ? mFiltrateContentBean.getHousingType() : BLANK;
        String style = (mFiltrateContentBean != null && mFiltrateContentBean.getStyle() != null) ? mFiltrateContentBean.getStyle() : BLANK;
        String space = (mFiltrateContentBean != null && mFiltrateContentBean.getSpace() != null) ? mFiltrateContentBean.getSpace() : BLANK;

        getCaseLibraryData(custom_string_keywords, "01", area, string_form, style, space, BLANK, BLANK, offset, LIMIT, state);
    }

    /// 获取数据.
    public void getCaseLibraryData(String custom_string_keywords, String taxonomy_id, String custom_string_area, String custom_string_form, String custom_string_style, String custom_string_type, String custom_string_restroom, String custom_string_bedroom, final int offset, final int limit, final int state) {
        OkJsonRequest.OKResponseCallback callback = new OkJsonRequest.OKResponseCallback() {

            @Override
            public void onResponse(JSONObject jsonObject) {
                notifyConsumeHomeAdapter(jsonObject, state);
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                MPNetworkUtils.logError(TAG, volleyError);
                mPullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.FAIL);

                new AlertView(UIUtils.getString(R.string.tip), UIUtils.getString(R.string.network_error), null, new String[]{UIUtils.getString(R.string.chatroom_audio_recording_erroralert_ok)}, null, SearchActivity.this,
                        AlertView.Style.Alert, null).show();
                hideFooterView(mCasesEntities);
                mUserHomeCaseAdapter.notifyDataSetChanged();
            }
        };
        MPServerHttpManager.getInstance().getCaseListData(custom_string_style, custom_string_type, custom_string_keywords,
                custom_string_area, custom_string_bedroom, taxonomy_id,
                custom_string_restroom, custom_string_form, offset, limit, callback);
    }

    /**
     * 搜索数据
     *
     * @param type 类型
     * @param map  集合
     */
    private void getSearchData(int type, Map<String, String> map) {
        for (Map.Entry mapString : map.entrySet()) {
            String key = (String) mapString.getKey();
            String value = (String) mapString.getValue();

            SearchHoverCaseBean searchHoverCaseBean = new SearchHoverCaseBean();
            searchHoverCaseBean.setType(type);
            searchHoverCaseBean.setKey(key);
            searchHoverCaseBean.setValue(value);

            mSearchHoverCaseBeenList.add(searchHoverCaseBean);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // 第一次进入自动刷新
        if (isFirstIn) {
            mPullToRefreshLayout.autoRefresh();
            isFirstIn = false;
        }
    }

    /**
     * 打开PopupWindow窗口
     *
     * @param searchTextData
     */
    private void openPopupWindow(String searchTextData) {

        mPopupView = LayoutInflater.from(this).inflate(R.layout.activity_search_popup, null);
        final ImageView ivSearchCBack = (ImageView) mPopupView.findViewById(R.id.ect_searchc_back);
        final TextView tvSearchCancel = (TextView) mPopupView.findViewById(R.id.tv_ect_search_cancel);
        PinnedHeaderListView pinnedHeaderListView = (PinnedHeaderListView) mPopupView.findViewById(R.id.tv_ect_search_listview);
        mClearEditText2 = (ClearEditText) mPopupView.findViewById(R.id.ect_et_search);

        mClearEditText2.setText(searchTextData);
        setSelection(mClearEditText2);
        mSearchHoverCaseBeanArrayList.clear();
        if (searchTextData != null && searchTextData.length() > 0) {

            mSearchHoverCaseBeanArrayList.addAll(filterData(searchTextData));
        }
        if (mFuzzySearchAdapter == null) {
            mFuzzySearchAdapter = new FuzzySearchAdapter(SearchActivity.this, mSearchHoverCaseBeanArrayList);
        }
        pinnedHeaderListView.setAdapter(mFuzzySearchAdapter);
        mFuzzySearchAdapter.notifyDataSetChanged();

        setOnClickListener(tvSearchCancel, ivSearchCBack);
        setOnKeyListenerForClearEditText();
        addTextChangedListenerForClearEditText();
        setOnItemClickListenerForTvEctSearchListView(pinnedHeaderListView);

        mPopupWindow = new PopupWindow(mPopupView, android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.showAtLocation(mSearchBack, Gravity.CENTER, 0, 0);
        mPopupWindow.setFocusable(true);
        mPopupWindow.update();
    }

    /**
     * 设置取消和返回的监听.
     *
     * @param textView
     * @param imageView
     */
    private void setOnClickListener(TextView textView, ImageView imageView) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelPopupWindowAndClearEditText();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelPopupWindowAndClearEditText();
            }
        });
    }

    /**
     * 搜索框内容改变监听.
     */
    private void addTextChangedListenerForClearEditText() {
        mClearEditText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mSearchHoverCaseBeanArrayList.clear();
                String searchInPutString = s.toString();
                searchInPutString = searchInPutString.trim();
                mSearchHoverCaseBeanArrayList.addAll(filterData(searchInPutString));
                mFuzzySearchAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 搜索按键监听.
     */
    private void setOnKeyListenerForClearEditText() {
        mClearEditText2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(SearchActivity.this.getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                    custom_string_keywords = mClearEditText2.getText().toString().trim();
                    mClearEditText1.setText(custom_string_keywords);
                    if (custom_string_keywords != null && custom_string_keywords.length() > 0) {
                        try {
                            custom_string_keywords = URLEncoder.encode(custom_string_keywords, Constant.NetBundleKey.UTF_8);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }

                    setSelection(mClearEditText1);
                    mFiltrateContentBean = null;
                    isFirstIn = true;
                    cancelPopupWindowAndClearEditText();
                }
                return false;
            }
        });
    }

    /**
     * 弹框消失,搜索条件制空.
     */
    private void cancelPopupWindowAndClearEditText() {
        mPopupWindow.dismiss();
        mClearEditText2.setText("");
    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private List<SearchHoverCaseBean> filterData(String filterStr) {
        List<SearchHoverCaseBean> filterDateList = new ArrayList<SearchHoverCaseBean>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = mSearchHoverCaseBeenList;
        } else {
            filterDateList.clear();
            for (SearchHoverCaseBean sortModel : mSearchHoverCaseBeenList) {
                String name = sortModel.getValue();
                if (name.indexOf(filterStr.toString()) != -1 || mCharacterParser.getSelling(name).startsWith(filterStr.toString())) {
                    filterDateList.add(sortModel);
                }
            }
        }
        return filterDateList;
    }

    /// 加载更多.
    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
        requestManage(OFFSET, 1);
    }

    /// 刷新.
    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        requestManage(0, 0);
    }

    /**
     * 更新适配数据
     *
     * @param jsonObject json数据
     * @param state      状态
     */
    private void notifyConsumeHomeAdapter(JSONObject jsonObject, int state) {
        try {
            String jsonString = GsonUtil.jsonToString(jsonObject);
            mCaseLibraryBean = GsonUtil.jsonToBean(jsonString, CaseLibraryBean.class);
            KLog.json(TAG, jsonString);
            switch (state) {
                case 0:
                    OFFSET = 10;
                    mCasesEntities.clear();
                    break;
                case 1:
                    OFFSET += 10;
                    break;
                default:
                    break;
            }
            List<CaseLibraryBean.CasesEntity> casesEntity = mCaseLibraryBean.getCases();
            if (casesEntity != null && casesEntity.size() > 0) {
                mCasesEntities.addAll(casesEntity);
            }
        } finally {
            hideFooterView(mCasesEntities);
            mUserHomeCaseAdapter.notifyDataSetChanged();
            mPullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
        }
    }

    /**
     * 是否隐藏底部布局.
     *
     * @param list 数据集合
     */
    private void hideFooterView(List<CaseLibraryBean.CasesEntity> list) {
        if (list != null && list.size() > 0) {
            mRlEmpty.setVisibility(View.GONE);
        } else {
            mRlEmpty.setVisibility(View.VISIBLE);
        }
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.photopicker_thumbnail_placeholder);
        mIvTemp.setImageBitmap(bmp);
        WindowManager wm = (WindowManager) SearchActivity.this.getSystemService(SearchActivity.WINDOW_SERVICE);
        int height = wm.getDefaultDisplay().getHeight();
        android.view.ViewGroup.LayoutParams mRlEmptyLayoutParams = mRlEmpty.getLayoutParams();
        mRlEmptyLayoutParams.height = height - 10;
        mRlEmpty.getLayoutParams();
        mRlEmpty.setLayoutParams(mRlEmptyLayoutParams);
        mTvEmptyMessage.setText(UIUtils.getString(R.string.no_designer_case));
    }

    private void setSelection(ClearEditText editText) {
        CharSequence charSequence = editText.getText();
        if (charSequence instanceof Spannable) {
            Spannable spanText = (Spannable) charSequence;
            Selection.setSelection(spanText, charSequence.length());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSearchHoverCaseBeenList = null;
        mSearchHoverCaseBeanArrayList = null;
        mCasesEntities = null;
        mFuzzySearchAdapter = null;
        mUserHomeCaseAdapter = null;
        mCharacterParser = null;
        mFiltrateContentBean = null;
        custom_string_keywords = null;
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
        if (mPopupView != null) {
            mPopupView = null;
        }
    }

    public static final String BLANK = "";

    /// 控件.
    private ClearEditText mClearEditText1;
    private ClearEditText mClearEditText2;
    private ListView mListView;
    private PullToRefreshLayout mPullToRefreshLayout;
    private ImageView mSearchBack;
    private RelativeLayout mRlEmpty;
    private TextView mTvEmptyMessage;
    private ImageView mIvTemp;
    /// 点击搜索框时弹出的popupWindow .
    private PopupWindow mPopupWindow;
    private View mPopupView;
    private View mFooterView;

    /// 常量.
    private int LIMIT = 10;
    private int OFFSET = 0;
    private int screenWidth;
    private int screenHeight;
    private String custom_string_keywords;
    private boolean isFirstIn = false;

    /// 集合,类.
    private List<SearchHoverCaseBean> mSearchHoverCaseBeenList = new ArrayList<>();
    private List<SearchHoverCaseBean> mSearchHoverCaseBeanArrayList = new ArrayList<>();
    private ArrayList<CaseLibraryBean.CasesEntity> mCasesEntities = new ArrayList<>();
    private UserHomeCaseAdapter mUserHomeCaseAdapter;
    private FiltrateContentBean mFiltrateContentBean;
    private CaseLibraryBean mCaseLibraryBean;
    private FuzzySearchAdapter mFuzzySearchAdapter;
    private CharacterParser mCharacterParser;
    private Intent mIntent;
}