package com.diyy.music.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diyy.innertube.YouTube
import com.diyy.innertube.models.AlbumItem
import com.diyy.innertube.models.ArtistItem
import com.diyy.innertube.models.PlaylistItem
import com.diyy.innertube.models.SongItem
import com.diyy.innertube.models.YTItem
import com.diyy.music.R
import com.diyy.music.extensions.toMediaItem
import com.diyy.music.playback.PlayerConnection
import com.diyy.music.playback.queues.ListQueue
import com.diyy.music.ui.component.Artwork
import com.diyy.music.ui.component.DiyyEditorialCard
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.EmptyFigmaState
import com.diyy.music.ui.component.FigmaMediaGridItem
import com.diyy.music.ui.component.FigmaMediaRow
import com.diyy.music.ui.component.FigmaSectionHeader
import com.diyy.music.ui.component.LiquidGlassBox
import com.diyy.music.ui.displayArtistName
import com.diyy.music.ui.displaySubtitle
import com.diyy.music.ui.theme.DiyyRed
import com.diyy.music.viewmodels.OnlineSearchSuggestionViewModel
import java.util.Locale

private data class SearchCategory(
    val title: String,
    val subtitle: String,
    val imageRes: Int,
)

private data class SearchEditorial(
    val title: String,
    val subtitle: String,
    val imageRes: Int,
    val query: String,
)

private val trendingSearches = listOf(
    "Taylor Swift",
    "The Weeknd",
    "Raisa",
    "Joji",
    "Nadin Amizah",
)

private val searchCategories = listOf(
    SearchCategory("Pop", "Popular hits", R.drawable.diyy_category_pop),
    SearchCategory("Chill", "Slow & dreamy", R.drawable.diyy_category_chill),
    SearchCategory("Indie", "Fresh finds", R.drawable.diyy_category_indie),
    SearchCategory("Hip-Hop", "Beats & rhythm", R.drawable.diyy_category_hiphop),
)

private val searchForYou = listOf(
    SearchEditorial("Late Night Vibes", "Chill tracks for the night", R.drawable.diyy_mix_late_night, "late night r&b"),
    SearchEditorial("Rainy Day", "Perfect for rainy weather", R.drawable.diyy_mix_rainy, "rainy day songs"),
    SearchEditorial("Feel Good Hits", "Uplifting and energetic", R.drawable.diyy_mix_feel_good, "feel good hits"),
    SearchEditorial("After Hours", "Neon nights and slow beats", R.drawable.diyy_mix_after_hours, "after hours music"),
)

