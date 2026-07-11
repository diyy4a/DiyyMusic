package com.diyy.music.ui.screens

import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.diyy.music.R
import com.diyy.music.constants.HidePlayerThumbnailKey
import com.diyy.music.db.entities.Song
import com.diyy.music.models.MediaMetadata
import com.diyy.music.playback.DownloadUtil
import com.diyy.music.playback.ExoDownloadService
import com.diyy.music.playback.PlayerConnection
import com.diyy.music.ui.component.AddToPlaylistSheet
import com.diyy.music.ui.component.Artwork
import com.diyy.music.ui.component.DiyyBrandMark
import com.diyy.music.ui.component.DiyyInlineLyricsPreview
import com.diyy.music.ui.component.DiyyLyricsSheet
import com.diyy.music.ui.component.DiyyPlayerMenuSheet
import com.diyy.music.ui.component.DiyyQueueSheet
import com.diyy.music.ui.component.FigmaCircleButton
import com.diyy.music.ui.component.LiquidGlassBox
import com.diyy.music.ui.theme.DiyyRed
import com.diyy.music.ui.theme.LocalDiyyUiConfig
import com.diyy.music.utils.dataStore
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.math.max
import kotlin.math.roundToLong

@Composable
fun PlayerScreen(
    playerConnection: PlayerConnection?,
    downloadUtil: DownloadUtil,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metadataState = playerConnection?.mediaMetadata?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf<MediaMetadata?>(null) }
    val metadata by metadataState
    val playingState = playerConnection?.isPlaying?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(false) }
    val isPlaying by playingState
    val shuffleState = playerConnection?.shuffleModeEnabled?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(false) }
    val shuffleEnabled by shuffleState
    val repeatState = playerConnection?.repeatMode?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(Player.REPEAT_MODE_OFF) }
    val repeatMode by repeatState
    val songState = playerConnection?.currentSong?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf<Song?>(null) }
    val currentSong by songState
    val favorite = currentSong?.song?.liked ?: metadata?.liked ?: false
    val context = LocalContext.current
    val ui = LocalDiyyUiConfig.current
    val hideArtwork by context.dataStore.data
        .map { preferences -> preferences[HidePlayerThumbnailKey] ?: false }
        .collectAsStateWithLifecycle(initialValue = false)
    val downloadFlow = remember(metadata?.id, downloadUtil) {
        metadata?.id?.let(downloadUtil::getDownload) ?: flowOf<Download?>(null)
    }
    val download by downloadFlow.collectAsStateWithLifecycle(initialValue = null)
    val isDownloaded = download?.state == Download.STATE_COMPLETED
    val isDownloading = download?.state == Download.STATE_QUEUED || download?.state == Download.STATE_DOWNLOADING
    val downloadProgress = download?.percentDownloaded?.takeIf { it >= 0f }

    var showQueue by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showAddToPlaylist by remember { mutableStateOf(false) }

    val toggleDownload: () -> Unit = {
        metadata?.let { toggleSongDownload(context, downloadUtil, it, download) }
        Unit
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val tiny = maxHeight < 650.dp
        val compact = maxHeight < 770.dp
        val horizontalPadding = if (tiny) 18.dp else 22.dp
        val lyricsHeight = when {
            tiny -> 104.dp
            compact -> 116.dp
            maxHeight < 880.dp -> 128.dp
            else -> 138.dp
        }

        if (ui.backgroundGlow) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = if (compact) 76.dp else 98.dp)
                    .size(if (compact) 300.dp else 380.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                DiyyRed.copy(alpha = 0.10f * ui.accentStrength),
                                DiyyRed.copy(alpha = 0.025f * ui.accentStrength),
                                Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = if (tiny) 6.dp else 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FigmaCircleButton(
                    icon = R.drawable.arrow_back,
                    contentDescription = "Back",
                    onClick = onBack,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
                DiyyBrandMark(modifier = Modifier.weight(1f))
                FigmaCircleButton(
                    icon = R.drawable.more_horiz,
                    contentDescription = "More",
                    onClick = { showMenu = true },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(if (tiny) 6.dp else 10.dp))

            if (!hideArtwork) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    val availableArtwork = minOf(maxWidth - 30.dp, maxHeight - 4.dp)
                    val artworkSize = availableArtwork.coerceIn(
                        minimumValue = if (tiny) 154.dp else 178.dp,
                        maximumValue = if (compact) 286.dp else 336.dp,
                    )
                    AnimatedContent(
                        targetState = metadata?.id,
                        transitionSpec = {
                            (fadeIn(tween(190)) + scaleIn(tween(230), initialScale = 0.985f)) togetherWith
                                (fadeOut(tween(130)) + scaleOut(tween(170), targetScale = 1.015f))
                        },
                        label = "playerArtwork",
                    ) {
                        Artwork(
                            url = metadata?.thumbnailUrl,
                            modifier = Modifier.size(artworkSize),
                            cornerRadius = 28,
                        )
                    }
                }
                Spacer(Modifier.height(if (tiny) 4.dp else 7.dp))
            } else {
                Spacer(Modifier.height(if (tiny) 6.dp else 10.dp))
            }

            Text(
                text = metadata?.title ?: "Not Playing",
                style = if (tiny) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = metadata?.artists?.joinToString { it.name }.orEmpty().ifBlank { "DiyyMusic" },
                style = if (tiny) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(if (tiny) 5.dp else 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (tiny) 40.dp else 44.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                PlayerActionPill(
                    icon = if (favorite) R.drawable.favorite else R.drawable.favorite_border,
                    label = if (favorite) "Liked" else "Favorite",
                    active = favorite,
                    modifier = Modifier.weight(1f),
                    onClick = { playerConnection?.toggleLike() },
                )
                PlayerActionPill(
                    icon = R.drawable.lyrics,
                    label = "Lyrics",
                    active = showLyrics,
                    modifier = Modifier.weight(1f),
                    onClick = { if (metadata != null && playerConnection != null) showLyrics = true },
                )
                PlayerActionPill(
                    icon = R.drawable.queue_music,
                    label = "Queue",
                    active = showQueue,
                    modifier = Modifier.weight(1f),
                    onClick = { if (playerConnection != null) showQueue = true },
                )
            }

            Spacer(Modifier.height(if (tiny) 6.dp else 9.dp))
            if (playerConnection != null) {
                DiyyInlineLyricsPreview(
                    playerConnection = playerConnection,
                    metadata = metadata,
                    onExpand = { if (metadata != null) showLyrics = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lyricsHeight),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lyricsHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Player is unavailable",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(if (tiny) 6.dp else 8.dp))

            PlayerControlsDock(
                playerConnection = playerConnection,
                metadata = metadata,
                isPlaying = isPlaying,
                shuffleEnabled = shuffleEnabled,
                repeatMode = repeatMode,
                tiny = tiny,
            )

            Spacer(Modifier.height(if (tiny) 2.dp else 5.dp))
        }
    }

    if (showQueue && playerConnection != null) {
        DiyyQueueSheet(
            playerConnection = playerConnection,
            onDismiss = { showQueue = false },
        )
    }
    if (showLyrics && playerConnection != null) {
        DiyyLyricsSheet(
            playerConnection = playerConnection,
            metadata = metadata,
            onDismiss = { showLyrics = false },
        )
    }
    if (showMenu) {
        DiyyPlayerMenuSheet(
            isFavorite = favorite,
            isDownloaded = isDownloaded,
            isDownloading = isDownloading,
            downloadProgress = downloadProgress,
            onDownload = toggleDownload,
            onLyrics = { if (playerConnection != null && metadata != null) showLyrics = true },
            onQueue = { if (playerConnection != null) showQueue = true },
            onRetryPlayback = { playerConnection?.retryPlayback() },
            onToggleFavorite = { playerConnection?.toggleLike() },
            onAddToPlaylist = { if (metadata != null) showAddToPlaylist = true },
            onDismiss = { showMenu = false },
        )
    }
    if (showAddToPlaylist) {
        metadata?.let { currentMetadata ->
            AddToPlaylistSheet(
                database = downloadUtil.database,
                songs = listOf(currentMetadata),
                onDismiss = { showAddToPlaylist = false },
            )
        }
    }
}

