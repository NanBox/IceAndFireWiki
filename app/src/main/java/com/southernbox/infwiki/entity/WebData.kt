package com.southernbox.infwiki.entity

/**
 * Created by SouthernBox on 2017/7/3 0003.
 * WebView 数据
 */

data class WebData(val title: String, var content: String, var scrollY: Int, val type: Type) {
    enum class Type {HTML, URL }
}