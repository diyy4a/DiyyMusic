package com.diyy.music.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.diyy.music.R
import com.diyy.music.extensions.togglePlayPause
import com.diyy.music.extensions.toggleRepeatMode
import com.diyy.music.playback.PlayerConnection
import com.diyy.music.ui.component.Artwork
import com.diyy.music.ui.component.FigmaCircleButton
import com.diyy.music.ui.theme.DiyyRed
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun PlayerScreen(
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metadataState = playerConnection?.mediaMetadata?.collectAsStateWithLifecycle()
        ?: remember { androidx.compose.runtime.mutableStateOf<com.diyy.music.models.MediaMetadata?>(null) }
    val metadata by metadataState
    val playingState = playerConnection?.isPlaying?.collectAsStateWithLifecycle()
        ?: remember { androidx.compose.runtime.mutableStateOf(false) }
    val isPlaying by playingState
    val shuffleState = playerConnection?.shuffleModeEnabled?.collectAsStateWithLifecycle()
        ?: remember { androidx.compose.runtime.mutableStateOf(false) }
    val shuffleEnabled by shuffleState
    val repeatState = playerConnection?.repeatMode?.collectAsStateWithLifecycle()
        ?: remember { androidx.compose.runtime.mutableStateOf(0) }
    val repeatMode by repeatState

    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(1L) }
    var sliderValue by remember { mutableFloatStateOf(0f) }
    var seeking by remember { mutableStateOf(false) }
    var volume by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(playerConnection, isPlaying, seeking) {
        while (true) {
            val player = runCatching { playerConnection?.player }.getOrNull()
            if (player != null) {
                position = max(0L, player.currentPosition)
                duration = max(1L, player.duration.takeIf { it > 0 } ?: metadata?.duration?.times(1000L) ?: 1L)
                if (!seeking) sliderValue = (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                volume = player.volume
            }
            delay(350)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val artworkSize = maxWidth.coerceAtMost(390.dp) - 40.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
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
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(18.dp))
            Artwork(
                url = metadata?.thumbnailUrl,
                modifier = Modifier
                    .size(artworkSize)
                    .align(Alignment.CenterHorizontally),
                cornerRadius = 18,
            )

            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = metadata?.title ?: "Not Playing",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = metadata?.artists?.joinToString { it.name }.orEmpty().ifBlank { "DiyyMusic" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                FigmaCircleButton(
                    icon = R.drawable.more_horiz,
                    contentDescription = "More",
                    onClick = {},
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(14.dp))
            Slider(
                value = sliderValue,
                onValueChange = {
                    seeking = true
                    sliderValue = it
                },
                onValueChangeFinished = {
                    runCatching { playerConnection?.player?.seekTo((duration * sliderValue).toLong()) }
                    seeking = false
                },
                colors = SliderDefaults.colors(
                    thumbColor = DiyyRed,
                    activeTrackColor = DiyyRed,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(formatTime((duration * sliderValue).toLong()), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                Text("-${formatTime((duration - duration * sliderValue).toLong())}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        runCatching {
                            if ((playerConnection?.player?.currentPosition ?: 0L) > 3_000L) {
                                playerConnection?.player?.seekTo(0L)
                            } else {
                                playerConnection?.player?.seekToPreviousMediaItem()
                            }
                        }
                    },
                ) {
                    Icon(painterResource(R.drawable.skip_previous), "Previous", modifier = Modifier.size(34.dp))
                }
                Surface(
                    modifier = Modifier.size(62.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = { runCatching { playerConnection?.player?.togglePlayPause() } },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = DiyyRed,
                            modifier = Modifier.size(34.dp),
                        )
                    }
                }
                IconButton(onClick = { runCatching { playerConnection?.player?.seekToNextMediaItem() } }) {
                    Icon(painterResource(R.drawable.skip_next), "Next", modifier = Modifier.size(34.dp))
                }
            }

            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.volume_down), null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Slider(
                    value = volume,
                    onValueChange = {
                        volume = it
                        runCatching { playerConnection?.player?.volume = it }
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.outline,
                        activeTrackColor = MaterialTheme.colorScheme.outline,
                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Icon(painterResource(R.drawable.volume_up), null, modifier = Modifier.size(18.dp))
            }

            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                IconButton(onClick = {}) {
                    Icon(painterResource(R.drawable.favorite_border), "Favorite")
                }
                IconButton(
                    onClick = {
                        runCatching {
                            playerConnection?.player?.shuffleModeEnabled = !(playerConnection?.player?.shuffleModeEnabled ?: false)
                        }
                    },
                ) {
                    Icon(
                        painterResource(if (shuffleEnabled) R.drawable.shuffle_on else R.drawable.shuffle),
                        "Shuffle",
                        tint = if (shuffleEnabled) DiyyRed else MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(onClick = { runCatching { playerConnection?.player?.toggleRepeatMode() } }) {
                    Icon(
                        painterResource(
                            when (repeatMode) {
                                Player.REPEAT_MODE_ONE -> R.drawable.repeat_one_on
                                Player.REPEAT_MODE_ALL -> R.drawable.repeat_on
                                else -> R.drawable.repeat
                            },
                        ),
                        "Repeat",
                        tint = if (repeatMode == Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.onSurface else DiyyRed,
                    )
                }
                IconButton(onClick = {}) {
                    Icon(painterResource(R.drawable.queue_music), "Queue")
                }
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds.coerceAtLeast(0L) / 1000L).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
