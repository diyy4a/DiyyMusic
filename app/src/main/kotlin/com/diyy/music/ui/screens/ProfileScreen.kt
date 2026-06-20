package com.diyy.music.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.diyy.music.R
import com.diyy.music.db.entities.Album
import com.diyy.music.db.entities.Artist
import com.diyy.music.db.entities.Playlist
import com.diyy.music.db.entities.Song
import com.diyy.music.ui.component.FigmaDivider
import com.diyy.music.ui.component.FigmaMediaGridItem
import com.diyy.music.ui.component.FigmaSectionHeader
import com.diyy.music.ui.component.FigmaSettingsRow
import com.diyy.music.ui.theme.DiyyRed
import com.diyy.music.viewmodels.HomeViewModel

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCollection: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val accountName by viewModel.accountName.collectAsStateWithLifecycle()
    val accountImageUrl by viewModel.accountImageUrl.collectAsStateWithLifecycle()
    val keepListening by viewModel.keepListening.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadHomeData() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 18.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(top = 8.dp, bottom = 28.dp),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart)) {
                    Icon(painterResource(R.drawable.arrow_back), "Back")
                }
                IconButton(onClick = onOpenSettings, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(painterResource(R.drawable.edit), "Edit", tint = DiyyRed)
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!accountImageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = accountImageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Icon(
                                painterResource(R.drawable.account),
                                contentDescription = null,
                                tint = DiyyRed,
                                modifier = Modifier.size(48.dp),
                            )
                        }
                    }
                    Text(
                        text = accountName.ifBlank { "DiyyMusic User" },
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    Text(
                        text = if (accountName == "Guest") "Local profile" else "Connected music account",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item { FigmaSectionHeader(title = "Listening To") }
        val listening = keepListening.orEmpty().take(8)
        if (listening.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(listening, key = { it.id }) { item ->
                        FigmaMediaGridItem(
                            title = item.title,
                            subtitle = when (item) {
                                is Song -> item.orderedArtists.joinToString { it.name }
                                is Album -> item.artists.joinToString { it.name }
                                is Artist -> "${item.songCount} songs"
                                is Playlist -> "${item.songCount} songs"
                            },
                            imageUrl = item.thumbnailUrl,
                            circular = item is Artist,
                            onClick = {
                                when (item) {
                                    is Song -> onOpenCollection("song:${item.id}")
                                    is Album -> onOpenCollection("album:${item.id}")
                                    is Artist -> onOpenCollection("artist:${item.id}")
                                    is Playlist -> onOpenCollection("playlist:${item.id}")
                                }
                            },
                            modifier = Modifier.fillParentMaxWidth(0.36f),
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
        item {
            FigmaSettingsRow(
                title = "Connected Services",
                icon = R.drawable.integration,
                onClick = onOpenSettings,
            )
        }
        item { FigmaDivider() }
        item {
            FigmaSettingsRow(
                title = "About",
                icon = R.drawable.info,
                onClick = onOpenSettings,
            )
        }
        item { FigmaDivider() }
        item {
            FigmaSettingsRow(
                title = "Help",
                icon = R.drawable.bug_report,
                onClick = onOpenSettings,
            )
        }
        item { FigmaDivider() }
        item {
            FigmaSettingsRow(
                title = "Settings",
                icon = R.drawable.settings,
                onClick = onOpenSettings,
            )
        }
        item { FigmaDivider() }
        item {
            FigmaSettingsRow(
                title = "Logout",
                icon = R.drawable.logout,
                destructive = true,
                onClick = onLogout,
            )
        }
    }
}
