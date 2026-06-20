package com.diyy.music.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diyy.music.BuildConfig
import com.diyy.music.R
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.FigmaDivider
import com.diyy.music.ui.component.FigmaSettingsRow
import com.diyy.music.ui.theme.DiyyRed

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenSection: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item { DiyyScreenHeader(title = "Settings", onBack = onBack) }

        item { SettingsGroupTitle("Interface") }
        item {
            FigmaSettingsRow(
                title = "Appearance",
                subtitle = "Figma layout, typography, and app theme",
                icon = R.drawable.palette,
                onClick = { onOpenSection("appearance") },
            )
        }

        item { SettingsGroupTitle("Playback & Content") }
        item {
            FigmaSettingsRow(
                title = "Player and audio",
                subtitle = "Quality, autoplay, crossfade, and equalizer",
                icon = R.drawable.play,
                onClick = { onOpenSection("player") },
            )
        }
        item { FigmaDivider() }
        item {
            FigmaSettingsRow(
                title = "Content",
                subtitle = "Language, explicit music, and recommendations",
                icon = R.drawable.language,
                onClick = { onOpenSection("content") },
            )
        }

        item { SettingsGroupTitle("Account & Connections") }
        item {
            FigmaSettingsRow(
                title = "Account and session",
                subtitle = "Profile, sign-in, and local session data",
                icon = R.drawable.account,
                onClick = { onOpenSection("account") },
            )
        }
        item { FigmaDivider() }
        item {
            FigmaSettingsRow(
                title = "Discord Rich Presence",
                subtitle = "Show the song currently playing on Discord",
                icon = R.drawable.discord,
                onClick = { onOpenSection("discord") },
            )
        }

        item { SettingsGroupTitle("Storage & Data") }
        item {
            FigmaSettingsRow(
                title = "Storage",
                subtitle = "Downloads, cache, and offline music",
                icon = R.drawable.storage,
                onClick = { onOpenSection("storage") },
            )
        }
        item { FigmaDivider() }
        item {
            FigmaSettingsRow(
                title = "Backup and restore",
                subtitle = "Export or import your DiyyMusic data",
                icon = R.drawable.backup,
                onClick = { onOpenSection("backup") },
            )
        }

        item { SettingsGroupTitle("DiyyMusic") }
        item {
            FigmaSettingsRow(
                title = "About",
                subtitle = "Version ${BuildConfig.VERSION_NAME} • Open source GPLv3",
                icon = R.drawable.info,
                onClick = { onOpenSection("about") },
            )
        }
    }
}

@Composable
private fun SettingsGroupTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        color = DiyyRed,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 6.dp),
    )
}
