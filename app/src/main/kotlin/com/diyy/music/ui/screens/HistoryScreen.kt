package com.diyy.music.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diyy.music.R
import com.diyy.music.db.MusicDatabase
import com.diyy.music.extensions.toMediaItem
import com.diyy.music.playback.PlayerConnection
import com.diyy.music.playback.queues.ListQueue
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.EmptyFigmaState
import com.diyy.music.ui.component.FigmaMediaRow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    database: MusicDatabase,
    playerConnection: PlayerConnection?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val events by database.events().collectAsStateWithLifecycle(initialValue = emptyList())
    val grouped = events.groupBy { it.event.timestamp.toLocalDate() }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item { DiyyScreenHeader(title = "History", onBack = onBack) }
        if (events.isEmpty()) {
            item {
                EmptyFigmaState(
                    title = "No history yet",
                    subtitle = "Songs you play will appear here.",
                    icon = R.drawable.history,
                )
            }
        } else {
            grouped.forEach { (date, dateEvents) ->
                item(key = "header-$date") {
                    Text(
                        text = historyDateLabel(date),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
                items(dateEvents, key = { it.event.id }) { event ->
                    FigmaMediaRow(
                        title = event.song.title,
                        subtitle = event.song.orderedArtists.joinToString { it.name },
                        imageUrl = event.song.thumbnailUrl,
                        onClick = {
                            val queue = dateEvents.map { it.song }
                            playerConnection?.playQueue(
                                ListQueue(
                                    title = historyDateLabel(date),
                                    items = queue.map { it.toMediaItem() },
                                    startIndex = dateEvents.indexOf(event),
                                ),
                            )
                        },
                    )
                }
            }
        }
    }
}

private fun historyDateLabel(date: LocalDate): String = when (date) {
    LocalDate.now() -> "Today"
    LocalDate.now().minusDays(1) -> "Yesterday"
    else -> date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
}
