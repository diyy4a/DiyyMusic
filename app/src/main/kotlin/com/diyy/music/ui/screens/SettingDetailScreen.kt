package com.diyy.music.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
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
import coil3.compose.AsyncImage
import com.diyy.music.BuildConfig
import com.diyy.music.R
import com.diyy.music.constants.AccountChannelHandleKey
import com.diyy.music.constants.AccountEmailKey
import com.diyy.music.constants.AccountNameKey
import com.diyy.music.constants.AudioNormalizationKey
import com.diyy.music.constants.AudioOffload
import com.diyy.music.constants.AudioQuality
import com.diyy.music.constants.AudioQualityKey
import com.diyy.music.constants.AutoDownloadOnLikeKey
import com.diyy.music.constants.AutoRadioQueueKey
import com.diyy.music.constants.AutoSkipNextOnErrorKey
import com.diyy.music.constants.AutoplayKey
import com.diyy.music.constants.CrossfadeDurationKey
import com.diyy.music.constants.CrossfadeEnabledKey
import com.diyy.music.constants.CrossfadeGaplessKey
import com.diyy.music.constants.DarkMode
import com.diyy.music.constants.DarkModeKey
import com.diyy.music.constants.DataSyncIdKey
import com.diyy.music.constants.DisableScreenshotKey
import com.diyy.music.constants.DiscordActivityTypeKey
import com.diyy.music.constants.DiscordAdvancedModeKey
import com.diyy.music.constants.DiscordAvatarKey
import com.diyy.music.constants.DiscordButton1EnabledKey
import com.diyy.music.constants.DiscordButton2EnabledKey
import com.diyy.music.constants.DiscordDetailsTemplateKey
import com.diyy.music.constants.DiscordNameKey
import com.diyy.music.constants.DiscordStateTemplateKey
import com.diyy.music.constants.DiscordUsernameKey
import com.diyy.music.constants.DiscordUserStatusKey
import com.diyy.music.constants.EnableDiscordRPCKey
import com.diyy.music.constants.EnableHighRefreshRateKey
import com.diyy.music.constants.EnableSongCacheKey
import com.diyy.music.constants.HideExplicitKey
import com.diyy.music.constants.HidePlayerThumbnailKey
import com.diyy.music.constants.HideVideoSongsKey
import com.diyy.music.constants.HideYoutubeShortsKey
import com.diyy.music.constants.InnerTubeCookieKey
import com.diyy.music.constants.KeepScreenOn
import com.diyy.music.constants.MiniPlayerOutlineKey
import com.diyy.music.constants.PauseListenHistoryKey
import com.diyy.music.constants.PauseOnMute
import com.diyy.music.constants.PauseSearchHistoryKey
import com.diyy.music.constants.PersistentQueueKey
import com.diyy.music.constants.PreventDuplicateTracksInQueueKey
import com.diyy.music.constants.PureBlackKey
import com.diyy.music.constants.RememberShuffleAndRepeatKey
import com.diyy.music.constants.ResumeOnBluetoothConnectKey
import com.diyy.music.constants.SkipSilenceInstantKey
import com.diyy.music.constants.SkipSilenceKey
import com.diyy.music.constants.StopMusicOnTaskClearKey
import com.diyy.music.constants.VisitorDataKey
import com.diyy.music.constants.YtmSyncKey
import com.diyy.music.discord.DiscordDefaults
import com.diyy.music.discord.DiscordRpcManager
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.FigmaDivider
import com.diyy.music.ui.component.FigmaGroupedList
import com.diyy.music.ui.component.FigmaSettingsRow
import com.diyy.music.ui.component.LiquidGlassBox
import com.diyy.music.ui.theme.DiyyRed
import com.diyy.music.utils.dataStore
import com.diyy.music.utils.rememberPreference
import com.diyy.music.viewmodels.AccountSettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    onOpenLogin: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    when (section) {
        "about" -> AboutDetail(onBack, modifier)
        "account" -> AccountDetail(onBack, onOpenLogin, modifier)
        "appearance" -> AppearanceDetail(onBack, modifier)
        "player" -> PlayerAudioDetail(onBack, modifier)
        "discord" -> DiscordDetail(onBack, modifier)
        else -> ToggleSettingsDetail(section, onBack, modifier)
    }
}

