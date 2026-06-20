package com.diyy.music

import android.Manifest
import android.app.ForegroundServiceStartNotAllowedException
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.diyy.music.constants.DisableScreenshotKey
import com.diyy.music.constants.EnableHighRefreshRateKey
import com.diyy.music.constants.StopMusicOnTaskClearKey
import com.diyy.music.db.MusicDatabase
import com.diyy.music.listentogether.ListenTogetherManager
import com.diyy.music.playback.DownloadUtil
import com.diyy.music.playback.MusicService
import com.diyy.music.playback.MusicService.MusicBinder
import com.diyy.music.playback.PlayerConnection
import com.diyy.music.ui.DiyyMusicRoot
import com.diyy.music.ui.theme.DiyyMusicTheme
import com.diyy.music.utils.SyncUtils
import com.diyy.music.utils.dataStore
import com.diyy.music.utils.get
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        private const val ACTION_SEARCH = "com.diyy.music.action.SEARCH"
        private const val ACTION_LIBRARY = "com.diyy.music.action.LIBRARY"
        const val ACTION_RECOGNITION = "com.diyy.music.action.RECOGNITION"
        const val ACTION_OPEN_WIDGET_TARGET = "com.diyy.music.action.OPEN_WIDGET_TARGET"
        const val EXTRA_AUTO_START_RECOGNITION = "auto_start_recognition"
        const val EXTRA_WIDGET_TARGET_TYPE = "widget_target_type"
        const val EXTRA_WIDGET_TARGET_ID = "widget_target_id"
    }

    @Inject lateinit var database: MusicDatabase
    @Inject lateinit var downloadUtil: DownloadUtil
    @Inject lateinit var syncUtils: SyncUtils
    @Inject lateinit var listenTogetherManager: ListenTogetherManager

    private var playerConnection: PlayerConnection? = null
    private var playerConnectionSnapshot by androidx.compose.runtime.mutableStateOf<PlayerConnection?>(null)
    private var requestedRoute by androidx.compose.runtime.mutableStateOf<String?>(null)
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service !is MusicBinder) return
            fun connect() {
                playerConnection?.dispose()
                playerConnection = PlayerConnection(this@MainActivity, service, database, lifecycleScope)
                playerConnectionSnapshot = playerConnection
                listenTogetherManager.setPlayerConnection(playerConnection)
            }
            runCatching(::connect).onFailure { firstError ->
                Timber.tag("MainActivity").w(firstError, "Player connection was not ready; retrying")
                lifecycleScope.launch {
                    delay(500)
                    runCatching(::connect).onFailure {
                        Timber.tag("MainActivity").e(it, "Unable to connect to the playback service")
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            listenTogetherManager.setPlayerConnection(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        listenTogetherManager.initialize()
        handleIntent(intent)

        lifecycleScope.launch {
            dataStore.data
                .map { it[DisableScreenshotKey] ?: false }
                .distinctUntilChanged()
                .collectLatest { disabled ->
                    if (disabled) {
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE,
                        )
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }
        }

        lifecycleScope.launch {
            dataStore.data
                .map { it[EnableHighRefreshRateKey] ?: true }
                .distinctUntilChanged()
                .collectLatest { enabled ->
                    val params = window.attributes
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (enabled) {
                            params.preferredDisplayModeId = 0
                        } else {
                            val modes = window.windowManager.defaultDisplay.supportedModes
                            val mode60 = modes.firstOrNull { kotlin.math.abs(it.refreshRate - 60f) < 1f }
                                ?: modes.minByOrNull { kotlin.math.abs(it.refreshRate - 60f) }
                            if (mode60 != null) params.preferredDisplayModeId = mode60.modeId
                        }
                    } else {
                        params.preferredRefreshRate = if (enabled) 0f else 60f
                    }
                    window.attributes = params
                }
        }

        setContent {
            DiyyMusicTheme(darkTheme = false) {
                DiyyMusicRoot(
                    database = database,
                    playerConnection = playerConnectionSnapshot,
                    requestedRoute = requestedRoute,
                    onRequestedRouteConsumed = { requestedRoute = null },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1000)
        }

        if (!MusicService.isRunning) {
            val serviceIntent = Intent(this, MusicService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ContextCompat.startForegroundService(this, serviceIntent)
                } else {
                    startService(serviceIntent)
                }
            } catch (error: ForegroundServiceStartNotAllowedException) {
                Timber.tag("MainActivity").w(error, "Foreground service start was blocked")
            } catch (error: IllegalStateException) {
                Timber.tag("MainActivity").w(error, "Playback service could not be started")
            }
        }

        if (!isServiceBound) {
            isServiceBound = bindService(
                Intent(this, MusicService::class.java),
                serviceConnection,
                BIND_AUTO_CREATE,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onDestroy() {
        if (isFinishing) listenTogetherManager.disconnect()

        val stopServiceOnClear =
            dataStore.get(StopMusicOnTaskClearKey, false) &&
                playerConnection?.isEffectivelyPlaying?.value == true &&
                isFinishing

        listenTogetherManager.setPlayerConnection(null)
        playerConnection?.dispose()
        playerConnection = null
        playerConnectionSnapshot = null

        if (isServiceBound) {
            runCatching { unbindService(serviceConnection) }
            isServiceBound = false
        }
        if (stopServiceOnClear) stopService(Intent(this, MusicService::class.java))
        super.onDestroy()
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        requestedRoute = when (intent.action) {
            ACTION_SEARCH -> "search"
            ACTION_LIBRARY -> "library"
            ACTION_RECOGNITION -> "search"
            ACTION_OPEN_WIDGET_TARGET -> {
                val type = intent.getStringExtra(EXTRA_WIDGET_TARGET_TYPE)
                val id = intent.getStringExtra(EXTRA_WIDGET_TARGET_ID)
                when (type) {
                    "local" -> id?.let { "collection/playlist:$it" } ?: "library"
                    "online" -> id?.let { "collection/online_playlist:$it" } ?: "library"
                    "liked" -> "collection/favorites"
                    "downloaded" -> "collection/songs"
                    "top" -> "collection/recent"
                    else -> "library"
                }
            }
            Intent.ACTION_VIEW, Intent.ACTION_SEND -> "search"
            else -> requestedRoute
        }
    }
}
