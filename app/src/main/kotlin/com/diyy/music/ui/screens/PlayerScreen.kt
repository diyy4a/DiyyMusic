package com.diyy.music.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
    val artworkScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.965f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "artworkBreathingScale",
    )
    val playButtonScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.055f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "playButtonScale",
    )

    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(1L) }
    var sliderValue by remember { mutableFloatStateOf(0f) }
    var seeking by remember { mutableStateOf(false) }
    var volume by remember { mutableFloatStateOf(1f) }
    var showQueue by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val toggleDownload: () -> Unit = {
        metadata?.let { toggleSongDownload(context, downloadUtil, it, download) }
        Unit
    }

    LaunchedEffect(playerConnection, isPlaying, seeking, metadata?.id) {
        while (true) {
            val player = runCatching { playerConnection?.player }.getOrNull()
            if (player != null) {
                position = max(0L, player.currentPosition)
                duration = max(1L, player.duration.takeIf { it > 0 } ?: metadata?.duration?.times(1000L) ?: 1L)
                if (!seeking) sliderValue = (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                volume = player.volume
            }
            delay(300)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val artworkSize = (maxWidth - 40.dp).coerceAtMost(410.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
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
                    tint = DiyyRed,
                )
            }

            if (!hideArtwork) {
                Spacer(Modifier.height(20.dp))
                Artwork(
                    url = metadata?.thumbnailUrl,
                    modifier = Modifier
                        .size(artworkSize)
                        .align(Alignment.CenterHorizontally)
                        .graphicsLayer {
                            scaleX = artworkScale
                            scaleY = artworkScale
                        },
                    cornerRadius = 28,
                )
                Spacer(Modifier.height(22.dp))
            } else {
                Spacer(Modifier.height(12.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = metadata?.title ?: "Not Playing",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = metadata?.artists?.joinToString { it.name }.orEmpty().ifBlank { "DiyyMusic" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = DiyyRed,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                FigmaCircleButton(
                    icon = if (favorite) R.drawable.favorite else R.drawable.favorite_border,
                    contentDescription = "Favorite",
                    onClick = { playerConnection?.toggleLike() },
                    tint = DiyyRed,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.width(8.dp))
                FigmaCircleButton(
                    icon = when {
                        isDownloaded -> R.drawable.offline
                        isDownloading -> R.drawable.close
                        else -> R.drawable.download
                    },
                    contentDescription = when {
                        isDownloaded -> "Remove download"
                        isDownloading -> "Cancel download"
                        else -> "Download song"
                    },
                    onClick = toggleDownload,
                    tint = DiyyRed,
                    modifier = Modifier.size(48.dp),
                )
            }

            Spacer(Modifier.height(14.dp))
            DiyyTrackSlider(
                value = sliderValue,
                onValueChange = {
                    seeking = true
                    sliderValue = it
                },
                onValueChangeFinished = {
                    playerConnection?.seekTo((duration * sliderValue).toLong())
                    seeking = false
                },
                activeColor = DiyyRed,
                inactiveColor = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = formatTime((duration * sliderValue).toLong()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "-${formatTime((duration - duration * sliderValue).toLong())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlayerGlassButton(
                    icon = R.drawable.skip_previous,
                    contentDescription = "Previous",
                    size = 58,
                    iconSize = 30,
                    onClick = { playerConnection?.seekToPrevious() },
                )
                Spacer(Modifier.width(24.dp))
                Surface(
                    modifier = Modifier
                        .size(78.dp)
                        .graphicsLayer {
                            scaleX = playButtonScale
                            scaleY = playButtonScale
                        },
                    shape = CircleShape,
                    color = Color.Transparent,
                    onClick = { playerConnection?.togglePlayPause() },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFF6A9B), DiyyRed, Color(0xFFFF0D5E)),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp),
                        )
                    }
                }
                Spacer(Modifier.width(24.dp))
                PlayerGlassButton(
                    icon = R.drawable.skip_next,
                    contentDescription = "Next",
                    size = 58,
                    iconSize = 30,
                    onClick = { playerConnection?.seekToNext() },
                )
            }

            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.volume_down),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(10.dp))
                DiyyTrackSlider(
                    value = volume,
                    onValueChange = {
                        volume = it
                        runCatching { playerConnection?.player?.volume = it }
                    },
                    activeColor = DiyyRed.copy(alpha = 0.74f),
                    inactiveColor = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(10.dp))
                Icon(
                    painter = painterResource(R.drawable.volume_up),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp),
                )
            }

            Spacer(Modifier.height(20.dp))
            LiquidGlassBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp),
                shape = RoundedCornerShape(30.dp),
                elevation = 12.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlayerAction(
                        icon = if (favorite) R.drawable.favorite else R.drawable.favorite_border,
                        label = "Favorite",
                        active = favorite,
                        onClick = { playerConnection?.toggleLike() },
                    )
                    PlayerAction(
                        icon = if (shuffleEnabled) R.drawable.shuffle_on else R.drawable.shuffle,
                        label = "Shuffle",
                        active = shuffleEnabled,
                        onClick = { playerConnection?.toggleShuffle() },
                    )
                    PlayerAction(
                        icon = when (repeatMode) {
                            Player.REPEAT_MODE_ONE -> R.drawable.repeat_one_on
                            Player.REPEAT_MODE_ALL -> R.drawable.repeat_on
                            else -> R.drawable.repeat
                        },
                        label = "Repeat",
                        active = repeatMode != Player.REPEAT_MODE_OFF,
                        onClick = { playerConnection?.cycleRepeatMode() },
                    )
                    PlayerAction(
                        icon = R.drawable.lyrics,
                        label = "Lyrics",
                        active = showLyrics,
                        onClick = { if (metadata != null) showLyrics = true },
                    )
                    PlayerAction(
                        icon = R.drawable.queue_music,
                        label = "Queue",
                        active = showQueue,
                        onClick = { if (playerConnection != null) showQueue = true },
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
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
            positionMs = position,
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
private fun PlayerGlassButton(
    icon: Int,
    contentDescription: String,
    size: Int,
    iconSize: Int,
    onClick: () -> Unit,
) {
    LiquidGlassBox(
        modifier = Modifier.size(size.dp),
        shape = RoundedCornerShape((size / 2).dp),
        elevation = 10.dp,
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = DiyyRed,
            modifier = Modifier
                .size(iconSize.dp)
                .align(Alignment.Center),
        )
    }
}

@Composable
private fun PlayerAction(
    icon: Int,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(if (active) DiyySoftRed else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = if (active) DiyyRed else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(23.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) DiyyRed else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
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
            .height(30.dp)
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
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(inactiveColor),
        )

        if (fraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
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
