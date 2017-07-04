package com.southernbox.infwiki.entity

/**
 * Created by SouthernBox on 2017/7/4 0004.
 * 搜索结果
 */

data class Search(
        val ns: Int,
        //页面标题
        val title: String,
        //已解析的页面标题
        val titlesnippet: String?,
        //已解析的匹配分类
        var categorysnippet: String?,
        //已解析的匹配章节标题
        val sectionsnippet: String?)