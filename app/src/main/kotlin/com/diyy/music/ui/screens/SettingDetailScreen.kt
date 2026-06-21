package com.diyy.music.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diyy.music.BuildConfig
import com.diyy.music.R
import com.diyy.music.constants.AccountChannelHandleKey
import com.diyy.music.constants.AccountEmailKey
import com.diyy.music.constants.AccountNameKey
import com.diyy.music.constants.AutoDownloadOnLikeKey
import com.diyy.music.constants.AutoplayKey
import com.diyy.music.constants.CrossfadeEnabledKey
import com.diyy.music.constants.DataSyncIdKey
import com.diyy.music.constants.DisableScreenshotKey
import com.diyy.music.constants.DiscordButton1EnabledKey
import com.diyy.music.constants.DiscordButton2EnabledKey
import com.diyy.music.constants.EnableDiscordRPCKey
import com.diyy.music.constants.EnableHighRefreshRateKey
import com.diyy.music.constants.EnableSongCacheKey
import com.diyy.music.constants.HideExplicitKey
import com.diyy.music.constants.HideVideoSongsKey
import com.diyy.music.constants.HideYoutubeShortsKey
import com.diyy.music.constants.InnerTubeCookieKey
import com.diyy.music.constants.MiniPlayerOutlineKey
import com.diyy.music.constants.PauseListenHistoryKey
import com.diyy.music.constants.PauseSearchHistoryKey
import com.diyy.music.constants.PersistentQueueKey
import com.diyy.music.constants.SkipSilenceKey
import com.diyy.music.constants.VisitorDataKey
import com.diyy.music.constants.YtmSyncKey
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.FigmaDivider
import com.diyy.music.ui.component.FigmaGroupedList
import com.diyy.music.ui.component.FigmaSettingsRow
import com.diyy.music.ui.component.LiquidGlassBox
import com.diyy.music.ui.theme.DiyyRed
import com.diyy.music.utils.dataStore
import com.diyy.music.viewmodels.AccountSettingsViewModel
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
        "account" -> AccountTokenDetail(onBack, modifier)
        else -> ToggleSettingsDetail(section, onBack, modifier)
    }
}

