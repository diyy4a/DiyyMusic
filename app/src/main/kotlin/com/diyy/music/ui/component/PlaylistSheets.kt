/**
 * Copyright (C) upstream contributors and DiyyMusic contributors
 * Modified for DiyyMusic in 2026. Licensed under GPL-3.0.
 */

package com.diyy.music.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diyy.music.R
import com.diyy.music.db.MusicDatabase
import com.diyy.music.db.entities.Playlist
import com.diyy.music.db.entities.PlaylistEntity
import com.diyy.music.models.MediaMetadata
import com.diyy.music.ui.theme.DiyyRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Dialog to create a new local playlist. If [songsToAdd] is non-empty, those songs
 * are inserted into the library (if needed) and added to the freshly created playlist.
 */
@Composable
fun CreatePlaylistDialog(
    database: MusicDatabase,
    onDismiss: () -> Unit,
    songsToAdd: List<MediaMetadata> = emptyList(),
    onCreated: (String) -> Unit = {},
) {
    var name by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        title = { Text("New playlist") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Playlist name") },
                placeholder = { Text("e.g. Weekend Vibes") },
                leadingIcon = {
                    Icon(painterResource(R.drawable.queue_music), contentDescription = null)
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DiyyRed,
                    focusedLabelColor = DiyyRed,
                    cursorColor = DiyyRed,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    val trimmed = name.trim()
                    val entity = PlaylistEntity(name = trimmed, bookmarkedAt = LocalDateTime.now())
                    scope.launch(Dispatchers.IO) {
                        database.withTransaction {
                            insert(entity)
                            if (songsToAdd.isNotEmpty()) {
                                songsToAdd.forEach { insert(it) }
                                addSongToPlaylist(
                                    Playlist(playlist = entity, songCount = 0, songThumbnails = emptyList()),
                                    songsToAdd.map { it.id },
                                )
                            }
                        }
                    }
                    onCreated(entity.id)
                    onDismiss()
                },
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

/**
 * Bottom sheet listing the user's playlists so [songs] can be added to one of them,
 * with a shortcut to create a brand-new playlist inline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    database: MusicDatabase,
    songs: List<MediaMetadata>,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
    val playlists by database.playlistsByCreateDateAsc().collectAsStateWithLifecycle(initialValue = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }

    fun dismissSheet() {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = ::dismissSheet,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.99f),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 24.dp),
        ) {
            Text(
                text = if (songs.size == 1) "Add to playlist" else "Add ${songs.size} songs to playlist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            LiquidGlassBox(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = 4.dp,
                onClick = { showCreateDialog = true },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(38.dp)) {
                            Icon(
                                painter = painterResource(R.drawable.add),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Text("New playlist", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(10.dp))

            if (playlists.isEmpty()) {
                Text(
                    text = "You don't have any playlists yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 18.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    contentPadding = PaddingValues(bottom = 4.dp),
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        FigmaMediaRow(
                            title = playlist.title,
                            subtitle = "${playlist.songCount} songs",
                            imageUrl = playlist.thumbnails.firstOrNull(),
                            modifier = Modifier.animateItem(),
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    database.withTransaction {
                                        songs.forEach { insert(it) }
                                        addSongToPlaylist(playlist, songs.map { it.id })
                                    }
                                }
                                dismissSheet()
                            },
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            database = database,
            songsToAdd = songs,
            onDismiss = {
                showCreateDialog = false
                dismissSheet()
            },
        )
    }
}
