package com.southernbox.infwiki.util

import com.southernbox.infwiki.entity.Tab
import com.southernbox.infwiki.entity.WikiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by nanquan.lin on 2017/1/19 0019.
 * 网络请求服务器
 */

interface RequestServes {

    @get:GET("WikiTab.json")
    val tab: Call<List<Tab>>

    //获取页面详情
    @GET("api.php?action=query&prop=revisions&format=json&rvprop=content&rvparse=true")
    fun getContent(@Query("titles") title: String): Call<String>

    //获取图片详情
    @GET("api.php?action=query&prop=pageimages&format=json&pithumbsize=500&pilicense=any")
    fun getImage(@Query("titles") title: String): Call<String>

    //获取分类成员页面
    @GET("api.php?action=query&format=json&list=categorymembers&cmlimit=20&cmsort=timestamp")
    fun getCategoryMembers(@Query("cmtitle") title: String): Call<WikiResponse>

    //获取下一页分类成员页面
    @GET("api.php?action=query&format=json&list=categorymembers&cmlimit=20&cmsort=timestamp")
    fun getCategoryMembers(@Query("cmtitle") title: String, @Query("cmcontinue") cmcontinue: String): Call<WikiResponse>

    //获取页面封面
    @GET("api.php?action=query&prop=pageimages&format=json&pithumbsize=300&pilimit=20")
    fun getPageImages(@Query("titles") titles: String): Call<String>

    //全文搜索
    @GET("api.php?action=query&list=search&format=json&srnamespace=0&srlimit=50&srprop=titlesnippet|sectionsnippet|categorysnippet")
    fun search(@Query("srsearch") keyword: String): Call<WikiResponse>
}
