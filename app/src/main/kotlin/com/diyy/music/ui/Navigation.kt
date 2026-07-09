package com.diyy.music.ui

import androidx.annotation.DrawableRes
import com.diyy.music.R

enum class DiyyMainTab(
    val route: String,
    val label: String,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
) {
    LISTEN_NOW("listen_now", "Home", R.drawable.home_filled, R.drawable.home_outlined),
    SEARCH("search", "Search", R.drawable.diyy_nav_search_filled, R.drawable.diyy_nav_search_outlined),
    LIBRARY("library", "Library", R.drawable.library_music_filled, R.drawable.library_music_outlined),
    PROFILE("profile", "Profile", R.drawable.account, R.drawable.person),
}

object DiyyRoutes {
    const val PLAYER = "player"
    const val LOGIN = "login"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val FEATURE = "feature/{section}"
    const val HISTORY = "history"
    const val DISPLAY_OPTIONS = "display_options"
    const val COLLECTION = "collection/{type}"

    fun collection(type: String): String = "collection/$type"
}
