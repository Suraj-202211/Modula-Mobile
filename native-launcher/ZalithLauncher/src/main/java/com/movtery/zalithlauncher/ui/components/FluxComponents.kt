package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import com.movtery.zalithlauncher.ui.theme.*

// ── GLASS MODIFIER ───────────────────────────────

enum class GlassVariant { DARK, GOLD, HERO, DEEP }

fun Modifier.fluxGlass(
    variant: GlassVariant = GlassVariant.DARK,
    radius: Dp = 16.dp
): Modifier {
    val bgAlpha     = when(variant) {
        GlassVariant.DARK -> 0.06f
        GlassVariant.GOLD -> 0.10f
        GlassVariant.HERO -> 0.16f
        GlassVariant.DEEP -> 0.04f
    }
    val borderAlpha = when(variant) {
        GlassVariant.DARK -> 0.15f
        GlassVariant.GOLD -> 0.40f
        GlassVariant.HERO -> 0.65f
        GlassVariant.DEEP -> 0.10f
    }
    val tint        = when(variant) {
        GlassVariant.DARK -> Color.White
        GlassVariant.GOLD -> FluxGold
        GlassVariant.HERO -> FluxGold
        GlassVariant.DEEP -> Color.White
    }
    val shape = RoundedCornerShape(radius)
    return this
        .clip(shape)
        .background(
            Brush.linearGradient(
                listOf(
                    tint.copy(alpha = bgAlpha + 0.04f),
                    tint.copy(alpha = bgAlpha)
                )
            )
        )
        .border(
            width = 0.8.dp,
            brush = Brush.linearGradient(
                listOf(
                    tint.copy(alpha = borderAlpha),
                    tint.copy(alpha = borderAlpha * 0.4f),
                    tint.copy(alpha = borderAlpha)
                )
            ),
            shape = shape
        )
}

// ── FLUX CARD ────────────────────────────────────

@Composable
fun FluxCard(
    modifier: Modifier = Modifier,
    variant: GlassVariant = GlassVariant.DARK,
    radius: Dp = 16.dp,
    padding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val mod = modifier
        .fluxGlass(variant, radius)
        .then(
            if (onClick != null)
                Modifier.clickable(onClick = onClick)
            else Modifier
        )
        .padding(padding)
    Column(modifier = mod, content = content)
}

// ── FLUX BUTTON ──────────────────────────────────

enum class FluxButtonVariant { PRIMARY, GHOST, DANGER }

@Composable
fun FluxButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: FluxButtonVariant = FluxButtonVariant.PRIMARY,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(999.dp)
    when (variant) {
        FluxButtonVariant.PRIMARY -> {
            Box(
                modifier = modifier
                    .height(52.dp)
                    .clip(shape)
                    .background(
                        Brush.linearGradient(
                            listOf(FluxGold,
                                   FluxAmber,
                                   FluxCopper)
                        )
                    )
                    .clickable(enabled = enabled,
                               onClick = onClick)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.Center
                ) {
                    icon?.invoke()
                    if (icon != null)
                        Spacer(Modifier.width(8.dp))
                    Text(
                        text  = label,
                        style = LabelLG,
                        color = TextOnGold
                    )
                }
            }
        }

        FluxButtonVariant.GHOST -> {
            Box(
                modifier = modifier
                    .height(52.dp)
                    .clip(shape)
                    .border(1.dp, Glow35, shape)
                    .clickable(enabled = enabled,
                               onClick = onClick)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = label,
                    style = LabelLG,
                    color = FluxGold
                )
            }
        }

        FluxButtonVariant.DANGER -> {
            Box(
                modifier = modifier
                    .height(52.dp)
                    .clip(shape)
                    .background(
                        StateError.copy(alpha = 0.15f))
                    .border(1.dp,
                        StateError.copy(alpha = 0.5f),
                        shape)
                    .clickable(enabled = enabled,
                               onClick = onClick)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = label,
                    style = LabelLG,
                    color = StateError
                )
            }
        }
    }
}

// ── FLUX CHIP ────────────────────────────────────

@Composable
fun FluxChip(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (selected) FluxGold
                else Color.Transparent
            )
            .border(
                1.dp,
                if (selected) FluxGold
                else Glow20,
                shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = label,
            style = LabelSM,
            color = if (selected) TextOnGold
                    else TextSecondary
        )
    }
}

// ── FLUX BADGE ───────────────────────────────────

@Composable
fun FluxBadge(
    label: String,
    color: Color = FluxGold,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.15f))
            .border(
                0.8.dp,
                color.copy(alpha = 0.5f),
                RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text  = label,
            style = LabelSM,
            color = color
        )
    }
}

// ── FLUX TOP BAR ─────────────────────────────────

@Composable
fun FluxTopBar(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Bg1)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        navigationIcon?.invoke()
        if (navigationIcon != null)
            Spacer(Modifier.width(8.dp))
        Text(
            text     = title,
            style    = HeadingLG,
            color    = TextHero,
            fontFamily = OrbitronFamily
        )
        Spacer(Modifier.weight(1f))
        Row(content = actions)
    }
}

// ── FLUX PROGRESS BAR ────────────────────────────

@Composable
fun FluxProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String = ""
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Bg4)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress
                        .coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(FluxGold, FluxAmber)
                        )
                    )
            )
        }
        if (label.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(label, style = BodySM,
                 color = TextMuted)
        }
    }
}

// ── FLUX TOGGLE ──────────────────────────────────

@Composable
fun FluxToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label,   style = BodyLG,
                 color = TextPrimary)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, style = BodySM,
                     color = TextMuted)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor      = TextOnGold,
                checkedTrackColor      = FluxGold,
                uncheckedThumbColor    = TextMuted,
                uncheckedTrackColor    = Bg4,
                uncheckedBorderColor   = Glow20
            )
        )
    }
}

// ── FLUX SLIDER ──────────────────────────────────

@Composable
fun FluxSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    label: String,
    valueLabel: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = BodyMD,
                 color = TextPrimary)
            FluxBadge(valueLabel)
        }
        Spacer(Modifier.height(8.dp))
        Slider(
            value         = value,
            onValueChange = onValueChange,
            valueRange    = valueRange,
            colors = SliderDefaults.colors(
                thumbColor            = FluxGold,
                activeTrackColor      = FluxGold,
                inactiveTrackColor    = Bg4
            )
        )
    }
}

// ── FLUX DIVIDER ─────────────────────────────────

@Composable
fun FluxDivider(withGlow: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                if (withGlow) Glow20 else Bg4
            )
    )
}

// ── FLUX SECTION HEADER ──────────────────────────

@Composable
fun FluxSectionHeader(
    title: String,
    actionLabel: String = "",
    onAction: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text       = title,
            style      = HeadingSM,
            color      = FluxGold,
            fontFamily = OrbitronFamily
        )
        if (actionLabel.isNotEmpty()) {
            Text(
                text     = actionLabel,
                style    = LabelSM,
                color    = FluxGold,
                modifier = Modifier.clickable(
                    onClick = onAction)
            )
        }
    }
}
