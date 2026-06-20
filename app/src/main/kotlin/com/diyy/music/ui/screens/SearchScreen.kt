package com.diyy.music.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.diyy.innertube.models.WatchEndpoint
import com.diyy.innertube.models.YTItem
import com.diyy.music.R
import com.diyy.music.models.toMediaMetadata
import com.diyy.music.playback.PlayerConnection
import com.diyy.music.playback.queues.YouTubeQueue
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.EmptyFigmaState
import com.diyy.music.ui.component.FigmaMediaRow
import com.diyy.music.ui.theme.DiyyRed
import com.diyy.music.viewmodels.OnlineSearchSuggestionViewModel

private data class SearchCategory(
    val title: String,
    val icon: Int,
)

private val searchCategories = listOf(
    SearchCategory("Bollywood", R.drawable.music_note),
    SearchCategory("Punjabi", R.drawable.graphic_eq),
    SearchCategory("Hip - Hop", R.drawable.trending_up),
    SearchCategory("Rock", R.drawable.radio_button_checked),
    SearchCategory("Classical", R.drawable.artist),
    SearchCategory("Pop", R.drawable.music_note),
    SearchCategory("Charts", R.drawable.stats),
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

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 18.dp),
    ) {
        item { DiyyScreenHeader(title = "Categories") }
        item {
            SearchField(
                value = query,
                onValueChange = { query = it },
                onSearch = { submittedQuery = query.trim() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }

        if (query.isBlank()) {
            val pairs = searchCategories.chunked(2)
            items(pairs, key = { row -> row.joinToString { it.title } }) { row ->
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEachIndexed { index, category ->
                        CategoryCard(
                            category = category,
                            onClick = { query = category.title; submittedQuery = category.title },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        } else {
            if (state.history.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Searches",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
                items(state.history.take(3), key = { it.id }) { history ->
                    SuggestionRow(
                        text = history.query,
                        icon = R.drawable.history,
                        onClick = { query = history.query; submittedQuery = history.query },
                    )
                }
            }

            if (state.suggestions.isNotEmpty()) {
                item {
                    Text(
                        text = "Suggestions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
                items(state.suggestions.take(8), key = { it }) { suggestion ->
                    SuggestionRow(
                        text = suggestion,
                        icon = R.drawable.search,
                        onClick = { query = suggestion; submittedQuery = suggestion },
                    )
                }
            }

            if (displayResults.isNotEmpty()) {
                item {
                    Text(
                        text = "Top Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
                items(displayResults, key = { it.id }) { item ->
                    FigmaMediaRow(
                        title = item.title,
                        subtitle = searchSubtitle(item),
                        imageUrl = item.thumbnail,
                        onClick = {
                            when (item) {
                                is SongItem -> playerConnection?.playQueue(
                                    YouTubeQueue(
                                        endpoint = item.endpoint ?: WatchEndpoint(videoId = item.id),
                                        preloadItem = item.toMediaMetadata(),
                                    ),
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

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(21.dp),
            )
            Spacer(Modifier.width(9.dp))
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
                                "Search music",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        inner()
                    }
                },
            )
            Icon(
                painter = painterResource(R.drawable.language),
                contentDescription = null,
                tint = DiyyRed,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: SearchCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Icon(
                painter = painterResource(category.icon),
                contentDescription = null,
                tint = DiyyRed,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.End),
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
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

private fun searchSubtitle(item: YTItem): String? = when (item) {
    is SongItem -> item.artists.joinToString { it.name }
    is AlbumItem -> item.artists.orEmpty().joinToString { it.name }
    is ArtistItem -> "Artist"
    is PlaylistItem -> item.author?.name ?: item.songCountText
    else -> null
}
