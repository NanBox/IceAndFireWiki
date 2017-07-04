package com.southernbox.infwiki.entity

import com.google.gson.annotations.SerializedName

/**
 * Created by Southern on 2017/6/27 0027.
 * 维基返回数据
 */

data class WikiResponse(
        @SerializedName("continue")
        val next: Continue?,
        val query: Query
) {
    data class Continue(val cmcontinue: String,
                        val sroffset: Int,
                        @SerializedName("continue")
                        val next: String)

    data class Query(val categorymembers: ArrayList<Page>,
                     val search: ArrayList<Search>)
}