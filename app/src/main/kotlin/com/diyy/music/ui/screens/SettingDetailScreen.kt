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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.diyy.music.constants.BackgroundGlowKey
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
import com.diyy.music.constants.DynamicAccentStrengthKey
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
import com.diyy.music.constants.GlassIntensityKey
import com.diyy.music.constants.GlassSoftnessKey
import com.diyy.music.constants.HideExplicitKey
import com.diyy.music.constants.HidePlayerThumbnailKey
import com.diyy.music.constants.HideVideoSongsKey
import com.diyy.music.constants.HideYoutubeShortsKey
import com.diyy.music.constants.InnerTubeCookieKey
import com.diyy.music.constants.KeepScreenOn
import com.diyy.music.constants.MaxSongCacheSizeKey
import com.diyy.music.constants.MiniPlayerOutlineKey
import com.diyy.music.constants.MotionSmoothnessKey
import com.diyy.music.constants.PauseListenHistoryKey
import com.diyy.music.constants.PauseOnMute
import com.diyy.music.constants.PauseSearchHistoryKey
import com.diyy.music.constants.PersistentQueueKey
import com.diyy.music.constants.PreventDuplicateTracksInQueueKey
import com.diyy.music.constants.PureBlackKey
import com.diyy.music.constants.ReduceMotionKey
import com.diyy.music.constants.RoundedArtworkKey
import com.diyy.music.constants.RememberShuffleAndRepeatKey
import com.diyy.music.constants.ResumeOnBluetoothConnectKey
import com.diyy.music.constants.SkipSilenceInstantKey
import com.diyy.music.constants.SkipSilenceKey
import com.diyy.music.constants.StopMusicOnTaskClearKey
import com.diyy.music.constants.VisitorDataKey
import com.diyy.music.constants.YtmSyncKey
import com.diyy.music.discord.DiscordDefaults
import com.diyy.music.discord.DiscordRpcManager
import com.diyy.music.eq.data.SavedEQProfile
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.FigmaDivider
import com.diyy.music.ui.component.FigmaGroupedList
import com.diyy.music.ui.component.FigmaSettingsRow
import com.diyy.music.ui.component.LiquidGlassBox
import com.diyy.music.ui.theme.DiyyMotionPreset
import com.diyy.music.ui.theme.DiyyRed
import com.diyy.music.utils.dataStore
import com.diyy.music.utils.rememberPreference
import com.diyy.music.viewmodels.AccountSettingsViewModel
import com.diyy.music.viewmodels.EqualizerSettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private data class BooleanSetting(
    val title: String,
    val subtitle: String,
    val key: Preferences.Key<Boolean>,
    val default: Boolean,
    val invert: Boolean = false,
    val icon: Int? = null,
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
    var pureBlack by rememberPreference(PureBlackKey, false)
    var accentStrengthPreference by rememberPreference(DynamicAccentStrengthKey, 0.74f)
    var motionSmoothness by rememberPreference(MotionSmoothnessKey, DiyyMotionPreset.SMOOTH.name)
    var glassIntensityPreference by rememberPreference(GlassIntensityKey, 0.60f)
    var glassSoftnessPreference by rememberPreference(GlassSoftnessKey, 0.45f)
    var accentStrength by remember { mutableStateOf(accentStrengthPreference) }
    var glassIntensity by remember { mutableStateOf(glassIntensityPreference) }
    var glassSoftness by remember { mutableStateOf(glassSoftnessPreference) }

    LaunchedEffect(accentStrengthPreference) { accentStrength = accentStrengthPreference }
    LaunchedEffect(glassIntensityPreference) { glassIntensity = glassIntensityPreference }
    LaunchedEffect(glassSoftnessPreference) { glassSoftness = glassSoftnessPreference }

    val selectedTheme = if (pureBlack) "PURE_BLACK" else darkMode

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 32.dp)) {
        item {
            DiyyScreenHeader(
                title = "Appearance",
                subtitle = "Customize how DiyyMusic looks and feels.",
                onBack = onBack,
            )
        }
        item { SettingsLabel("Theme mode") }
        item {
            FigmaGroupedList(modifier = Modifier.padding(horizontal = 18.dp)) {
                DropdownPreferenceRow(
                    title = "Theme mode",
                    subtitle = "Choose the display style used across DiyyMusic.",
                    icon = R.drawable.palette,
                    selected = selectedTheme,
                    options = listOf(
                        DarkMode.AUTO.name to "System",
                        DarkMode.OFF.name to "Light",
                        DarkMode.ON.name to "Dark",
                        "PURE_BLACK" to "Pure Black",
                    ),
                    onSelected = { selected ->
                        if (selected == "PURE_BLACK") {
                            darkMode = DarkMode.ON.name
                            pureBlack = true
                        } else {
                            darkMode = selected
                            pureBlack = false
                        }
                    },
                )
            }
        }

        item { SettingsLabel("Interface") }
        item {
            FigmaGroupedList(modifier = Modifier.padding(horizontal = 18.dp)) {
                PreferenceSwitchRow(
                    BooleanSetting(
                        "High refresh rate",
                        "Use the smoothest display mode available.",
                        EnableHighRefreshRateKey,
                        true,
                        icon = R.drawable.speed,
                    ),
                )
                FigmaDivider()
                SliderPreferenceRow(
                    title = "Dynamic accent strength",
                    subtitle = "Adjust the intensity of the pink accent color.",
                    icon = R.drawable.gradient,
                    value = accentStrength,
                    onValueChange = { accentStrength = it },
                    onValueChangeFinished = { accentStrengthPreference = accentStrength },
                    valueRange = 0.25f..1f,
                    valueText = "${(accentStrength * 100).roundToInt()}%",
                )
                FigmaDivider()
                PreferenceSwitchRow(
                    BooleanSetting(
                        "Rounded artwork",
                        "Use rounded corners for album artwork.",
                        RoundedArtworkKey,
                        true,
                        icon = R.drawable.crop,
                    ),
                )
            }
        }

        item { SettingsLabel("Animations") }
        item {
            FigmaGroupedList(modifier = Modifier.padding(horizontal = 18.dp)) {
                DropdownPreferenceRow(
                    title = "Motion smoothness",
                    subtitle = "Control the speed of interface animations.",
                    icon = R.drawable.slow_motion_video,
                    selected = motionSmoothness,
                    options = listOf(
                        DiyyMotionPreset.GENTLE.name to "Gentle",
                        DiyyMotionPreset.SMOOTH.name to "Smooth",
                        DiyyMotionPreset.SNAPPY.name to "Snappy",
                    ),
                    onSelected = { motionSmoothness = it },
                )
                FigmaDivider()
                PreferenceSwitchRow(
                    BooleanSetting(
                        "Reduce motion",
                        "Minimize animations across the app.",
                        ReduceMotionKey,
                        false,
                        icon = R.drawable.linear_scale,
                    ),
                )
            }
        }

        item { SettingsLabel("Mini-player") }
        item {
            BooleanSettingsGroup(
                settings = listOf(
                    BooleanSetting(
                        "Mini-player outline",
                        "Show the subtle Liquid Glass edge highlight.",
                        MiniPlayerOutlineKey,
                        true,
                        icon = R.drawable.dock_to_top,
                    ),
                    BooleanSetting(
                        "Hide player artwork",
                        "Use a compact player without the large cover image.",
                        HidePlayerThumbnailKey,
                        false,
                        icon = R.drawable.hide_image,
                    ),
                ),
                modifier = Modifier.padding(horizontal = 18.dp),
            )
        }

        item { SettingsLabel("Visual effects") }
        item {
            FigmaGroupedList(modifier = Modifier.padding(horizontal = 18.dp)) {
                SliderPreferenceRow(
                    title = "Glass intensity",
                    subtitle = "Control the strength of Liquid Glass surfaces.",
                    icon = R.drawable.contrast,
                    value = glassIntensity,
                    onValueChange = { glassIntensity = it },
                    onValueChangeFinished = { glassIntensityPreference = glassIntensity },
                    valueRange = 0.15f..1f,
                    valueText = "${(glassIntensity * 100).roundToInt()}%",
                )
                FigmaDivider()
                SliderPreferenceRow(
                    title = "Glass softness",
                    subtitle = "Adjust the softness of glass borders and depth.",
                    icon = R.drawable.discover_tune,
                    value = glassSoftness,
                    onValueChange = { glassSoftness = it },
                    onValueChangeFinished = { glassSoftnessPreference = glassSoftness },
                    valueRange = 0.10f..1f,
                    valueText = "${(glassSoftness * 100).roundToInt()}%",
                )
                FigmaDivider()
                PreferenceSwitchRow(
                    BooleanSetting(
                        "Background glow",
                        "Enable a subtle accent glow behind key elements.",
                        BackgroundGlowKey,
                        true,
                        icon = R.drawable.gradient,
                    ),
                )
                FigmaDivider()
                PreferenceSwitchRow(
                    BooleanSetting(
                        "Block screenshots",
                        "Protect the app window from screenshots and recording.",
                        DisableScreenshotKey,
                        false,
                        icon = R.drawable.security,
                    ),
                )
            }
        }
    }
}

