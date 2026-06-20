package com.diyy.music.ui

import androidx.annotation.DrawableRes
import com.diyy.music.R

enum class DiyyMainTab(
    val route: String,
    val label: String,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
) {
    LISTEN_NOW("listen_now", "Listen Now", R.drawable.home_filled, R.drawable.home_outlined),
    RADIO("radio", "Radio", R.drawable.radio, R.drawable.radio),
    LIBRARY("library", "Library", R.drawable.library_music_filled, R.drawable.library_music_outlined),
    SEARCH("search", "Search", R.drawable.search, R.drawable.search),
}

object DiyyRoutes {
    const val PLAYER = "player"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val HISTORY = "history"
    const val DISPLAY_OPTIONS = "display_options"
    const val COLLECTION = "collection/{type}"

    fun collection(type: String): String = "collection/$type"
}
