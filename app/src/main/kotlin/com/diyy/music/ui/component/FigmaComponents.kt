package com.diyy.music.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.diyy.music.R
import com.diyy.music.constants.MiniPlayerOutlineKey
import com.diyy.music.models.MediaMetadata
import com.diyy.music.ui.DiyyMainTab
import com.diyy.music.ui.theme.DiyyDivider
import com.diyy.music.ui.theme.DiyyGlass
import com.diyy.music.ui.theme.DiyyPinkLight
import com.diyy.music.ui.theme.DiyyRed
import com.diyy.music.ui.theme.DiyyRedStrong
import com.diyy.music.ui.theme.DiyySoftRed
import com.diyy.music.ui.theme.DiyySurface
import com.diyy.music.ui.theme.isDiyyDarkTheme
import com.diyy.music.utils.dataStore
import kotlinx.coroutines.flow.map

@Composable
fun LiquidGlassBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    elevation: Dp = 10.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val dark = isDiyyDarkTheme()
    val top = if (dark) Color(0xE62A2830) else Color(0xF2FFFFFF)
    val bottom = if (dark) Color(0xD91C1A20) else DiyyGlass
    val border = if (dark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.92f)
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .shadow(elevation, shape, clip = false)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(top, bottom)))
            .border(1.dp, border, shape)
            .then(clickableModifier),
        content = content,
    )
}

@Composable
fun DiyyBrandMark(
    modifier: Modifier = Modifier,
    showName: Boolean = true,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.diyy_brand_mark),
            contentDescription = "DiyyMusic",
            modifier = Modifier.size(30.dp),
        )
        if (showName) {
            Spacer(Modifier.width(7.dp))
            Text(
                text = "DiyyMusic",
                color = DiyyRed,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun DiyyScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onHistory: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                FigmaCircleButton(
                    icon = R.drawable.arrow_back,
                    contentDescription = "Back",
                    onClick = onBack,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            } else {
                Spacer(Modifier.size(44.dp))
            }
            DiyyBrandMark(
                modifier = Modifier.weight(1f),
                showName = true,
            )
            when {
                onHistory != null -> {
                    FigmaCircleButton(
                        icon = R.drawable.history,
                        contentDescription = "History",
                        onClick = onHistory,
                    )
                }
                onProfile != null -> {
                    FigmaCircleButton(
                        icon = R.drawable.account,
                        contentDescription = "Profile",
                        onClick = onProfile,
                    )
                }
                else -> Spacer(Modifier.size(44.dp))
            }
            if (onHistory != null && onProfile != null) {
                Spacer(Modifier.width(8.dp))
                FigmaCircleButton(
                    icon = R.drawable.account,
                    contentDescription = "Profile",
                    onClick = onProfile,
                )
            }
            trailing?.invoke(this)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun FigmaCircleButton(
    @DrawableRes icon: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = DiyyRed,
) {
    LiquidGlassBox(
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = 7.dp,
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier
                .size(21.dp)
                .align(Alignment.Center),
        )
    }
}

@Composable
fun DiyyBottomNavigation(
    selected: DiyyMainTab,
    onSelected: (DiyyMainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        LiquidGlassBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp),
            shape = RoundedCornerShape(30.dp),
            elevation = 14.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DiyyMainTab.entries.forEach { tab ->
                    val active = selected == tab
                    val tint by animateColorAsState(
                        targetValue = if (active) DiyyRed else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.74f),
                        label = "navigationTint",
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onSelected(tab) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (active) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(DiyySoftRed),
                                )
                            }
                            Icon(
                                painter = painterResource(if (active) tab.selectedIcon else tab.unselectedIcon),
                                contentDescription = tab.label,
                                tint = tint,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = tint,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiyyMiniPlayer(
    metadata: MediaMetadata?,
    isPlaying: Boolean,
    progress: Float,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val showOutline by context.dataStore.data
        .map { preferences -> preferences[MiniPlayerOutlineKey] ?: true }
        .collectAsStateWithLifecycle(initialValue = true)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp),
    ) {
        LiquidGlassBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .then(
                    if (showOutline) Modifier.border(
                        BorderStroke(1.dp, DiyyPinkLight.copy(alpha = 0.5f)),
                        RoundedCornerShape(26.dp),
                    ) else Modifier,
                ),
            shape = RoundedCornerShape(26.dp),
            elevation = 12.dp,
            onClick = onOpen,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Artwork(
                        url = metadata?.thumbnailUrl,
                        modifier = Modifier.size(52.dp),
                        cornerRadius = 15,
                    )
                    Spacer(Modifier.width(11.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = metadata?.title ?: "Not Playing",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = metadata?.artists?.joinToString { it.name }.orEmpty().ifBlank { "DiyyMusic" },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (metadata == null) MaterialTheme.colorScheme.onSurfaceVariant else DiyyRed,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(onClick = onPlayPause, enabled = metadata != null) {
                        AnimatedContent(isPlaying, label = "miniPlayerPlayPause") { playing ->
                            Surface(
                                modifier = Modifier.size(42.dp),
                                shape = CircleShape,
                                color = if (metadata == null) MaterialTheme.colorScheme.outlineVariant else DiyyRed,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(if (playing) R.drawable.pause else R.drawable.play),
                                        contentDescription = if (playing) "Pause" else "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(23.dp),
                                    )
                                }
                            }
                        }
                    }
                    IconButton(onClick = onNext, enabled = metadata != null) {
                        Icon(
                            painter = painterResource(R.drawable.skip_next),
                            contentDescription = "Next",
                            tint = if (metadata == null) MaterialTheme.colorScheme.outline else DiyyRed,
                            modifier = Modifier.size(25.dp),
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = DiyyRed,
                    trackColor = Color.Transparent,
                )
            }
        }
    }
}

