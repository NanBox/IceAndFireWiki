package com.southernbox.infwiki.entity

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by SouthernBox on 2017/6/27 0027.
 * 维基页面数据
 */

open class Page : RealmObject() {
    @PrimaryKey
    var pageid: Int = 0
    var ns: Int = 0
    //页面标题
    var title: String = ""
    //所属分类
    var categories: String = ""
    //封面图片
    var coverImg: String = ""
    //封面图片宽度
    var coverImgWidth: Int = 0
    //封面图片高度
    var coverImgHeight: Int = 0
    //页面内容
    var content = ""
}