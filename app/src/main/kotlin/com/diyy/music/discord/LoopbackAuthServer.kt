package com.diyy.music.discord

import io.ktor.http.ContentType
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import timber.log.Timber

data class AuthCodeResult(val code: String, val state: String)

class LoopbackAuthServer(
    private val expectedState: String,
    private val port: Int,
    private val callbackPath: String = "/callback",
) {

    private companion object {
        const val TAG = "DiscordSvc"
        const val DEFAULT_HOST = "127.0.0.1"
    }

    private val deferred = CompletableDeferred<AuthCodeResult>()

    private var server: EmbeddedServer<*, *>? = null

    suspend fun start(): Int {
        Timber.tag(TAG).i("loopback: starting on %s:%d%s", DEFAULT_HOST, port, callbackPath)
        server = embeddedServer(CIO, port = port, host = DEFAULT_HOST) {
            routing {
                get(callbackPath) {
                    val code = call.request.queryParameters["code"]
                    val state = call.request.queryParameters["state"]
                    val error = call.request.queryParameters["error"]

                    if (error != null && state == expectedState) {
                        Timber.tag(TAG).w("loopback: callback received with error=%s", error)
                        deferred.completeExceptionally(DiscordAuthException.OAuthRejected(error))
                        call.respondText(
                            callbackHtml(
                                title = "Authorization denied",
                                message = "Discord returned: $error",
                                success = false,
                            ),
                            ContentType.Text.Html,
                        )
                        return@get
                    }

                    if (code == null) {
                        Timber.tag(TAG).w("loopback: callback received with missing code")
                        deferred.completeExceptionally(DiscordAuthException.InvalidGrant("Missing authorization code"))
                        call.respondText(
                            callbackHtml(
                                title = "Authorization failed",
                                message = "Discord did not return an authorization code.",
                                success = false,
                            ),
                            ContentType.Text.Html,
                        )
                        return@get
                    }

                    if (state != expectedState) {
                        Timber.tag(TAG).w(
                            "loopback: state mismatch (expected=%s, got=%s)",
                            expectedState.take(8),
                            state?.take(8),
                        )
                        deferred.completeExceptionally(DiscordAuthException.StateMismatch())
                        call.respondText(
                            callbackHtml(
                                title = "Authorization failed",
                                message = "The authorization state could not be verified.",
                                success = false,
                            ),
                            ContentType.Text.Html,
                        )
                        return@get
                    }

                    Timber.tag(TAG).i("loopback: callback received with valid code (length=%d)", code.length)
                    deferred.complete(AuthCodeResult(code = code, state = state))
                    call.respondText(
                        callbackHtml(
                            title = "Discord connected",
                            message = "You can close this tab and return to DiyyMusic.",
                            success = true,
                        ),
                        ContentType.Text.Html,
                    )
                }
            }
        }.start(wait = false)
        Timber.tag(TAG).i("loopback: started on port %d", port)
        return port
    }


    private fun callbackHtml(title: String, message: String, success: Boolean): String {
        val accent = if (success) "#ff2d75" else "#ff557f"
        val symbol = if (success) "&#10003;" else "&#10005;"
        return """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="utf-8" />
              <meta name="viewport" content="width=device-width,initial-scale=1" />
              <meta name="color-scheme" content="dark" />
              <title>DiyyMusic</title>
              <style>
                * { box-sizing: border-box; }
                body { margin: 0; min-height: 100vh; display: grid; place-items: center; padding: 24px;
                       background: radial-gradient(circle at top, #28101b, #0d0c11 52%); color: #fff;
                       font-family: system-ui, -apple-system, sans-serif; }
                .card { width: min(430px, 100%); padding: 30px; border-radius: 28px;
                        background: rgba(31,29,36,.92); border: 1px solid rgba(255,255,255,.12);
                        box-shadow: 0 24px 70px rgba(0,0,0,.42); text-align: center; }
                .icon { width: 72px; height: 72px; margin: 0 auto 18px; border-radius: 50%; display: grid;
                        place-items: center; font-size: 38px; font-weight: 800; color: white;
                        background: $accent; box-shadow: 0 0 34px ${accent}66; }
                .brand { color: #ff2d75; font-weight: 800; letter-spacing: .2px; margin-bottom: 18px; }
                h1 { margin: 0 0 10px; font-size: 26px; }
                p { margin: 0; color: rgba(255,255,255,.68); line-height: 1.55; word-break: break-word; }
              </style>
            </head>
            <body>
              <main class="card">
                <div class="brand">&#9835; DiyyMusic</div>
                <div class="icon">$symbol</div>
                <h1>$title</h1>
                <p>$message</p>
              </main>
            </body>
            </html>
        """.trimIndent()
    }

    suspend fun awaitCode(timeoutMs: Long = 120_000L): AuthCodeResult {
        return withTimeout(timeoutMs) { deferred.await() }
    }

    fun cancel() {
        if (!deferred.isCompleted) {
            Timber.tag(TAG).i("loopback: cancelling authorization")
            deferred.completeExceptionally(CancellationException("Authorization cancelled by user"))
        }
    }

    fun stop() {
        Timber.tag(TAG).i("loopback: stopping")
        server?.stop(1000L, 2000L)
        server = null
    }
}
