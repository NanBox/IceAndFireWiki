package com.southernbox.infwiki.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.southernbox.infwiki.R
import com.southernbox.infwiki.util.DayNightHelper
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by SouthernBox on 2017/2/20 0020.
 * Activity基类
 */

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {

    lateinit var mContext: Context
    lateinit var mDayNightHelper: DayNightHelper
    lateinit var mRealm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this

        //设置主题
        mDayNightHelper = DayNightHelper(this)
        if (mDayNightHelper.isDay) {
            setTheme(R.style.DayTheme)
        } else {
            setTheme(R.style.NightTheme)
        }

        //初始化Realm
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder().build()
        try {
            mRealm = Realm.getInstance(realmConfig)
        } catch (e: RuntimeException) {
            //删除数据库后重新初始化
            Realm.deleteRealm(realmConfig)
            mRealm = Realm.getInstance(realmConfig)
        }
    }

}