@Composable
private fun PlayerActionPill(
    icon: Int,
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    LiquidGlassBox(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        elevation = if (active) 6.dp else 3.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                tint = if (active) DiyyRed else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp),
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (active) DiyyRed else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PlayerControlsDock(
    playerConnection: PlayerConnection?,
    metadata: MediaMetadata?,
    isPlaying: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    tiny: Boolean,
) {
    val ui = LocalDiyyUiConfig.current
    LiquidGlassBox(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = if (tiny) 9.dp else 12.dp),
        ) {
            PlayerProgressSection(
                playerConnection = playerConnection,
                metadata = metadata,
                isPlaying = isPlaying,
            )
            Spacer(Modifier.height(if (tiny) 1.dp else 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SimplePlayerButton(
                    icon = Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    active = shuffleEnabled,
                    size = if (tiny) 36 else 40,
                    onClick = { playerConnection?.toggleShuffle() },
                )
                SimplePlayerButton(
                    icon = Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous",
                    size = if (tiny) 44 else 48,
                    onClick = { playerConnection?.seekToPrevious() },
                )
                Surface(
                    modifier = Modifier.size(if (tiny) 58.dp else 66.dp),
                    shape = CircleShape,
                    color = DiyyRed,
                    tonalElevation = 0.dp,
                    shadowElevation = if (ui.backgroundGlow) 6.dp else 1.dp,
                    onClick = { playerConnection?.togglePlayPause() },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AnimatedContent(
                            targetState = isPlaying,
                            transitionSpec = {
                                (fadeIn(tween(110)) + scaleIn(tween(140), initialScale = 0.88f)) togetherWith
                                    (fadeOut(tween(90)) + scaleOut(tween(110), targetScale = 0.88f))
                            },
                            label = "playPauseIcon",
                        ) { playing ->
                            Icon(
                                imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (playing) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(if (tiny) 27.dp else 30.dp),
                            )
                        }
                    }
                }
                SimplePlayerButton(
                    icon = Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    size = if (tiny) 44 else 48,
                    onClick = { playerConnection?.seekToNext() },
                )
                SimplePlayerButton(
                    icon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                    contentDescription = "Repeat",
                    active = repeatMode != Player.REPEAT_MODE_OFF,
                    size = if (tiny) 36 else 40,
                    onClick = { playerConnection?.cycleRepeatMode() },
                )
            }
        }
    }
}

