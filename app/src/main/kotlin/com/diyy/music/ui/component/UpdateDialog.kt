/**
 * Copyright (C) upstream contributors and DiyyMusic contributors
 * Added for DiyyMusic in 2026. Licensed under GPL-3.0.
 */

package com.diyy.music.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diyy.music.utils.ReleaseInfo

/**
 * Shown once per new version when a newer DiyyMusic release is published on GitHub.
 */
@Composable
fun UpdateAvailableDialog(
    release: ReleaseInfo,
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        title = { Text("Update available") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "DiyyMusic ${release.versionName} is out. You're on an older version.",
                    fontWeight = FontWeight.Medium,
                )
                if (release.description.isNotBlank()) {
                    Text(
                        text = release.description.trim().take(500),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .verticalScroll(rememberScrollState()),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDownload) { Text("Download") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Later") }
        },
    )
}
