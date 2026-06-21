package com.diyy.music.ui.component

import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.media3.common.Player
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

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
fun DiyyInlineLyricsPreview(
    playerConnection: PlayerConnection,
    metadata: MediaMetadata?,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lyricsEntity by playerConnection.currentLyrics.collectAsStateWithLifecycle()
    val currentSong by playerConnection.currentSong.collectAsStateWithLifecycle()
    val activeLyrics = lyricsEntity?.takeIf { it.id == metadata?.id }
    val lines = remember(activeLyrics?.lyrics) { parseDiyyLyrics(activeLyrics?.lyrics) }
    val hasSyncedLyrics = remember(lines) { lines.any { it.timeMs != null } }
    val context = androidx.compose.ui.platform.LocalContext.current
    val lyricsOffsetMs = currentSong?.song?.lyricsOffset?.toLong() ?: 0L
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

    LaunchedEffect(playerConnection, metadata?.id, lines, lyricsOffsetMs) {
        if (!hasSyncedLyrics || lines.isEmpty()) {
            activeIndex = -1
            return@LaunchedEffect
        }

        var lastPlayerPosition = runCatching {
            playerConnection.player.currentPosition.coerceAtLeast(0L)
        }.getOrDefault(0L)
        var lastPlayerUpdateAt = SystemClock.elapsedRealtime()

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

                val interpolated = lastPlayerPosition + if (player.isPlaying) {
                    ((now - lastPlayerUpdateAt).coerceAtLeast(0L) * player.playbackParameters.speed)
                        .roundToLong()
                } else {
                    0L
                }
                playbackPositionMs = interpolated + lyricsOffsetMs + LYRICS_DISPLAY_COMPENSATION_MS
                activeIndex = findActiveLyricIndex(lines, playbackPositionMs)
            }
        }
    }

    LiquidGlassBox(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        elevation = 5.dp,
        onClick = onExpand,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 13.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.lyrics),
                    contentDescription = null,
                    tint = DiyyRed,
                    modifier = Modifier.size(21.dp),
                )
                Spacer(Modifier.size(9.dp))
                Text(
                    text = "Lyrics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Full lyrics",
                    style = MaterialTheme.typography.labelMedium,
                    color = DiyyRed,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.size(5.dp))
                Icon(
                    painter = painterResource(R.drawable.fullscreen),
                    contentDescription = "Open full lyrics",
                    tint = DiyyRed,
                    modifier = Modifier.size(18.dp),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    metadata == null -> InlineLyricsMessage("Play a song to see synced lyrics")
                    activeLyrics == null -> CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        color = DiyyRed,
                        strokeWidth = 2.5.dp,
                    )
                    activeLyrics.lyrics == LyricsEntity.LYRICS_NOT_FOUND || lines.isEmpty() ->
                        InlineLyricsMessage("Lyrics are not available for this song")
                    !hasSyncedLyrics -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        lines.take(3).forEachIndexed { index, line ->
                            Text(
                                text = line.text,
                                style = if (index == 0) {
                                    MaterialTheme.typography.titleMedium
                                } else {
                                    MaterialTheme.typography.bodyMedium
                                },
                                fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (index == 0) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    else -> AnimatedContent(
                        targetState = activeIndex.coerceAtLeast(0),
                        transitionSpec = {
                            (fadeIn(tween(260)) + slideInVertically(tween(360)) { it / 5 }) togetherWith
                                (fadeOut(tween(170)) + slideOutVertically(tween(280)) { -it / 5 })
                        },
                        label = "inlineLyricsLine",
                    ) { index ->
                        InlineSyncedLyricsLines(
                            lines = lines,
                            activeIndex = index,
                            playbackPositionMs = playbackPositionMs,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineLyricsMessage(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 20.dp),
    )
}

@Composable
private fun InlineSyncedLyricsLines(
    lines: List<DiyyLyricLine>,
    activeIndex: Int,
    playbackPositionMs: Long,
) {
    val previous = lines.getOrNull(activeIndex - 1)
    val active = lines.getOrNull(activeIndex)
    val next = lines.getOrNull(activeIndex + 1)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        previous?.let {
            Text(
                text = it.text,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.34f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(5.dp))
        }

        active?.let { line ->
            Text(
                text = buildSmoothLyricText(
                    line = line,
                    playbackPositionMs = playbackPositionMs,
                    completedColor = DiyyRed,
                    pendingColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 29.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        next?.let {
            Spacer(Modifier.height(6.dp))
            Text(
                text = it.text,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
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
    val currentSong by playerConnection.currentSong.collectAsStateWithLifecycle()
    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    val shuffleEnabled by playerConnection.shuffleModeEnabled.collectAsStateWithLifecycle()
    val repeatMode by playerConnection.repeatMode.collectAsStateWithLifecycle()
    val activeLyrics = lyricsEntity?.takeIf { it.id == metadata?.id }
    val lines = remember(activeLyrics?.lyrics) { parseDiyyLyrics(activeLyrics?.lyrics) }
    val hasSyncedLyrics = remember(lines) { lines.any { it.timeMs != null } }
    val listState = rememberLazyListState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val lyricsOffsetMs = currentSong?.song?.lyricsOffset?.toLong() ?: 0L
    var rawPlaybackPositionMs by remember(metadata?.id) { mutableLongStateOf(0L) }
    var effectivePlaybackPositionMs by remember(metadata?.id) { mutableLongStateOf(0L) }
    var durationMs by remember(metadata?.id) {
        mutableLongStateOf((metadata?.duration?.times(1000L) ?: 1L).coerceAtLeast(1L))
    }
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

    LaunchedEffect(playerConnection, metadata?.id, lines, lyricsOffsetMs) {
        var lastPlayerPosition = runCatching {
            playerConnection.player.currentPosition.coerceAtLeast(0L)
        }.getOrDefault(0L)
        var lastPlayerUpdateAt = SystemClock.elapsedRealtime()

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

                val interpolated = lastPlayerPosition + if (player.isPlaying) {
                    ((now - lastPlayerUpdateAt).coerceAtLeast(0L) * player.playbackParameters.speed)
                        .roundToLong()
                } else {
                    0L
                }
                rawPlaybackPositionMs = interpolated.coerceAtLeast(0L)
                durationMs = (
                    player.duration.takeIf { it > 0L }
                        ?: metadata?.duration?.times(1000L)
                        ?: durationMs
                    ).coerceAtLeast(1L)
                effectivePlaybackPositionMs = (
                    rawPlaybackPositionMs + lyricsOffsetMs + LYRICS_DISPLAY_COMPENSATION_MS
                    ).coerceAtLeast(0L)
                activeIndex = if (hasSyncedLyrics && lines.isNotEmpty()) {
                    findActiveLyricIndex(lines, effectivePlaybackPositionMs)
                } else {
                    -1
                }
            }
        }
    }

    LaunchedEffect(activeIndex, lines.size) {
        if (activeIndex < 0 || lines.isEmpty()) return@LaunchedEffect
        withFrameMillis { }
        val viewportHeight = listState.layoutInfo.viewportSize.height
        if (viewportHeight <= 0) return@LaunchedEffect
        val targetY = (viewportHeight * 0.37f).roundToInt()
        var item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == activeIndex }
        if (item == null) {
            listState.scrollToItem((activeIndex - 2).coerceAtLeast(0))
            withFrameMillis { }
            item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == activeIndex }
        }
        item?.let {
            val itemCenter = it.offset + (it.size / 2)
            val delta = (itemCenter - targetY).toFloat()
            if (abs(delta) > 2f) {
                runCatching {
                    listState.animateScrollBy(
                        value = delta,
                        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
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
        val lyricTop = lerp(background, DiyyRed, 0.38f)
        val lyricMiddle = lerp(background, DiyyRed, 0.14f)

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
                        .padding(horizontal = 18.dp, vertical = 10.dp),
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
                            text = "Full lyrics",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = activeLyrics?.provider
                                ?.takeIf { it.isNotBlank() && it != "Unknown" }
                                ?.let { "Synced by $it" }
                                ?: "Smooth synced lyrics",
                            color = Color.White.copy(alpha = 0.68f),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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
                                top = 22.dp,
                                bottom = maxHeight * 0.42f,
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
                                    playbackPositionMs = if (index == activeIndex) {
                                        effectivePlaybackPositionMs
                                    } else {
                                        Long.MIN_VALUE
                                    },
                                    onSeek = line.timeMs?.let { time ->
                                        {
                                            playerConnection.seekTo(
                                                (time - lyricsOffsetMs).coerceAtLeast(0L),
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                metadata?.let {
                    FullLyricsPlayerDock(
                        playerConnection = playerConnection,
                        metadata = it,
                        isPlaying = isPlaying,
                        shuffleEnabled = shuffleEnabled,
                        repeatMode = repeatMode,
                        positionMs = rawPlaybackPositionMs,
                        durationMs = durationMs,
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.LyricsMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 28.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = Color.White.copy(alpha = 0.78f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}


@Composable
private fun FullLyricsPlayerDock(
    playerConnection: PlayerConnection,
    metadata: MediaMetadata,
    isPlaying: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    positionMs: Long,
    durationMs: Long,
) {
    var seeking by remember(metadata.id) { mutableStateOf(false) }
    var seekFraction by remember(metadata.id) { mutableFloatStateOf(0f) }
    val liveFraction = (positionMs.toFloat() / durationMs.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
    val visibleFraction = if (seeking) seekFraction else liveFraction
    val visiblePosition = if (seeking) {
        (durationMs * seekFraction).roundToLong()
    } else {
        positionMs
    }

    LiquidGlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        shape = RoundedCornerShape(26.dp),
        elevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Artwork(
                    url = metadata.thumbnailUrl,
                    modifier = Modifier.size(46.dp),
                    cornerRadius = 13,
                )
                Spacer(Modifier.size(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = metadata.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = metadata.artists.joinToString { it.name }.ifBlank { "DiyyMusic" },
                        color = Color.White.copy(alpha = 0.66f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Slider(
                value = visibleFraction,
                onValueChange = {
                    seeking = true
                    seekFraction = it.coerceIn(0f, 1f)
                },
                onValueChangeFinished = {
                    playerConnection.seekTo((durationMs * seekFraction).roundToLong())
                    seeking = false
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.24f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = formatLyricsTime(visiblePosition),
                    color = Color.White.copy(alpha = 0.62f),
                    style = MaterialTheme.typography.labelSmall,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatLyricsTime(durationMs),
                    color = Color.White.copy(alpha = 0.62f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            Spacer(Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FullLyricsControlButton(
                    icon = if (shuffleEnabled) R.drawable.shuffle_on else R.drawable.shuffle,
                    contentDescription = "Shuffle",
                    active = shuffleEnabled,
                    size = 38,
                    onClick = playerConnection::toggleShuffle,
                )
                FullLyricsControlButton(
                    icon = R.drawable.skip_previous,
                    contentDescription = "Previous",
                    size = 44,
                    onClick = playerConnection::seekToPrevious,
                )
                Surface(
                    modifier = Modifier.size(58.dp),
                    shape = CircleShape,
                    color = Color.White,
                    onClick = playerConnection::togglePlayPause,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(27.dp),
                        )
                    }
                }
                FullLyricsControlButton(
                    icon = R.drawable.skip_next,
                    contentDescription = "Next",
                    size = 44,
                    onClick = playerConnection::seekToNext,
                )
                FullLyricsControlButton(
                    icon = when (repeatMode) {
                        Player.REPEAT_MODE_ONE -> R.drawable.repeat_one_on
                        Player.REPEAT_MODE_ALL -> R.drawable.repeat_on
                        else -> R.drawable.repeat
                    },
                    contentDescription = "Repeat",
                    active = repeatMode != Player.REPEAT_MODE_OFF,
                    size = 38,
                    onClick = playerConnection::cycleRepeatMode,
                )
            }
        }
    }
}

@Composable
private fun FullLyricsControlButton(
    icon: Int,
    contentDescription: String,
    size: Int,
    active: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(size.dp),
        shape = CircleShape,
        color = if (active) Color.White.copy(alpha = 0.18f) else Color.Transparent,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
                contentDescription = contentDescription,
                tint = if (active) Color.White else Color.White.copy(alpha = 0.86f),
                modifier = Modifier.size((size * 0.48f).dp),
            )
        }
    }
}

private fun formatLyricsTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds.coerceAtLeast(0L) / 1000L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
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

    val displayedText = if (active && playbackPositionMs != Long.MIN_VALUE) {
        buildSmoothLyricText(
            line = line,
            playbackPositionMs = playbackPositionMs,
            completedColor = Color.White,
            pendingColor = Color.White.copy(alpha = 0.42f),
        )
    } else {
        buildAnnotatedString { append(line.text) }
    }

    Text(
        text = displayedText,
        color = Color.White.copy(alpha = alpha),
        fontSize = if (active) 26.sp else 22.sp,
        lineHeight = if (active) 32.sp else 28.sp,
        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
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


private fun buildSmoothLyricText(
    line: DiyyLyricLine,
    playbackPositionMs: Long,
    completedColor: Color,
    pendingColor: Color,
) = buildAnnotatedString {
    val words = line.words.orEmpty()
    if (words.isNotEmpty()) {
        val positionSeconds = playbackPositionMs / 1000.0
        words.forEach { word ->
            val wordText = word.text
            val progress = when {
                positionSeconds <= word.startTime -> 0f
                positionSeconds >= word.endTime -> 1f
                word.endTime <= word.startTime -> 1f
                else -> ((positionSeconds - word.startTime) / (word.endTime - word.startTime))
                    .toFloat()
                    .coerceIn(0f, 1f)
            }
            val splitIndex = (wordText.length * progress).roundToInt().coerceIn(0, wordText.length)
            if (splitIndex > 0) {
                withStyle(SpanStyle(color = completedColor)) {
                    append(wordText.substring(0, splitIndex))
                }
            }
            if (splitIndex < wordText.length) {
                withStyle(SpanStyle(color = pendingColor)) {
                    append(wordText.substring(splitIndex))
                }
            }
            if (word.hasTrailingSpace) {
                withStyle(SpanStyle(color = if (progress >= 1f) completedColor else pendingColor)) {
                    append(' ')
                }
            }
        }
        return@buildAnnotatedString
    }

    val start = line.timeMs
    val end = line.endTimeMs
    if (start == null || end == null || end <= start) {
        withStyle(SpanStyle(color = completedColor)) { append(line.text) }
        return@buildAnnotatedString
    }

    val progress = ((playbackPositionMs - start).toFloat() / (end - start).toFloat())
        .coerceIn(0f, 1f)
    val splitIndex = (line.text.length * progress).roundToInt().coerceIn(0, line.text.length)
    if (splitIndex > 0) {
        withStyle(SpanStyle(color = completedColor)) {
            append(line.text.substring(0, splitIndex))
        }
    }
    if (splitIndex < line.text.length) {
        withStyle(SpanStyle(color = pendingColor)) {
            append(line.text.substring(splitIndex))
        }
    }
}

private val metadataTagRegex = Regex("""^\[[a-zA-Z]+:.*]$""")
private val lyricAgentTagRegex = Regex("""\{(?:agent:[^}]+|bg)\}""")
private val inlineLyricTimeRegex = Regex("""<\d{1,3}:\d{2}(?:[.:]\d{1,3})?>""")
private val lyricOffsetRegex = Regex("""(?im)^\[offset:([+-]?\d+)]\s*$""")
private const val LYRICS_DISPLAY_COMPENSATION_MS = 120L

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
