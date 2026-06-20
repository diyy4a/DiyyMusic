/**
 * Copyright (C) upstream contributors and DiyyMusic contributors
 * Modified for DiyyMusic in 2026. Licensed under GPL-3.0.
 */

package com.diyy.music.wrapped

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WrappedViewModel @Inject constructor() : ViewModel()