@Composable
fun SearchScreen(
    playerConnection: PlayerConnection?,
    initialQuery: String = "",
    onOpenCollection: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnlineSearchSuggestionViewModel = hiltViewModel(),
) {
    var query by remember { mutableStateOf(initialQuery) }
    var submittedQuery by remember { mutableStateOf(initialQuery.trim()) }
    var searchResults by remember { mutableStateOf<List<YTItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenQuery = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.trim()
            if (!spokenQuery.isNullOrBlank()) {
                query = spokenQuery
                submittedQuery = spokenQuery
            }
        }
    }
    val launchVoiceSearch = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Search music")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        try {
            voiceSearchLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Voice search is not available on this device.", Toast.LENGTH_SHORT).show()
        }
        Unit
    }

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) {
            query = initialQuery
            submittedQuery = initialQuery.trim()
        }
    }
    LaunchedEffect(query) { viewModel.query.value = query }
    LaunchedEffect(submittedQuery) {
        if (submittedQuery.isBlank()) {
            searchResults = emptyList()
            isSearching = false
        } else {
            isSearching = true
            searchResults = YouTube.searchSummary(submittedQuery)
                .getOrNull()
                ?.summaries
                ?.flatMap { it.items }
                ?.distinctBy { it.id }
                .orEmpty()
            isSearching = false
        }
    }

    val displayResults = if (submittedQuery.isNotBlank() && query.trim() == submittedQuery) {
        searchResults.ifEmpty { state.items }
    } else {
        state.items
    }
    val playableResults = displayResults.filterIsInstance<SongItem>()
    val artistResults = displayResults.filterIsInstance<ArtistItem>()
    val albumResults = displayResults.filterIsInstance<AlbumItem>()
    val playlistResults = displayResults.filterIsInstance<PlaylistItem>()
    val normalizedQuery = submittedQuery.trim().lowercase()
    val matchedArtist = artistResults.firstOrNull {
        it.title.trim().lowercase() == normalizedQuery
    } ?: artistResults.firstOrNull()
    val topResult: YTItem? = matchedArtist
        ?: playableResults.firstOrNull()
        ?: albumResults.firstOrNull()
        ?: playlistResults.firstOrNull()
    val fallbackArtistName = matchedArtist?.title
    val openResult: (YTItem) -> Unit = { item ->
        when (item) {
            is SongItem -> playSearchSongs(
                connection = playerConnection,
                songs = playableResults,
                startIndex = playableResults.indexOfFirst { it.id == item.id },
                title = submittedQuery.ifBlank { "Search" },
            )
            is AlbumItem -> onOpenCollection("online_album:${item.id}")
            is ArtistItem -> onOpenCollection("online_artist:${item.id}")
            is PlaylistItem -> onOpenCollection("online_playlist:${item.id}")
            else -> Unit
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item { DiyyScreenHeader(title = "Search") }
        item {
            SearchField(
                value = query,
                onValueChange = { query = it },
                onSearch = { submittedQuery = query.trim() },
                onVoiceSearch = launchVoiceSearch,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
            )
        }

        if (query.isBlank()) {
            item {
                FigmaSectionHeader(
                    title = "Trending Searches",
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    items(trendingSearches, key = { it }) { trend ->
                        SearchChip(
                            text = trend,
                            onClick = {
                                query = trend
                                submittedQuery = trend
                            },
                        )
                    }
                }
            }
            item {
                FigmaSectionHeader(
                    title = "Browse Categories",
                    actionText = "See All",
                    onAction = {
                        query = "Music"
                        submittedQuery = "Music"
                    },
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(searchCategories, key = { it.title }) { category ->
                        CategoryCard(
                            category = category,
                            onClick = {
                                query = category.title
                                submittedQuery = category.title
                            },
                        )
                    }
                }
            }

            item {
                FigmaSectionHeader(
                    title = "For You",
                    actionText = "More",
                    onAction = {
                        query = "personalized music"
                        submittedQuery = query
                    },
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(searchForYou, key = { it.title }) { card ->
                        DiyyEditorialCard(
                            title = card.title,
                            subtitle = card.subtitle,
                            imageRes = card.imageRes,
                            onClick = {
                                query = card.query
                                submittedQuery = card.query
                            },
                            modifier = Modifier.width(176.dp),
                            cardHeight = 210.dp,
                        )
                    }
                }
            }
            if (state.history.isNotEmpty()) {
                item { FigmaSectionHeader(title = "Recent Searches") }
                items(state.history.take(4), key = { "blank-${it.id}" }) { history ->
                    SuggestionRow(
                        text = history.query,
                        icon = R.drawable.history,
                        onClick = {
                            query = history.query
                            submittedQuery = history.query
                        },
                    )
                }
            }
        } else {
            if (state.history.isNotEmpty()) {
                item { FigmaSectionHeader(title = "Recent Searches") }
                items(state.history.take(3), key = { it.id }) { history ->
                    SuggestionRow(
                        text = history.query,
                        icon = R.drawable.history,
                        onClick = {
                            query = history.query
                            submittedQuery = history.query
                        },
                    )
                }
            }

            if (state.suggestions.isNotEmpty() && submittedQuery.isBlank()) {
                item { FigmaSectionHeader(title = "Suggestions") }
                items(state.suggestions.take(8), key = { it }) { suggestion ->
                    SuggestionRow(
                        text = suggestion,
                        icon = R.drawable.search,
                        onClick = {
                            query = suggestion
                            submittedQuery = suggestion
                        },
                    )
                }
            }

            if (displayResults.isNotEmpty()) {
                topResult?.let { result ->
                    item { FigmaSectionHeader(title = "Top Result") }
                    item {
                        if (result is ArtistItem) {
                            SearchArtistResultCard(
                                artist = result,
                                onClick = { openResult(result) },
                            )
                        } else {
                            FigmaMediaRow(
                                title = result.title,
                                subtitle = result.displaySubtitle(fallbackArtistName),
                                imageUrl = result.thumbnail,
                                onClick = { openResult(result) },
                            )
                        }
                    }
                }

                if (playableResults.isNotEmpty()) {
                    item {
                        FigmaSectionHeader(
                            title = "Songs",
                            actionText = if (playableResults.size > 6) "See All" else null,
                        )
                    }
                    items(playableResults.take(6), key = { "song-${it.id}" }) { song ->
                        FigmaMediaRow(
                            title = song.title,
                            subtitle = song.displayArtistName(fallbackArtistName),
                            imageUrl = song.thumbnail,
                            onClick = { openResult(song) },
                        )
                    }
                }

                if (artistResults.isNotEmpty()) {
                    item { FigmaSectionHeader(title = "Artists") }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(artistResults.take(8), key = { "artist-${it.id}" }) { artist ->
                                FigmaMediaGridItem(
                                    title = artist.title,
                                    subtitle = "Artist",
                                    imageUrl = artist.thumbnail,
                                    circular = true,
                                    showPlayButton = false,
                                    onClick = { openResult(artist) },
                                    modifier = Modifier.fillParentMaxWidth(0.34f),
                                )
                            }
                        }
                    }
                }

                if (albumResults.isNotEmpty()) {
                    item { FigmaSectionHeader(title = "Albums") }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(albumResults.take(8), key = { "album-${it.id}" }) { album ->
                                FigmaMediaGridItem(
                                    title = album.title,
                                    subtitle = album.displaySubtitle(fallbackArtistName),
                                    imageUrl = album.thumbnail,
                                    onClick = { openResult(album) },
                                    modifier = Modifier.fillParentMaxWidth(0.38f),
                                )
                            }
                        }
                    }
                }

                if (playlistResults.isNotEmpty()) {
                    item { FigmaSectionHeader(title = "Playlists") }
                    items(playlistResults.take(4), key = { "playlist-${it.id}" }) { playlist ->
                        FigmaMediaRow(
                            title = playlist.title,
                            subtitle = playlist.displaySubtitle(),
                            imageUrl = playlist.thumbnail,
                            onClick = { openResult(playlist) },
                        )
                    }
                }
            } else if (isSearching) {
                item {
                    EmptyFigmaState(
                        title = "Searching",
                        subtitle = "Finding music for “$submittedQuery”.",
                        icon = R.drawable.search,
                    )
                }
            } else if (submittedQuery.isNotBlank() && query.trim() == submittedQuery) {
                item {
                    EmptyFigmaState(
                        title = "No results",
                        subtitle = "Nothing matched “$submittedQuery”.",
                        icon = R.drawable.search,
                    )
                }
            }
        }
    }
}


