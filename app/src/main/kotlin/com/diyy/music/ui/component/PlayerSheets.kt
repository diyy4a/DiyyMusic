package com.diyy.music.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Timeline
import com.diyy.music.R
import com.diyy.music.db.entities.LyricsEntity
import com.diyy.music.di.LyricsHelperEntryPoint
import com.diyy.music.extensions.metadata
import com.diyy.music.models.MediaMetadata
import com.diyy.music.playback.PlayerConnection
import com.diyy.music.ui.theme.DiyyRed
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiyyQueueSheet(
    playerConnection: PlayerConnection,
    onDismiss: () -> Unit,
) {
    val queueWindows by playerConnection.queueWindows.collectAsStateWithLifecycle()
    val currentIndex by playerConnection.currentWindowIndex.collectAsStateWithLifecycle()
    val queueTitle by playerConnection.queueTitle.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.78f)
                .padding(top = 18.dp),
        ) {
            SheetHeader(
                title = queueTitle ?: "Up Next",
                subtitle = "${queueWindows.size} songs in queue",
                icon = R.drawable.queue_music,
                onDismiss = onDismiss,
            )

            if (queueWindows.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Queue is empty",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 28.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    itemsIndexed(queueWindows, key = { index, window ->
                        "${window.mediaItem.mediaId}-$index"
                    }) { index, window ->
                        QueueRow(
                            window = window,
                            active = index == currentIndex,
                            onClick = {
                                playerConnection.playQueueItem(index)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
    window: Timeline.Window,
    active: Boolean,
    onClick: () -> Unit,
) {
    val metadata = window.mediaItem.metadata
    LiquidGlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = if (active) 8.dp else 3.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Artwork(
                url = metadata?.thumbnailUrl,
                modifier = Modifier.size(54.dp),
                cornerRadius = 16,
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = metadata?.title ?: window.mediaItem.mediaMetadata.title?.toString().orEmpty().ifBlank { "Unknown song" },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (active) DiyyRed else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = metadata?.artists?.joinToString { it.name }
                        ?: window.mediaItem.mediaMetadata.artist?.toString().orEmpty(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (active) {
                Icon(
                    painter = painterResource(R.drawable.graphic_eq),
                    contentDescription = "Now playing",
                    tint = DiyyRed,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiyyLyricsSheet(
    playerConnection: PlayerConnection,
    metadata: MediaMetadata?,
    positionMs: Long,
    onDismiss: () -> Unit,
) {
    val lyricsEntity by playerConnection.currentLyrics.collectAsStateWithLifecycle()
    val activeLyrics = lyricsEntity?.takeIf { it.id == metadata?.id }
    val listState = rememberLazyListState()
    val lines = remember(activeLyrics?.lyrics) { parseLyrics(activeLyrics?.lyrics) }
    val activeIndex = remember(lines, positionMs) {
        if (lines.any { it.timeMs != null }) {
            lines.indexOfLast { it.timeMs != null && it.timeMs <= positionMs }.coerceAtLeast(0)
        } else {
            -1
        }
    }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(metadata?.id, activeLyrics?.id) {
        val current = metadata ?: return@LaunchedEffect
        if (activeLyrics == null) {
            delay(250)
            withContext(Dispatchers.IO) {
                runCatching {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        LyricsHelperEntryPoint::class.java,
                    )
                    val result = entryPoint.lyricsHelper().getLyrics(current)
                    playerConnection.database.query {
                        upsert(
                            LyricsEntity(
                                id = current.id,
                                lyrics = result.lyrics,
                                provider = result.provider,
                            ),
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && lines.isNotEmpty()) {
            listState.animateScrollToItem((activeIndex - 2).coerceAtLeast(0))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.99f),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(top = 18.dp),
        ) {
            SheetHeader(
                title = "Lyrics",
                subtitle = activeLyrics?.provider?.takeIf { it.isNotBlank() && it != "Unknown" }
                    ?.let { "From $it" }
                    ?: metadata?.title.orEmpty(),
                icon = R.drawable.lyrics,
                onDismiss = onDismiss,
            )

            when {
                metadata == null -> LyricsMessage("Play a song first")
                activeLyrics == null -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = DiyyRed)
                }
                activeLyrics.lyrics == LyricsEntity.LYRICS_NOT_FOUND || lines.isEmpty() ->
                    LyricsMessage("Lyrics not found for this song")
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 22.dp,
                        end = 22.dp,
                        top = 12.dp,
                        bottom = 80.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(lines) { index, line ->
                        val active = index == activeIndex
                        Text(
                            text = line.text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = line.timeMs != null) {
                                    line.timeMs?.let(playerConnection::seekTo)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            color = if (active) DiyyRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            fontSize = if (active) 22.sp else 18.sp,
                            lineHeight = if (active) 29.sp else 25.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.LyricsMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().weight(1f),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(28.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiyyPlayerMenuSheet(
    isFavorite: Boolean,
    onLyrics: () -> Unit,
    onQueue: () -> Unit,
    onRadio: () -> Unit,
    onRetryPlayback: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.99f),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SheetHeader(
                title = "Song options",
                subtitle = "Playback actions",
                icon = R.drawable.more_horiz,
                onDismiss = onDismiss,
                horizontalPadding = 0.dp,
            )
            MenuAction(
                icon = R.drawable.lyrics,
                title = "Lyrics",
                subtitle = "Open synced or plain lyrics",
                onClick = { onDismiss(); onLyrics() },
            )
            MenuAction(
                icon = R.drawable.queue_music,
                title = "Queue",
                subtitle = "See what plays next",
                onClick = { onDismiss(); onQueue() },
            )
            MenuAction(
                icon = R.drawable.radio,
                title = "Start radio",
                subtitle = "Continue with similar songs",
                onClick = { onDismiss(); onRadio() },
            )
            MenuAction(
                icon = R.drawable.refresh,
                title = "Retry playback",
                subtitle = "Reconnect and prepare the current track",
                onClick = { onDismiss(); onRetryPlayback() },
            )
            MenuAction(
                icon = if (isFavorite) R.drawable.favorite else R.drawable.favorite_border,
                title = if (isFavorite) "Remove from favorites" else "Add to favorites",
                subtitle = "Save this song in your library",
                onClick = { onToggleFavorite(); onDismiss() },
            )
        }
    }
}

@Composable
private fun MenuAction(
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    LiquidGlassBox(
        modifier = Modifier.fillMaxWidth().height(72.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = 4.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = DiyyRed,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.size(13.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SheetHeader(
    title: String,
    subtitle: String,
    icon: Int,
    onDismiss: () -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp = 18.dp,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = DiyyRed,
            modifier = Modifier.size(28.dp),
        )
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        FigmaCircleButton(
            icon = R.drawable.close,
            contentDescription = "Close",
            onClick = onDismiss,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(42.dp),
        )
    }
}

private data class LyricLine(val timeMs: Long?, val text: String)

private val lrcLineRegex = Regex("""((?:\[\d{1,3}:\d{2}(?:[.:]\d{1,3})?])+)(.*)""")
private val lrcTimeRegex = Regex("""\[(\d{1,3}):(\d{2})(?:[.:](\d{1,3}))?]""")
private val metadataTagRegex = Regex("""^\[[a-zA-Z]+:.*]$""")

private fun parseLyrics(raw: String?): List<LyricLine> {
    if (raw.isNullOrBlank() || raw == LyricsEntity.LYRICS_NOT_FOUND) return emptyList()
    val result = mutableListOf<LyricLine>()
    raw.lineSequence().forEach { original ->
        val line = original.trim().removePrefix("\uFEFF")
        if (line.isBlank() || metadataTagRegex.matches(line)) return@forEach
        val match = lrcLineRegex.matchEntire(line)
        if (match == null) {
            val clean = line.replace(Regex("""\{(?:agent:[^}]+|bg)}"""), "").trim()
            if (clean.isNotBlank()) result += LyricLine(null, clean)
        } else {
            val text = match.groupValues[2]
                .replace(Regex("""<\d{1,3}:\d{2}(?:[.:]\d{1,3})?>"""), "")
                .replace(Regex("""\{(?:agent:[^}]+|bg)}"""), "")
                .trim()
            if (text.isBlank()) return@forEach
            lrcTimeRegex.findAll(match.groupValues[1]).forEach { timeMatch ->
                val minute = timeMatch.groupValues[1].toLongOrNull() ?: 0L
                val second = timeMatch.groupValues[2].toLongOrNull() ?: 0L
                val fractionText = timeMatch.groupValues[3]
                val fraction = when (fractionText.length) {
                    1 -> fractionText.toLongOrNull()?.times(100L) ?: 0L
                    2 -> fractionText.toLongOrNull()?.times(10L) ?: 0L
                    else -> fractionText.take(3).padEnd(3, '0').toLongOrNull() ?: 0L
                }
                result += LyricLine((minute * 60_000L) + (second * 1_000L) + fraction, text)
            }
        }
    }
    val hasTimed = result.any { it.timeMs != null }
    return if (hasTimed) result.sortedBy { it.timeMs ?: Long.MAX_VALUE } else result
}
