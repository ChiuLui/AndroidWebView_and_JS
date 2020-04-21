package com.orange.myandroidandweb;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * 定义一个与JS对象映射关系的Android类
 *
 * @author 神经大条蕾弟
 * @date 2019/03/27 14:46
 */
public class AndroidtoJs extends Object {

    private Context context;

    public AndroidtoJs(Context context) {
        this.context = context;
    }

    // 定义JS需要调用的方法
    // 被JS调用的方法必须加入@JavascriptInterface注解
    @JavascriptInterface
    public void hello(String msg) {
        Toast.makeText(context, "JS调用了Android的hello方法\nJS传过来了: " + msg, Toast.LENGTH_LONG).show();
    }

}
