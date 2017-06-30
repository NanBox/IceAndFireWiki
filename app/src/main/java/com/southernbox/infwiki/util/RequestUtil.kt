package com.southernbox.infwiki.util

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Created by SouthernBox on 2017/6/30 0030.
 * 请求工具类
 */

object RequestUtil {

    var myRequestServes: RequestServes
    var wikiRequestServes: RequestServes

    init {
        val myRetrofit = Retrofit.Builder()
                .baseUrl(BaseUrl.MY_URL)
                //增加返回值为String的支持
                .addConverterFactory(ScalarsConverterFactory.create())
                //增加返回值为实体类的支持
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        myRequestServes = myRetrofit.create(RequestServes::class.java)

        val wikiRetrofit = Retrofit.Builder()
                .baseUrl(BaseUrl.WIKI_URL)
                //增加返回值为String的支持
                .addConverterFactory(ScalarsConverterFactory.create())
                //增加返回值为实体类的支持
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        wikiRequestServes = wikiRetrofit.create(RequestServes::class.java)
    }

}