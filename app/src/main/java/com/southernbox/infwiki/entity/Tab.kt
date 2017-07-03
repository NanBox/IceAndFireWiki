package com.southernbox.infwiki.entity

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by SouthernBox on 2017/6/27 0027.
 * 分类标签
 */

open class Tab : RealmObject() {
    @PrimaryKey
    var title: String? = null
    var type: String? = null
    var tabTitle: String? = null
}