@Composable
private fun AccountDetail(
    onBack: () -> Unit,
    onOpenLogin: () -> Unit,
    modifier: Modifier,
    viewModel: AccountSettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storedCookie by context.dataStore.data.map { it[InnerTubeCookieKey].orEmpty() }
        .collectAsStateWithLifecycle(initialValue = "")
    val storedVisitor by context.dataStore.data.map { it[VisitorDataKey].orEmpty() }
        .collectAsStateWithLifecycle(initialValue = "")
    val storedSyncId by context.dataStore.data.map { it[DataSyncIdKey].orEmpty() }
        .collectAsStateWithLifecycle(initialValue = "")
    val storedName by context.dataStore.data.map { it[AccountNameKey].orEmpty() }
        .collectAsStateWithLifecycle(initialValue = "")
    val storedEmail by context.dataStore.data.map { it[AccountEmailKey].orEmpty() }
        .collectAsStateWithLifecycle(initialValue = "")
    val storedHandle by context.dataStore.data.map { it[AccountChannelHandleKey].orEmpty() }
        .collectAsStateWithLifecycle(initialValue = "")

    var cookie by rememberSaveable { mutableStateOf("") }
    var visitorData by rememberSaveable { mutableStateOf("") }
    var dataSyncId by rememberSaveable { mutableStateOf("") }
    var accountName by rememberSaveable { mutableStateOf("") }
    var accountEmail by rememberSaveable { mutableStateOf("") }
    var accountHandle by rememberSaveable { mutableStateOf("") }
    var showToken by rememberSaveable { mutableStateOf(false) }
    var showAdvanced by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(storedCookie, storedVisitor, storedSyncId, storedName, storedEmail, storedHandle) {
        if (cookie.isBlank()) cookie = storedCookie
        if (visitorData.isBlank()) visitorData = storedVisitor
        if (dataSyncId.isBlank()) dataSyncId = storedSyncId
        if (accountName.isBlank()) accountName = storedName
        if (accountEmail.isBlank()) accountEmail = storedEmail
        if (accountHandle.isBlank()) accountHandle = storedHandle
    }

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 28.dp)) {
        item { DiyyScreenHeader(title = "Account", onBack = onBack) }
        item {
            Text(
                text = "Login normally through Google. Manual cookie fields remain available under Advanced when needed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }
        item {
            LiquidGlassBox(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = 10.dp,
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = if (storedCookie.isBlank()) "Not connected" else storedName.ifBlank { "Account connected" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (storedCookie.isBlank()) MaterialTheme.colorScheme.onSurface else DiyyRed,
                    )
                    if (storedCookie.isNotBlank()) {
                        Text(
                            listOf(storedEmail, storedHandle).filter { it.isNotBlank() }.joinToString(" • ")
                                .ifBlank { "YouTube Music session is active" },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Button(
                        onClick = onOpenLogin,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DiyyRed),
                    ) {
                        Text(if (storedCookie.isBlank()) "Login with Google" else "Login with another account")
                    }

                    if (storedCookie.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    viewModel.logoutKeepData(context) { cookieValue ->
                                        cookie = cookieValue
                                        visitorData = ""
                                        dataSyncId = ""
                                        accountName = ""
                                        accountEmail = ""
                                        accountHandle = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            Text("Logout", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    TextButton(
                        onClick = { showAdvanced = !showAdvanced },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (showAdvanced) "Hide advanced session data" else "Show advanced session data", color = DiyyRed)
                    }

                    if (showAdvanced) {
                        TokenField(cookie, { cookie = it }, "Account cookie", secret = !showToken)
                        TextButton(onClick = { showToken = !showToken }) {
                            Text(if (showToken) "Hide cookie" else "Show cookie", color = DiyyRed)
                        }
                        TokenField(accountName, { accountName = it }, "Account name")
                        TokenField(accountEmail, { accountEmail = it }, "Email (optional)")
                        TokenField(accountHandle, { accountHandle = it }, "Channel handle (optional)")
                        TokenField(visitorData, { visitorData = it }, "Visitor data", secret = true)
                        TokenField(dataSyncId, { dataSyncId = it }, "Data sync ID", secret = true)
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
                            Text("Save manual session & restart")
                        }
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
private fun AppearanceDetail(onBack: () -> Unit, modifier: Modifier) {
    var darkMode by rememberPreference(DarkModeKey, DarkMode.AUTO.name)
    val settings = listOf(
        BooleanSetting("Pure black dark mode", "Use true black backgrounds when dark mode is active.", PureBlackKey, false),
        BooleanSetting("High refresh rate", "Use the smoothest display mode available.", EnableHighRefreshRateKey, true),
        BooleanSetting("Mini-player outline", "Show the subtle Liquid Glass edge highlight.", MiniPlayerOutlineKey, true),
        BooleanSetting("Hide player artwork", "Use a compact player without the large cover image.", HidePlayerThumbnailKey, false),
        BooleanSetting("Block screenshots", "Protect the app window from screenshots and screen recording.", DisableScreenshotKey, false),
    )

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 28.dp)) {
        item { DiyyScreenHeader("Appearance", onBack = onBack) }
        item { SettingsLabel("Theme") }
        item {
            ChoiceRow(
                choices = listOf(
                    DarkMode.AUTO.name to "System",
                    DarkMode.OFF.name to "Light",
                    DarkMode.ON.name to "Dark",
                ),
                selected = darkMode,
                onSelected = { darkMode = it },
                modifier = Modifier.padding(horizontal = 18.dp),
            )
        }
        item { SettingsLabel("Interface") }
        item { BooleanSettingsGroup(settings, Modifier.padding(horizontal = 18.dp)) }
    }
}

@Composable
private fun PlayerAudioDetail(onBack: () -> Unit, modifier: Modifier) {
    var audioQuality by rememberPreference(AudioQualityKey, AudioQuality.AUTO.name)
    var crossfadeEnabled by rememberPreference(CrossfadeEnabledKey, false)
    var crossfadeDuration by rememberPreference(CrossfadeDurationKey, 5f)

    val playbackSettings = listOf(
        BooleanSetting("Autoplay", "Continue with related music when the queue ends.", AutoplayKey, true),
        BooleanSetting("Persistent queue", "Restore the last queue after reopening DiyyMusic.", PersistentQueueKey, true),
        BooleanSetting("Remember shuffle and repeat", "Keep both modes between sessions.", RememberShuffleAndRepeatKey, true),
        BooleanSetting("Auto radio queue", "Load related tracks near the end of the queue.", AutoRadioQueueKey, true),
        BooleanSetting("Prevent duplicate tracks", "Avoid inserting the same song repeatedly.", PreventDuplicateTracksInQueueKey, true),
        BooleanSetting("Skip failed songs", "Move to the next track when playback cannot recover.", AutoSkipNextOnErrorKey, true),
        BooleanSetting("Stop when app is cleared", "Stop playback after removing DiyyMusic from recents.", StopMusicOnTaskClearKey, false),
    )
    val audioSettings = listOf(
        BooleanSetting("Normalize volume", "Balance loud and quiet tracks.", AudioNormalizationKey, true),
        BooleanSetting("Skip silence", "Skip silent sections when supported.", SkipSilenceKey, false),
        BooleanSetting("Instant silence skip", "Use more aggressive silence detection.", SkipSilenceInstantKey, false),
        BooleanSetting("Audio offload", "Let supported hardware handle audio decoding.", AudioOffload, false),
        BooleanSetting("Pause on mute", "Pause playback when volume reaches zero.", PauseOnMute, false),
        BooleanSetting("Resume on Bluetooth connect", "Continue playback after your device reconnects.", ResumeOnBluetoothConnectKey, false),
        BooleanSetting("Keep screen awake", "Prevent the display from sleeping while DiyyMusic is open.", KeepScreenOn, false),
    )

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 28.dp)) {
        item { DiyyScreenHeader("Player and Audio", onBack = onBack) }
        item { SettingsLabel("Streaming quality") }
        item {
            ChoiceRow(
                choices = listOf(
                    AudioQuality.AUTO.name to "Auto",
                    AudioQuality.LOW.name to "Data saver",
                    AudioQuality.HIGH.name to "High",
                ),
                selected = audioQuality,
                onSelected = { audioQuality = it },
                modifier = Modifier.padding(horizontal = 18.dp),
            )
        }
        item { SettingsLabel("Transitions") }
        item {
            FigmaGroupedList(modifier = Modifier.padding(horizontal = 18.dp)) {
                InlineSwitchRow(
                    title = "Crossfade",
                    subtitle = "Blend the end of one track into the next.",
                    checked = crossfadeEnabled,
                    onCheckedChange = { crossfadeEnabled = it },
                )
                if (crossfadeEnabled) {
                    FigmaDivider()
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text("Duration: ${crossfadeDuration.toInt()} seconds", fontWeight = FontWeight.Medium)
                        Slider(
                            value = crossfadeDuration,
                            onValueChange = { crossfadeDuration = it },
                            valueRange = 1f..12f,
                            steps = 10,
                        )
                    }
                    FigmaDivider()
                    PreferenceSwitchRow(
                        BooleanSetting("Gapless handoff", "Reduce silence before crossfade starts.", CrossfadeGaplessKey, true),
                    )
                }
            }
        }
        item { SettingsLabel("Playback") }
        item { BooleanSettingsGroup(playbackSettings, Modifier.padding(horizontal = 18.dp)) }
        item { SettingsLabel("Audio behavior") }
        item { BooleanSettingsGroup(audioSettings, Modifier.padding(horizontal = 18.dp)) }
    }
}

@Composable
private fun DiscordDetail(onBack: () -> Unit, modifier: Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val accessToken by DiscordRpcManager.accessTokenFlow.collectAsStateWithLifecycle()
    val connectionStatus by DiscordRpcManager.connectionStatus.collectAsStateWithLifecycle()
    val currentUser by DiscordRpcManager.currentUser.collectAsStateWithLifecycle()
    val lastError by DiscordRpcManager.lastError.collectAsStateWithLifecycle()
    var discordName by rememberPreference(DiscordNameKey, "")
    var discordUsername by rememberPreference(DiscordUsernameKey, "")
    var discordAvatar by rememberPreference(DiscordAvatarKey, "")
    var enabled by rememberPreference(EnableDiscordRPCKey, true)
    var button1Enabled by rememberPreference(DiscordButton1EnabledKey, true)
    var button2Enabled by rememberPreference(DiscordButton2EnabledKey, true)
    var advancedMode by rememberPreference(DiscordAdvancedModeKey, false)
    var activityType by rememberPreference(DiscordActivityTypeKey, DiscordDefaults.ACTIVITY_TYPE_LISTENING)
    var stateTemplate by rememberPreference(DiscordStateTemplateKey, DiscordDefaults.STATE_TEMPLATE)
    var detailsTemplate by rememberPreference(DiscordDetailsTemplateKey, DiscordDefaults.DETAILS_TEMPLATE)
    var userStatus by rememberPreference(DiscordUserStatusKey, DiscordDefaults.USER_STATUS)
    var advancedExpanded by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!DiscordRpcManager.isInitialized()) DiscordRpcManager.init(context.applicationContext)
    }
    LaunchedEffect(accessToken) {
        val token = accessToken
        if (!token.isNullOrBlank() && currentUser == null) {
            withContext(Dispatchers.IO) { DiscordRpcManager.fetchCurrentUser(token) }
        }
    }
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            discordName = user.name
            discordUsername = user.username
            discordAvatar = user.avatar.orEmpty()
        }
    }

    val connected = !accessToken.isNullOrBlank()
    val status = when (connectionStatus) {
        DiscordRpcManager.Status.Connected -> "Connected"
        DiscordRpcManager.Status.Authorizing -> "Waiting for Discord authorization"
        DiscordRpcManager.Status.Disconnected -> if (connected) "Authorized, reconnecting" else "Not connected"
    }
    val actionButtonsEnabled = button1Enabled || button2Enabled

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 36.dp)) {
        item { DiyyScreenHeader("Discord Rich Presence", onBack = onBack) }
        item {
            Text(
                text = "Show what you’re listening to on Discord.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
            )
        }
        item {
            LiquidGlassBox(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                shape = RoundedCornerShape(30.dp),
                elevation = 12.dp,
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (connected && discordAvatar.isNotBlank()) {
                                AsyncImage(
                                    model = discordAvatar,
                                    contentDescription = "Discord profile",
                                    modifier = Modifier.size(68.dp).clip(CircleShape),
                                )
                            } else {
                                Text("D", style = MaterialTheme.typography.headlineMedium, color = DiyyRed)
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (connected) discordName.ifBlank { "Discord connected" } else "Discord not connected",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                if (connected && discordUsername.isNotBlank()) "@$discordUsername • $status" else status,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (busy) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = DiyyRed)
                    } else if (!connected) {
                        Button(
                            onClick = {
                                val activity = findActivity(context)
                                if (activity != null) {
                                    busy = true
                                    DiscordRpcManager.authorize(activity) { success ->
                                        scope.launch {
                                            busy = false
                                            if (success) {
                                                enabled = true
                                                withContext(Dispatchers.IO) {
                                                    DiscordRpcManager.getAccessToken()?.let(DiscordRpcManager::fetchCurrentUser)
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DiyyRed),
                        ) {
                            Text("Connect Discord")
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    DiscordRpcManager.getAccessToken()?.let(DiscordRpcManager::reconnectWithToken)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp),
                            ) {
                                Text("Reconnect")
                            }
                            OutlinedButton(
                                onClick = {
                                    DiscordRpcManager.logout()
                                    enabled = false
                                    discordName = ""
                                    discordUsername = ""
                                    discordAvatar = ""
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp),
                            ) {
                                Text("Disconnect", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Text(
                        text = "This connection is used only by DiyyMusic. The name shown on Discord’s authorization page follows the Discord application ID used for the build.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (!lastError.isNullOrBlank()) {
                        Text(
                            text = "Discord error: $lastError",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        item { SettingsLabel("Presence") }
        item {
            FigmaGroupedList(modifier = Modifier.padding(horizontal = 18.dp)) {
                InlineSwitchRow(
                    title = "Enable Rich Presence",
                    subtitle = "Share the current track on Discord.",
                    checked = enabled,
                    enabled = connected,
                    onCheckedChange = {
                        enabled = it
                        DiscordRpcManager.notifySettingsChanged()
                    },
                )
                FigmaDivider()
                InlineSwitchRow(
                    title = "Show action buttons",
                    subtitle = "Show Listen and DiyyMusic buttons on the activity.",
                    checked = actionButtonsEnabled,
                    enabled = connected && enabled,
                    onCheckedChange = {
                        button1Enabled = it
                        button2Enabled = it
                        DiscordRpcManager.notifySettingsChanged()
                    },
                )
            }
        }

        item { SettingsLabel("Advanced") }
        item {
            LiquidGlassBox(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                shape = RoundedCornerShape(26.dp),
                elevation = 7.dp,
                onClick = { advancedExpanded = !advancedExpanded },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Customize presence", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (advancedExpanded) "Hide advanced options" else "Activity style and text format",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(if (advancedExpanded) "−" else "+", color = DiyyRed, style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
        item {
            AnimatedVisibility(
                visible = advancedExpanded,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 8 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 8 }),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FigmaGroupedList {
                        InlineSwitchRow(
                            title = "Custom presence text",
                            subtitle = "Use templates instead of the default song and artist layout.",
                            checked = advancedMode,
                            enabled = connected && enabled,
                            onCheckedChange = {
                                advancedMode = it
                                DiscordRpcManager.notifySettingsChanged()
                            },
                        )
                    }
                    Text("Activity style", fontWeight = FontWeight.SemiBold, color = DiyyRed)
                    ChoiceRow(
                        choices = listOf(
                            DiscordDefaults.ACTIVITY_TYPE_LISTENING to "Listening",
                            DiscordDefaults.ACTIVITY_TYPE_PLAYING to "Playing",
                            DiscordDefaults.ACTIVITY_TYPE_WATCHING to "Watching",
                        ),
                        selected = activityType,
                        onSelected = {
                            activityType = it
                            DiscordRpcManager.notifySettingsChanged()
                        },
                    )
                    Text("Discord status", fontWeight = FontWeight.SemiBold, color = DiyyRed)
                    ChoiceRow(
                        choices = listOf(
                            DiscordDefaults.USER_STATUS to "Online",
                            DiscordDefaults.STATUS_IDLE to "Idle",
                            DiscordDefaults.STATUS_DND to "DND",
                        ),
                        selected = userStatus,
                        onSelected = {
                            userStatus = it
                            DiscordRpcManager.notifySettingsChanged()
                        },
                    )
                    AnimatedVisibility(visible = advancedMode) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = detailsTemplate,
                                onValueChange = {
                                    detailsTemplate = it
                                    DiscordRpcManager.notifySettingsChanged()
                                },
                                label = { Text("Main text") },
                                supportingText = { Text("Example: {song.name}") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(18.dp),
                            )
                            OutlinedTextField(
                                value = stateTemplate,
                                onValueChange = {
                                    stateTemplate = it
                                    DiscordRpcManager.notifySettingsChanged()
                                },
                                label = { Text("Secondary text") },
                                supportingText = { Text("Example: {artist.name}") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun findActivity(context: Context): Activity? {
    var current: Context? = context
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

@Composable
private fun ChoiceRow(
    choices: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        choices.forEach { (value, label) ->
            val active = selected == value
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = if (active) DiyyRed else MaterialTheme.colorScheme.surfaceVariant,
                onClick = { onSelected(value) },
            ) {
                Text(
                    text = label,
                    color = if (active) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 13.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun BooleanSettingsGroup(
    settings: List<BooleanSetting>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onChanged: () -> Unit = {},
) {
    FigmaGroupedList(modifier = modifier) {
        settings.forEachIndexed { index, setting ->
            PreferenceSwitchRow(setting, enabled, onChanged)
            if (index != settings.lastIndex) FigmaDivider()
        }
    }
}

@Composable
private fun ToggleSettingsDetail(section: String, onBack: () -> Unit, modifier: Modifier) {
    val title = sectionTitle(section)
    val settings = settingsFor(section)
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 24.dp)) {
        item { DiyyScreenHeader(title, onBack = onBack) }
        item { SettingsLabel("DiyyMusic") }
        item { BooleanSettingsGroup(settings, Modifier.padding(horizontal = 18.dp)) }
    }
}

@Composable
private fun PreferenceSwitchRow(
    setting: BooleanSetting,
    enabled: Boolean = true,
    onChanged: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storedValue by context.dataStore.data
        .map { preferences -> preferences[setting.key] ?: setting.default }
        .collectAsStateWithLifecycle(initialValue = setting.default)
    val checked = if (setting.invert) !storedValue else storedValue

    InlineSwitchRow(
        title = setting.title,
        subtitle = setting.subtitle,
        checked = checked,
        enabled = enabled,
        onCheckedChange = { value ->
            scope.launch {
                context.dataStore.edit { preferences ->
                    preferences[setting.key] = if (setting.invert) !value else value
                }
                onChanged()
            }
        },
    )
}

@Composable
private fun InlineSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.45f),
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun SettingsLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        color = DiyyRed,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
    )
}

private fun sectionTitle(section: String): String = when (section) {
    "content" -> "Content"
    "storage" -> "Storage"
    "backup" -> "Backup and Restore"
    else -> section.replaceFirstChar { it.uppercase() }
}

private fun settingsFor(section: String): List<BooleanSetting> = when (section) {
    "content" -> listOf(
        BooleanSetting("Hide explicit music", "Filter explicit results and recommendations.", HideExplicitKey, false),
        BooleanSetting("Hide video songs", "Prefer audio releases in music lists.", HideVideoSongsKey, false),
        BooleanSetting("Hide short videos", "Remove short-form video playlists from results.", HideYoutubeShortsKey, false),
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
