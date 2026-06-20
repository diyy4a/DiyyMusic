/**
 * Copyright (C) upstream contributors and DiyyMusic contributors
 * Modified for DiyyMusic in 2026. Licensed under GPL-3.0.
 */

package com.diyy.music.extensions

fun <T> tryOrNull(block: () -> T): T? =
    try {
        block()
    } catch (e: Exception) {
        null
    }