@Composable
fun Artwork(
    url: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Int = 16,
    fallbackIcon: Int = R.drawable.music_note,
) {
    Box(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(cornerRadius.dp), clip = false)
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(DiyySurface),
        contentAlignment = Alignment.Center,
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                painter = painterResource(R.drawable.diyy_brand_mark),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

@Composable
fun FigmaSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        if (actionText != null && onAction != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.bodyMedium,
                color = DiyyRed,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

@Composable
fun FigmaPromoCard(
    title: String,
    subtitle: String,
    footer: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(178.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp), clip = false),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFF77A2),
                            DiyyRed,
                            Color(0xFFB97CFF),
                        ),
                    ),
                ),
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = subtitle.uppercase(),
                    color = Color.White.copy(alpha = 0.86f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(0.76f),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = Color.White,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.play),
                            contentDescription = null,
                            tint = DiyyRed,
                            modifier = Modifier.padding(9.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = footer,
                        color = Color.White.copy(alpha = 0.92f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun FigmaMediaGridItem(
    title: String,
    subtitle: String?,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    circular: Boolean = false,
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Box {
            Artwork(
                url = imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .then(if (circular) Modifier.clip(CircleShape) else Modifier),
                cornerRadius = if (circular) 100 else 20,
            )
            Surface(
                modifier = Modifier
                    .size(34.dp)
                    .align(Alignment.BottomEnd)
                    .padding(3.dp),
                shape = CircleShape,
                color = DiyyRed,
            ) {
                Icon(
                    painter = painterResource(R.drawable.play),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
        Spacer(Modifier.height(9.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun FigmaMediaRow(
    title: String,
    subtitle: String?,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    LiquidGlassBox(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = 5.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Artwork(
                url = imageUrl,
                modifier = Modifier.size(58.dp),
                cornerRadius = 14,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (trailing != null) trailing() else FigmaCircleButton(
                icon = R.drawable.play,
                contentDescription = "Play",
                onClick = onClick,
                modifier = Modifier.size(38.dp),
            )
        }
    }
}

@Composable
fun FigmaLibraryShortcut(
    title: String,
    @DrawableRes icon: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LiquidGlassBox(
        modifier = modifier.height(68.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = 6.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(39.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(if (selected) DiyyRed else DiyySoftRed),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = if (selected) Color.White else DiyyRed,
                    modifier = Modifier.size(21.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun DiyyStatCard(
    value: String,
    label: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
) {
    LiquidGlassBox(
        modifier = modifier.height(105.dp),
        shape = RoundedCornerShape(23.dp),
        elevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DiyySoftRed),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = DiyyRed,
                    modifier = Modifier.size(19.dp),
                )
            }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun FigmaSettingsRow(
    title: String,
    subtitle: String? = null,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(if (destructive) MaterialTheme.colorScheme.error.copy(alpha = 0.10f) else DiyySoftRed),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (destructive) MaterialTheme.colorScheme.error else DiyyRed,
                modifier = Modifier.size(21.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            painter = painterResource(R.drawable.navigate_next),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun FigmaGroupedList(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    LiquidGlassBox(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = 7.dp,
    ) {
        Column(content = content)
    }
}

@Composable
fun FigmaDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
        color = DiyyDivider.copy(alpha = 0.55f),
    )
}

@Composable
fun EmptyFigmaState(
    title: String,
    subtitle: String,
    @DrawableRes icon: Int = R.drawable.music_note,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.diyy_brand_mark),
            contentDescription = null,
            modifier = Modifier.size(66.dp),
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
