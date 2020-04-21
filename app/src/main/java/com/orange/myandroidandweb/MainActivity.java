package com.orange.myandroidandweb;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    Button btnToJs;
    Button btn2ToJs;
    WebView webView2;
    WebView webView3;
    WebView webView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        webView2 = findViewById(R.id.webView2);
        webView3 = findViewById(R.id.webView3);
        webView4 = findViewById(R.id.webView4);
        btnToJs = findViewById(R.id.btn_to_js);
        btn2ToJs = findViewById(R.id.btn2_to_js);


        WebSettings settings = webView.getSettings();

        //设置与Js交互的权限
        settings.setJavaScriptEnabled(true);
        //允许js弹窗
        settings.setJavaScriptCanOpenWindowsAutomatically(true);


        //载入js文件代码
        webView.loadUrl("file:///android_asset/javascript.html");

        btnToJs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.post(new Runnable() {
                    @Override
                    public void run() {

                        // 注意调用的JS方法名要对应上
                        // 调用javascript的callJS()方法
                        webView.loadUrl("javascript:callJS()");

                    }
                });

            }
        });


        btn2ToJs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.evaluateJavascript("javascript:callJS()", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                //此处为 js 返回的结果
                                Toast.makeText(MainActivity.this, value, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle("Alert");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        result.confirm();
                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }
        });




        //-----------------------JS调用Android addJavascriptInterface()方式
        WebSettings webSettings = webView2.getSettings();

        // 设置与Js交互的权限
        webSettings.setJavaScriptEnabled(true);

        // 通过addJavascriptInterface()将Java对象映射到JS对象
        //参数1：Javascript对象名
        //参数2：Java对象名
        webView2.addJavascriptInterface(new AndroidtoJs(this), "test");//AndroidtoJS类对象映射到js的test对象

        // 加载JS代码
        // 格式规定为:file:///android_asset/文件名.html
        webView2.loadUrl("file:///android_asset/javascript_to_Android.html");



        //-------------------JS通过 WebViewClient 的方法shouldOverrideUrlLoading ()回调

        WebSettings webSettings3 = webView3.getSettings();
        // 设置与Js交互的权限
        webSettings3.setJavaScriptEnabled(true);
        // 设置允许JS弹窗
        webSettings3.setJavaScriptCanOpenWindowsAutomatically(true);

        // 步骤1：加载JS代码
        // 格式规定为:file:///android_asset/文件名.html
        webView3.loadUrl("file:///android_asset/javascript_to_Android2.html");
        // 复写WebViewClient类的shouldOverrideUrlLoading方法
        webView3.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                // 步骤2：根据协议的参数，判断是否是所需要的url
                // 一般根据scheme（协议格式） & authority（协议名）判断（前两个参数）
                //假定传入进来的 url = "js://webview?arg1=111&arg2=222"（同时也是约定好的需要拦截的）
                Uri uri = Uri.parse(String.valueOf(request.getUrl()));
                // 如果url的协议 = 预先约定的 js 协议
                // 就解析往下解析参数
                if (uri.getScheme().equals("js")){
                    // 如果 authority  = 预先约定协议里的 webview，即代表都符合约定的协议
                    // 所以拦截url,下面JS开始调用Android需要的方法
                    if (uri.getAuthority().equals("webview")){
                        //  步骤3：
                        // 执行JS所需要调用的逻辑
                        // 可以在协议上带有参数并传递到Android上
                        ArrayList<String> strings = new ArrayList<>();
                        Set<String> collection = uri.getQueryParameterNames();
                        for (String s : collection) {
                            strings.add(s);
                        }
                        Toast.makeText(MainActivity.this, "JS调用了Android的方法\n" + strings.get(0) + "\n" + strings.get(1), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, request);
            }
        });
        //注意: 如果Android 给返回值 JS, 则需要用loadUrl 的方式去个传递给JS
        webView3.loadUrl("javascript_to_Android2:returnResult(" + 1 + ")");




        //-------------------通过 WebChromeClient 的onJsAlert()、onJsConfirm()、onJsPrompt（）方法回调拦截JS对话框alert()、confirm()、prompt（） 消息
        WebSettings settings4 = webView4.getSettings();
        settings4.setJavaScriptEnabled(true);
        settings4.setJavaScriptCanOpenWindowsAutomatically(true);
        webView4.loadUrl("file:///android_asset/javascript_to_Android3.html");

        webView4.setWebChromeClient(new WebChromeClient(){
            // 拦截输入框(原理同方式2)
            // 参数message:代表promt（）的内容（不是url）
            // 参数result:代表输入框的返回值
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                // 根据协议的参数，判断是否是所需要的url(原理同方式2)
                // 一般根据scheme（协议格式） & authority（协议名）判断（前两个参数）
                //假定传入进来的 url = "js://demo?arg1=111&arg2=222"（同时也是约定好的需要拦截的）
                Uri uri = Uri.parse(message);
                // 如果url的协议 = 预先约定的 js 协议
                // 就解析往下解析参数
                if (uri.getScheme().equals("js")){
                    // 如果 authority  = 预先约定协议里的 webview，即代表都符合约定的协议
                    // 所以拦截url,下面JS开始调用Android需要的方法
                    if (uri.getAuthority().equals("demo")){
                        //执行所需要的协议
                        Set<String> queryParameterNames = uri.getQueryParameterNames();
                        ArrayList<String> strings = new ArrayList<>();
                        for (String queryParameterName : queryParameterNames) {
                            strings.add(queryParameterName);
                        }

                        Toast.makeText(MainActivity.this, message + "\n" + strings.get(0) + "\n" + strings.get(1), Toast.LENGTH_SHORT).show();

                        //参数result:代表消息框的返回值(输入值)
//                        result.confirm("JS调用Android成功啦!!");
                    }

                    return true;

                }

                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            // 拦截JS的警告框
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            // 拦截JS的确认框
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

        });


    }
}
