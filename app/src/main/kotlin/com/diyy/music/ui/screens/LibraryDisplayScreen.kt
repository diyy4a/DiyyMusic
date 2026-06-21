package com.diyy.music.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.diyy.music.R
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.FigmaLibraryShortcut
import com.diyy.music.ui.theme.DiyyRed

private data class DisplayOption(val label: String, val icon: Int)

private val displayOptions = listOf(
    DisplayOption("Songs", R.drawable.music_note),
    DisplayOption("Albums", R.drawable.album),
    DisplayOption("Artists", R.drawable.artist),
    DisplayOption("Genres", R.drawable.music_note),
    DisplayOption("Playlists", R.drawable.queue_music),
    DisplayOption("Composers", R.drawable.queue_music),
    DisplayOption("Download", R.drawable.download),
    DisplayOption("Compilations", R.drawable.library_music),
    DisplayOption("Music Videos", R.drawable.play),
    DisplayOption("Favorites", R.drawable.favorite),
)

@Composable
fun LibraryDisplayScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var enabled by remember {
        mutableStateOf(displayOptions.mapIndexedNotNull { index, option -> if (index in setOf(0, 1, 4, 9)) option.label else null }.toSet())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DiyyScreenHeader(title = "Library", onBack = onBack)
        Text(
            text = "What do you want\nto display:",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )

        displayOptions.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { option ->
                    FigmaLibraryShortcut(
                        title = option.label,
                        icon = option.icon,
                        selected = option.label in enabled,
                        onClick = {
                            enabled = if (option.label in enabled) enabled - option.label else enabled + option.label
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = DiyyRed,
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Text("Done")
        }
        Spacer(Modifier.height(24.dp))
    }
}
