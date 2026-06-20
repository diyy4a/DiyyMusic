package com.diyy.music.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diyy.music.ui.component.DiyyScreenHeader
import com.diyy.music.ui.component.FigmaMediaRow
import com.diyy.music.ui.component.FigmaPromoCard
import com.diyy.music.ui.component.FigmaSectionHeader

private data class RadioStation(
    val title: String,
    val subtitle: String = "From TuneIn",
    val imageUrl: String? = null,
)

private val localStations = listOf(
    RadioStation("Radio City Hindi"),
    RadioStation("Radio City Love Guru"),
    RadioStation("Radio City Ghazals"),
)

private val internationalStations = listOf(
    RadioStation("Virgin Radio Italy"),
    RadioStation("Nova 100"),
    RadioStation("RUM - Radio"),
)

@Composable
fun RadioScreen(
    onOpenProfile: () -> Unit,
    onOpenHistory: () -> Unit,
    onSearchStation: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 18.dp),
    ) {
        item {
            DiyyScreenHeader(
                title = "Radio",
                onHistory = onOpenHistory,
                onProfile = onOpenProfile,
            )
        }
        item {
            FigmaPromoCard(
                title = "Music",
                subtitle = "Try it now",
                footer = "Get 1 month free",
                onClick = { onSearchStation("radio hits") },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        item { FigmaSectionHeader(title = "Local Broadcasters") }
        items(localStations, key = { it.title }) { station ->
            FigmaMediaRow(
                title = station.title,
                subtitle = station.subtitle,
                imageUrl = station.imageUrl,
                onClick = { onSearchStation(station.title) },
            )
        }
        item { FigmaSectionHeader(title = "International Broadcasters") }
        items(internationalStations, key = { it.title }) { station ->
            FigmaMediaRow(
                title = station.title,
                subtitle = station.subtitle,
                imageUrl = station.imageUrl,
                onClick = { onSearchStation(station.title) },
            )
        }
    }
}
