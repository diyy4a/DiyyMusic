/**
 * Copyright (C) upstream contributors and DiyyMusic contributors
 * Modified for DiyyMusic in 2026. Licensed under GPL-3.0.
 */

package com.diyy.music.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlayerCache

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DownloadCache

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
