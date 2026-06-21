package com.diyy.music.ui

import com.diyy.innertube.models.AlbumItem
import com.diyy.innertube.models.ArtistItem
import com.diyy.innertube.models.PlaylistItem
import com.diyy.innertube.models.SongItem
import com.diyy.innertube.models.YTItem

private val genericCreatorLabels = setOf(
    "song",
    "songs",
    "track",
    "tracks",
    "video",
    "music",
    "music video",
    "official audio",
    "official video",
    "single",
    "album",
    "ep",
    "playlist",
    "audio",
    "lagu",
    "musik",
    "video musik",
    "singel",
)

fun String.isUsefulCreatorLabel(): Boolean {
    val normalized = trim().lowercase()
    if (normalized.isBlank()) return false
    if (normalized in genericCreatorLabels) return false
    if (normalized.matches(Regex("\\d{4}"))) return false
    if (normalized.matches(Regex("\\d{1,2}:\\d{2}"))) return false
    return true
}

fun SongItem.displayArtistName(fallback: String? = null): String {
    val creatorNames = artists
        .asSequence()
        .map { it.name.trim() }
        .filter { it.isUsefulCreatorLabel() }
        .distinctBy { it.lowercase() }
        .toList()

    if (creatorNames.isNotEmpty()) return creatorNames.joinToString()

    val safeFallback = fallback
        ?.trim()
        ?.takeIf { it.isUsefulCreatorLabel() }
        ?.takeUnless { it.equals(title, ignoreCase = true) }

    return safeFallback ?: "Unknown artist"
}

fun YTItem.displaySubtitle(fallbackArtist: String? = null): String? = when (this) {
    is SongItem -> displayArtistName(fallbackArtist)
    is AlbumItem -> artists
        .orEmpty()
        .map { it.name.trim() }
        .filter { it.isUsefulCreatorLabel() }
        .distinctBy { it.lowercase() }
        .joinToString()
        .ifBlank { "Album" }
    is ArtistItem -> "Artist"
    is PlaylistItem -> author?.name
        ?.takeIf { it.isUsefulCreatorLabel() }
        ?: songCountText
        ?: "Playlist"
    else -> null
}
