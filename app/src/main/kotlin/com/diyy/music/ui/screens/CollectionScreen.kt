package com.diyy.music.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diyy.innertube.YouTube
import com.diyy.innertube.models.AlbumItem
import com.diyy.innertube.models.ArtistItem
import com.diyy.innertube.models.PlaylistItem
import com.diyy.innertube.models.SongItem
import com.diyy.innertube.models.YTItem
import com.diyy.innertube.pages.AlbumPage
import com.diyy.innertube.pages.ArtistPage
import com.diyy.innertube.pages.PlaylistPage
import com.diyy.music.R
import com.diyy.music.constants.HideVideoSongsKey
import com.diyy.music.constants.PlaylistSongSortDescendingKey
import com.diyy.music.constants.PlaylistSongSortType
import com.diyy.music.constants.PlaylistSongSortTypeKey
import com.diyy.music.db.MusicDatabase
import com.diyy.music.db.entities.Album
import com.diyy.music.db.entities.Artist
import com.diyy.music.db.entities.Playlist
import com.diyy.music.db.entities.PlaylistSong
import com.diyy.music.db.entities.Song
import com.diyy.music.extensions.reversed
import com.diyy.music.extensions.toEnum
import com.diyy.music.extensions.toMediaItem
import com.diyy.music.models.MediaMetadata
import com.diyy.music.models.toMediaMetadata
import com.diyy.music.playback.PlayerConnection
import com.diyy.music.playback.queues.ListQueue
import com.diyy.music.ui.component.AddToPlaylistSheet
import com.diyy.music.ui.component.CreatePlaylistDialog
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.EmptyFigmaState
import com.diyy.music.ui.component.FigmaMediaRow
import com.diyy.music.utils.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.Collator
import java.util.Locale

@Composable
fun CollectionScreen(
    type: String,
    database: MusicDatabase,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    onOpenCollection: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        type == "songs" -> LocalSongsCollection(database, playerConnection, onBack, false, modifier)
        type == "favorites" -> LocalSongsCollection(database, playerConnection, onBack, true, modifier)
        type == "downloads" -> DownloadedSongsCollection(database, playerConnection, onBack, modifier)
        type == "albums" -> LocalAlbumsCollection(database, onBack, onOpenCollection, modifier)
        type == "artists" -> LocalArtistsCollection(database, onBack, onOpenCollection, modifier)
        type == "playlists" -> LocalPlaylistsCollection(database, onBack, onOpenCollection, modifier)
        type == "recent" -> RecentCollection(database, playerConnection, onBack, modifier)
        type.startsWith("album:") -> LocalAlbumDetail(type.substringAfter(':'), database, playerConnection, onBack, modifier)
        type.startsWith("artist:") -> LocalArtistDetail(type.substringAfter(':'), database, playerConnection, onBack, modifier)
        type.startsWith("playlist:") -> LocalPlaylistDetail(type.substringAfter(':'), database, playerConnection, onBack, modifier)
        type.startsWith("song:") -> LocalSingleSong(type.substringAfter(':'), database, playerConnection, onBack, modifier)
        type.startsWith("online_album:") -> OnlineAlbumDetail(type.substringAfter(':'), database, playerConnection, onBack, modifier)
        type.startsWith("online_artist:") -> OnlineArtistDetail(type.substringAfter(':'), database, playerConnection, onBack, onOpenCollection, modifier)
        type.startsWith("online_playlist:") -> OnlinePlaylistDetail(type.substringAfter(':'), database, playerConnection, onBack, modifier)
        else -> UnknownCollection(onBack, modifier)
    }
}

