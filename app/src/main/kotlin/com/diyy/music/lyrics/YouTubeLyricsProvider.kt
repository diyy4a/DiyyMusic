/**
 * Copyright (C) upstream contributors and DiyyMusic contributors
 * Modified for DiyyMusic in 2026. Licensed under GPL-3.0.
 */

package com.diyy.music.lyrics

import android.content.Context
import com.diyy.innertube.YouTube
import com.diyy.innertube.models.WatchEndpoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object YouTubeLyricsProvider : LyricsProvider {
    override val name = "YouTube Music"

    override fun isEnabled(context: Context) = true

    override suspend fun getLyrics(
        context: Context,
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val nextResult = YouTube.next(WatchEndpoint(videoId = id)).getOrThrow()
            Result.success(
                YouTube
                    .lyrics(
                        endpoint = nextResult.lyricsEndpoint
                            ?: throw IllegalStateException("Lyrics endpoint not found"),
                    ).getOrThrow() ?: throw IllegalStateException("Lyrics unavailable")
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
}
