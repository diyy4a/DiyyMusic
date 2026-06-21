package com.diyy.music.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
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
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.EmptyFigmaState
import com.diyy.music.ui.component.FigmaMediaRow
import com.diyy.music.ui.component.FigmaSectionHeader
import com.diyy.music.ui.component.LiquidGlassBox
import com.diyy.music.ui.theme.DiyyRed
import com.diyy.music.viewmodels.OnlineSearchSuggestionViewModel
import java.util.Locale

private data class SearchCategory(
    val title: String,
    val subtitle: String,
    val icon: Int,
    val lightColors: List<Color>,
    val darkColors: List<Color>,
)

private val trendingSearches = listOf(
    "Taylor Swift",
    "The Weeknd",
    "Raisa",
    "Joji",
    "Nadin Amizah",
)

private val searchCategories = listOf(
    SearchCategory(
        "Pop", "Popular hits", R.drawable.favorite,
        lightColors = listOf(Color(0xFFFFB7D0), Color(0xFFFFE4ED)),
        darkColors = listOf(Color(0xFF5C1731), Color(0xFF29111C)),
    ),
    SearchCategory(
        "Chill", "Slow & dreamy", R.drawable.cloud,
        lightColors = listOf(Color(0xFFD5BCFF), Color(0xFFF0E8FF)),
        darkColors = listOf(Color(0xFF3E2A64), Color(0xFF21182F)),
    ),
    SearchCategory(
        "Indie", "Fresh finds", R.drawable.radio,
        lightColors = listOf(Color(0xFFFFD2B6), Color(0xFFFFEEE1)),
        darkColors = listOf(Color(0xFF5A3024), Color(0xFF2A1915)),
    ),
    SearchCategory(
        "Anime", "J-pop & OST", R.drawable.artist,
        lightColors = listOf(Color(0xFFBFD1FF), Color(0xFFE8EEFF)),
        darkColors = listOf(Color(0xFF263E68), Color(0xFF151E31)),
    ),
    SearchCategory(
        "Focus", "Stay in flow", R.drawable.bedtime,
        lightColors = listOf(Color(0xFFBDEADF), Color(0xFFE4F8F3)),
        darkColors = listOf(Color(0xFF205045), Color(0xFF132A25)),
    ),
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

            item { FigmaSectionHeader(title = "Discover") }
            item {
                LiquidGlassBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .height(118.dp),
                    shape = RoundedCornerShape(28.dp),
                    elevation = 10.dp,
                    onClick = {
                        query = "Trending music"
                        submittedQuery = query
                    },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFFF88AF), DiyyRed),
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.trending_up),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(34.dp),
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Trending right now",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Find songs everyone is playing today.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            painter = painterResource(R.drawable.navigate_next),
                            contentDescription = null,
                            tint = DiyyRed,
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
                item { FigmaSectionHeader(title = "Top Results") }
                items(displayResults, key = { it.id }) { item ->
                    FigmaMediaRow(
                        title = item.title,
                        subtitle = searchSubtitle(item),
                        imageUrl = item.thumbnail,
                        onClick = {
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
                        },
                    )
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.45f
    val colors = if (isDark) category.darkColors else category.lightColors
    val titleColor = if (isDark) Color.White else Color(0xFF24171D)
    val subtitleColor = titleColor.copy(alpha = if (isDark) 0.66f else 0.58f)

    Box(
        modifier = Modifier
            .width(126.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(colors))
            .clickable(onClick = onClick)
            .padding(15.dp),
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(19.dp))
                .background(
                    if (isDark) Color.White.copy(alpha = 0.12f)
                    else Color.White.copy(alpha = 0.58f),
                )
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(category.icon),
                contentDescription = null,
                tint = DiyyRed,
                modifier = Modifier.size(28.dp),
            )
        }
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor,
            )
            Text(
                text = category.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor,
            )
        }
    }
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

private fun searchSubtitle(item: YTItem): String? = when (item) {
    is SongItem -> item.artists.joinToString { it.name }
    is AlbumItem -> item.artists.orEmpty().joinToString { it.name }
    is ArtistItem -> "Artist"
    is PlaylistItem -> item.author?.name ?: item.songCountText
    else -> null
}
