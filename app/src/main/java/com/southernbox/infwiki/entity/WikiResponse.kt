package com.southernbox.infwiki.entity

import com.google.gson.annotations.SerializedName

/**
 * Created by nanquan.lin on 2017/6/27 0027.
 */

data class WikiResponse(
        @SerializedName("continue")
        var next: Continue,
        var query: Query
)