package com.southernbox.infwiki.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.southernbox.infwiki.R
import kotlinx.android.synthetic.main.activity_about.*


/**
 * Created by SouthernBox on 2017/7/10.
 * 「关于」页面
 */

class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        initToolbar()
        initVersion()
    }

    private fun initToolbar() {
        about_toolbar.setNavigationOnClickListener({
            onBackPressed()
        })
        about_toolbar.post({
            //设置Toolbar的图标颜色
            val navigationIcon = about_toolbar.navigationIcon
            if (navigationIcon != null) {
                if (mDayNightHelper.isDay) {
                    navigationIcon.alpha = 255
                } else {
                    navigationIcon.alpha = 128
                }
            }
        })
    }

    private fun initVersion() {
        val packageManager = packageManager
        val packInfo = packageManager.getPackageInfo(packageName, 0)
        tv_version.text = packInfo.versionName
    }

    fun onHuijiClick(view: View) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("http://asoiaf.huijiwiki.com/wiki/%E5%86%B0%E4%B8%8E%E7%81%AB%E4%B9%8B%E6%AD%8C%E4%B8%AD%E6%96%87%E7%BB%B4%E5%9F%BA")
        startActivity(intent)
    }

    fun onSouthernBoxClick(view: View) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://github.com/southernbox")
        startActivity(intent)
    }

}