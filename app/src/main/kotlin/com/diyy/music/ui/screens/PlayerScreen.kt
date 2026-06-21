package com.diyy.music.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.diyy.music.ui.component.Artwork
import com.diyy.music.ui.component.DiyyBrandMark
import com.diyy.music.ui.component.DiyyLyricsSheet
import com.diyy.music.ui.component.DiyyPlayerMenuSheet
import com.diyy.music.ui.component.DiyyQueueSheet
import com.diyy.music.ui.component.FigmaCircleButton
import com.diyy.music.ui.component.LiquidGlassBox
import com.diyy.music.ui.theme.DiyyRed
import com.diyy.music.ui.theme.DiyySoftRed
import com.diyy.music.ui.theme.LocalDiyyUiConfig
import com.diyy.music.utils.dataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.math.max

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

    val toggleDownload: () -> Unit = {
        metadata?.let { toggleSongDownload(context, downloadUtil, it, download) }
        Unit
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compact = maxHeight < 700.dp
        val artworkSize = when {
            hideArtwork -> 0.dp
            compact -> (maxWidth - 82.dp).coerceAtMost(270.dp)
            else -> (maxWidth - 64.dp).coerceAtMost(330.dp)
        }

        if (ui.backgroundGlow) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = if (compact) 76.dp else 104.dp)
                    .size(if (compact) 300.dp else 380.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                DiyyRed.copy(alpha = 0.12f * ui.accentStrength),
                                DiyyRed.copy(alpha = 0.035f * ui.accentStrength),
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
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

            Spacer(Modifier.height(if (compact) 12.dp else 20.dp))

            if (!hideArtwork) {
                AnimatedContent(
                    targetState = metadata?.id,
                    transitionSpec = {
                        (fadeIn(tween(180)) + scaleIn(tween(220), initialScale = 0.99f)) togetherWith
                            (fadeOut(tween(120)) + scaleOut(tween(150), targetScale = 1.01f))
                    },
                    label = "playerArtwork",
                ) {
                    Artwork(
                        url = metadata?.thumbnailUrl,
                        modifier = Modifier.size(artworkSize),
                        cornerRadius = 28,
                    )
                }
                Spacer(Modifier.height(if (compact) 16.dp else 24.dp))
            }

            Text(
                text = metadata?.title ?: "Not Playing",
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = metadata?.artists?.joinToString { it.name }.orEmpty().ifBlank { "DiyyMusic" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(if (compact) 16.dp else 22.dp))
            PlayerProgressSection(
                playerConnection = playerConnection,
                metadata = metadata,
                isPlaying = isPlaying,
            )

            Spacer(Modifier.height(if (compact) 12.dp else 18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SimplePlayerButton(
                    icon = if (shuffleEnabled) R.drawable.shuffle_on else R.drawable.shuffle,
                    contentDescription = "Shuffle",
                    active = shuffleEnabled,
                    size = 42,
                    onClick = { playerConnection?.toggleShuffle() },
                )
                SimplePlayerButton(
                    icon = R.drawable.skip_previous,
                    contentDescription = "Previous",
                    size = 52,
                    onClick = { playerConnection?.seekToPrevious() },
                )
                Surface(
                    modifier = Modifier.size(if (compact) 66.dp else 72.dp),
                    shape = CircleShape,
                    color = DiyyRed,
                    tonalElevation = 0.dp,
                    shadowElevation = if (ui.backgroundGlow) 5.dp else 1.dp,
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
                                painter = painterResource(if (playing) R.drawable.pause else R.drawable.play),
                                contentDescription = if (playing) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(33.dp),
                            )
                        }
                    }
                }
                SimplePlayerButton(
                    icon = R.drawable.skip_next,
                    contentDescription = "Next",
                    size = 52,
                    onClick = { playerConnection?.seekToNext() },
                )
                SimplePlayerButton(
                    icon = when (repeatMode) {
                        Player.REPEAT_MODE_ONE -> R.drawable.repeat_one_on
                        Player.REPEAT_MODE_ALL -> R.drawable.repeat_on
                        else -> R.drawable.repeat
                    },
                    contentDescription = "Repeat",
                    active = repeatMode != Player.REPEAT_MODE_OFF,
                    size = 42,
                    onClick = { playerConnection?.cycleRepeatMode() },
                )
            }

            Spacer(Modifier.height(if (compact) 14.dp else 22.dp))
            LiquidGlassBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = 5.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CompactPlayerAction(
                        icon = if (favorite) R.drawable.favorite else R.drawable.favorite_border,
                        contentDescription = "Favorite",
                        active = favorite,
                        onClick = { playerConnection?.toggleLike() },
                    )
                    CompactPlayerAction(
                        icon = R.drawable.lyrics,
                        contentDescription = "Lyrics",
                        active = showLyrics,
                        onClick = { if (metadata != null && playerConnection != null) showLyrics = true },
                    )
                    CompactPlayerAction(
                        icon = R.drawable.queue_music,
                        contentDescription = "Queue",
                        active = showQueue,
                        onClick = { if (playerConnection != null) showQueue = true },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
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
            onRadio = { playerConnection?.startRadioSeamlessly() },
            onRetryPlayback = { playerConnection?.retryPlayback() },
            onToggleFavorite = { playerConnection?.toggleLike() },
            onDismiss = { showMenu = false },
        )
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
        while (true) {
            val player = runCatching { playerConnection?.player }.getOrNull()
            if (player != null) {
                position = max(0L, player.currentPosition)
                duration = max(1L, player.duration.takeIf { it > 0 } ?: metadata?.duration?.times(1000L) ?: 1L)
                if (!seeking) {
                    sliderValue = (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                }
            }
            delay(if (isPlaying && !seeking) 220L else 500L)
        }
    }

    val animatedValue by animateFloatAsState(
        targetValue = sliderValue,
        animationSpec = if (seeking) tween(0) else tween(240, easing = LinearEasing),
        label = "playerProgress",
    )
    val visibleValue = if (seeking) sliderValue else animatedValue

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
    icon: Int,
    contentDescription: String,
    size: Int,
    active: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(size.dp),
        shape = CircleShape,
        color = if (active) DiyySoftRed else Color.Transparent,
        tonalElevation = 0.dp,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
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
        modifier = Modifier.size(46.dp),
        shape = CircleShape,
        color = if (active) DiyySoftRed else Color.Transparent,
        tonalElevation = 0.dp,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
                contentDescription = contentDescription,
                tint = if (active) DiyyRed else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(23.dp),
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
