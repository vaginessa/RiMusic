package it.vfsfitvnm.innertube.models.bodies

import it.vfsfitvnm.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class ContinuationBodyWithLocale(
    val context: Context = Context.DefaultWebWithLocale,
    val continuation: String,
)
