package com.autodesk.shejijia.shared.components.common.tools.about;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.autodesk.shejijia.shared.R;
import com.autodesk.shejijia.shared.components.common.appglobal.Constant;
import com.autodesk.shejijia.shared.framework.activity.NavigationBarActivity;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;

/**
 * @author DongXueQiu .
 * @version 1.0 .
 * @date 16-6-7 下午1:11
 * @file AboutVersionIntroducedActivity.java  .
 * @brief 关于设计家-版本说明.
 */
public class AboutVersionIntroducedActivity extends NavigationBarActivity{

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_about_version_introduced;
    }

    @Override
    protected void initView() {
        super.initView();
        webView = (WebView) findViewById(R.id.web_view_version_introduced);
        tv_introduced_version = (TextView) findViewById(R.id.tv_introduced_version);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        setTitleForNavbar(UIUtils.getString(R.string.version_introduced));
        tv_introduced_version.setText(Constant.VERSION_NUMBER);
        initwebView();
    }

    private void initwebView() {

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDefaultTextEncodingName(Constant.NetBundleKey.UTF_8);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        webView.loadUrl("file:///android_asset/about/legal/legal1.html");
        webView.setWebViewClient(new webViewClient());
    }


    private class webViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    /// 控件.
    private WebView webView;
    private TextView tv_introduced_version;

}
