package com.movtery.zalithlauncher.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val OrbitronFamily = FontFamily.SansSerif
val InterFamily = FontFamily.SansSerif
val MonoFamily = FontFamily.Monospace

val DisplayLG  = TextStyle(fontFamily=OrbitronFamily,
    fontWeight=FontWeight.Bold,   fontSize=32.sp,
    letterSpacing=(-0.3).sp)
val HeadingLG  = TextStyle(fontFamily=OrbitronFamily,
    fontWeight=FontWeight.Bold,   fontSize=24.sp)
val HeadingMD  = TextStyle(fontFamily=OrbitronFamily,
    fontWeight=FontWeight.Medium, fontSize=20.sp)
val HeadingSM  = TextStyle(fontFamily=OrbitronFamily,
    fontWeight=FontWeight.Medium, fontSize=16.sp)
val TitleLG    = TextStyle(fontFamily=InterFamily,
    fontWeight=FontWeight.SemiBold, fontSize=18.sp)
val BodyLG     = TextStyle(fontFamily=InterFamily,
    fontWeight=FontWeight.Normal,   fontSize=16.sp)
val BodyMD     = TextStyle(fontFamily=InterFamily,
    fontWeight=FontWeight.Normal,   fontSize=14.sp)
val BodySM     = TextStyle(fontFamily=InterFamily,
    fontWeight=FontWeight.Normal,   fontSize=13.sp)
val LabelLG    = TextStyle(fontFamily=InterFamily,
    fontWeight=FontWeight.SemiBold, fontSize=14.sp,
    letterSpacing=0.4.sp)
val LabelSM    = TextStyle(fontFamily=InterFamily,
    fontWeight=FontWeight.Medium,   fontSize=11.sp,
    letterSpacing=0.5.sp)
val MonoMD     = TextStyle(fontFamily=MonoFamily,
    fontWeight=FontWeight.Normal,   fontSize=13.sp)