@Composable
private fun LocalSongsCollection(
    database: MusicDatabase,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    favorites: Boolean,
    modifier: Modifier,
) {
    val songs by (if (favorites) database.likedSongsByCreateDateAsc() else database.songsByCreateDateAsc())
        .collectAsStateWithLifecycle(initialValue = emptyList())
    SongListScreen(
        title = if (favorites) "Favorites" else "Songs",
        songs = songs,
        database = database,
        playerConnection = playerConnection,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun DownloadedSongsCollection(
    database: MusicDatabase,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val songs by database.downloadedSongsByCreateDateAsc()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    SongListScreen(
        title = "Downloads",
        songs = songs,
        database = database,
        playerConnection = playerConnection,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun LocalAlbumsCollection(
    database: MusicDatabase,
    onBack: () -> Unit,
    onOpenCollection: (String) -> Unit,
    modifier: Modifier,
) {
    val albums by database.albumsByCreateDateAsc().collectAsStateWithLifecycle(initialValue = emptyList())
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 24.dp)) {
        item { DiyyScreenHeader("Albums", onBack = onBack) }
        if (albums.isEmpty()) item { EmptyFigmaState("No albums", "Saved albums will appear here.", R.drawable.album) }
        items(albums, key = { it.id }) { album ->
            FigmaMediaRow(
                title = album.title,
                subtitle = album.artists.joinToString { it.name },
                imageUrl = album.thumbnailUrl,
                onClick = { onOpenCollection("album:${album.id}") },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun LocalArtistsCollection(
    database: MusicDatabase,
    onBack: () -> Unit,
    onOpenCollection: (String) -> Unit,
    modifier: Modifier,
) {
    val artists by database.artistsByCreateDateAsc().collectAsStateWithLifecycle(initialValue = emptyList())
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 24.dp)) {
        item { DiyyScreenHeader("Artists", onBack = onBack) }
        if (artists.isEmpty()) item { EmptyFigmaState("No artists", "Artists from your library will appear here.", R.drawable.artist) }
        items(artists, key = { it.id }) { artist ->
            FigmaMediaRow(
                title = artist.title,
                subtitle = "${artist.songCount} songs",
                imageUrl = artist.thumbnailUrl,
                onClick = { onOpenCollection("artist:${artist.id}") },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun LocalPlaylistsCollection(
    database: MusicDatabase,
    onBack: () -> Unit,
    onOpenCollection: (String) -> Unit,
    modifier: Modifier,
) {
    val playlists by database.playlistsByCreateDateAsc().collectAsStateWithLifecycle(initialValue = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            DiyyScreenHeader(
                "Playlists",
                onBack = onBack,
                trailing = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(painterResource(R.drawable.add), contentDescription = "New playlist")
                    }
                },
            )
        }
        if (playlists.isEmpty()) item { EmptyFigmaState("No playlists", "Create or save a playlist to see it here.", R.drawable.queue_music) }
        items(playlists, key = { it.id }) { playlist ->
            FigmaMediaRow(
                title = playlist.title,
                subtitle = "${playlist.songCount} songs",
                imageUrl = playlist.thumbnails.firstOrNull(),
                onClick = { onOpenCollection("playlist:${playlist.id}") },
                modifier = Modifier.animateItem(),
            )
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            database = database,
            onDismiss = { showCreateDialog = false },
            onCreated = { id -> onOpenCollection("playlist:$id") },
        )
    }
}

@Composable
private fun RecentCollection(
    database: MusicDatabase,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val events by database.events().collectAsStateWithLifecycle(initialValue = emptyList())
    val songs = events.distinctBy { it.song.id }.map { it.song }
    SongListScreen("Recently Played", songs, database, playerConnection, onBack, modifier)
}

@Composable
private fun LocalAlbumDetail(
    id: String,
    database: MusicDatabase,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val album by database.albumWithSongs(id).collectAsStateWithLifecycle(initialValue = null)
    SongListScreen(
        title = album?.album?.title ?: "Album",
        songs = album?.songs.orEmpty(),
        database = database,
        playerConnection = playerConnection,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun LocalArtistDetail(
    id: String,
    database: MusicDatabase,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val artist by database.artist(id).collectAsStateWithLifecycle(initialValue = null)
    val songs by database.artistSongsByCreateDateAsc(id).collectAsStateWithLifecycle(initialValue = emptyList())
    SongListScreen(
        title = artist?.title ?: "Artist",
        songs = songs,
        database = database,
        playerConnection = playerConnection,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun LocalPlaylistDetail(
    id: String,
    database: MusicDatabase,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val playlist by database.playlist(id).collectAsStateWithLifecycle(initialValue = null)
    val rawPlaylistSongs by database.playlistSongs(id).collectAsStateWithLifecycle(initialValue = emptyList())

    val sortType by context.dataStore.data
        .map { it[PlaylistSongSortTypeKey].toEnum(PlaylistSongSortType.CUSTOM) }
        .collectAsStateWithLifecycle(initialValue = PlaylistSongSortType.CUSTOM)
    val sortDescending by context.dataStore.data
        .map { it[PlaylistSongSortDescendingKey] ?: true }
        .collectAsStateWithLifecycle(initialValue = true)
    val hideVideoSongs by context.dataStore.data
        .map { it[HideVideoSongsKey] ?: false }
        .collectAsStateWithLifecycle(initialValue = false)

    val displayedSongs = remember(rawPlaylistSongs, sortType, sortDescending, hideVideoSongs) {
        val filtered = if (hideVideoSongs) rawPlaylistSongs.filter { !it.song.song.isVideo } else rawPlaylistSongs
        when (sortType) {
            PlaylistSongSortType.CUSTOM -> filtered
            PlaylistSongSortType.CREATE_DATE -> filtered.sortedBy { it.map.id }
            PlaylistSongSortType.NAME -> {
                val collator = Collator.getInstance(Locale.getDefault()).apply { strength = Collator.PRIMARY }
                filtered.sortedWith(compareBy(collator) { it.song.song.title })
            }
            PlaylistSongSortType.ARTIST -> {
                val collator = Collator.getInstance(Locale.getDefault()).apply { strength = Collator.PRIMARY }
                filtered.sortedWith(compareBy(collator) { s -> s.song.artists.joinToString("") { it.name } })
            }
            PlaylistSongSortType.PLAY_TIME -> filtered.sortedBy { it.song.song.totalPlayTime }
        }.reversed(sortDescending && sortType != PlaylistSongSortType.CUSTOM)
    }

    var localOrder by remember { mutableStateOf(displayedSongs) }
    LaunchedEffect(displayedSongs) { localOrder = displayedSongs }

    var showSortMenu by remember { mutableStateOf(false) }
    var addToPlaylistSong by remember { mutableStateOf<MediaMetadata?>(null) }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromIndex = from.index - 1 // account for header item
        val toIndex = to.index - 1
        if (fromIndex < 0 || toIndex < 0 || fromIndex >= localOrder.size || toIndex >= localOrder.size) return@rememberReorderableLazyListState
        localOrder = localOrder.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        scope.launch(Dispatchers.IO) {
            database.transaction {
                move(id, fromIndex, toIndex)
            }
        }
    }

    LazyColumn(state = lazyListState, modifier = modifier, contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            DiyyScreenHeader(
                title = playlist?.title ?: "Playlist",
                subtitle = "${rawPlaylistSongs.size} songs",
                onBack = onBack,
                trailing = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(painterResource(R.drawable.tune), contentDescription = "Sort")
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            listOf(
                                PlaylistSongSortType.CUSTOM to "Custom order",
                                PlaylistSongSortType.CREATE_DATE to "Date added",
                                PlaylistSongSortType.NAME to "Title",
                                PlaylistSongSortType.ARTIST to "Artist",
                                PlaylistSongSortType.PLAY_TIME to "Play time",
                            ).forEach { (type, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(label, fontWeight = if (type == sortType) FontWeight.Bold else FontWeight.Normal)
                                    },
                                    onClick = {
                                        scope.launch { context.dataStore.edit { it[PlaylistSongSortTypeKey] = type.name } }
                                        showSortMenu = false
                                    },
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(if (sortDescending) "Descending" else "Ascending") },
                                onClick = {
                                    scope.launch { context.dataStore.edit { it[PlaylistSongSortDescendingKey] = !sortDescending } }
                                    showSortMenu = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(if (hideVideoSongs) "Show video songs" else "Hide video songs") },
                                onClick = {
                                    scope.launch { context.dataStore.edit { it[HideVideoSongsKey] = !hideVideoSongs } }
                                    showSortMenu = false
                                },
                            )
                        }
                    }
                },
            )
        }

        if (localOrder.isEmpty()) {
            item { EmptyFigmaState("This playlist is empty", "Add songs from anywhere in your library.", R.drawable.queue_music) }
        } else {
            items(localOrder, key = { it.map.id }) { playlistSong ->
                ReorderableItem(reorderableState, key = playlistSong.map.id) { isDragging ->
                    FigmaMediaRow(
                        title = playlistSong.song.title,
                        subtitle = playlistSong.song.orderedArtists.joinToString { it.name },
                        imageUrl = playlistSong.song.thumbnailUrl,
                        onClick = {
                            playerConnection?.playQueue(
                                ListQueue(
                                    title = playlist?.title ?: "Playlist",
                                    items = localOrder.map { it.song.toMediaItem() },
                                    startIndex = localOrder.indexOf(playlistSong),
                                ),
                            )
                        },
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { addToPlaylistSong = playlistSong.song.toMediaMetadata() }) {
                                    Icon(painterResource(R.drawable.playlist_add), contentDescription = "Add to another playlist")
                                }
                                IconButton(onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        database.transaction { delete(playlistSong.map) }
                                    }
                                }) {
                                    Icon(painterResource(R.drawable.close), contentDescription = "Remove from playlist")
                                }
                                if (sortType == PlaylistSongSortType.CUSTOM) {
                                    IconButton(
                                        onClick = {},
                                        modifier = Modifier.draggableHandle(),
                                    ) {
                                        Icon(painterResource(R.drawable.drag_handle), contentDescription = "Reorder")
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    addToPlaylistSong?.let { metadata ->
        AddToPlaylistSheet(
            database = database,
            songs = listOf(metadata),
            onDismiss = { addToPlaylistSong = null },
        )
    }
}

@Composable
private fun LocalSingleSong(
    id: String,
    database: MusicDatabase,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val song by database.song(id).collectAsStateWithLifecycle(initialValue = null)
    SongListScreen("Song", listOfNotNull(song), database, playerConnection, onBack, modifier)
}

@Composable
private fun SongListScreen(
    title: String,
    songs: List<Song>,
    database: MusicDatabase,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    var addToPlaylistSong by remember { mutableStateOf<MediaMetadata?>(null) }

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 24.dp)) {
        item { DiyyScreenHeader(title, onBack = onBack) }
        if (songs.isEmpty()) {
            item { EmptyFigmaState("Nothing here yet", "This collection is currently empty.") }
        } else {
            items(songs, key = { it.id }) { song ->
                FigmaMediaRow(
                    title = song.title,
                    subtitle = song.orderedArtists.joinToString { it.name },
                    imageUrl = song.thumbnailUrl,
                    onClick = {
                        playerConnection?.playQueue(
                            ListQueue(
                                title = title,
                                items = songs.map { it.toMediaItem() },
                                startIndex = songs.indexOf(song),
                            ),
                        )
                    },
                    modifier = Modifier.animateItem(),
                    trailing = {
                        IconButton(onClick = { addToPlaylistSong = song.toMediaMetadata() }) {
                            Icon(painterResource(R.drawable.playlist_add), contentDescription = "Add to playlist")
                        }
                    },
                )
            }
        }
    }

    addToPlaylistSong?.let { metadata ->
        AddToPlaylistSheet(
            database = database,
            songs = listOf(metadata),
            onDismiss = { addToPlaylistSong = null },
        )
    }
}

@Composable
private fun OnlineAlbumDetail(
    id: String,
    database: MusicDatabase,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    var page by remember { mutableStateOf<AlbumPage?>(null) }
    LaunchedEffect(id) { page = YouTube.album(id).getOrNull() }
    OnlineSongList(
        title = page?.album?.title ?: "Album",
        songs = page?.songs.orEmpty(),
        database = database,
        playerConnection = playerConnection,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun OnlinePlaylistDetail(
    id: String,
    database: MusicDatabase,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    var page by remember { mutableStateOf<PlaylistPage?>(null) }
    LaunchedEffect(id) { page = YouTube.playlist(id).getOrNull() }
    OnlineSongList(
        title = page?.playlist?.title ?: "Playlist",
        songs = page?.songs.orEmpty(),
        database = database,
        playerConnection = playerConnection,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun OnlineArtistDetail(
    id: String,
    database: MusicDatabase,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    onOpenCollection: (String) -> Unit,
    modifier: Modifier,
) {
    var page by remember { mutableStateOf<ArtistPage?>(null) }
    LaunchedEffect(id) { page = YouTube.artist(id).getOrNull() }
    val items = page?.sections.orEmpty().flatMap { it.items }
    val playableSongs = items.filterIsInstance<SongItem>()
    var addToPlaylistSong by remember { mutableStateOf<MediaMetadata?>(null) }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 24.dp)) {
        item { DiyyScreenHeader(page?.artist?.title ?: "Artist", onBack = onBack) }
        if (items.isEmpty()) {
            item { EmptyFigmaState("Loading artist", "Songs and albums will appear here.", R.drawable.artist) }
        } else {
            items(items, key = { it.id }) { item ->
                val songItem = item as? SongItem
                FigmaMediaRow(
                    title = item.title,
                    subtitle = onlineSubtitle(item),
                    imageUrl = item.thumbnail,
                    onClick = {
                        when (item) {
                            is SongItem -> playOnlineSongs(
                                connection = playerConnection,
                                title = page?.artist?.title ?: "Artist",
                                songs = playableSongs,
                                startIndex = playableSongs.indexOfFirst { it.id == item.id },
                            )
                            is AlbumItem -> onOpenCollection("online_album:${item.id}")
                            is PlaylistItem -> onOpenCollection("online_playlist:${item.id}")
                            is ArtistItem -> onOpenCollection("online_artist:${item.id}")
                            else -> Unit
                        }
                    },
                    modifier = Modifier.animateItem(),
                    trailing = if (songItem != null) {
                        {
                            IconButton(onClick = { addToPlaylistSong = songItem.toMediaMetadata() }) {
                                Icon(painterResource(R.drawable.playlist_add), contentDescription = "Add to playlist")
                            }
                        }
                    } else null,
                )
            }
        }
    }

    addToPlaylistSong?.let { metadata ->
        AddToPlaylistSheet(
            database = database,
            songs = listOf(metadata),
            onDismiss = { addToPlaylistSong = null },
        )
    }
}

@Composable
private fun OnlineSongList(
    title: String,
    songs: List<SongItem>,
    database: MusicDatabase,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    var addToPlaylistSong by remember { mutableStateOf<MediaMetadata?>(null) }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 24.dp)) {
        item { DiyyScreenHeader(title, onBack = onBack) }
        if (songs.isEmpty()) {
            item { EmptyFigmaState("Loading music", "This collection is being fetched.") }
        } else {
            items(songs, key = { it.id }) { song ->
                FigmaMediaRow(
                    title = song.title,
                    subtitle = song.artists.joinToString { it.name },
                    imageUrl = song.thumbnail,
                    onClick = {
                        playOnlineSongs(
                            connection = playerConnection,
                            title = title,
                            songs = songs,
                            startIndex = songs.indexOfFirst { it.id == song.id },
                        )
                    },
                    modifier = Modifier.animateItem(),
                    trailing = {
                        IconButton(onClick = { addToPlaylistSong = song.toMediaMetadata() }) {
                            Icon(painterResource(R.drawable.playlist_add), contentDescription = "Add to playlist")
                        }
                    },
                )
            }
        }
    }

    addToPlaylistSong?.let { metadata ->
        AddToPlaylistSheet(
            database = database,
            songs = listOf(metadata),
            onDismiss = { addToPlaylistSong = null },
        )
    }
}

private fun playOnlineSongs(
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

@Composable
private fun UnknownCollection(onBack: () -> Unit, modifier: Modifier) {
    LazyColumn(modifier = modifier) {
        item { DiyyScreenHeader("Collection", onBack = onBack) }
        item { EmptyFigmaState("Not available", "This collection cannot be opened yet.") }
    }
}

private fun onlineSubtitle(item: YTItem): String? = when (item) {
    is SongItem -> item.artists.joinToString { it.name }
    is AlbumItem -> item.artists.orEmpty().joinToString { it.name }
    is ArtistItem -> "Artist"
    is PlaylistItem -> item.author?.name ?: item.songCountText
    else -> null
}