@Composable
private fun PlayerAudioDetail(
    onBack: () -> Unit,
    modifier: Modifier,
    equalizerViewModel: EqualizerSettingsViewModel = hiltViewModel(),
) {
    var audioQuality by rememberPreference(AudioQualityKey, AudioQuality.AUTO.name)
    var crossfadeEnabled by rememberPreference(CrossfadeEnabledKey, false)
    var crossfadeDurationPreference by rememberPreference(CrossfadeDurationKey, 5f)
    var crossfadeDuration by remember { mutableStateOf(crossfadeDurationPreference) }
    var cacheSize by rememberPreference(MaxSongCacheSizeKey, 1024)
    var showEqualizerBands by rememberSaveable { mutableStateOf(false) }
    val activeEqProfile by equalizerViewModel.activeProfile.collectAsStateWithLifecycle()
    val equalizerOptions = remember(equalizerViewModel.presets, activeEqProfile?.id) {
        buildList {
            add(EqualizerSettingsViewModel.FLAT_ID to "Off / Flat")
            addAll(equalizerViewModel.presets.map { it.id to it.name })
            val custom = activeEqProfile
            if (custom != null && none { it.first == custom.id }) add(custom.id to custom.name)
        }
    }

    LaunchedEffect(crossfadeDurationPreference) { crossfadeDuration = crossfadeDurationPreference }

    val playbackSettings = listOf(
        BooleanSetting("Autoplay", "Continue with related music when the queue ends.", AutoplayKey, true, icon = R.drawable.play),
        BooleanSetting("Persistent queue", "Restore the last queue after reopening DiyyMusic.", PersistentQueueKey, true, icon = R.drawable.queue_music),
        BooleanSetting("Remember shuffle and repeat", "Keep both playback modes between sessions.", RememberShuffleAndRepeatKey, true, icon = R.drawable.repeat),
        BooleanSetting("Auto radio queue", "Load related tracks near the end of the queue.", AutoRadioQueueKey, true, icon = R.drawable.radio),
        BooleanSetting("Prevent duplicate tracks", "Avoid inserting the same song repeatedly.", PreventDuplicateTracksInQueueKey, true, icon = R.drawable.content_copy),
        BooleanSetting("Skip failed songs", "Move on when playback cannot recover.", AutoSkipNextOnErrorKey, true, icon = R.drawable.skip_next),
        BooleanSetting("Stop when app is cleared", "Stop playback after removing DiyyMusic from recents.", StopMusicOnTaskClearKey, false, icon = R.drawable.close),
    )
    val audioSettings = listOf(
        BooleanSetting("Normalize volume", "Balance loud and quiet tracks.", AudioNormalizationKey, true, icon = R.drawable.graphic_eq),
        BooleanSetting("Skip silence", "Automatically skip silent sections when supported.", SkipSilenceKey, false, icon = R.drawable.fast_forward),
        BooleanSetting("Instant silence skip", "Use more aggressive silence detection.", SkipSilenceInstantKey, false, icon = R.drawable.speed),
        BooleanSetting("Audio offload", "Let supported hardware reduce CPU usage.", AudioOffload, false, icon = R.drawable.equalizer),
        BooleanSetting("Pause on mute", "Pause playback when device volume reaches zero.", PauseOnMute, false, icon = R.drawable.volume_off_pause),
        BooleanSetting("Resume on Bluetooth", "Continue playback after a device reconnects.", ResumeOnBluetoothConnectKey, false, icon = R.drawable.bluetooth),
        BooleanSetting("Keep screen awake", "Prevent the display from sleeping while the player is open.", KeepScreenOn, false, icon = R.drawable.time_auto),
    )

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 32.dp)) {
        item {
            DiyyScreenHeader(
                title = "Player & Audio",
                subtitle = "Fine-tune how DiyyMusic plays and sounds.",
                onBack = onBack,
            )
        }
        item { SettingsLabel("Streaming quality") }
        item {
            FigmaGroupedList(modifier = Modifier.padding(horizontal = 18.dp)) {
                DropdownPreferenceRow(
                    title = "Streaming quality",
                    subtitle = "Choose the balance between data usage and sound quality.",
                    icon = R.drawable.equalizer,
                    selected = audioQuality,
                    options = listOf(
                        AudioQuality.AUTO.name to "Auto",
                        AudioQuality.LOW.name to "Data saver",
                        AudioQuality.HIGH.name to "High",
                    ),
                    onSelected = { audioQuality = it },
                )
            }
        }

        item { SettingsLabel("Crossfade") }
        item {
            FigmaGroupedList(modifier = Modifier.padding(horizontal = 18.dp)) {
                InlineSwitchRow(
                    title = "Crossfade",
                    subtitle = "Blend the end of one track into the next.",
                    checked = crossfadeEnabled,
                    icon = R.drawable.graphic_eq,
                    onCheckedChange = { crossfadeEnabled = it },
                )
                AnimatedVisibility(visible = crossfadeEnabled) {
                    Column {
                        FigmaDivider()
                        SliderPreferenceRow(
                            title = "Duration",
                            subtitle = "How long the transition should overlap.",
                            icon = R.drawable.timer,
                            value = crossfadeDuration,
                            onValueChange = { crossfadeDuration = it },
                            onValueChangeFinished = { crossfadeDurationPreference = crossfadeDuration },
                            valueRange = 1f..12f,
                            steps = 10,
                            valueText = "${crossfadeDuration.roundToInt()} sec",
                        )
                        FigmaDivider()
                        PreferenceSwitchRow(
                            BooleanSetting(
                                "Gapless handoff",
                                "Reduce silence before the next track starts.",
                                CrossfadeGaplessKey,
                                true,
                                icon = R.drawable.cached,
                            ),
                        )
                    }
                }
            }
        }

        item { SettingsLabel("Playback") }
        item { BooleanSettingsGroup(playbackSettings, Modifier.padding(horizontal = 18.dp)) }
        item { SettingsLabel("Audio behavior") }
        item { BooleanSettingsGroup(audioSettings, Modifier.padding(horizontal = 18.dp)) }

        item { SettingsLabel("Equalizer") }
        item {
            FigmaGroupedList(modifier = Modifier.padding(horizontal = 18.dp)) {
                DropdownPreferenceRow(
                    title = "Equalizer preset",
                    subtitle = activeEqProfile?.let {
                        "${it.bands.size} bands • ${formatGain(it.preamp)} dB preamp"
                    } ?: "Flat response with no EQ processing.",
                    icon = R.drawable.equalizer,
                    selected = activeEqProfile?.id ?: EqualizerSettingsViewModel.FLAT_ID,
                    options = equalizerOptions,
                    onSelected = equalizerViewModel::selectPreset,
                )
                if (activeEqProfile != null) {
                    FigmaDivider()
                    TextButton(
                        onClick = { showEqualizerBands = !showEqualizerBands },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (showEqualizerBands) "Hide band controls" else "Tune equalizer bands",
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    AnimatedVisibility(visible = showEqualizerBands) {
                        activeEqProfile?.let { profile ->
                            EqualizerBandEditor(
                                profile = profile,
                                onBandGainChanged = equalizerViewModel::updateBandGain,
                                onPreampChanged = equalizerViewModel::updatePreamp,
                            )
                        }
                    }
                }
            }
        }

        item { SettingsLabel("Downloads & cache") }
        item {
            FigmaGroupedList(modifier = Modifier.padding(horizontal = 18.dp)) {
                PreferenceSwitchRow(
                    BooleanSetting(
                        "Song cache",
                        "Cache streamed audio for faster replay.",
                        EnableSongCacheKey,
                        true,
                        icon = R.drawable.offline,
                    ),
                )
                FigmaDivider()
                DropdownPreferenceRow(
                    title = "Cache size",
                    subtitle = "Set the maximum storage used by temporary audio.",
                    icon = R.drawable.storage,
                    selected = cacheSize.toString(),
                    options = listOf(
                        "256" to "256 MB",
                        "512" to "512 MB",
                        "1024" to "1 GB",
                        "2048" to "2 GB",
                        "4096" to "4 GB",
                    ),
                    onSelected = { selected -> cacheSize = selected.toIntOrNull() ?: 1024 },
                )
            }
        }
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
    var enabled by rememberPreference(EnableDiscordRPCKey, false)
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
    val presenceReady = connectionStatus == DiscordRpcManager.Status.Connected

    LaunchedEffect(presenceReady) {
        if (!presenceReady && enabled) enabled = false
    }
    val status = when (connectionStatus) {
        DiscordRpcManager.Status.Connected -> "Connected"
        DiscordRpcManager.Status.Linked -> "Account linked"
        DiscordRpcManager.Status.Authorizing -> "Connecting…"
        DiscordRpcManager.Status.Disconnected -> if (connected) "Account linked" else "Not connected"
    }
    val actionButtonsEnabled = button1Enabled || button2Enabled
    val discordErrorMessage = when (lastError) {
        "discord_error_social_sdk_unavailable" -> "Discord account is linked, but this build has no active Rich Presence transport."
        "discord_error_state_mismatch" -> "Discord returned an invalid authorization state. Close the browser and reconnect."
        "discord_error_invalid_scope" -> "Discord rejected one of the requested permissions. This build now requests only the standard identify scope by default."
        "discord_error_public_client_required" -> "Enable Public Client in Discord Developer Portal → OAuth2, then reconnect."
        "discord_error_oauth_rejected" -> "Discord rejected the authorization request. Check the application OAuth2 settings."
        "discord_error_no_browser" -> "No browser was found to complete Discord authorization."
        "discord_error_token_refresh_failed" -> "The Discord session expired. Disconnect, then connect again."
        "discord_error_loopback_timeout" -> "Discord did not finish the local callback in time. Close the browser and retry."
        null, "" -> null
        else -> lastError
    }

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 36.dp)) {
        item {
            DiyyScreenHeader(
                title = "Discord Rich Presence",
                subtitle = "Show what you’re listening to on Discord.",
                onBack = onBack,
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
                                Text("Refresh account")
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

                    if (connected) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (presenceReady) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                            },
                        ) {
                            Text(
                                text = if (presenceReady) {
                                    "Rich Presence is ready and will follow the current track."
                                } else {
                                    "Account linked. Rich Presence transport is not available in this Android build."
                                },
                                modifier = Modifier.padding(12.dp),
                                color = if (presenceReady) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    if (!discordErrorMessage.isNullOrBlank()) {
                        Text(
                            text = discordErrorMessage,
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
                    subtitle = when {
                        !connected -> "Link a Discord account first."
                        presenceReady -> "Share the current track on Discord."
                        else -> "Unavailable until the Discord presence transport is included."
                    },
                    checked = enabled && presenceReady,
                    enabled = connected && presenceReady,
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
                    enabled = connected && presenceReady && enabled,
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
                            enabled = connected && presenceReady && enabled,
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
private fun EqualizerBandEditor(
    profile: SavedEQProfile,
    onBandGainChanged: (Int, Double) -> Unit,
    onPreampChanged: (Double) -> Unit,
) {
    var gains by remember(profile.id, profile.bands) {
        mutableStateOf(profile.bands.map { it.gain.toFloat() })
    }
    var preamp by remember(profile.id, profile.preamp) { mutableStateOf(profile.preamp.toFloat()) }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Preamp",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = preamp,
                onValueChange = { preamp = it },
                onValueChangeFinished = { onPreampChanged(preamp.toDouble()) },
                valueRange = -12f..6f,
                steps = 35,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${formatGain(preamp.toDouble())} dB",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.width(64.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
            )
        }

        profile.bands.forEachIndexed { index, band ->
            val value = gains.getOrElse(index) { band.gain.toFloat() }
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatFrequency(band.frequency),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(54.dp),
                    )
                    Slider(
                        value = value,
                        onValueChange = { updated ->
                            gains = gains.toMutableList().also { list ->
                                if (index in list.indices) list[index] = updated
                            }
                        },
                        onValueChangeFinished = { onBandGainChanged(index, value.toDouble()) },
                        valueRange = -12f..12f,
                        steps = 47,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = formatGain(value.toDouble()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(46.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    )
                }
            }
        }
    }
}

private fun formatFrequency(frequency: Double): String =
    if (frequency >= 1000.0) "${(frequency / 1000.0).let { if (it % 1.0 == 0.0) it.toInt() else it }}k"
    else frequency.toInt().toString()

private fun formatGain(gain: Double): String = String.format(java.util.Locale.US, "%+.1f", gain)

@Composable
private fun DropdownPreferenceRow(
    title: String,
    subtitle: String,
    icon: Int,
    selected: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second
        ?: options.firstOrNull()?.second.orEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(21.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = selectedLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            painter = painterResource(R.drawable.expand_more),
            contentDescription = "Open $title options",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }

    if (expanded) {
        Dialog(
            onDismissRequest = { expanded = false },
            properties = DialogProperties(usePlatformDefaultWidth = true),
        ) {
            LiquidGlassBox(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                elevation = 10.dp,
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                    options.forEach { (value, label) ->
                        val active = selected == value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (active) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.30f),
                                )
                                .clickable {
                                    onSelected(value)
                                    expanded = false
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = label,
                                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                            )
                            if (active) {
                                Icon(
                                    painter = painterResource(R.drawable.check),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
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
        icon = setting.icon,
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
    icon: Int? = null,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = DiyyRed,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
        }
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
private fun SliderPreferenceRow(
    title: String,
    subtitle: String,
    icon: Int,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = DiyyRed,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DiyyRed,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                onValueChangeFinished = onValueChangeFinished,
                valueRange = valueRange,
                steps = steps,
                modifier = Modifier.fillMaxWidth(),
            )
        }
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
