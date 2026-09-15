package com.modulamobile.ui.theme

import androidx.compose.ui.graphics.Color

// Deep Void Background Layers
val ColorBg0 = Color(0xFF04040A) // Base / Canvas
val ColorBg1 = Color(0xFF080810) // Base / Canvas (Secondary)
val ColorBg2 = Color(0xFF0C0C16) // Card & Container Surfaces
val ColorBg3 = Color(0xFF10101C) // Card & Container Surfaces (Elevated)
val ColorBg4 = Color(0xFF161626) // Floating Surfaces
val ColorBg5 = Color(0xFF1C1C30) // Floating Surfaces (Highest)

// Golden Flux Primary Palette
val FluxGold   = Color(0xFFFFD700)
val FluxAmber  = Color(0xFFFFA500)
val FluxCopper = Color(0xFFFF6B00)
val FluxPale   = Color(0xFFFFE566)
val FluxDim    = Color(0xFFCC9900)

// Legacy Aliases for Compatibility
val GoldBright = FluxGold
val GoldMid    = FluxAmber
val GoldDeep   = FluxCopper
val GoldGlow10 = FluxGold.copy(alpha = 0.10f)
val GoldGlow20 = FluxGold.copy(alpha = 0.20f)
val GoldGlow40 = FluxGold.copy(alpha = 0.40f)
val GoldGlow60 = FluxGold.copy(alpha = 0.60f)

// Onyx System
val OnyxCanvas = Color(0xFF000000)
val OnyxAccent = Color(0xFF334155)

// Volcanic System
val VolcanicCanvas = Color(0xFF450A0A)
val VolcanicAccent = Color(0xFFDC2626)

// Neon System
val NeonCanvas = Color(0xFF1E1B4B)
val NeonAccent = Color(0xFFC026D3)

// Arctic System
val ArcticCanvas = Color(0xFF0C4A6E)
val ArcticAccent = Color(0xFF38BDF8)

// Typography & Readability Palette
val TextHero      = Color(0xFFFFFFFF)
val TextPrimary   = Color(0xFFF2F2F2)
val TextSecondary = Color(0xFFAAAAAA)
val TextMuted     = Color(0xFF5C5C78)
val TextGold      = FluxGold
val TextOnGold    = Color(0xFF060609)

// Functional Status Colors
val ColorSuccess = Color(0xFF22C55E)
val ColorWarning = Color(0xFFF59E0B)
val ColorError   = Color(0xFFEF4444)
val ColorInfo    = Color(0xFF3B82F6)
