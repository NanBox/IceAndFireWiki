package com.southernbox.infwiki.entity

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by nanquan.lin on 2017/6/27 0027.
 */

open class Page : RealmObject() {
    @PrimaryKey
    var pageid: String? = null
    var ns: String? = null
    var title: String? = null
    var coverImg: String? = null
    var coverImgWidth: Int = 0
    var coverImgHeight: Int = 0
    var revisions: String? = null

}