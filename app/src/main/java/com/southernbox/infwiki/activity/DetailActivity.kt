package com.southernbox.infwiki.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.webkit.WebSettings
import com.bumptech.glide.Glide
import com.southernbox.infwiki.R
import com.southernbox.infwiki.js.Js2Java
import com.southernbox.infwiki.util.BaseUrl
import com.southernbox.infwiki.util.ToastUtil
import kotlinx.android.synthetic.main.activity_detail.*
import retrofit2.Call
import retrofit2.Callback

/**
 * Created SouthernBox on 2016/3/27.
 * 详情页面
 */

@SuppressLint("SetJavaScriptEnabled")
class DetailActivity : BaseActivity() {

    lateinit var title: String
    lateinit var img: String
    lateinit var html: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val bundle = intent.extras
        title = bundle.getString("title")
        img = bundle.getString("img")
        html = bundle.getString("html")
        initView()
        initData()
    }

    private fun initView() {
        val theme = mContext.theme
        val lightTextColor = TypedValue()
        theme.resolveAttribute(R.attr.lightTextColor, lightTextColor, true)
        collapsing_toolbar.setCollapsedTitleTextColor(ContextCompat
                .getColor(mContext, lightTextColor.resourceId))
        collapsing_toolbar.setExpandedTitleTextColor(ContextCompat
                .getColorStateList(mContext, lightTextColor.resourceId))
        web_view.settings.javaScriptEnabled = true
        web_view.addJavascriptInterface(Js2Java(this), "Android")
        // 支持多窗口
        web_view.settings.setSupportMultipleWindows(true)
        // 开启 DOM storage API 功能
        web_view.settings.domStorageEnabled = true
        // 开启 Application Caches 功能
        web_view.settings.setAppCacheEnabled(true)

        toolbar.post({
            //设置Toolbar的图标颜色
            val navigationIcon = toolbar.navigationIcon
            if (navigationIcon != null) {
                if (mDayNightHelper.isDay) {
                    navigationIcon.alpha = 255
                } else {
                    navigationIcon.alpha = 128
                }
            }
        })
    }

    private fun initData() {
        toolbar.title = title

        Glide
                .with(this)
                .load(BaseUrl.WIKI_URL + img)
                .override(480, 270)
                .crossFade()
                .into(image_view)

        val call = requestServes.get(html)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
                if (response.body() != null) {
                    var htmlData: String = response.body().toString()
                    if (mDayNightHelper.isNight) {
                        htmlData = htmlData.replace("p {",
                                "p {color:#9F9F9F;")
                        htmlData = htmlData.replace("<body>", "<body bgcolor=\"#4F4F4F\">")
                    }
                    web_view.settings.cacheMode = WebSettings.LOAD_DEFAULT
                    web_view.loadDataWithBaseURL(
                            "file:///android_asset/", htmlData, "text/html", "utf-8", null)
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                ToastUtil.show(mContext, "网络连接失败，请重试")
                web_view.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            }
        })

        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener({
            web_view.visibility = View.GONE
            onBackPressed()
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            web_view.visibility = View.GONE
            onBackPressed()
        }
        return true
    }

    companion object {

        fun show(context: Context, options: ActivityOptionsCompat,
                 title: String, img: String, html: String) {
            val intent = Intent(context, DetailActivity::class.java)
            val bundle = Bundle()
            bundle.putString("title", title)
            bundle.putString("img", img)
            bundle.putString("html", html)
            intent.putExtras(bundle)
            ActivityCompat.startActivity(context, intent, options.toBundle())
        }

        fun show(context: Context, title: String?, img: String?, html: String?) {
            val intent = Intent(context, DetailActivity::class.java)
            val bundle = Bundle()
            bundle.putString("title", title)
            bundle.putString("img", img)
            bundle.putString("html", html)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }
}
