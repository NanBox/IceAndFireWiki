package com.southernbox.infwiki.js

import android.content.Context
import android.webkit.JavascriptInterface

import com.southernbox.infwiki.activity.DetailActivity
import com.southernbox.infwiki.entity.ContentDTO

import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by SouthernBox on 2016/4/1.
 * 实现和JavaScript交互
 */

class Js2Java(private val mContext: Context) {

    @JavascriptInterface
    fun goDetail(id: String) {
        val realmConfig = RealmConfiguration.Builder().build()
        val mRealm = Realm.getInstance(realmConfig)

        val content = mRealm.where(ContentDTO::class.java)
                .equalTo("id", id)
                .findFirst()

//        if (content != null) {
//            DetailActivity.show(
//                    mContext,
//                    content.name,
//                    content.img,
//                    content.html)
//        }
    }
}
