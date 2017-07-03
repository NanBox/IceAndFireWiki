package com.southernbox.infwiki.entity

import com.google.gson.annotations.SerializedName

/**
 * Created by Southern on 2017/6/27 0027.
 * 维基返回数据
 */

data class WikiResponse(
        @SerializedName("continue")
        var next: Continue?,
        var query: Query
) {
    data class Continue(var cmcontinue: String,
                        @SerializedName("continue")
                        var next: String)

    data class Query(var categorymembers: ArrayList<Page>)
}