package com.diyy.music.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diyy.music.R
import com.diyy.music.db.MusicDatabase
import com.diyy.music.extensions.toMediaItem
import com.diyy.music.playback.PlayerConnection
import com.diyy.music.playback.queues.ListQueue
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.EmptyFigmaState
import com.diyy.music.ui.component.FigmaLibraryShortcut
import com.diyy.music.ui.component.FigmaMediaGridItem
import com.diyy.music.ui.component.FigmaSectionHeader

@Composable
fun LibraryScreen(
    database: MusicDatabase,
    playerConnection: PlayerConnection?,
    onOpenProfile: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenCollection: (String) -> Unit,
    onOpenDisplayOptions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val songs by database.songsByCreateDateAsc().collectAsStateWithLifecycle(initialValue = emptyList())
    val albums by database.albumsByCreateDateAsc().collectAsStateWithLifecycle(initialValue = emptyList())
    val playlists by database.playlistsByCreateDateAsc().collectAsStateWithLifecycle(initialValue = emptyList())
    val artists by database.artistsByCreateDateAsc().collectAsStateWithLifecycle(initialValue = emptyList())
    val events by database.events().collectAsStateWithLifecycle(initialValue = emptyList())

    val recentSongs = songs.asReversed().take(12)
    val recentlyPlayed = events
        .distinctBy { it.song.id }
        .take(12)
        .map { it.song }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 18.dp),
    ) {
        item {
            DiyyScreenHeader(
                title = "Library",
                onHistory = onOpenHistory,
                onProfile = onOpenProfile,
            )
        }

        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FigmaLibraryShortcut(
                    title = "Songs",
                    icon = R.drawable.music_note,
                    selected = true,
                    onClick = { onOpenCollection("songs") },
                    modifier = Modifier.weight(1f),
                )
                FigmaLibraryShortcut(
                    title = "Albums",
                    icon = R.drawable.album,
                    selected = false,
                    onClick = { onOpenCollection("albums") },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FigmaLibraryShortcut(
                    title = "Playlists",
                    icon = R.drawable.queue_music,
                    selected = false,
                    onClick = { onOpenCollection("playlists") },
                    modifier = Modifier.weight(1f),
                )
                FigmaLibraryShortcut(
                    title = "Favorites",
                    icon = R.drawable.favorite,
                    selected = false,
                    onClick = { onOpenCollection("favorites") },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FigmaLibraryShortcut(
                    title = "Artists",
                    icon = R.drawable.artist,
                    selected = false,
                    onClick = { onOpenCollection("artists") },
                    modifier = Modifier.weight(1f),
                )
                FigmaLibraryShortcut(
                    title = "Display",
                    icon = R.drawable.tune,
                    selected = false,
                    onClick = onOpenDisplayOptions,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            FigmaSectionHeader(
                title = "Recently Added",
                actionText = "›",
                onAction = { onOpenCollection("songs") },
            )
        }
        if (recentSongs.isEmpty()) {
            item {
                EmptyFigmaState(
                    title = "Your library is empty",
                    subtitle = "Saved songs and albums will appear here.",
                    icon = R.drawable.library_music,
                )
            }
        } else {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(recentSongs, key = { it.id }) { song ->
                        FigmaMediaGridItem(
                            title = song.title,
                            subtitle = song.orderedArtists.joinToString { it.name },
                            imageUrl = song.thumbnailUrl,
                            onClick = {
                                playerConnection?.playQueue(
                                    ListQueue(
                                        title = "Recently Added",
                                        items = recentSongs.map { it.toMediaItem() },
                                        startIndex = recentSongs.indexOf(song),
                                    ),
                                )
                            },
                            modifier = Modifier.fillParentMaxWidth(0.38f),
                        )
                    }
                }
            }
        }

        item {
            FigmaSectionHeader(
                title = "Recently Played",
                actionText = "›",
                onAction = onOpenHistory,
            )
        }
        if (recentlyPlayed.isEmpty()) {
            item {
                EmptyFigmaState(
                    title = "No listening history",
                    subtitle = "Play something and DiyyMusic will remember it here.",
                    icon = R.drawable.history,
                )
            }
        } else {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(recentlyPlayed, key = { it.id }) { song ->
                        FigmaMediaGridItem(
                            title = song.title,
                            subtitle = song.orderedArtists.joinToString { it.name },
                            imageUrl = song.thumbnailUrl,
                            onClick = {
                                playerConnection?.playQueue(
                                    ListQueue(
                                        title = "Recently Played",
                                        items = recentlyPlayed.map { it.toMediaItem() },
                                        startIndex = recentlyPlayed.indexOf(song),
                                    ),
                                )
                            },
                            modifier = Modifier.fillParentMaxWidth(0.38f),
                        )
                    }
                }
            }
        }

        if (albums.isNotEmpty()) {
            item { FigmaSectionHeader(title = "Albums") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(albums.take(10), key = { it.id }) { album ->
                        FigmaMediaGridItem(
                            title = album.title,
                            subtitle = album.artists.joinToString { it.name },
                            imageUrl = album.thumbnailUrl,
                            onClick = { onOpenCollection("album:${album.id}") },
                            modifier = Modifier.fillParentMaxWidth(0.38f),
                        )
                    }
                }
            }
        }

        if (artists.isNotEmpty()) {
            item { FigmaSectionHeader(title = "Artists") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(artists.take(10), key = { it.id }) { artist ->
                        FigmaMediaGridItem(
                            title = artist.title,
                            subtitle = "${artist.songCount} songs",
                            imageUrl = artist.thumbnailUrl,
                            onClick = { onOpenCollection("artist:${artist.id}") },
                            circular = true,
                            modifier = Modifier.fillParentMaxWidth(0.38f),
                        )
                    }
                }
            }
        }

        if (playlists.isNotEmpty()) {
            item { FigmaSectionHeader(title = "Playlists") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(playlists.take(10), key = { it.id }) { playlist ->
                        FigmaMediaGridItem(
                            title = playlist.title,
                            subtitle = "${playlist.songCount} songs",
                            imageUrl = playlist.thumbnails.firstOrNull(),
                            onClick = { onOpenCollection("playlist:${playlist.id}") },
                            modifier = Modifier.fillParentMaxWidth(0.38f),
                        )
                    }
                }
            }
        }
    }
}
