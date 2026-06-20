package com.diyy.innertube.models.body

import com.diyy.innertube.models.Context
import com.diyy.innertube.models.Continuation
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val context: Context,
    val browseId: String?,
    val params: String?,
    val continuation: String?
)
