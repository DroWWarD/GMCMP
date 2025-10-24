package ru.acs.grandmap.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MetricSpec(
    val icon: ImageVector,
    val value: String,
    val caption: String,
    val iconTint: Color? = null,
    val onClick: (() -> Unit)? = null,
    val enabled: Boolean = true,
)

/**
 * Универсальная карточка: картинка/аватар + заголовок + подзаголовки + до 3 метрик.
 */
@Composable
fun InfoMetricsCard(
    title: String,
    subtitles: List<String> = emptyList(),
    metrics: List<MetricSpec> = emptyList(),      // будут показаны первые три
    image: Painter? = null,                       // картинка (если null — плейсхолдер + иконка)
    placeholderIcon: ImageVector = Icons.Default.Person,
    subtitleSeparator: String = "\n",
    onAvatarClick: (() -> Unit)? = null,
    avatarEnabled: Boolean = true,

    onClick: (() -> Unit)? = null,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: CardElevation = CardDefaults.cardElevation(4.dp),
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor   = MaterialTheme.colorScheme.onSurface
    ),
    borderColor: Color? = null,

    // Токены/настройки в духе MenuItem
    leadingTint: Color = MaterialTheme.colorScheme.primary, // для placeholderIcon
    metricIconTint: Color = MaterialTheme.colorScheme.primary,
    maxWidth: Dp = 400.dp,
) {
    // ---- design tokens ----
    val HorizontalPad    = 16.dp
    val VerticalPad      = 16.dp
    val AvatarSize       = 70.dp
    val PlaceholderDot   = 52.dp
    val MetricsGap       = 8.dp
    val TitleBottomSpace = 10.dp
    val DividerVPad      = 10.dp

    val border = borderColor?.let { BorderStroke(1.dp, it) }

    val cardContent: @Composable ColumnScope.() -> Unit = {
        Column(Modifier.padding(horizontal = HorizontalPad, vertical = VerticalPad)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Аватар / плейсхолдер
                val avatarInteraction = remember { MutableInteractionSource() }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(AvatarSize)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (onAvatarClick != null)
                                Modifier
                                    .clickable(
                                        interactionSource = avatarInteraction,
                                        enabled = avatarEnabled,
                                        role = Role.Button
                                    ) { onAvatarClick() }
                            else Modifier
                        )
                ) {
                    // Мягкий фон-кружок
                    Box(
                        modifier = Modifier
                            .size(PlaceholderDot)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = .15f)),
                        contentAlignment = Alignment.Center,
                        content = {}
                    )

                    // Фото (если есть) ИЛИ плейсхолдер-иконка
                    if (image != null) {
                        Image(
                            painter = image,
                            contentDescription = "Аватар",
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = placeholderIcon,
                            contentDescription = "Аватар (плейсхолдер)",
                            tint = leadingTint
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = TitleBottomSpace)
                    )
                    if (subtitles.isNotEmpty()) {
                        Text(
                            text = subtitles.joinToString(subtitleSeparator),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Divider(Modifier.padding(vertical = DividerVPad))

            if (metrics.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(MetricsGap)
                ) {
                    val items = metrics.take(3)
                    items.forEach { m ->
                        MetricItem(
                            icon     = m.icon,
                            value    = m.value,
                            caption  = m.caption,
                            iconTint = m.iconTint ?: metricIconTint,
                            onClick  = m.onClick,
                            enabled  = m.enabled,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // симметрия, если метрик < 3
                    repeat((3 - items.size).coerceAtLeast(0)) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            enabled = !isLoading,
            shape = shape,
            colors = colors,
            border = border,
            elevation = elevation,
            modifier = modifier
                .widthIn(max = maxWidth)
                .semantics { role = Role.Button }
        ) { cardContent() }
    } else {
        Card(
            shape = shape,
            colors = colors,
            border = border,
            elevation = elevation,
            modifier = modifier.widthIn(max = maxWidth)
        ) { cardContent() }
    }
}

/** Инлайновая метрика (иконка + число + подпись), без внешних зависимостей. */
@Composable
private fun MetricItem(
    icon: ImageVector,
    value: String,
    caption: String,
    iconTint: Color,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val interaction = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(shape)
            .then(
                if (onClick != null)
                    Modifier.clickable(
                        interactionSource = interaction,
                        enabled = enabled,
                        role = Role.Button
                    ) { onClick() }
                else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = iconTint)
        Text(value, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(
            caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}