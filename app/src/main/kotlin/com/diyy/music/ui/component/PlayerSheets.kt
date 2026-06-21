package com.diyy.music.ui.component

import android.os.SystemClock
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
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
import androidx.compose.foundation.layout.safeDrawingPadding
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
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    var playbackPositionMs by remember(metadata?.id) { mutableLongStateOf(0L) }
    var activeIndex by remember(lines) { mutableIntStateOf(if (hasSyncedLyrics) 0 else -1) }

    LaunchedEffect(metadata?.id, activeLyrics?.id) {
        val current = metadata ?: return@LaunchedEffect
        if (activeLyrics == null) {
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

    // Read the player's clock once per frame and use one shared value for every lyric line.
    // This avoids one coroutine per active line and removes the visible lag from the old sheet.
    LaunchedEffect(playerConnection, metadata?.id, lines) {
        if (!hasSyncedLyrics || lines.isEmpty()) {
            activeIndex = -1
            return@LaunchedEffect
        }

        var lastPlayerPosition = runCatching {
            playerConnection.player.currentPosition.coerceAtLeast(0L)
        }.getOrDefault(0L)
        var lastPlayerUpdateAt = SystemClock.elapsedRealtime()
        val hasWordTimings = lines.any { !it.words.isNullOrEmpty() }

        while (isActive) {
            withFrameMillis {
                val now = SystemClock.elapsedRealtime()
                val player = playerConnection.player
                val reportedPosition = runCatching { player.currentPosition.coerceAtLeast(0L) }
                    .getOrDefault(lastPlayerPosition)

                if (reportedPosition != lastPlayerPosition) {
                    lastPlayerPosition = reportedPosition
                    lastPlayerUpdateAt = now
                }

                // ExoPlayer's public position can update less frequently than the display.
                // Interpolate between reported positions so lyric motion follows the audio clock
                // instead of visibly waiting for the next player callback.
                playbackPositionMs = lastPlayerPosition + if (player.isPlaying) {
                    (now - lastPlayerUpdateAt).coerceAtLeast(0L)
                } else {
                    0L
                }

                activeIndex = findActiveLyricIndex(
                    lines = lines,
                    positionMs = playbackPositionMs + if (hasWordTimings) 0L else LYRICS_RENDER_AHEAD_MS,
                )
            }
        }
    }

    LaunchedEffect(activeIndex, lines.size) {
        if (activeIndex < 0 || lines.isEmpty()) return@LaunchedEffect
        withFrameMillis { }
        val viewportHeight = listState.layoutInfo.viewportSize.height
        if (viewportHeight <= 0) return@LaunchedEffect
        val targetY = (viewportHeight * 0.34f).roundToInt()
        var item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == activeIndex }
        if (item == null) {
            listState.scrollToItem((activeIndex - 2).coerceAtLeast(0))
            withFrameMillis { }
            item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == activeIndex }
        }
        item?.let {
            val itemCenter = it.offset + (it.size / 2)
            val delta = (itemCenter - targetY).toFloat()
            if (abs(delta) > 3f) {
                runCatching {
                    listState.animateScrollBy(
                        value = delta,
                        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                    )
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnClickOutside = false,
        ),
    ) {
        val background = MaterialTheme.colorScheme.background
        val lyricTop = lerp(background, DiyyRed, 0.42f)
        val lyricMiddle = lerp(background, DiyyRed, 0.20f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(lyricTop, lyricMiddle, background),
                    ),
                )
                .safeDrawingPadding(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FigmaCircleButton(
                        icon = R.drawable.arrow_back,
                        contentDescription = "Close lyrics",
                        onClick = onDismiss,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp),
                    )
                    Spacer(Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lyrics",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = metadata?.let {
                                buildString {
                                    append(it.title)
                                    val artist = it.artists.joinToString { artist -> artist.name }
                                    if (artist.isNotBlank()) append(" • ").append(artist)
                                }
                            }.orEmpty(),
                            color = Color.White.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    activeLyrics?.provider
                        ?.takeIf { it.isNotBlank() && it != "Unknown" }
                        ?.let { provider ->
                            Text(
                                text = provider,
                                color = Color.White.copy(alpha = 0.72f),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                }

                when {
                    metadata == null -> LyricsMessage("Play a song first")
                    activeLyrics == null -> Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                    activeLyrics.lyrics == LyricsEntity.LYRICS_NOT_FOUND || lines.isEmpty() ->
                        LyricsMessage("Lyrics not found for this song")
                    else -> BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        val activeTime = lines.getOrNull(activeIndex)?.timeMs
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                start = 24.dp,
                                end = 24.dp,
                                top = 28.dp,
                                bottom = maxHeight * 0.56f,
                            ),
                            verticalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            itemsIndexed(
                                items = lines,
                                key = { index, line -> "${line.timeMs ?: -1L}-$index-${line.text}" },
                            ) { index, line ->
                                SpotifyLyricLine(
                                    line = line,
                                    active = hasSyncedLyrics && activeTime != null && line.timeMs == activeTime,
                                    passed = hasSyncedLyrics && activeIndex >= 0 && index < activeIndex,
                                    playbackPositionMs = if (index == activeIndex) playbackPositionMs else Long.MIN_VALUE,
                                    onSeek = line.timeMs?.let { time ->
                                        { playerConnection.seekTo(time.coerceAtLeast(0L)) }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpotifyLyricLine(
    line: DiyyLyricLine,
    active: Boolean,
    passed: Boolean,
    playbackPositionMs: Long,
    onSeek: (() -> Unit)?,
) {
    val ui = LocalDiyyUiConfig.current
    val alpha by animateFloatAsState(
        targetValue = when {
            line.timeMs == null -> 0.92f
            active -> 1f
            passed -> 0.34f
            else -> 0.62f
        },
        animationSpec = tween(if (ui.reduceMotion) 60 else 180),
        label = "spotifyLyricAlpha",
    )
    val scale by animateFloatAsState(
        targetValue = if (active && !ui.reduceMotion) 1.025f else 1f,
        animationSpec = tween(if (ui.reduceMotion) 60 else 180, easing = FastOutSlowInEasing),
        label = "spotifyLyricScale",
    )

    val displayedText = if (active && playbackPositionMs != Long.MIN_VALUE && !line.words.isNullOrEmpty()) {
        val positionSeconds = playbackPositionMs / 1000.0
        buildAnnotatedString {
            line.words.forEach { word ->
                val wordAlpha = when {
                    positionSeconds >= word.endTime -> 1f
                    positionSeconds >= word.startTime -> 0.88f
                    else -> 0.42f
                }
                withStyle(SpanStyle(color = Color.White.copy(alpha = wordAlpha))) {
                    append(word.text)
                    if (word.hasTrailingSpace) append(' ')
                }
            }
        }
    } else {
        buildAnnotatedString { append(line.text) }
    }

    Text(
        text = displayedText,
        color = Color.White.copy(alpha = alpha),
        fontSize = if (active) 29.sp else 25.sp,
        lineHeight = if (active) 35.sp else 31.sp,
        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Bold,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
            }
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = onSeek != null) { onSeek?.invoke() }
            .padding(vertical = 9.dp),
    )
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
    val endTimeMs: Long? = null,
    val text: String,
    val words: List<WordTimestamp>? = null,
)

private val metadataTagRegex = Regex("""^\[[a-zA-Z]+:.*]$""")
private val lyricAgentTagRegex = Regex("""\{(?:agent:[^}]+|bg)\}""")
private val inlineLyricTimeRegex = Regex("""<\d{1,3}:\d{2}(?:[.:]\d{1,3})?>""")
private val lyricOffsetRegex = Regex("""(?im)^\[offset:([+-]?\d+)]\s*$""")
private const val LYRICS_RENDER_AHEAD_MS = 220L

private fun parseDiyyLyrics(raw: String?): List<DiyyLyricLine> {
    if (raw.isNullOrBlank() || raw == LyricsEntity.LYRICS_NOT_FOUND) return emptyList()

    val declaredOffsetMs = lyricOffsetRegex.find(raw)
        ?.groupValues
        ?.getOrNull(1)
        ?.toLongOrNull()
        ?: 0L
    val offsetSeconds = declaredOffsetMs / 1000.0
    val richLines = runCatching { LyricsUtils.parseLyrics(raw) }.getOrDefault(emptyList())
    val parsed = if (richLines.isNotEmpty()) {
        richLines
            .filter { it.text.isNotBlank() }
            .map { entry ->
                DiyyLyricLine(
                    timeMs = (entry.time + declaredOffsetMs).coerceAtLeast(0L),
                    text = entry.text.trim(),
                    words = entry.words?.map { word ->
                        word.copy(
                            startTime = (word.startTime + offsetSeconds).coerceAtLeast(0.0),
                            endTime = (word.endTime + offsetSeconds).coerceAtLeast(0.0),
                        )
                    },
                )
            }
    } else {
        raw.lineSequence()
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

    return parsed.mapIndexed { index, line ->
        val nextTime = parsed.getOrNull(index + 1)?.timeMs
        val fallbackDuration = (line.text.length.coerceIn(12, 80) * 90L).coerceIn(1700L, 5200L)
        line.copy(
            endTimeMs = when {
                line.timeMs == null -> null
                nextTime != null -> (nextTime - 80L).coerceAtLeast(line.timeMs + 700L)
                else -> line.timeMs + fallbackDuration
            },
        )
    }
}

private fun findActiveLyricIndex(lines: List<DiyyLyricLine>, positionMs: Long): Int {
    if (lines.isEmpty()) return -1
    var low = 0
    var high = lines.lastIndex
    var result = -1
    while (low <= high) {
        val mid = (low + high) ushr 1
        val time = lines[mid].timeMs ?: Long.MAX_VALUE
        if (time <= positionMs) {
            result = mid
            low = mid + 1
        } else {
            high = mid - 1
        }
    }
    return result
}
