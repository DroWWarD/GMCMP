package ru.acs.grandmap.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.acs.grandmap.core.normalizePhone
import ru.acs.grandmap.core.openDialer
import ru.acs.grandmap.core.rememberKmpContext

@Composable
fun MenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: CardElevation = CardDefaults.cardElevation(4.dp),
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor   = MaterialTheme.colorScheme.onSurface
    ),
    borderColor: Color? = null,
    leadingTint: Color = MaterialTheme.colorScheme.primary,
    trailingTint: Color = MaterialTheme.colorScheme.primary,
) {
    // ---- design tokens ----
    val LeadingIconSize  = 24.dp
    val ChevronSize      = 24.dp
    val HorizontalPad    = 16.dp
    val VerticalPad      = 15.dp
    val MinHeight        = 56.dp

    val border = borderColor?.let { BorderStroke(1.dp, it) }

    Card(
        onClick = onClick,
        enabled = !isLoading,
        shape = shape,
        colors = colors,
        border = border,
        elevation = elevation,
        modifier = modifier
            .widthIn(max = 400.dp)
            .heightIn(min = MinHeight)
            .semantics { role = Role.Button }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = HorizontalPad, vertical = VerticalPad),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = leadingTint,
                modifier = Modifier.size(LeadingIconSize)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f) // тянем текст, чтобы шеврон стоял у края
            )
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = trailingTint,
                modifier = Modifier.size(ChevronSize) // всегда один размер
            )
        }
    }
}

@Composable
fun TwoTilesRow(
    left: @Composable () -> Unit,
    right: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.weight(1f)) { left() }
        Box(Modifier.weight(1f)) { right() }
    }
}

@Composable
fun SupportPhone() {
    val ctx = rememberKmpContext()
    val display = "8-800-200-99-74"
    val raw     = normalizePhone(display)

    Text(
        text = display,
        lineHeight = 24.sp,
        color = Color.White,
        fontSize = 16.sp,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier.clickable {
            openDialer(ctx, raw)
        }
    )
}

data class AppBarIconAction(
    val icon: ImageVector,
    val contentDescription: String?,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val visible: Boolean = true
)

data class AppBarOverflowItem(
    val title: String,
    val onClick: () -> Unit,
    val leadingIcon: ImageVector? = null,
    val enabled: Boolean = true,
    val danger: Boolean = false        // для «красных» пунктов (Удалить, Очистить и т.п.)
)