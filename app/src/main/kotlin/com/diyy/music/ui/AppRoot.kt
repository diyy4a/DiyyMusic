package com.diyy.music.ui

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.diyy.music.constants.AccountChannelHandleKey
import com.diyy.music.constants.AccountEmailKey
import com.diyy.music.constants.AccountNameKey
import com.diyy.music.constants.DataSyncIdKey
import com.diyy.music.constants.DismissedUpdateVersionKey
import com.diyy.music.constants.InnerTubeCookieKey
import com.diyy.music.db.MusicDatabase
import com.diyy.music.models.MediaMetadata
import com.diyy.music.playback.DownloadUtil
import com.diyy.music.playback.PlayerConnection
import com.diyy.music.ui.component.DiyyBottomNavigation
import com.diyy.music.ui.component.DiyyBrandMark
import com.diyy.music.ui.component.DiyyMiniPlayer
import com.diyy.music.ui.component.DiyyPageMotion
import com.diyy.music.ui.component.UpdateAvailableDialog
import com.diyy.music.ui.screens.CollectionScreen
import com.diyy.music.ui.screens.HistoryScreen
import com.diyy.music.ui.screens.LibraryDisplayScreen
import com.diyy.music.ui.screens.LibraryScreen
import com.diyy.music.ui.screens.LoginScreen
import com.diyy.music.ui.screens.ListenNowScreen
import com.diyy.music.ui.screens.PlayerScreen
import com.diyy.music.ui.screens.ProfileScreen
import com.diyy.music.ui.screens.SearchScreen
import com.diyy.music.ui.screens.SettingDetailScreen
import com.diyy.music.ui.screens.SettingsScreen
import com.diyy.music.ui.theme.DiyyMotionPreset
import com.diyy.music.ui.theme.DiyyRed
import com.diyy.music.ui.theme.LocalDiyyUiConfig
import com.diyy.music.utils.dataStore
import com.diyy.music.utils.ReleaseInfo
import com.diyy.music.utils.Updater
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun DiyyMusicRoot(
    database: MusicDatabase,
    downloadUtil: DownloadUtil,
    playerConnection: PlayerConnection?,
    requestedRoute: String? = null,
    onRequestedRouteConsumed: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: DiyyRoutes.MAIN_TABS
    val showBottomBar = currentRoute != DiyyRoutes.PLAYER && currentRoute != DiyyRoutes.LOGIN
    var currentMainTab by remember { mutableStateOf(DiyyMainTab.LISTEN_NOW) }
    val currentTab = currentMainTab
    val goToTab: (DiyyMainTab) -> Unit = { tab ->
        navigateToTab(navController, tab) { currentMainTab = it }
    }

    val metadataState = playerConnection?.mediaMetadata?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf<MediaMetadata?>(null) }
    val metadata by metadataState
    val playingState = playerConnection?.isPlaying?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(false) }
    val isPlaying by playingState
    val progress = rememberMiniPlayerProgress(playerConnection)

    var searchSeed by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showStartupSplash by remember { mutableStateOf(true) }
    var splashTargetAlpha by remember { mutableFloatStateOf(1f) }
    val splashAlpha by animateFloatAsState(
        targetValue = splashTargetAlpha,
        animationSpec = tween(280),
        label = "splashFade",
        finishedListener = { value -> if (value <= 0f) showStartupSplash = false },
    )

    LaunchedEffect(Unit) {
        delay(700)
        splashTargetAlpha = 0f
    }

    if (showStartupSplash) {
        DiyyStartupSplash(modifier = Modifier.graphicsLayer { alpha = splashAlpha })
        return
    }

    var availableUpdate by remember { mutableStateOf<ReleaseInfo?>(null) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        runCatching {
            val result = Updater.checkForUpdate()
            val (release, hasUpdate) = result.getOrNull() ?: return@LaunchedEffect
            if (release == null || !hasUpdate) return@LaunchedEffect

            val dismissedVersion = context.dataStore.data
                .map { it[DismissedUpdateVersionKey] }
                .first()
            if (dismissedVersion != release.versionName) {
                availableUpdate = release
            }
        }
    }

    LaunchedEffect(requestedRoute) {
        when (requestedRoute) {
            "search" -> goToTab(DiyyMainTab.SEARCH)
            "library" -> goToTab(DiyyMainTab.LIBRARY)
            "profile" -> goToTab(DiyyMainTab.PROFILE)
            "home", "listen_now" -> goToTab(DiyyMainTab.LISTEN_NOW)
            "player" -> navController.navigate(DiyyRoutes.PLAYER)
            else -> if (requestedRoute?.startsWith("collection/") == true) {
                val type = requestedRoute.substringAfter("collection/")
                openCollection(navController, type)
            }
        }
        if (requestedRoute != null) onRequestedRouteConsumed()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                modifier = Modifier.background(Color.Transparent),
                enter = fadeIn(tween(220)) + slideInVertically(
                    animationSpec = tween(220),
                    initialOffsetY = { it / 2 },
                ),
                exit = fadeOut(tween(160)) + slideOutVertically(
                    animationSpec = tween(160),
                    targetOffsetY = { it / 2 },
                ),
            ) {
                Column(modifier = Modifier.background(Color.Transparent)) {
                    if (metadata != null) {
                        DiyyMiniPlayer(
                            metadata = metadata,
                            isPlaying = isPlaying,
                            progress = progress,
                            onOpen = { navController.navigate(DiyyRoutes.PLAYER) },
                            onPlayPause = { playerConnection?.togglePlayPause() },
                            onNext = { playerConnection?.seekToNext() },
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    DiyyBottomNavigation(
                        selected = currentTab,
                        onSelected = { goToTab(it) },
                    )
                }
            }
        },
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        val animatedBottomPadding by animateDpAsState(
            targetValue = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp,
            animationSpec = tween(180),
            label = "navBottomPadding",
        )
        NavHost(
            navController = navController,
            startDestination = DiyyRoutes.MAIN_TABS,
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                start = innerPadding.calculateStartPadding(layoutDirection),
                end = innerPadding.calculateEndPadding(layoutDirection),
                bottom = animatedBottomPadding,
            ),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            composable(DiyyRoutes.MAIN_TABS) {
                val ui = LocalDiyyUiConfig.current
                val duration = when (ui.motionPreset) {
                    DiyyMotionPreset.GENTLE -> 280
                    DiyyMotionPreset.SMOOTH -> 200
                    DiyyMotionPreset.SNAPPY -> 130
                }
                AnimatedContent(
                    targetState = currentMainTab,
                    transitionSpec = {
                        val forward = targetState.ordinal >= initialState.ordinal
                        if (ui.reduceMotion) {
                            fadeIn(tween(90)) togetherWith fadeOut(tween(70))
                        } else {
                            (
                                fadeIn(tween(duration)) +
                                    slideInHorizontally(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = if (ui.motionPreset == DiyyMotionPreset.GENTLE) {
                                                Spring.StiffnessLow
                                            } else {
                                                Spring.StiffnessMediumLow
                                            },
                                        ),
                                        initialOffsetX = { if (forward) it / 5 else -it / 5 },
                                    )
                                ) togetherWith (
                                fadeOut(tween((duration * 0.6f).toInt())) +
                                    slideOutHorizontally(
                                        targetOffsetX = { if (forward) -it / 8 else it / 8 },
                                        animationSpec = tween((duration * 0.7f).toInt()),
                                    )
                                )
                        }
                    },
                    label = "mainTabContent",
                ) { tab ->
                    when (tab) {
                        DiyyMainTab.LISTEN_NOW -> ListenNowScreen(
                            playerConnection = playerConnection,
                            onOpenProfile = { goToTab(DiyyMainTab.PROFILE) },
                            onOpenHistory = { navController.navigate(DiyyRoutes.HISTORY) },
                            onOpenCollection = { openCollection(navController, it) },
                        )
                        DiyyMainTab.SEARCH -> SearchScreen(
                            playerConnection = playerConnection,
                            initialQuery = searchSeed,
                            onOpenCollection = { openCollection(navController, it) },
                        )
                        DiyyMainTab.LIBRARY -> LibraryScreen(
                            database = database,
                            playerConnection = playerConnection,
                            onOpenProfile = { goToTab(DiyyMainTab.PROFILE) },
                            onOpenHistory = { navController.navigate(DiyyRoutes.HISTORY) },
                            onOpenCollection = { openCollection(navController, it) },
                            onOpenDisplayOptions = { navController.navigate(DiyyRoutes.DISPLAY_OPTIONS) },
                        )
                        DiyyMainTab.PROFILE -> ProfileScreen(
                            database = database,
                            onBack = null,
                            onOpenFeature = { section -> navController.navigate("feature/${Uri.encode(section)}") },
                            onOpenCollection = { openCollection(navController, it) },
                            onLogout = {
                                scope.launch {
                                    context.dataStore.edit { preferences ->
                                        preferences.remove(InnerTubeCookieKey)
                                        preferences.remove(DataSyncIdKey)
                                        preferences.remove(AccountNameKey)
                                        preferences.remove(AccountEmailKey)
                                        preferences.remove(AccountChannelHandleKey)
                                    }
                                    goToTab(DiyyMainTab.LISTEN_NOW)
                                }
                            },
                        )
                    }
                }
            }
            composable(DiyyRoutes.PLAYER) {
                DiyyPageMotion {
                    PlayerScreen(
                        playerConnection = playerConnection,
                        downloadUtil = downloadUtil,
                        onBack = navController::navigateUp,
                    )
                }
            }
            composable(DiyyRoutes.LOGIN) {
                DiyyPageMotion { LoginScreen(onBack = navController::navigateUp) }
            }
            composable(
                route = DiyyRoutes.FEATURE,
                arguments = listOf(navArgument("section") { type = NavType.StringType }),
            ) { entry ->
                DiyyPageMotion {
                    SettingDetailScreen(
                        section = Uri.decode(entry.arguments?.getString("section").orEmpty()),
                        onBack = navController::navigateUp,
                        onOpenLogin = { navController.navigate(DiyyRoutes.LOGIN) },
                    )
                }
            }
            composable(DiyyRoutes.SETTINGS) {
                DiyyPageMotion {
                    SettingsScreen(
                        onBack = navController::navigateUp,
                        onOpenSection = { section -> navController.navigate("settings/${Uri.encode(section)}") },
                    )
                }
            }
            composable(
                route = "settings/{section}",
                arguments = listOf(navArgument("section") { type = NavType.StringType }),
            ) { entry ->
                DiyyPageMotion {
                    SettingDetailScreen(
                        section = Uri.decode(entry.arguments?.getString("section").orEmpty()),
                        onBack = navController::navigateUp,
                        onOpenLogin = { navController.navigate(DiyyRoutes.LOGIN) },
                    )
                }
            }
            composable(DiyyRoutes.HISTORY) {
                DiyyPageMotion {
                    HistoryScreen(
                        database = database,
                        playerConnection = playerConnection,
                        onBack = navController::navigateUp,
                    )
                }
            }
            composable(DiyyRoutes.DISPLAY_OPTIONS) {
                DiyyPageMotion { LibraryDisplayScreen(onBack = navController::navigateUp) }
            }
            composable(
                route = DiyyRoutes.COLLECTION,
                arguments = listOf(navArgument("type") { type = NavType.StringType }),
            ) { entry ->
                DiyyPageMotion {
                    CollectionScreen(
                        type = Uri.decode(entry.arguments?.getString("type").orEmpty()),
                        database = database,
                        playerConnection = playerConnection,
                        onBack = navController::navigateUp,
                        onOpenCollection = { openCollection(navController, it) },
                    )
                }
            }
        }
    }

    availableUpdate?.let { release ->
        UpdateAvailableDialog(
            release = release,
            onDownload = {
                val url = Updater.getDownloadUrlForCurrentVariant(release)
                    ?: "https://github.com/diyy4a/DiyyMusic/releases/tag/${release.tagName}"
                runCatching { uriHandler.openUri(url) }
                availableUpdate = null
            },
            onDismiss = {
                scope.launch {
                    context.dataStore.edit { prefs -> prefs[DismissedUpdateVersionKey] = release.versionName }
                }
                availableUpdate = null
            },
        )
    }
}

