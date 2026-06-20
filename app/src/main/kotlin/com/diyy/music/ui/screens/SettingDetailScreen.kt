package com.diyy.music.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diyy.music.BuildConfig
import com.diyy.music.R
import com.diyy.music.constants.AutoDownloadOnLikeKey
import com.diyy.music.constants.AutoplayKey
import com.diyy.music.constants.CrossfadeEnabledKey
import com.diyy.music.constants.DisableScreenshotKey
import com.diyy.music.constants.DiscordButton1EnabledKey
import com.diyy.music.constants.DiscordButton2EnabledKey
import com.diyy.music.constants.EnableDiscordRPCKey
import com.diyy.music.constants.EnableHighRefreshRateKey
import com.diyy.music.constants.EnableSongCacheKey
import com.diyy.music.constants.HideExplicitKey
import com.diyy.music.constants.HideVideoSongsKey
import com.diyy.music.constants.HideYoutubeShortsKey
import com.diyy.music.constants.MiniPlayerOutlineKey
import com.diyy.music.constants.PauseListenHistoryKey
import com.diyy.music.constants.PauseSearchHistoryKey
import com.diyy.music.constants.PersistentQueueKey
import com.diyy.music.constants.SkipSilenceKey
import com.diyy.music.constants.YtmSyncKey
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.FigmaDivider
import com.diyy.music.ui.component.FigmaSettingsRow
import com.diyy.music.ui.theme.DiyyRed
import com.diyy.music.utils.dataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private data class BooleanSetting(
    val title: String,
    val subtitle: String,
    val key: Preferences.Key<Boolean>,
    val default: Boolean,
    val invert: Boolean = false,
)

@Composable
fun SettingDetailScreen(
    section: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (section) {
        "about" -> AboutDetail(onBack, modifier)
        else -> ToggleSettingsDetail(section, onBack, modifier)
    }
}

@Composable
private fun ToggleSettingsDetail(section: String, onBack: () -> Unit, modifier: Modifier) {
    val title = sectionTitle(section)
    val settings = settingsFor(section)

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 24.dp)) {
        item { DiyyScreenHeader(title, onBack = onBack) }
        item {
            Text(
                text = "DiyyMusic",
                style = MaterialTheme.typography.bodyMedium,
                color = DiyyRed,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
        settings.forEachIndexed { index, setting ->
            item(key = setting.key.name) {
                PreferenceSwitchRow(setting)
            }
            if (index != settings.lastIndex) item { FigmaDivider() }
        }
    }
}

@Composable
private fun PreferenceSwitchRow(setting: BooleanSetting) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storedValue by context.dataStore.data
        .map { preferences -> preferences[setting.key] ?: setting.default }
        .collectAsStateWithLifecycle(initialValue = setting.default)
    val checked = if (setting.invert) !storedValue else storedValue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(setting.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                setting.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = { enabled ->
                scope.launch {
                    context.dataStore.edit { preferences -> preferences[setting.key] = if (setting.invert) !enabled else enabled }
                }
            },
        )
    }
}

private fun sectionTitle(section: String): String = when (section) {
    "appearance" -> "Appearance"
    "player" -> "Player and Audio"
    "content" -> "Content"
    "account" -> "Account and Session"
    "discord" -> "Discord Rich Presence"
    "storage" -> "Storage"
    "backup" -> "Backup and Restore"
    else -> section.replaceFirstChar { it.uppercase() }
}

private fun settingsFor(section: String): List<BooleanSetting> = when (section) {
    "appearance" -> listOf(
        BooleanSetting("High refresh rate", "Use the smoothest display mode available.", EnableHighRefreshRateKey, true),
        BooleanSetting("Mini-player outline", "Show the thin Figma-style border around the mini player.", MiniPlayerOutlineKey, true),
        BooleanSetting("Block screenshots", "Protect the app window from screenshots and screen recording.", DisableScreenshotKey, false),
    )
    "player" -> listOf(
        BooleanSetting("Autoplay", "Continue with related music when the queue ends.", AutoplayKey, true),
        BooleanSetting("Crossfade", "Blend the end of one track into the next.", CrossfadeEnabledKey, false),
        BooleanSetting("Skip silence", "Skip silent sections when supported.", SkipSilenceKey, false),
    )
    "content" -> listOf(
        BooleanSetting("Hide explicit music", "Filter explicit results and recommendations.", HideExplicitKey, false),
        BooleanSetting("Hide video songs", "Prefer audio releases in music lists.", HideVideoSongsKey, false),
        BooleanSetting("Hide short videos", "Remove short-form video playlists from results.", HideYoutubeShortsKey, false),
    )
    "account" -> listOf(
        BooleanSetting("Sync music library", "Synchronize supported account library data.", YtmSyncKey, true),
        BooleanSetting("Pause listening history", "Do not add new plays to local history.", PauseListenHistoryKey, false),
        BooleanSetting("Pause search history", "Do not save new search terms.", PauseSearchHistoryKey, false),
    )
    "discord" -> listOf(
        BooleanSetting("Enable Rich Presence", "Share the current track on Discord.", EnableDiscordRPCKey, false),
        BooleanSetting("Show first button", "Display the first configured Discord action.", DiscordButton1EnabledKey, false),
        BooleanSetting("Show second button", "Display the second configured Discord action.", DiscordButton2EnabledKey, false),
    )
    "storage" -> listOf(
        BooleanSetting("Song cache", "Cache streamed audio for faster replay.", EnableSongCacheKey, true),
        BooleanSetting("Download liked songs", "Automatically download music after liking it.", AutoDownloadOnLikeKey, false),
        BooleanSetting("Keep playback queue", "Restore the last queue after reopening DiyyMusic.", PersistentQueueKey, true),
    )
    "backup" -> listOf(
        BooleanSetting("Include account sync", "Keep account synchronization enabled with restored data.", YtmSyncKey, true),
        BooleanSetting("Include listening history", "Keep local listening history active after restore.", PauseListenHistoryKey, false, invert = true),
        BooleanSetting("Include search history", "Keep local search history active after restore.", PauseSearchHistoryKey, false, invert = true),
    )
    else -> emptyList()
}

@Composable
private fun AboutDetail(onBack: () -> Unit, modifier: Modifier) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 24.dp)) {
        item { DiyyScreenHeader("About", onBack = onBack) }
        item {
            FigmaSettingsRow(
                title = "DiyyMusic",
                subtitle = "Version ${BuildConfig.VERSION_NAME}",
                icon = R.drawable.music_note,
                onClick = {},
            )
        }
        item { FigmaDivider() }
        item {
            FigmaSettingsRow(
                title = "Open source licenses",
                subtitle = "GPLv3 and third-party libraries",
                icon = R.drawable.info,
                onClick = {},
            )
        }
        item { FigmaDivider() }
        item {
            FigmaSettingsRow(
                title = "Source code",
                subtitle = "GitHub repository",
                icon = R.drawable.github,
                onClick = {},
            )
        }
    }
}