@Composable
private fun AccountTokenDetail(
    onBack: () -> Unit,
    modifier: Modifier,
    viewModel: AccountSettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storedCookie by context.dataStore.data
        .map { it[InnerTubeCookieKey].orEmpty() }
        .collectAsStateWithLifecycle(initialValue = "")
    val storedVisitor by context.dataStore.data
        .map { it[VisitorDataKey].orEmpty() }
        .collectAsStateWithLifecycle(initialValue = "")
    val storedSyncId by context.dataStore.data
        .map { it[DataSyncIdKey].orEmpty() }
        .collectAsStateWithLifecycle(initialValue = "")
    val storedName by context.dataStore.data
        .map { it[AccountNameKey].orEmpty() }
        .collectAsStateWithLifecycle(initialValue = "")
    val storedEmail by context.dataStore.data
        .map { it[AccountEmailKey].orEmpty() }
        .collectAsStateWithLifecycle(initialValue = "")
    val storedHandle by context.dataStore.data
        .map { it[AccountChannelHandleKey].orEmpty() }
        .collectAsStateWithLifecycle(initialValue = "")

    var cookie by rememberSaveable { mutableStateOf("") }
    var visitorData by rememberSaveable { mutableStateOf("") }
    var dataSyncId by rememberSaveable { mutableStateOf("") }
    var accountName by rememberSaveable { mutableStateOf("") }
    var accountEmail by rememberSaveable { mutableStateOf("") }
    var accountHandle by rememberSaveable { mutableStateOf("") }
    var showToken by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(storedCookie, storedVisitor, storedSyncId, storedName, storedEmail, storedHandle) {
        if (cookie.isBlank()) cookie = storedCookie
        if (visitorData.isBlank()) visitorData = storedVisitor
        if (dataSyncId.isBlank()) dataSyncId = storedSyncId
        if (accountName.isBlank()) accountName = storedName
        if (accountEmail.isBlank()) accountEmail = storedEmail
        if (accountHandle.isBlank()) accountHandle = storedHandle
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 28.dp),
    ) {
        item { DiyyScreenHeader(title = "Account & Token", onBack = onBack) }
        item {
            Text(
                text = "Connect your music account without the old cluttered token page.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
            )
        }
        item {
            LiquidGlassBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = 10.dp,
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = if (storedCookie.isBlank()) "Not connected" else "Account connected",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (storedCookie.isBlank()) MaterialTheme.colorScheme.onSurface else DiyyRed,
                    )
                    TokenField(
                        value = cookie,
                        onValueChange = { cookie = it },
                        label = "Account token / cookie",
                        secret = !showToken,
                    )
                    TextButton(onClick = { showToken = !showToken }) {
                        Text(if (showToken) "Hide token" else "Show token", color = DiyyRed)
                    }
                    TokenField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = "Account name",
                    )
                    TokenField(
                        value = accountEmail,
                        onValueChange = { accountEmail = it },
                        label = "Email (optional)",
                    )
                    TokenField(
                        value = accountHandle,
                        onValueChange = { accountHandle = it },
                        label = "Channel handle (optional)",
                    )
                    TokenField(
                        value = visitorData,
                        onValueChange = { visitorData = it },
                        label = "Visitor data (advanced)",
                        secret = true,
                    )
                    TokenField(
                        value = dataSyncId,
                        onValueChange = { dataSyncId = it },
                        label = "Data sync ID (advanced)",
                        secret = true,
                    )
                    Spacer(Modifier.height(2.dp))
                    Button(
                        onClick = {
                            viewModel.saveTokenAndRestart(
                                context = context,
                                cookie = cookie.trim(),
                                visitorData = visitorData.trim(),
                                dataSyncId = dataSyncId.trim(),
                                accountName = accountName.trim().ifBlank { "DiyyMusic User" },
                                accountEmail = accountEmail.trim(),
                                accountChannelHandle = accountHandle.trim(),
                            )
                        },
                        enabled = cookie.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DiyyRed),
                    ) {
                        Text("Save & restart DiyyMusic")
                    }
                    TextButton(
                        onClick = {
                            scope.launch {
                                context.dataStore.edit { settings ->
                                    settings.remove(InnerTubeCookieKey)
                                    settings.remove(VisitorDataKey)
                                    settings.remove(DataSyncIdKey)
                                    settings.remove(AccountNameKey)
                                    settings.remove(AccountEmailKey)
                                    settings.remove(AccountChannelHandleKey)
                                }
                                cookie = ""
                                visitorData = ""
                                dataSyncId = ""
                                accountName = ""
                                accountEmail = ""
                                accountHandle = ""
                            }
                        },
                        enabled = storedCookie.isNotBlank() || cookie.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Clear account token", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun TokenField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    secret: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
    )
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
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
        item {
            FigmaGroupedList(modifier = Modifier.padding(horizontal = 18.dp)) {
                settings.forEachIndexed { index, setting ->
                    PreferenceSwitchRow(setting)
                    if (index != settings.lastIndex) FigmaDivider()
                }
            }
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
    "discord" -> "Discord Rich Presence"
    "storage" -> "Storage"
    "backup" -> "Backup and Restore"
    else -> section.replaceFirstChar { it.uppercase() }
}

private fun settingsFor(section: String): List<BooleanSetting> = when (section) {
    "appearance" -> listOf(
        BooleanSetting("High refresh rate", "Use the smoothest display mode available.", EnableHighRefreshRateKey, true),
        BooleanSetting("Mini-player outline", "Show the subtle Liquid Glass edge highlight.", MiniPlayerOutlineKey, true),
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
    val uriHandler = LocalUriHandler.current
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 24.dp)) {
        item { DiyyScreenHeader("About", onBack = onBack) }
        item {
            FigmaGroupedList(modifier = Modifier.padding(horizontal = 18.dp)) {
                FigmaSettingsRow(
                    title = "DiyyMusic",
                    subtitle = "Version ${BuildConfig.VERSION_NAME}",
                    icon = R.drawable.music_note,
                    onClick = { runCatching { uriHandler.openUri("https://github.com/diyy4a/DiyyMusic/releases") } },
                )
                FigmaDivider()
                FigmaSettingsRow(
                    title = "Open source licenses",
                    subtitle = "GPLv3 and third-party libraries",
                    icon = R.drawable.info,
                    onClick = { runCatching { uriHandler.openUri("https://github.com/diyy4a/DiyyMusic/blob/main/LICENSE") } },
                )
                FigmaDivider()
                FigmaSettingsRow(
                    title = "Source code",
                    subtitle = "github.com/diyy4a/DiyyMusic",
                    icon = R.drawable.github,
                    onClick = { runCatching { uriHandler.openUri("https://github.com/diyy4a/DiyyMusic") } },
                )
            }
        }
    }
}
