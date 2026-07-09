/**
 * Copyright (C) upstream contributors and DiyyMusic contributors
 * Added for DiyyMusic in 2026. Licensed under GPL-3.0.
 */

package com.diyy.music.playback

import androidx.media3.common.MediaItem
import com.diyy.music.db.MusicDatabase
import com.diyy.music.db.entities.Song
import com.diyy.music.extensions.toMediaItem
import com.diyy.music.models.MediaMetadata
import kotlinx.coroutines.flow.first

/**
 * Builds a locally personalized "up next" queue for [seed] using the user's own library
 * signals (liked songs, play counts, and other songs by the same artist) instead of
 * depending on an online recommendation call. This keeps continuation working smoothly
 * offline and prioritizes music the user actually listens to over a generic online mix.
 *
 * Returns an empty list when the local library doesn't have enough related songs,
 * letting the caller fall back to an online source.
 */
suspend fun buildLocalMatchQueue(
    database: MusicDatabase,
    seed: MediaMetadata,
    excludeIds: Set<String>,
    limit: Int = 12,
): List<MediaItem> {
    val artistIds = seed.artists.mapNotNull { it.id }.distinct()
    if (artistIds.isEmpty()) return emptyList()

    val candidates = LinkedHashMap<String, Song>()
    for (artistId in artistIds) {
        val songsForArtist = runCatching {
            database.artistSongsByPlayTimeAsc(artistId).first()
        }.getOrDefault(emptyList())

        // Query returns ascending play time; walk from the end for most-played-first.
        for (song in songsForArtist.asReversed()) {
            if (song.id == seed.id || song.id in excludeIds) continue
            candidates.putIfAbsent(song.id, song)
        }
    }

    if (candidates.isEmpty()) return emptyList()

    // Score each candidate: liked songs and heavily-played songs rank higher,
    // giving a personalized mix instead of a purely chronological or random one.
    val ranked = candidates.values.sortedByDescending { song ->
        val likedBonus = if (song.song.liked) 1_000_000L else 0L
        likedBonus + song.song.totalPlayTime
    }

    return ranked.take(limit).map { it.toMediaItem() }
}
