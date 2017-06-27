package com.southernbox.infwiki.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.southernbox.infwiki.R
import com.southernbox.infwiki.util.DayNightHelper
import com.southernbox.infwiki.util.RequestServes
import com.southernbox.infwiki.util.BaseUrl

import io.realm.Realm
import io.realm.RealmConfiguration
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Created by nanquan.lin on 2017/2/20 0020.
 * Activity基类
 */

open class BaseActivity : AppCompatActivity() {

    lateinit var mContext: Context
    lateinit var mDayNightHelper: DayNightHelper
    lateinit var mRealm: Realm
    lateinit var requestServes: RequestServes

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

        //初始化Retrofit
        val retrofit = Retrofit.Builder()
                .baseUrl(BaseUrl.MY_URL)
                //增加返回值为String的支持
                .addConverterFactory(ScalarsConverterFactory.create())
                //增加返回值为实体类的支持
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        requestServes = retrofit.create(RequestServes::class.java)
    }

}
