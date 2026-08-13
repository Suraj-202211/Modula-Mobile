package com.modulamobile.ui.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.modulamobile.ui.theme.*
import com.modulamobile.ui.state.GlobalState
import com.modulamobile.ui.state.FluxTheme

import androidx.compose.ui.composed

enum class GlassVariant { GOLD, DARK, DEEP, HERO }

fun Modifier.glass(
    variant: GlassVariant = GlassVariant.DARK,
    radius: Dp = 4.dp
): Modifier = composed {
    val themeAccent = com.modulamobile.ui.theme.LocalModulaColors.current.primary
    val transparency = com.modulamobile.ui.state.LocalUiSettings.current.uiTransparency

    val (bgColor, borderColor) = when (variant) {
        GlassVariant.GOLD  -> themeAccent to Color.Transparent
        GlassVariant.DARK  -> Color(0xFF18181D).copy(alpha = (1f - transparency).coerceAtLeast(0.1f)) to Color(0xFF2A2A35)
        GlassVariant.DEEP  -> Color(0xFF101015).copy(alpha = (1f - transparency).coerceAtLeast(0.1f)) to Color.Transparent
        GlassVariant.HERO  -> themeAccent.copy(alpha = 0.2f * (1f - transparency).coerceAtLeast(0.1f)) to themeAccent.copy(alpha = 0.6f)
    }

    this
        .clip(RoundedCornerShape(radius))
        .background(bgColor)
        .border(if (borderColor != Color.Transparent) 1.dp else 0.dp, borderColor, RoundedCornerShape(radius))
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    variant: GlassVariant = GlassVariant.DARK,
    radius: Dp = 4.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.glass(variant, radius), content = content)
}

@Composable
fun GlassHeroCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.glass(GlassVariant.HERO, 8.dp), content = content)
}

@Composable
fun GlassListItem(
    modifier: Modifier = Modifier,
    variant: GlassVariant = GlassVariant.DARK,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glass(variant, 4.dp)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        content = content
    )
}

@Composable
fun GlassChip(
    text: String,
    modifier: Modifier = Modifier,
    variant: GlassVariant = GlassVariant.DARK,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val boxModifier = modifier.glass(if (selected) GlassVariant.GOLD else variant, 4.dp)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(horizontal = 12.dp, vertical = 6.dp)
    Box(
        modifier = boxModifier
    ) {
        Text(
            text = text,
            style = ModulaTypography.labelSmall,
            color = if (selected) ColorBg0 else TextPrimary
        )
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: GlassVariant = GlassVariant.GOLD,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.glass(variant, 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (variant == GlassVariant.GOLD) ColorBg0 else TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = ModulaTypography.labelLarge,
                color = if (variant == GlassVariant.GOLD) ColorBg0 else TextPrimary
            )
        }
    }
}

@Composable
fun GlassGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(4.dp)) {
        Text(text, style = ModulaTypography.labelLarge, color = TextPrimary)
    }
}

@Composable
fun GlassDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFF2A2A35))
    )
}

@Composable
fun GlassTextField(modifier: Modifier = Modifier, value: String, onValueChange: (String) -> Unit, placeholder: String = "") {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextMuted) },
        modifier = modifier.glass(GlassVariant.DARK, 4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}

@Composable
fun GlassTopBar(modifier: Modifier = Modifier, title: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxWidth().height(64.dp).background(ColorBg0), contentAlignment = Alignment.Center) {
        title()
    }
}

@Composable
fun GlassBottomNav(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth().height(80.dp).background(ColorBg0).border(1.dp, Color(0xFF15151A)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun GlassSideNav(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.width(240.dp).fillMaxHeight().background(ColorBg0).padding(16.dp),
        content = content
    )
}

@Composable
fun GlassBadge(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.glass(GlassVariant.GOLD, 4.dp).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text, style = ModulaTypography.labelSmall, color = ColorBg0)
    }
}

@Composable
fun GlassProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val themeAccent = com.modulamobile.ui.theme.LocalModulaColors.current.primary
    Box(modifier = modifier.fillMaxWidth().height(4.dp).background(Color(0xFF18181D))) {
        Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(themeAccent))
    }
}

@Composable
fun GlassToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val themeAccent = com.modulamobile.ui.theme.LocalModulaColors.current.primary
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = themeAccent,
            checkedTrackColor = Color(0xFF2A2A35),
            uncheckedThumbColor = TextMuted,
            uncheckedTrackColor = ColorBg3
        ),
        modifier = modifier
    )
}

@Composable
fun GlassSlider(value: Float, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier, valueRange: ClosedFloatingPointRange<Float> = 0f..1f) {
    var localValue by androidx.compose.runtime.remember(value) { androidx.compose.runtime.mutableStateOf(value) }
    val themeAccent = com.modulamobile.ui.theme.LocalModulaColors.current.primary
    Slider(
        value = localValue,
        onValueChange = { localValue = it },
        onValueChangeFinished = { onValueChange(localValue) },
        valueRange = valueRange,
        colors = SliderDefaults.colors(
            thumbColor = themeAccent,
            activeTrackColor = themeAccent,
            inactiveTrackColor = ColorBg3
        ),
        modifier = modifier
    )
}

