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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.diyy.music.R
import com.diyy.music.db.entities.Album
import com.diyy.music.db.entities.Artist
import com.diyy.music.db.entities.Playlist
import com.diyy.music.db.entities.Song
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.DiyyStatCard
import com.diyy.music.ui.component.FigmaDivider
import com.diyy.music.ui.component.FigmaGroupedList
import com.diyy.music.ui.component.FigmaMediaGridItem
import com.diyy.music.ui.component.FigmaSectionHeader
import com.diyy.music.ui.component.FigmaSettingsRow
import com.diyy.music.ui.component.LiquidGlassBox
import com.diyy.music.ui.theme.DiyyRed
import com.diyy.music.ui.theme.DiyySoftRed
import com.diyy.music.viewmodels.HomeViewModel

@Composable
fun ProfileScreen(
    onBack: (() -> Unit)?,
    onOpenSettings: () -> Unit,
    onOpenCollection: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val accountName by viewModel.accountName.collectAsStateWithLifecycle()
    val accountImageUrl by viewModel.accountImageUrl.collectAsStateWithLifecycle()
    val keepListening by viewModel.keepListening.collectAsStateWithLifecycle()
    val isGuest = accountName.isBlank() || accountName == "Guest"

    LaunchedEffect(Unit) { viewModel.loadHomeData() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            DiyyScreenHeader(
                title = "Profile",
                onBack = onBack,
                trailing = {
                    if (onBack != null) Spacer(Modifier.width(44.dp))
                },
            )
        }

        item {
            LiquidGlassBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .height(190.dp),
                shape = RoundedCornerShape(32.dp),
                elevation = 12.dp,
            ) {
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0x66FF7DA8),
                                    Color.Transparent,
                                ),
                            ),
                            CircleShape,
                        ),
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(DiyySoftRed),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!accountImageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = accountImageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.account),
                                contentDescription = null,
                                tint = DiyyRed,
                                modifier = Modifier.size(54.dp),
                            )
                        }
                    }
                    Spacer(Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = accountName.ifBlank { "DiyyMusic User" },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (isGuest) "Local profile" else "Connected music account",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DiyyRed,
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "music is my escape ♥",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    DiyyStatCard(
                        value = keepListening.orEmpty().size.toString(),
                        label = "Recent",
                        icon = R.drawable.history,
                        modifier = Modifier.width(116.dp),
                    )
                }
                item {
                    DiyyStatCard(
                        value = if (isGuest) "Local" else "Online",
                        label = "Account",
                        icon = R.drawable.account,
                        modifier = Modifier.width(116.dp),
                    )
                }
                item {
                    DiyyStatCard(
                        value = "0.7.0",
                        label = "Version",
                        icon = R.drawable.info,
                        modifier = Modifier.width(116.dp),
                    )
                }
            }
        }

        val listening = keepListening.orEmpty().take(8)
        if (listening.isNotEmpty()) {
            item {
                FigmaSectionHeader(
                    title = "Listening To",
                    actionText = "See All ›",
                    onAction = { onOpenCollection("recent") },
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
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
                            modifier = Modifier.fillParentMaxWidth(0.41f),
                        )
                    }
                }
            }
        }

        item { FigmaSectionHeader(title = "Account & App") }
        item {
            FigmaGroupedList(
                modifier = Modifier.padding(horizontal = 18.dp),
            ) {
                FigmaSettingsRow(
                    title = "Account & Token",
                    subtitle = "Manage login token and connected account",
                    icon = R.drawable.key,
                    onClick = onOpenSettings,
                )
                FigmaDivider()
                FigmaSettingsRow(
                    title = "Appearance",
                    subtitle = "Theme, interface, and player style",
                    icon = R.drawable.palette,
                    onClick = onOpenSettings,
                )
                FigmaDivider()
                FigmaSettingsRow(
                    title = "Playback & Audio",
                    subtitle = "Quality, cache, and audio controls",
                    icon = R.drawable.equalizer,
                    onClick = onOpenSettings,
                )
                FigmaDivider()
                FigmaSettingsRow(
                    title = "Connected Services",
                    subtitle = "Integrations and account services",
                    icon = R.drawable.integration,
                    onClick = onOpenSettings,
                )
                FigmaDivider()
                FigmaSettingsRow(
                    title = "About DiyyMusic",
                    subtitle = "Version 0.7.0 Full UI Rebuild",
                    icon = R.drawable.info,
                    onClick = onOpenSettings,
                )
                FigmaDivider()
                FigmaSettingsRow(
                    title = "Logout",
                    icon = R.drawable.logout,
                    destructive = true,
                    onClick = onLogout,
                )
            }
        }
    }
}
