package com.diyy.music.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import com.diyy.music.ui.component.DiyyStatCard
import com.diyy.music.ui.component.EmptyFigmaState
import com.diyy.music.ui.component.FigmaDivider
import com.diyy.music.ui.component.FigmaGroupedList
import com.diyy.music.ui.component.FigmaMediaGridItem
import com.diyy.music.ui.component.FigmaMediaRow
import com.diyy.music.ui.component.FigmaSectionHeader
import com.diyy.music.ui.component.FigmaSettingsRow

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
    val downloadedSongs by database.downloadedSongsByCreateDateAsc().collectAsStateWithLifecycle(initialValue = emptyList())
    val artists by database.artistsByCreateDateAsc().collectAsStateWithLifecycle(initialValue = emptyList())
    val events by database.events().collectAsStateWithLifecycle(initialValue = emptyList())

    val recentSongs = songs.asReversed().take(12)
    val recentlyPlayed = events
        .distinctBy { it.song.id }
        .take(12)
        .map { it.song }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            DiyyScreenHeader(
                title = "Library",
                onHistory = onOpenHistory,
                onProfile = onOpenProfile,
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    DiyyStatCard(
                        value = songs.size.toString(),
                        label = "Songs",
                        icon = R.drawable.music_note,
                        modifier = Modifier.width(116.dp),
                        onClick = { onOpenCollection("songs") },
                    )
                }
                item {
                    DiyyStatCard(
                        value = playlists.size.toString(),
                        label = "Playlists",
                        icon = R.drawable.queue_music,
                        modifier = Modifier.width(116.dp),
                        onClick = { onOpenCollection("playlists") },
                    )
                }
                item {
                    DiyyStatCard(
                        value = albums.size.toString(),
                        label = "Albums",
                        icon = R.drawable.album,
                        modifier = Modifier.width(116.dp),
                        onClick = { onOpenCollection("albums") },
                    )
                }
                item {
                    DiyyStatCard(
                        value = artists.size.toString(),
                        label = "Artists",
                        icon = R.drawable.artist,
                        modifier = Modifier.width(116.dp),
                        onClick = { onOpenCollection("artists") },
                    )
                }
            }
        }

        item {
            FigmaSectionHeader(
                title = "Your Library",
                actionText = "Display",
                onAction = onOpenDisplayOptions,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        item {
            FigmaGroupedList(
                modifier = Modifier.padding(horizontal = 18.dp),
            ) {
                FigmaSettingsRow(
                    title = "Liked Songs",
                    subtitle = "Your favorite tracks",
                    icon = R.drawable.favorite,
                    onClick = { onOpenCollection("favorites") },
                )
                FigmaDivider()
                FigmaSettingsRow(
                    title = "Downloads",
                    subtitle = "${downloadedSongs.size} offline songs",
                    icon = R.drawable.download,
                    onClick = { onOpenCollection("downloads") },
                )
                FigmaDivider()
                FigmaSettingsRow(
                    title = "Playlists",
                    subtitle = "${playlists.size} playlists",
                    icon = R.drawable.queue_music,
                    onClick = { onOpenCollection("playlists") },
                )
                FigmaDivider()
                FigmaSettingsRow(
                    title = "Albums",
                    subtitle = "${albums.size} albums",
                    icon = R.drawable.album,
                    onClick = { onOpenCollection("albums") },
                )
                FigmaDivider()
                FigmaSettingsRow(
                    title = "Artists",
                    subtitle = "${artists.size} artists",
                    icon = R.drawable.artist,
                    onClick = { onOpenCollection("artists") },
                )
                FigmaDivider()
                FigmaSettingsRow(
                    title = "All Songs",
                    subtitle = "${songs.size} songs",
                    icon = R.drawable.library_music,
                    onClick = { onOpenCollection("songs") },
                )
            }
        }

        item {
            FigmaSectionHeader(
                title = "Recently Added",
                actionText = "See All ›",
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
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
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
                            modifier = Modifier.fillParentMaxWidth(0.41f),
                        )
                    }
                }
            }
        }

        if (recentlyPlayed.isNotEmpty()) {
            item {
                FigmaSectionHeader(
                    title = "Recently Played",
                    actionText = "History ›",
                    onAction = onOpenHistory,
                )
            }
            items(recentlyPlayed.take(5), key = { it.id }) { song ->
                FigmaMediaRow(
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
                )
            }
        }
    }
}
