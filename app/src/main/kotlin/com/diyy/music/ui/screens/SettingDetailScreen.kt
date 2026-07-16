package com.diyy.music.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
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
import com.diyy.music.constants.MixModeKey
import com.diyy.music.constants.CrossfadeGaplessKey
import com.diyy.music.constants.DarkMode
import com.diyy.music.constants.DarkModeKey
import com.diyy.music.constants.DynamicAccentStrengthKey
import com.diyy.music.constants.DataSyncIdKey
import com.diyy.music.constants.DisableScreenshotKey
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
import kotlinx.coroutines.delay
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

private const val MIX_SMART_CROSSFADE_SECONDS = 8f

@Composable
private fun PlayerAudioDetail(
    onBack: () -> Unit,
    modifier: Modifier,
    equalizerViewModel: EqualizerSettingsViewModel = hiltViewModel(),
) {
    var audioQuality by rememberPreference(AudioQualityKey, AudioQuality.AUTO.name)
    var mixEnabled by rememberPreference(MixModeKey, false)
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

    // Mix and manual Crossfade both control how one track hands off to the next, so they
    // can't both be in charge at once. Turning Mix on takes over the transition with its
    // own smart, longer blend and locks the manual crossfade controls; turning it off
    // hands control back to whatever the user had set manually.
    LaunchedEffect(mixEnabled) {
        if (mixEnabled) {
            crossfadeEnabled = true
            crossfadeDurationPreference = MIX_SMART_CROSSFADE_SECONDS
            crossfadeDuration = MIX_SMART_CROSSFADE_SECONDS
        }
    }

    val playbackSettings = listOf(
        BooleanSetting("Autoplay", "Continue with related music when the queue ends.", AutoplayKey, true, icon = R.drawable.play),
        BooleanSetting("Persistent queue", "Restore the last queue after reopening DiyyMusic.", PersistentQueueKey, true, icon = R.drawable.queue_music),
        BooleanSetting("Remember shuffle and repeat", "Keep both playback modes between sessions.", RememberShuffleAndRepeatKey, true, icon = R.drawable.repeat),
        BooleanSetting(
            "Smart Radio continuation",
            "Keep playing personalized recommendations after a playlist or queue ends.",
            AutoRadioQueueKey,
            true,
            icon = R.drawable.radio,
        ),
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

        item { SettingsLabel("Mix") }
        item {
            FigmaGroupedList(modifier = Modifier.padding(horizontal = 18.dp)) {
                InlineSwitchRow(
                    title = "Mix",
                    subtitle = "Automatically blend songs together, like a DJ. Takes over the transition, so manual Crossfade is locked while this is on.",
                    checked = mixEnabled,
                    icon = R.drawable.shuffle,
                    onCheckedChange = { mixEnabled = it },
                )
            }
        }

        item { SettingsLabel("Crossfade") }
        item {
            FigmaGroupedList(modifier = Modifier.padding(horizontal = 18.dp)) {
                InlineSwitchRow(
                    title = "Crossfade",
                    subtitle = if (mixEnabled) {
                        "Controlled automatically by Mix right now."
                    } else {
                        "Blend the end of one track into the next."
                    },
                    checked = crossfadeEnabled,
                    enabled = !mixEnabled,
                    icon = R.drawable.graphic_eq,
                    onCheckedChange = { crossfadeEnabled = it },
                )
                AnimatedVisibility(visible = crossfadeEnabled) {
                    Column {
                        FigmaDivider()
                        SliderPreferenceRow(
                            title = "Duration",
                            subtitle = if (mixEnabled) "Set automatically while Mix is on." else "How long the transition should overlap.",
                            icon = R.drawable.timer,
                            value = crossfadeDuration,
                            onValueChange = { crossfadeDuration = it },
                            onValueChangeFinished = { crossfadeDurationPreference = crossfadeDuration },
                            valueRange = 1f..12f,
                            steps = 10,
                            enabled = !mixEnabled,
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
                                onReset = equalizerViewModel::resetActiveProfile,
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
private fun EqualizerBandEditor(
    profile: SavedEQProfile,
    onBandGainChanged: (Int, Double) -> Unit,
    onPreampChanged: (Double) -> Unit,
    onReset: () -> Unit,
) {
    var gains by remember(profile.id, profile.bands) {
        mutableStateOf(profile.bands.map { it.gain.toFloat() })
    }
    var preamp by remember(profile.id, profile.preamp) { mutableStateOf(profile.preamp.toFloat()) }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        EqCurveGraph(
            gains = gains,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            profile.bands.forEachIndexed { index, band ->
                val value = gains.getOrElse(index) { band.gain.toFloat() }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = formatGain(value.toDouble()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(4.dp))
                    VerticalEqSlider(
                        value = value,
                        onValueChange = { updated ->
                            gains = gains.toMutableList().also { list ->
                                if (index in list.indices) list[index] = updated
                            }
                        },
                        onValueChangeFinished = { onBandGainChanged(index, value.toDouble()) },
                        valueRange = -12f..12f,
                        height = 130.dp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formatFrequency(band.frequency),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        FigmaDivider()

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

        TextButton(
            onClick = {
                gains = List(profile.bands.size) { 0f }
                preamp = 0f
                onReset()
            },
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("Reset to flat")
        }
    }
}

/**
 * A rotated [Slider] that reads top-to-bottom like a hardware graphic-EQ fader.
 */
@Composable
private fun VerticalEqSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    height: Dp,
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        modifier = Modifier
            .width(height)
            .graphicsLayer { rotationZ = 270f }
            .layout { measurable, constraints ->
                val placeable = measurable.measure(
                    Constraints(
                        minWidth = constraints.minHeight,
                        maxWidth = constraints.maxHeight,
                        minHeight = constraints.minWidth,
                        maxHeight = constraints.maxWidth,
                    ),
                )
                layout(placeable.height, placeable.width) {
                    placeable.place(
                        x = -(placeable.width / 2 - placeable.height / 2),
                        y = -(placeable.height / 2 - placeable.width / 2),
                    )
                }
            },
    )
}

/**
 * Smooth response-curve visualization for the current band gains, similar to the
 * curve shown on most dedicated graphic equalizers.
 */
@Composable
private fun EqCurveGraph(gains: List<Float>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val fillBrush = Brush.verticalGradient(
        listOf(lineColor.copy(alpha = 0.28f), lineColor.copy(alpha = 0f)),
    )
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)

    Canvas(modifier = modifier) {
        if (gains.isEmpty()) return@Canvas
        val maxGain = 12f
        val stepX = size.width / (gains.size - 1).coerceAtLeast(1)

        fun yFor(gain: Float): Float {
            val fraction = (gain / maxGain).coerceIn(-1f, 1f)
            return size.height / 2f - (fraction * size.height / 2f)
        }

        // Zero-dB reference line
        drawLine(
            color = gridColor,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 1.dp.toPx(),
        )

        val points = gains.mapIndexed { index, gain -> Offset(index * stepX, yFor(gain)) }

        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val current = points[i]
                val next = points[i + 1]
                val midX = (current.x + next.x) / 2f
                cubicTo(midX, current.y, midX, next.y, next.x, next.y)
            }
        }

        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(points.last().x, size.height)
            lineTo(points.first().x, size.height)
            close()
        }

        drawPath(fillPath, brush = fillBrush)
        drawPath(linePath, color = lineColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

        points.forEach { point ->
            drawCircle(color = lineColor, radius = 3.dp.toPx(), center = point)
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
        var contentVisible by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) { contentVisible = true }

        fun closeDialog() {
            contentVisible = false
            scope.launch {
                delay(160)
                expanded = false
            }
        }

        Dialog(
            onDismissRequest = { closeDialog() },
            properties = DialogProperties(usePlatformDefaultWidth = true),
        ) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(180)) + scaleIn(
                    initialScale = 0.88f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ),
                exit = fadeOut(tween(140)) + scaleOut(targetScale = 0.9f, animationSpec = tween(140)),
            ) {
                LiquidGlassBox(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    elevation = 10.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                        options.forEach { (value, label) ->
                            val active = selected == value
                            val rowColor by animateColorAsState(
                                targetValue = if (active) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.30f)
                                },
                                animationSpec = tween(180),
                                label = "themeRowColor",
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(rowColor)
                                    .border(
                                        width = 1.dp,
                                        color = if (active) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                        },
                                        shape = RoundedCornerShape(18.dp),
                                    )
                                    .clickable {
                                        onSelected(value)
                                        closeDialog()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = 2.dp,
                                            color = if (active) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            },
                                            shape = CircleShape,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (active) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                        )
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    text = label,
                                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
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
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
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
                enabled = enabled,
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
