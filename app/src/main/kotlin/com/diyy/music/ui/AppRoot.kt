package com.diyy.music.ui

import android.net.Uri
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
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
import com.diyy.music.constants.InnerTubeCookieKey
import com.diyy.music.db.MusicDatabase
import com.diyy.music.models.MediaMetadata
import com.diyy.music.playback.DownloadUtil
import com.diyy.music.playback.PlayerConnection
import com.diyy.music.ui.component.DiyyBottomNavigation
import com.diyy.music.ui.component.DiyyBrandMark
import com.diyy.music.ui.component.DiyyMiniPlayer
import com.diyy.music.ui.component.DiyyPageMotion
import com.diyy.music.ui.screens.CollectionScreen
import com.diyy.music.ui.screens.HistoryScreen
import com.diyy.music.ui.screens.LibraryDisplayScreen
import com.diyy.music.ui.screens.LibraryScreen
import com.diyy.music.ui.screens.LoginScreen
import com.diyy.music.ui.screens.ListenNowScreen
import com.diyy.music.ui.screens.PlayerScreen
import com.diyy.music.ui.screens.ProfileScreen
import com.diyy.music.ui.screens.RadioScreen
import com.diyy.music.ui.screens.SearchScreen
import com.diyy.music.ui.screens.SettingDetailScreen
import com.diyy.music.ui.screens.SettingsScreen
import com.diyy.music.ui.theme.DiyyRed
import com.diyy.music.utils.dataStore
import kotlinx.coroutines.delay
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
    val currentRoute = backStack?.destination?.route ?: DiyyMainTab.LISTEN_NOW.route
    val isMainRoute = DiyyMainTab.entries.any { it.route == currentRoute }
    val currentTab = DiyyMainTab.entries.firstOrNull { it.route == currentRoute } ?: DiyyMainTab.LISTEN_NOW

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

    LaunchedEffect(Unit) {
        delay(700)
        showStartupSplash = false
    }

    if (showStartupSplash) {
        DiyyStartupSplash()
        return
    }

    LaunchedEffect(requestedRoute) {
        when (requestedRoute) {
            "search" -> navigateToTab(navController, DiyyMainTab.SEARCH)
            "library" -> navigateToTab(navController, DiyyMainTab.LIBRARY)
            "profile" -> navigateToTab(navController, DiyyMainTab.PROFILE)
            "radio" -> navController.navigate(DiyyRoutes.RADIO)
            "home", "listen_now" -> navigateToTab(navController, DiyyMainTab.LISTEN_NOW)
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
            if (isMainRoute) {
                Column {
                    DiyyMiniPlayer(
                        metadata = metadata,
                        isPlaying = isPlaying,
                        progress = progress,
                        onOpen = { navController.navigate(DiyyRoutes.PLAYER) },
                        onPlayPause = { playerConnection?.togglePlayPause() },
                        onNext = { playerConnection?.seekToNext() },
                    )
                    DiyyBottomNavigation(
                        selected = currentTab,
                        onSelected = { navigateToTab(navController, it) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DiyyMainTab.LISTEN_NOW.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(DiyyMainTab.LISTEN_NOW.route) {
                DiyyPageMotion {
                    ListenNowScreen(
                        playerConnection = playerConnection,
                        onOpenProfile = { navigateToTab(navController, DiyyMainTab.PROFILE) },
                        onOpenHistory = { navController.navigate(DiyyRoutes.HISTORY) },
                        onOpenRadio = { navController.navigate(DiyyRoutes.RADIO) },
                        onOpenCollection = { openCollection(navController, it) },
                    )
                }
            }
            composable(DiyyMainTab.SEARCH.route) {
                DiyyPageMotion {
                    SearchScreen(
                        playerConnection = playerConnection,
                        initialQuery = searchSeed,
                        onOpenCollection = { openCollection(navController, it) },
                    )
                }
            }
            composable(DiyyMainTab.LIBRARY.route) {
                DiyyPageMotion {
                    LibraryScreen(
                        database = database,
                        playerConnection = playerConnection,
                        onOpenProfile = { navigateToTab(navController, DiyyMainTab.PROFILE) },
                        onOpenHistory = { navController.navigate(DiyyRoutes.HISTORY) },
                        onOpenCollection = { openCollection(navController, it) },
                        onOpenDisplayOptions = { navController.navigate(DiyyRoutes.DISPLAY_OPTIONS) },
                    )
                }
            }
            composable(DiyyMainTab.PROFILE.route) {
                DiyyPageMotion {
                    ProfileScreen(
                        database = database,
                        onBack = null,
                        onOpenFeature = { section -> navController.navigate("feature/${Uri.encode(section)}") },
                        onOpenCollection = { openCollection(navController, it) },
                        onLogout = {
                            scope.launch {
                                context.dataStore.edit { preferences ->
                                    preferences.remove(InnerTubeCookieKey)
                                }
                            }
                        },
                    )
                }
            }
            composable(DiyyRoutes.RADIO) {
                DiyyPageMotion {
                    RadioScreen(
                        onBack = navController::navigateUp,
                        onOpenProfile = { navigateToTab(navController, DiyyMainTab.PROFILE) },
                        onOpenHistory = { navController.navigate(DiyyRoutes.HISTORY) },
                        onSearchStation = { query ->
                            searchSeed = query
                            navigateToTab(navController, DiyyMainTab.SEARCH)
                        },
                    )
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
}

private fun navigateToTab(navController: NavHostController, tab: DiyyMainTab) {
    navController.navigate(tab.route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
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
private fun DiyyStartupSplash() {
    val transition = rememberInfiniteTransition(label = "startupPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(850),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "startupLogoScale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DiyyBrandMark(
                modifier = Modifier.graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                    alpha = 0.92f + ((pulse - 0.96f) * 1.2f)
                },
                showName = true,
            )
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 22.dp).size(24.dp),
                color = DiyyRed,
                strokeWidth = 2.5.dp,
            )
        }
    }
}
