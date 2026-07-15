package com.diyy.music.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diyy.innertube.models.AlbumItem
import com.diyy.innertube.models.ArtistItem
import com.diyy.innertube.models.PlaylistItem
import com.diyy.innertube.models.SongItem
import com.diyy.innertube.models.YTItem
import com.diyy.music.R
import com.diyy.music.db.entities.Album
import com.diyy.music.db.entities.Artist
import com.diyy.music.db.entities.LocalItem
import com.diyy.music.db.entities.Playlist
import com.diyy.music.db.entities.Song
import com.diyy.music.extensions.toMediaItem
import com.diyy.music.playback.PlayerConnection
import com.diyy.music.playback.queues.ListQueue
import com.diyy.music.ui.component.DiyyEditorialCard
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.EmptyFigmaState
import com.diyy.music.ui.component.FigmaMediaGridItem
import com.diyy.music.ui.component.FigmaMediaRow
import com.diyy.music.ui.component.FigmaSectionHeader
import com.diyy.music.ui.displaySubtitle
import com.diyy.music.viewmodels.HomeViewModel
import java.util.Calendar
import kotlinx.coroutines.delay

private data class TimeGreeting(
    val title: String,
    val subtitle: String,
)

private data class HomeEditorial(
    val title: String,
    val subtitle: String,
    val imageRes: Int,
)

private val homeForYou = listOf(
    HomeEditorial("Late Night Vibes", "Smooth R&B and mellow pop", R.drawable.diyy_mix_late_night),
    HomeEditorial("Rainy Day", "Soft tracks for quiet weather", R.drawable.diyy_mix_rainy),
    HomeEditorial("Feel Good Hits", "Bright songs to lift the mood", R.drawable.diyy_mix_feel_good),
)

private val homeTonight = listOf(
    HomeEditorial("Sunset Drive", "Warm songs for the road", R.drawable.diyy_mix_sunset),
    HomeEditorial("Starry Mood", "Dreamy music after dark", R.drawable.diyy_mix_starry),
    HomeEditorial("After Hours", "Neon nights and slow beats", R.drawable.diyy_mix_after_hours),
)

private fun currentGreeting(): TimeGreeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 4..10 -> TimeGreeting("Good morning", "Start the day with something worth hearing.")
    in 11..15 -> TimeGreeting("Good afternoon", "Keep the day moving with your music.")
    in 16..21 -> TimeGreeting("Good evening", "Slow down and let the music settle in.")
    else -> TimeGreeting("Good night", "Unwind, relax, and let the music play.")
}

