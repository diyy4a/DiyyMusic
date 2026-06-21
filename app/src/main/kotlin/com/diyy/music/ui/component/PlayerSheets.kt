package com.diyy.music.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Timeline
import com.diyy.music.R
import com.diyy.music.db.entities.LyricsEntity
import com.diyy.music.di.LyricsHelperEntryPoint
import com.diyy.music.extensions.metadata
import com.diyy.music.lyrics.LyricsUtils
import com.diyy.music.lyrics.WordTimestamp
import com.diyy.music.models.MediaMetadata
import com.diyy.music.playback.PlayerConnection
import com.diyy.music.ui.theme.DiyyRed
import com.diyy.music.ui.theme.LocalDiyyUiConfig
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt

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
                            onClick = { playerConnection.playQueueItem(index) },
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
        elevation = if (active) 7.dp else 3.dp,
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
                    text = metadata?.title
                        ?: window.mediaItem.mediaMetadata.title?.toString().orEmpty().ifBlank { "Unknown song" },
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
    onDismiss: () -> Unit,
) {
    val lyricsEntity by playerConnection.currentLyrics.collectAsStateWithLifecycle()
    val activeLyrics = lyricsEntity?.takeIf { it.id == metadata?.id }
    val lines = remember(activeLyrics?.lyrics) { parseDiyyLyrics(activeLyrics?.lyrics) }
    val hasSyncedLyrics = remember(lines) { lines.any { it.timeMs != null } }
    val listState = rememberLazyListState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var activeIndex by remember(lines) { mutableIntStateOf(if (hasSyncedLyrics) 0 else -1) }

    LaunchedEffect(metadata?.id, activeLyrics?.id) {
        val current = metadata ?: return@LaunchedEffect
        if (activeLyrics == null) {
            delay(220)
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

    LaunchedEffect(lines, metadata?.id) {
        if (!hasSyncedLyrics || lines.isEmpty()) {
            activeIndex = -1
            return@LaunchedEffect
        }
        while (isActive) {
            val position = runCatching { playerConnection.player.currentPosition }.getOrDefault(0L)
            val nextIndex = findActiveLyricIndex(lines, position)
            if (nextIndex != activeIndex) activeIndex = nextIndex
            // The index changes only when a new line starts. Polling here is cheap and keeps
            // sync accurate without forcing the entire player screen to recompose every frame.
            delay(45)
        }
    }

    LaunchedEffect(activeIndex, lines.size) {
        if (activeIndex >= 0 && lines.isNotEmpty()) {
            delay(32)
            val viewportHeight = listState.layoutInfo.viewportSize.height
            val anchorOffset = if (viewportHeight > 0) -(viewportHeight * 0.34f).roundToInt() else 0
            runCatching {
                listState.animateScrollToItem(activeIndex, scrollOffset = anchorOffset)
            }
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
                .fillMaxHeight(0.90f)
                .padding(top = 14.dp),
        ) {
            SheetHeader(
                title = "Lyrics",
                subtitle = activeLyrics?.provider?.takeIf { it.isNotBlank() && it != "Unknown" }
                    ?.let { "Smooth sync • $it" }
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
                else -> BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = maxHeight * 0.28f,
                            bottom = maxHeight * 0.42f,
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        itemsIndexed(
                            items = lines,
                            key = { index, line -> "${line.timeMs ?: -1L}-$index-${line.text}" },
                        ) { index, line ->
                            SmoothLyricLine(
                                line = line,
                                active = index == activeIndex,
                                distance = if (activeIndex >= 0) abs(index - activeIndex) else 0,
                                playerConnection = playerConnection,
                                onSeek = line.timeMs?.let { time -> { playerConnection.seekTo(time) } },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmoothLyricLine(
    line: DiyyLyricLine,
    active: Boolean,
    distance: Int,
    playerConnection: PlayerConnection,
    onSeek: (() -> Unit)?,
) {
    val ui = LocalDiyyUiConfig.current
    val targetAlpha = when {
        line.timeMs == null -> 0.82f
        active -> 1f
        distance == 1 -> 0.62f
        distance == 2 -> 0.42f
        else -> 0.24f
    }
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(if (ui.reduceMotion) 90 else 260),
        label = "lyricAlpha",
    )
    val scale by animateFloatAsState(
        targetValue = if (active && !ui.reduceMotion) 1.035f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "lyricScale",
    )
    val background by animateColorAsState(
        targetValue = if (active) DiyyRed.copy(alpha = 0.075f) else Color.Transparent,
        animationSpec = tween(220),
        label = "lyricBackground",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(enabled = onSeek != null) { onSeek?.invoke() }
            .padding(horizontal = 14.dp, vertical = if (active) 14.dp else 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        KaraokeLyricText(
            line = line,
            active = active,
            playerConnection = playerConnection,
        )
    }
}

@Composable
private fun KaraokeLyricText(
    line: DiyyLyricLine,
    active: Boolean,
    playerConnection: PlayerConnection,
) {
    val words = line.words
    var position by remember(line.timeMs) { mutableLongStateOf(0L) }

    LaunchedEffect(active, words, line.timeMs) {
        if (!active || words.isNullOrEmpty()) return@LaunchedEffect
        while (isActive) {
            position = runCatching { playerConnection.player.currentPosition }.getOrDefault(position)
            // Only the active lyric row refreshes at frame-like speed. The rest stays static.
            delay(32)
        }
    }

    val activeColor = DiyyRed
    val inactiveColor = MaterialTheme.colorScheme.onSurface
    val annotated = remember(line.text, words, position, active, activeColor, inactiveColor) {
        if (!active || words.isNullOrEmpty()) {
            buildAnnotatedString { append(line.text) }
        } else {
            buildAnnotatedString {
                words.forEach { word ->
                    val startMs = (word.startTime * 1000.0).toLong()
                    val endMs = (word.endTime * 1000.0).toLong().coerceAtLeast(startMs + 1L)
                    val progress = ((position - startMs).toFloat() / (endMs - startMs).toFloat()).coerceIn(0f, 1f)
                    val color = when {
                        position >= endMs -> activeColor
                        position >= startMs -> lerp(inactiveColor.copy(alpha = 0.78f), activeColor, progress)
                        else -> inactiveColor.copy(alpha = 0.72f)
                    }
                    withStyle(SpanStyle(color = color)) {
                        append(word.text)
                        if (word.hasTrailingSpace) append(' ')
                    }
                }
            }
        }
    }

    Text(
        text = annotated,
        color = if (active && words.isNullOrEmpty()) activeColor else inactiveColor,
        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
        fontSize = if (active) 26.sp else 20.sp,
        lineHeight = if (active) 33.sp else 28.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
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
    isDownloaded: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float?,
    onDownload: () -> Unit,
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
                icon = when {
                    isDownloaded -> R.drawable.offline
                    isDownloading -> R.drawable.close
                    else -> R.drawable.download
                },
                title = when {
                    isDownloaded -> "Remove download"
                    isDownloading -> "Cancel download"
                    else -> "Download song"
                },
                subtitle = when {
                    isDownloaded -> "Stored for offline playback"
                    isDownloading && downloadProgress != null -> "Downloading ${downloadProgress.toInt()}%"
                    isDownloading -> "Download is in progress"
                    else -> "Save this song for offline listening"
                },
                onClick = { onDownload(); onDismiss() },
            )
            MenuAction(
                icon = R.drawable.lyrics,
                title = "Lyrics",
                subtitle = "Open smooth synced lyrics",
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

private data class DiyyLyricLine(
    val timeMs: Long?,
    val text: String,
    val words: List<WordTimestamp>? = null,
)

private val metadataTagRegex = Regex("""^\[[a-zA-Z]+:.*]$""")
private val lyricAgentTagRegex = Regex("""\{(?:agent:[^}]+|bg)\}""")
private val inlineLyricTimeRegex = Regex("""<\d{1,3}:\d{2}(?:[.:]\d{1,3})?>""")

private fun parseDiyyLyrics(raw: String?): List<DiyyLyricLine> {
    if (raw.isNullOrBlank() || raw == LyricsEntity.LYRICS_NOT_FOUND) return emptyList()

    val richLines = runCatching { LyricsUtils.parseLyrics(raw) }.getOrDefault(emptyList())
    if (richLines.isNotEmpty()) {
        return richLines
            .filter { it.text.isNotBlank() }
            .map { entry ->
                DiyyLyricLine(
                    timeMs = entry.time,
                    text = entry.text.trim(),
                    words = entry.words,
                )
            }
    }

    return raw.lineSequence()
        .map { it.trim().removePrefix("\uFEFF") }
        .filter { it.isNotBlank() && !metadataTagRegex.matches(it) }
        .map { line ->
            DiyyLyricLine(
                timeMs = null,
                text = line
                    .replace(inlineLyricTimeRegex, "")
                    .replace(lyricAgentTagRegex, "")
                    .trim(),
            )
        }
        .filter { it.text.isNotBlank() }
        .toList()
}

private fun findActiveLyricIndex(lines: List<DiyyLyricLine>, positionMs: Long): Int {
    var low = 0
    var high = lines.lastIndex
    var result = 0
    while (low <= high) {
        val mid = (low + high) ushr 1
        val time = lines[mid].timeMs ?: Long.MAX_VALUE
        if (time <= positionMs + 100L) {
            result = mid
            low = mid + 1
        } else {
            high = mid - 1
        }
    }
    return result.coerceIn(0, lines.lastIndex)
}