private fun navigateToTab(navController: NavHostController, tab: DiyyMainTab, onSelect: (DiyyMainTab) -> Unit) {
    if (navController.currentDestination?.route != DiyyRoutes.MAIN_TABS) {
        navController.navigate(DiyyRoutes.MAIN_TABS) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
            launchSingleTop = true
        }
    }
    onSelect(tab)
}

private fun openCollection(navController: NavHostController, type: String) {
    navController.navigate("collection/${Uri.encode(type)}")
}

@Composable
private fun rememberMiniPlayerProgress(playerConnection: PlayerConnection?): Float {
    var progress by remember(playerConnection) { mutableFloatStateOf(0f) }
    LaunchedEffect(playerConnection) {
        while (true) {
            val player = runCatching { playerConnection?.player }.getOrNull()
            if (player != null) {
                val duration = player.duration.takeIf { it > 0 } ?: 0L
                progress = if (duration > 0) (player.currentPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f
            } else {
                progress = 0f
            }
            delay(500)
        }
    }
    return progress
}


@Composable
private fun DiyyStartupSplash(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "startupPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "startupLogoScale",
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "startupGlow",
    )

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val entrance by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(560, easing = FastOutSlowInEasing),
        label = "startupEntrance",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        // Soft brand-colored glow breathing behind the logo.
        Box(
            modifier = Modifier
                .size(280.dp)
                .graphicsLayer { alpha = entrance }
                .background(
                    Brush.radialGradient(
                        listOf(DiyyRed.copy(alpha = glowAlpha * 0.35f), Color.Transparent),
                    ),
                ),
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DiyyBrandMark(
                modifier = Modifier.graphicsLayer {
                    scaleX = pulse * (0.85f + 0.15f * entrance)
                    scaleY = pulse * (0.85f + 0.15f * entrance)
                    alpha = entrance
                    translationY = (1f - entrance) * 18f
                },
                showName = true,
            )
            Spacer(Modifier.height(30.dp))
            Row(
                modifier = Modifier.graphicsLayer { alpha = entrance },
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                repeat(3) { index ->
                    val dotScale by transition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 140, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                        label = "startupDot$index",
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .graphicsLayer {
                                scaleX = dotScale
                                scaleY = dotScale
                            }
                            .background(DiyyRed, CircleShape),
                    )
                }
            }
        }
    }
}
