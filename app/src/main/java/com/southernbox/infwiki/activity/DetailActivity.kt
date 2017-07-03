package com.southernbox.infwiki.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.southernbox.infwiki.R
import com.southernbox.infwiki.js.Js2Java
import com.southernbox.infwiki.util.RequestUtil
import kotlinx.android.synthetic.main.activity_detail.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLDecoder

/**
 * Created SouthernBox on 2016/3/27.
 * 详情页面
 */

@Suppress("NAME_SHADOWING")
@SuppressLint("SetJavaScriptEnabled")
class DetailActivity : BaseActivity() {

    var titleList = ArrayList<String>()

    companion object {

        fun show(context: Context, title: String?) {
            val intent = Intent(context, DetailActivity::class.java)
            val bundle = Bundle()
            bundle.putString("title", title)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val bundle = intent.extras
        val title = bundle.getString("title")
        web_view.setWebViewClient(MyWebViewClient())
        initView()
        showContent(title)
    }

    fun initView() {
        val theme = mContext.theme
        val lightTextColor = TypedValue()
        theme.resolveAttribute(R.attr.lightTextColor, lightTextColor, true)
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

        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener({
            web_view.visibility = View.GONE
            onBackPressed()
        })
    }

    fun setTitle() {
        toolbar.title = titleList[titleList.lastIndex]
    }

    fun showContent(title: String) {
        val call = RequestUtil.wikiRequestServes.getContent(title)
        progress_bar.visibility = View.VISIBLE
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>) {
                val jsonObject = JSONObject(response.body())
                val pageObject = jsonObject.getJSONObject("query").getJSONObject("pages")
                val keys = pageObject.keys()
                if (keys.hasNext()) {
                    val key = keys.next()
                    val page = pageObject.getJSONObject(key)
                    val htmlData = page.getJSONArray("revisions").getJSONObject(0).getString("*")
                    web_view.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "utf-8", null)
                    titleList.add(title)
                    setTitle()
                }
                progress_bar.visibility = View.GONE
            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                progress_bar.visibility = View.GONE
            }
        })
    }

    fun showImage(title: String) {
        val call = RequestUtil.wikiRequestServes.getImage(title)
        progress_bar.visibility = View.VISIBLE
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>) {
                val jsonObject = JSONObject(response.body())
                val pageObject = jsonObject.getJSONObject("query").getJSONObject("pages")
                val keys = pageObject.keys()
                if (keys.hasNext()) {
                    val key = keys.next()
                    val page = pageObject.getJSONObject(key)
                    val imgUrl = page.getJSONObject("thumbnail").getString("source")
                    web_view.loadUrl(imgUrl)
                    titleList.add("图片")
                    setTitle()
                }
                progress_bar.visibility = View.GONE
            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                progress_bar.visibility = View.GONE
            }
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && web_view.canGoBack()) {
            web_view.goBack()
            titleList.removeAt(titleList.lastIndex)
            setTitle()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    inner class MyWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.startsWith("file:///wiki/")) {
                val title = URLDecoder.decode(url.substring(13), "UTF-8")
                if (title.startsWith("File:")) {
                    //暂时无法播放视频
                    if (!title.endsWith(".video")) {
                        showImage(title)
                    }
                } else {
                    showContent(title)
                }
            } else {
                //使用浏览器打开
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            }
            return true
        }
    }
}
