package ru.acs.grandmap.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MenuWideItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: CardElevation = CardDefaults.cardElevation(4.dp),

    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ),
    borderColor: Color? = null,

    leadingTint: Color = MaterialTheme.colorScheme.primary,
    trailingTint: Color = MaterialTheme.colorScheme.primary,

    // Показывать ли стрелку справа
    showChevron: Boolean = true,
) {
    val border = borderColor?.let { BorderStroke(1.dp, it) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(!isLoading) { onClick() },
        shape = shape,
        colors = colors,
        border = border,
        elevation = elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(icon, contentDescription = null, tint = leadingTint)
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            if (showChevron) {
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = trailingTint)
            }
        }
    }
}