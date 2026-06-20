package com.diyy.music.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diyy.innertube.models.AlbumItem
import com.diyy.innertube.models.ArtistItem
import com.diyy.innertube.models.PlaylistItem
import com.diyy.innertube.models.SongItem
import com.diyy.innertube.models.WatchEndpoint
import com.diyy.innertube.models.YTItem
import com.diyy.music.db.entities.Album
import com.diyy.music.db.entities.Artist
import com.diyy.music.db.entities.LocalItem
import com.diyy.music.db.entities.Playlist
import com.diyy.music.db.entities.Song
import com.diyy.music.extensions.toMediaItem
import com.diyy.music.models.toMediaMetadata
import com.diyy.music.playback.PlayerConnection
import com.diyy.music.playback.queues.ListQueue
import com.diyy.music.playback.queues.YouTubeQueue
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.EmptyFigmaState
import com.diyy.music.ui.component.FigmaMediaGridItem
import com.diyy.music.ui.component.FigmaMediaRow
import com.diyy.music.ui.component.FigmaPromoCard
import com.diyy.music.ui.component.FigmaSectionHeader
import com.diyy.music.viewmodels.HomeViewModel

@Composable
fun ListenNowScreen(
    playerConnection: PlayerConnection?,
    onOpenProfile: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenCollection: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val keepListening by viewModel.keepListening.collectAsStateWithLifecycle()
    val quickPicks by viewModel.quickPicks.collectAsStateWithLifecycle()
    val homePage by viewModel.homePage.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadHomeData() }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 18.dp),
    ) {
        item {
            DiyyScreenHeader(
                title = "Listen Now",
                onHistory = onOpenHistory,
                onProfile = onOpenProfile,
            )
        }

        item {
            FigmaPromoCard(
                title = "Get 1 month of free\nmusic",
                subtitle = "Try It Free",
                footer = "1 month free then 99.00/month.",
                onClick = { viewModel.refresh() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        item {
            FigmaPromoCard(
                title = "Play anything with Siri.\nOnly 49.00/month.",
                subtitle = "Try It Free",
                footer = "Listen without interruptions.",
                onClick = { viewModel.refresh() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        item {
            FigmaSectionHeader(
                title = "Recently Played",
                actionText = "See All",
                onAction = { onOpenCollection("recent") },
            )
        }

        val recentItems = keepListening.orEmpty().take(10)
        if (recentItems.isEmpty()) {
            item {
                EmptyFigmaState(
                    title = "Nothing played yet",
                    subtitle = "Your recent music will appear here.",
                )
            }
        } else {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(recentItems, key = { it.id }) { item ->
                        FigmaMediaGridItem(
                            title = item.title,
                            subtitle = localSubtitle(item),
                            imageUrl = item.thumbnailUrl,
                            onClick = {
                                when (item) {
                                    is Song -> playLocalSong(playerConnection, listOf(item), 0)
                                    is Album -> onOpenCollection("album:${item.id}")
                                    is Artist -> onOpenCollection("artist:${item.id}")
                                    is Playlist -> onOpenCollection("playlist:${item.id}")
                                }
                            },
                            circular = item is Artist,
                            modifier = Modifier.fillParentMaxWidth(0.36f),
                        )
                    }
                }
            }
        }

        item {
            FigmaSectionHeader(
                title = "Quick Picks",
                actionText = "Refresh",
                onAction = viewModel::refresh,
            )
        }

        val quick = quickPicks.orEmpty().take(8)
        if (quick.isEmpty()) {
            item {
                EmptyFigmaState(
                    title = "Recommendations are loading",
                    subtitle = "DiyyMusic is preparing picks from your listening history.",
                )
            }
        } else {
            items(quick, key = { it.id }) { song ->
                FigmaMediaRow(
                    title = song.title,
                    subtitle = song.orderedArtists.joinToString { it.name },
                    imageUrl = song.thumbnailUrl,
                    onClick = { playLocalSong(playerConnection, quick, quick.indexOf(song)) },
                )
            }
        }

        homePage?.sections?.take(3)?.forEach { section ->
            item { FigmaSectionHeader(title = section.title) }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(section.items.take(12), key = { it.id }) { item ->
                        FigmaMediaGridItem(
                            title = itemTitle(item),
                            subtitle = itemSubtitle(item),
                            imageUrl = itemThumbnail(item),
                            onClick = {
                                when (item) {
                                    is SongItem -> playerConnection?.playQueue(
                                        YouTubeQueue(
                                            endpoint = WatchEndpoint(videoId = item.id),
                                            preloadItem = item.toMediaMetadata(),
                                        ),
                                    )
                                    is AlbumItem -> onOpenCollection("online_album:${item.id}")
                                    is ArtistItem -> onOpenCollection("online_artist:${item.id}")
                                    is PlaylistItem -> onOpenCollection("online_playlist:${item.id}")
                                    else -> Unit
                                }
                            },
                            circular = item is ArtistItem,
                            modifier = Modifier.fillParentMaxWidth(0.36f),
                        )
                    }
                }
            }
        }
    }
}

private fun playLocalSong(connection: PlayerConnection?, songs: List<Song>, index: Int) {
    if (connection == null || songs.isEmpty()) return
    connection.playQueue(
        ListQueue(
            title = "DiyyMusic",
            items = songs.map { it.toMediaItem() },
            startIndex = index.coerceIn(songs.indices),
        ),
    )
}

private fun localSubtitle(item: LocalItem): String? = when (item) {
    is Song -> item.orderedArtists.joinToString { it.name }
    is Album -> item.artists.joinToString { it.name }
    is Artist -> "${item.songCount} songs"
    is Playlist -> "${item.songCount} songs"
}

private fun itemTitle(item: YTItem): String = when (item) {
    is SongItem -> item.title
    is AlbumItem -> item.title
    is ArtistItem -> item.title
    is PlaylistItem -> item.title
    else -> item.id
}

private fun itemSubtitle(item: YTItem): String? = when (item) {
    is SongItem -> item.artists.joinToString { it.name }
    is AlbumItem -> item.artists.orEmpty().joinToString { it.name }
    is ArtistItem -> "Artist"
    is PlaylistItem -> item.author?.name
    else -> null
}

private fun itemThumbnail(item: YTItem): String? = when (item) {
    is SongItem -> item.thumbnail
    is AlbumItem -> item.thumbnail
    is ArtistItem -> item.thumbnail
    is PlaylistItem -> item.thumbnail
    else -> null
}
