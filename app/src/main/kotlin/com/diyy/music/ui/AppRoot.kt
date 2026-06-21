package com.diyy.music.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.diyy.music.extensions.togglePlayPause
import com.diyy.music.models.MediaMetadata
import com.diyy.music.playback.PlayerConnection
import com.diyy.music.ui.component.DiyyBottomNavigation
import com.diyy.music.ui.component.DiyyMiniPlayer
import com.diyy.music.ui.screens.CollectionScreen
import com.diyy.music.ui.screens.HistoryScreen
import com.diyy.music.ui.screens.LibraryDisplayScreen
import com.diyy.music.ui.screens.LibraryScreen
import com.diyy.music.ui.screens.ListenNowScreen
import com.diyy.music.ui.screens.PlayerScreen
import com.diyy.music.ui.screens.ProfileScreen
import com.diyy.music.ui.screens.RadioScreen
import com.diyy.music.ui.screens.SearchScreen
import com.diyy.music.ui.screens.SettingDetailScreen
import com.diyy.music.ui.screens.SettingsScreen
import com.diyy.music.utils.dataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DiyyMusicRoot(
    database: MusicDatabase,
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
                        onPlayPause = { runCatching { playerConnection?.player?.togglePlayPause() } },
                        onNext = { runCatching { playerConnection?.player?.seekToNextMediaItem() } },
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
                ListenNowScreen(
                    playerConnection = playerConnection,
                    onOpenProfile = { navigateToTab(navController, DiyyMainTab.PROFILE) },
                    onOpenHistory = { navController.navigate(DiyyRoutes.HISTORY) },
                    onOpenRadio = { navController.navigate(DiyyRoutes.RADIO) },
                    onOpenCollection = { openCollection(navController, it) },
                )
            }
            composable(DiyyMainTab.SEARCH.route) {
                SearchScreen(
                    playerConnection = playerConnection,
                    initialQuery = searchSeed,
                    onOpenCollection = { openCollection(navController, it) },
                )
            }
            composable(DiyyMainTab.LIBRARY.route) {
                LibraryScreen(
                    database = database,
                    playerConnection = playerConnection,
                    onOpenProfile = { navigateToTab(navController, DiyyMainTab.PROFILE) },
                    onOpenHistory = { navController.navigate(DiyyRoutes.HISTORY) },
                    onOpenCollection = { openCollection(navController, it) },
                    onOpenDisplayOptions = { navController.navigate(DiyyRoutes.DISPLAY_OPTIONS) },
                )
            }
            composable(DiyyMainTab.PROFILE.route) {
                ProfileScreen(
                    onBack = null,
                    onOpenSettings = { navController.navigate(DiyyRoutes.SETTINGS) },
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
            composable(DiyyRoutes.RADIO) {
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
            composable(DiyyRoutes.PLAYER) {
                PlayerScreen(playerConnection = playerConnection, onBack = navController::navigateUp)
            }
            composable(DiyyRoutes.SETTINGS) {
                SettingsScreen(
                    onBack = navController::navigateUp,
                    onOpenSection = { section -> navController.navigate("settings/${Uri.encode(section)}") },
                )
            }
            composable(
                route = "settings/{section}",
                arguments = listOf(navArgument("section") { type = NavType.StringType }),
            ) { entry ->
                SettingDetailScreen(
                    section = Uri.decode(entry.arguments?.getString("section").orEmpty()),
                    onBack = navController::navigateUp,
                )
            }
            composable(DiyyRoutes.HISTORY) {
                HistoryScreen(
                    database = database,
                    playerConnection = playerConnection,
                    onBack = navController::navigateUp,
                )
            }
            composable(DiyyRoutes.DISPLAY_OPTIONS) {
                LibraryDisplayScreen(onBack = navController::navigateUp)
            }
            composable(
                route = DiyyRoutes.COLLECTION,
                arguments = listOf(navArgument("type") { type = NavType.StringType }),
            ) { entry ->
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
