package com.diyy.music.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.diyy.innertube.YouTube
import com.diyy.music.constants.AccountChannelHandleKey
import com.diyy.music.constants.AccountEmailKey
import com.diyy.music.constants.AccountNameKey
import com.diyy.music.constants.DataSyncIdKey
import com.diyy.music.constants.InnerTubeCookieKey
import com.diyy.music.constants.VisitorDataKey
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.theme.DiyyRed
import com.diyy.music.utils.rememberPreference
import com.diyy.music.utils.reportException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var visitorData by rememberPreference(VisitorDataKey, "")
    var dataSyncId by rememberPreference(DataSyncIdKey, "")
    var innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    var accountName by rememberPreference(AccountNameKey, "")
    var accountEmail by rememberPreference(AccountEmailKey, "")
    var accountChannelHandle by rememberPreference(AccountChannelHandleKey, "")
    var isCompletingLogin by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    fun finishLogin() {
        if (isCompletingLogin) return
        isCompletingLogin = true
        scope.launch {
            val cookieManager = CookieManager.getInstance()
            cookieManager.flush()
            val cookie = cookieManager.getCookie("https://music.youtube.com").orEmpty()
            if (cookie.isBlank()) {
                isCompletingLogin = false
                onBack()
                return@launch
            }

            innerTubeCookie = cookie
            delay(350)
            YouTube.cookie = cookie
            YouTube.dataSyncId = dataSyncId
            YouTube.visitorData = visitorData

            YouTube.accountInfo()
                .onSuccess { info ->
                    accountName = info.name
                    accountEmail = info.email.orEmpty()
                    accountChannelHandle = info.channelHandle.orEmpty()
                    webView?.apply {
                        stopLoading()
                        clearHistory()
                        clearCache(true)
                        clearFormData()
                    }

                    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    if (launchIntent != null) context.startActivity(launchIntent)
                    Runtime.getRuntime().exit(0)
                }
                .onFailure { error ->
                    Timber.e(error, "DiyyMusic account login validation failed")
                    reportException(error)
                    isCompletingLogin = false
                }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                stopLoading()
                removeJavascriptInterface("Android")
                destroy()
            }
            webView = null
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        DiyyScreenHeader(
            title = "Login to DiyyMusic",
            onBack = ::finishLogin,
        )
        Text(
            text = "Sign in below, then tap Back to finish connecting your account.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
        )
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { webViewContext ->
                    CookieManager.getInstance().apply {
                        setAcceptCookie(true)
                    }
                    WebView(webViewContext).apply {
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String?) {
                                view.loadUrl(
                                    "javascript:(function(){var c=(window.yt&&window.yt.config_)||{};" +
                                        "if(window.Android){window.Android.onRetrieveVisitorData(c.VISITOR_DATA||'');}})()",
                                )
                                view.loadUrl(
                                    "javascript:(function(){var c=(window.yt&&window.yt.config_)||{};" +
                                        "if(window.Android){window.Android.onRetrieveDataSyncId(c.DATASYNC_ID||'');}})()",
                                )
                            }
                        }
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            userAgentString = userAgentString.replace("; wv", "")
                        }
                        addJavascriptInterface(
                            object {
                                @JavascriptInterface
                                fun onRetrieveVisitorData(value: String?) {
                                    if (!value.isNullOrBlank()) visitorData = value
                                }

                                @JavascriptInterface
                                fun onRetrieveDataSyncId(value: String?) {
                                    if (!value.isNullOrBlank()) dataSyncId = value.substringBefore("||")
                                }
                            },
                            "Android",
                        )
                        webView = this
                        loadUrl("https://accounts.google.com/ServiceLogin?continue=https%3A%2F%2Fmusic.youtube.com")
                    }
                },
            )

            if (isCompletingLogin) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = DiyyRed,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
        }
    }

    BackHandler {
        val current = webView
        if (!isCompletingLogin && current?.canGoBack() == true) {
            current.goBack()
        } else {
            finishLogin()
        }
    }
}
