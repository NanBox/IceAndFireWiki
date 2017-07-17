package com.southernbox.infwiki.app

import android.app.Application
import com.avos.avoscloud.AVAnalytics
import com.avos.avoscloud.AVOSCloud

/**
 * Created by nanquan.lin on 2017/7/17 0017.
 * Application
 */

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AVOSCloud.initialize(this, "f8UhPLkBYqzhDgx29y0fX1dG-gzGzoHsz", "O71xWliJjpJbdR7NWp6QHOnb")
        AVAnalytics.enableCrashReport(this, true)
    }
}
