package com.example.myrecycle.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.widget.Toolbar;

import com.example.myrecycle.R;
import com.llw.mvplibrary.base.BaseActivity;

/**
 * 新闻详情页面
 * news detail page
 */
public class NewsDetailsActivity extends BaseActivity {
    private Toolbar toolbar;
    private WebView webView;

    @Override
    public void initData(Bundle savedInstanceState) {
        toolbar = findViewById(R.id.toolbar);
        webView = findViewById(R.id.web_view);
        back(toolbar, false);
        //设置状态栏 Set status bar
        setStatubar(this, R.color.white, true);
        //加载WebView load webView
        loadWebView();
    }

    /**
     * 加载webView
     * load webView
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        //开始加载Url start load url
        showLoadingDialog();

        String url = getIntent().getStringExtra("url");
        if (url == null || url.isEmpty()) {
            showMsg("无法显示新闻详情");
            return;
        }

        //声明WebSettings子类 Declare a subclass of websettings
        WebSettings webSettings = webView.getSettings();
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);
        //支持插件 Support Plug-Ins
        //设置自适应屏幕，两者合用 Set the adaptive screen and use both
        webSettings.setUseWideViewPort(true);//将图片调整到适合webview的大小 Adjust the picture to fit WebView
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小 Zoom to screen size
        //缩放操作 Zoom operation
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。Supports scaling. The default value is true
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放 Set the built-in zoom control. If false, the WebView is not scalable
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件 Hide native zoom controls
        //其他细节操作 Others
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存 Close the cache in WebView
        webSettings.setAllowFileAccess(true); //设置可以访问文件 Set up access to files
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口 Support opening new windows through JS
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片 Support automatic loading of pictures
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式 Set encoding format
        //优先使用缓存 Use cache first
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        //加载一个网页 load a web page

        webView.loadUrl(url);

        //重写shouldOverrideUrlLoading()方法，使得打开网页时不调用系统浏览器， 而是在本WebView中显示
        //Rewrite the shouldoverrideurlloading () method so that when opening a web page, the system browser is not called, but is displayed in this WebView

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                //加载完成 finished
                hideLoadingDialog();
            }
        });

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_news_details;
    }
}

