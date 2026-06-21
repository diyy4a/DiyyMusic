package com.diyy.music.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.diyy.music.db.MusicDatabase
import com.diyy.music.db.entities.Album
import com.diyy.music.db.entities.Artist
import com.diyy.music.db.entities.Playlist
import com.diyy.music.db.entities.Song
import com.diyy.music.extensions.toMediaItem
import com.diyy.music.playback.PlayerConnection
import com.diyy.music.playback.queues.ListQueue
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.EmptyFigmaState
import com.diyy.music.ui.component.FigmaMediaRow

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
        type.startsWith("online_album:") -> OnlineAlbumDetail(type.substringAfter(':'), playerConnection, onBack, modifier)
        type.startsWith("online_artist:") -> OnlineArtistDetail(type.substringAfter(':'), playerConnection, onBack, onOpenCollection, modifier)
        type.startsWith("online_playlist:") -> OnlinePlaylistDetail(type.substringAfter(':'), playerConnection, onBack, modifier)
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
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 24.dp)) {
        item { DiyyScreenHeader("Playlists", onBack = onBack) }
        if (playlists.isEmpty()) item { EmptyFigmaState("No playlists", "Create or save a playlist to see it here.", R.drawable.queue_music) }
        items(playlists, key = { it.id }) { playlist ->
            FigmaMediaRow(
                title = playlist.title,
                subtitle = "${playlist.songCount} songs",
                imageUrl = playlist.thumbnails.firstOrNull(),
                onClick = { onOpenCollection("playlist:${playlist.id}") },
            )
        }
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
    SongListScreen("Recently Played", songs, playerConnection, onBack, modifier)
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
    val playlist by database.playlist(id).collectAsStateWithLifecycle(initialValue = null)
    val playlistSongs by database.playlistSongs(id).collectAsStateWithLifecycle(initialValue = emptyList())
    SongListScreen(
        title = playlist?.title ?: "Playlist",
        songs = playlistSongs.map { it.song },
        playerConnection = playerConnection,
        onBack = onBack,
        modifier = modifier,
    )
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
    SongListScreen("Song", listOfNotNull(song), playerConnection, onBack, modifier)
}

@Composable
private fun SongListScreen(
    title: String,
    songs: List<Song>,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    modifier: Modifier,
) {
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
                )
            }
        }
    }
}

@Composable
private fun OnlineAlbumDetail(
    id: String,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    var page by remember { mutableStateOf<AlbumPage?>(null) }
    LaunchedEffect(id) { page = YouTube.album(id).getOrNull() }
    OnlineSongList(
        title = page?.album?.title ?: "Album",
        songs = page?.songs.orEmpty(),
        playerConnection = playerConnection,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun OnlinePlaylistDetail(
    id: String,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    var page by remember { mutableStateOf<PlaylistPage?>(null) }
    LaunchedEffect(id) { page = YouTube.playlist(id).getOrNull() }
    OnlineSongList(
        title = page?.playlist?.title ?: "Playlist",
        songs = page?.songs.orEmpty(),
        playerConnection = playerConnection,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun OnlineArtistDetail(
    id: String,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    onOpenCollection: (String) -> Unit,
    modifier: Modifier,
) {
    var page by remember { mutableStateOf<ArtistPage?>(null) }
    LaunchedEffect(id) { page = YouTube.artist(id).getOrNull() }
    val items = page?.sections.orEmpty().flatMap { it.items }
    val playableSongs = items.filterIsInstance<SongItem>()
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 24.dp)) {
        item { DiyyScreenHeader(page?.artist?.title ?: "Artist", onBack = onBack) }
        if (items.isEmpty()) {
            item { EmptyFigmaState("Loading artist", "Songs and albums will appear here.", R.drawable.artist) }
        } else {
            items(items, key = { it.id }) { item ->
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
                )
            }
        }
    }
}

@Composable
private fun OnlineSongList(
    title: String,
    songs: List<SongItem>,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    modifier: Modifier,
) {
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
                )
            }
        }
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
