package com.southernbox.infwiki.entity

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by SouthernBox on 2017/6/6.
 * TabLayout中的Tab项
 */
open class TabDTO : RealmObject() {
    @PrimaryKey
    var id: Int = 0
    var firstType: String? = null
    var secondType: String? = null
    var title: String? = null
}