@Composable
private fun PlayerProgressSection(
    playerConnection: PlayerConnection?,
    metadata: MediaMetadata?,
    isPlaying: Boolean,
) {
    var position by remember(metadata?.id) { mutableLongStateOf(0L) }
    var duration by remember(metadata?.id) { mutableLongStateOf(1L) }
    var sliderValue by remember(metadata?.id) { mutableFloatStateOf(0f) }
    var seeking by remember { mutableStateOf(false) }

    LaunchedEffect(playerConnection, metadata?.id, isPlaying, seeking) {
        val player = runCatching { playerConnection?.player }.getOrNull() ?: return@LaunchedEffect
        var lastPlayerPosition = player.currentPosition.coerceAtLeast(0L)
        var lastPlayerUpdateAt = SystemClock.elapsedRealtime()

        while (true) {
            withFrameMillis {
                val now = SystemClock.elapsedRealtime()
                val reportedPosition = player.currentPosition.coerceAtLeast(0L)
                if (reportedPosition != lastPlayerPosition) {
                    lastPlayerPosition = reportedPosition
                    lastPlayerUpdateAt = now
                }

                duration = max(
                    1L,
                    player.duration.takeIf { it > 0L }
                        ?: metadata?.duration?.times(1000L)
                        ?: 1L,
                )
                if (!seeking) {
                    position = (
                        lastPlayerPosition + if (isPlaying) {
                            ((now - lastPlayerUpdateAt).coerceAtLeast(0L) * player.playbackParameters.speed)
                                .roundToLong()
                        } else {
                            0L
                        }
                        ).coerceIn(0L, duration)
                    sliderValue = (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                }
            }
        }
    }

    val visibleValue = sliderValue

    DiyyTrackSlider(
        value = visibleValue,
        onValueChange = {
            seeking = true
            sliderValue = it
        },
        onValueChangeFinished = {
            playerConnection?.seekTo((duration * sliderValue).toLong())
            position = (duration * sliderValue).toLong()
            seeking = false
        },
        activeColor = DiyyRed,
        inactiveColor = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.fillMaxWidth(),
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = formatTime(if (seeking) (duration * sliderValue).toLong() else position),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = formatTime(duration),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun toggleSongDownload(
    context: android.content.Context,
    downloadUtil: DownloadUtil,
    metadata: MediaMetadata,
    download: Download?,
) {
    if (download?.state == Download.STATE_COMPLETED ||
        download?.state == Download.STATE_QUEUED ||
        download?.state == Download.STATE_DOWNLOADING
    ) {
        DownloadService.sendRemoveDownload(
            context,
            ExoDownloadService::class.java,
            metadata.id,
            false,
        )
        return
    }

    downloadUtil.database.transaction { insert(metadata) }
    val request = DownloadRequest.Builder(metadata.id, metadata.id.toUri())
        .setCustomCacheKey(metadata.id)
        .setData(metadata.title.toByteArray())
        .build()
    DownloadService.sendAddDownload(
        context,
        ExoDownloadService::class.java,
        request,
        false,
    )
}

@Composable
private fun SimplePlayerButton(
    icon: ImageVector,
    contentDescription: String,
    size: Int,
    active: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(size.dp),
        shape = CircleShape,
        color = if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        tonalElevation = 0.dp,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (active) DiyyRed else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size((size * 0.46f).dp),
            )
        }
    }
}

@Composable
private fun CompactPlayerAction(
    icon: Int,
    contentDescription: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        tonalElevation = 0.dp,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
                contentDescription = contentDescription,
                tint = if (active) DiyyRed else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(21.dp),
            )
        }
    }
}

@Composable
private fun DiyyTrackSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    var widthPx by remember { mutableIntStateOf(1) }
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnValueChangeFinished by rememberUpdatedState(onValueChangeFinished)
    val fraction = value.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .height(28.dp)
            .onSizeChanged { widthPx = it.width.coerceAtLeast(1) }
            .pointerInput(widthPx) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)

                    fun updateValue(x: Float) {
                        currentOnValueChange((x / widthPx.toFloat()).coerceIn(0f, 1f))
                    }

                    updateValue(down.position.x)
                    down.consume()

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        updateValue(change.position.x)
                        change.consume()
                        if (!change.pressed) break
                    }

                    currentOnValueChangeFinished?.invoke()
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(inactiveColor),
        )

        if (fraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(activeColor),
            )
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds.coerceAtLeast(0L) / 1000L).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