@Composable
fun ListenNowScreen(
    playerConnection: PlayerConnection?,
    onOpenProfile: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenCollection: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val recentEvents by viewModel.database.events().collectAsStateWithLifecycle(initialValue = emptyList())
    val quickPicks by viewModel.quickPicks.collectAsStateWithLifecycle()
    val mixes by viewModel.mixes.collectAsStateWithLifecycle()
    val homePage by viewModel.homePage.collectAsStateWithLifecycle()
    val accountImageUrl by viewModel.accountImageUrl.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()
    val greeting by produceState(initialValue = currentGreeting()) {
        while (true) {
            value = currentGreeting()
            delay(60_000L)
        }
    }

    LaunchedEffect(Unit) { viewModel.loadHomeData() }

    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        state = pullToRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
        indicator = {
            Indicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp),
                isRefreshing = isRefreshing,
                state = pullToRefreshState,
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
        item {
            DiyyScreenHeader(
                title = greeting.title,
                subtitle = greeting.subtitle,
                onHistory = onOpenHistory,
                onProfile = onOpenProfile,
                profileImageUrl = accountImageUrl,
            )
        }

        item {
            FigmaSectionHeader(
                title = "Recently Played",
                actionText = "See All ›",
                onAction = { onOpenCollection("recent") },
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        // Use the same event history source as the "See All" screen. The old
        // implementation used a shuffled most-played query with an offset, so a
        // perfectly valid short history could appear empty on Home.
        val recentItems = recentEvents.distinctBy { it.song.id }.map { it.song }.take(10)
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
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
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
                            modifier = Modifier.fillParentMaxWidth(0.41f),
                        )
                    }
                }
            }
        }

        val mixList = mixes.orEmpty()
        if (mixList.isNotEmpty()) {
            item {
                FigmaSectionHeader(title = "Your Mixes")
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(mixList, key = { it.artist.id }) { mix ->
                        FigmaMediaGridItem(
                            title = "${mix.artist.title} Mix",
                            subtitle = "${mix.songs.size} songs",
                            imageUrl = mix.artist.thumbnailUrl,
                            onClick = { playLocalSong(playerConnection, mix.songs, 0) },
                            circular = true,
                            modifier = Modifier.fillParentMaxWidth(0.41f),
                        )
                    }
                }
            }
        }

        item {
            FigmaSectionHeader(
                title = "For You",
                actionText = "See All ›",
                onAction = viewModel::refresh,
            )
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(homeForYou, key = { it.title }) { card ->
                    DiyyEditorialCard(
                        title = card.title,
                        subtitle = card.subtitle,
                        imageRes = card.imageRes,
                        onClick = {
                            val picks = quickPicks.orEmpty()
                            if (picks.isNotEmpty()) {
                                val index = homeForYou.indexOf(card).coerceAtMost(picks.lastIndex)
                                playLocalSong(playerConnection, picks, index)
                            } else {
                                viewModel.refresh()
                            }
                        },
                        modifier = Modifier.fillParentMaxWidth(0.48f),
                    )
                }
            }
        }

        item {
            FigmaSectionHeader(
                title = "Recommended Tonight",
                actionText = "Refresh",
                onAction = viewModel::refresh,
            )
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(homeTonight, key = { it.title }) { card ->
                    DiyyEditorialCard(
                        title = card.title,
                        subtitle = card.subtitle,
                        imageRes = card.imageRes,
                        onClick = {
                            val picks = quickPicks.orEmpty()
                            if (picks.isNotEmpty()) {
                                val index = (homeTonight.indexOf(card) + 3).coerceAtMost(picks.lastIndex)
                                playLocalSong(playerConnection, picks, index)
                            } else {
                                viewModel.refresh()
                            }
                        },
                        modifier = Modifier.fillParentMaxWidth(0.43f),
                    )
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
            val sectionSongs = section.items.filterIsInstance<SongItem>()
            item {
                FigmaSectionHeader(title = section.title)
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(section.items.take(12), key = { it.id }) { item ->
                        FigmaMediaGridItem(
                            title = itemTitle(item),
                            subtitle = itemSubtitle(item),
                            imageUrl = itemThumbnail(item),
                            onClick = {
                                when (item) {
                                    is SongItem -> playOnlineSection(
                                        connection = playerConnection,
                                        title = section.title,
                                        songs = sectionSongs,
                                        startIndex = sectionSongs.indexOfFirst { it.id == item.id },
                                    )
                                    is AlbumItem -> onOpenCollection("online_album:${item.id}")
                                    is ArtistItem -> onOpenCollection("online_artist:${item.id}")
                                    is PlaylistItem -> onOpenCollection("online_playlist:${item.id}")
                                    else -> Unit
                                }
                            },
                            circular = item is ArtistItem,
                            modifier = Modifier.fillParentMaxWidth(0.41f),
                        )
                    }
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

private fun playOnlineSection(
    connection: PlayerConnection?,
    title: String,
    songs: List<SongItem>,
    startIndex: Int,
) {
    if (connection == null || songs.isEmpty()) return
    connection.playQueue(
        ListQueue(
            title = title,
            items = songs.map { it.toMediaItem() },
            startIndex = startIndex.coerceIn(songs.indices),
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

private fun itemSubtitle(item: YTItem): String? = item.displaySubtitle()

private fun itemThumbnail(item: YTItem): String? = when (item) {
    is SongItem -> item.thumbnail
    is AlbumItem -> item.thumbnail
    is ArtistItem -> item.thumbnail
    is PlaylistItem -> item.thumbnail
    else -> null
}
