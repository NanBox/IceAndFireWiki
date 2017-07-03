package com.southernbox.infwiki.entity

/**
 * Created by SouthernBox on 2017/7/3 0003.
 * WebView 数据
 */

data class WebData(val title: String, val data: String, val type: Type) {
    enum class Type {HTML, URL }
}