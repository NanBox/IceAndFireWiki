package com.southernbox.infwiki.entity

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by SouthernBox on 2017/6/27 0027.
 */

open class Tab : RealmObject() {
    var type: String? = null
    var tabTitle: String? = null
    @PrimaryKey
    var title: String? = null
}