@Composable
private fun SearchArtistResultCard(
    artist: ArtistItem,
    onClick: () -> Unit,
) {
    LiquidGlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = 6.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Artwork(
                url = artist.thumbnail,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                cornerRadius = 100,
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    text = "Artist",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                painter = painterResource(R.drawable.navigate_next),
                contentDescription = "Open artist",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun playSearchSongs(
    connection: PlayerConnection?,
    songs: List<SongItem>,
    startIndex: Int,
    title: String,
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
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onVoiceSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LiquidGlassBox(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = 10.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.search),
                contentDescription = null,
                tint = DiyyRed,
                modifier = Modifier.size(23.dp),
            )
            Spacer(Modifier.width(11.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(DiyyRed),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isBlank()) {
                            Text(
                                text = "Search songs, artists, albums…",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        inner()
                    }
                },
            )
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                onClick = onVoiceSearch,
            ) {
                Icon(
                    painter = painterResource(R.drawable.mic),
                    contentDescription = "Voice search",
                    tint = DiyyRed,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun SearchChip(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        onClick = onClick,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp),
        )
    }
}

@Composable
private fun CategoryCard(
    category: SearchCategory,
    onClick: () -> Unit,
) {
    DiyyEditorialCard(
        title = category.title,
        subtitle = category.subtitle,
        imageRes = category.imageRes,
        onClick = onClick,
        modifier = Modifier.width(136.dp),
        cardHeight = 158.dp,
        showPlayButton = false,
    )
}

@Composable
private fun SuggestionRow(
    text: String,
    icon: Int,
    onClick: () -> Unit,
) {
    LiquidGlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 4.dp)
            .height(54.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = 4.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = DiyyRed,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

