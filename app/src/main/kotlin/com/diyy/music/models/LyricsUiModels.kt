package com.diyy.music.models

import com.diyy.music.lyrics.LyricsEntry

enum class LyricsBackgroundStyle { SOLID, BLUR, GRADIENT }

sealed class LyricsListItem {
    data class Line(val index: Int, val entry: LyricsEntry) : LyricsListItem()
    data class Indicator(
        val afterLineIndex: Int,
        val gapMs: Long,
        val gapStartMs: Long,
        val gapEndMs: Long,
        val nextAgent: String?,
    ) : LyricsListItem()
}
