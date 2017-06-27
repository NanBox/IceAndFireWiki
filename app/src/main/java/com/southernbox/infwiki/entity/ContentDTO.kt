package com.southernbox.infwiki.entity

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by SouthernBox on 2017/2/19.
 * 内容
 */
open class ContentDTO : RealmObject() {
    @PrimaryKey
    var id: String? = null
    var firstType: String? = null
    var secondType: String? = null
    var img: String? = null
    var name: String? = null
    var intro: String? = null
    var html: String? = null
}