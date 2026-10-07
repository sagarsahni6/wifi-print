package com.wifiprint.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary — Luminous & Vibrant Indigo ──────────────────────
val Primary = Color(0xFF4F46E5)        // Vibrant Indigo 600
val PrimaryDark = Color(0xFF3730A3)    // Indigo 800
val PrimaryLight = Color(0xFFEEF2FF)   // Indigo 50
val PrimaryLuminous = Color(0xFF818CF8) // Indigo 400 for Dark Mode

// ── Secondary — Rich Violet ──────────────────────────────────
val Secondary = Color(0xFF7C3AED)      // Violet 600
val SecondaryLight = Color(0xFFF5F3FF) // Violet 50
val SecondaryLuminous = Color(0xFFA78BFA) // Violet 400 for Dark Mode

// ── Tertiary — Electric Teal for connectivity & status ───────
val Tertiary = Color(0xFF0D9488)       // Teal 600
val TertiaryLight = Color(0xFFF0FDFA)  // Teal 50
val TertiaryLuminous = Color(0xFF2DD4BF) // Teal 400 for Dark Mode

// ── Accent (Legacy Aliases) ──────────────────────────────────
val Accent = Tertiary
val AccentLight = TertiaryLight

// ── Status Colors (WCAG AA Compliant) ────────────────────────
val Green500 = Color(0xFF10B981)       // Emerald 500
val Green400 = Color(0xFF34D399)       // Emerald 400
val Green50 = Color(0xFFECFDF5)        // Emerald 50
val GreenDarkBg = Color(0xFF064E3B)    // Dark green container

val Orange500 = Color(0xFFF59E0B)      // Amber 500
val Orange400 = Color(0xFFFBBF24)      // Amber 400
val Orange50 = Color(0xFFFFFBEB)       // Amber 50
val OrangeDarkBg = Color(0xFF78350F)   // Dark amber container

val Red500 = Color(0xFFEF4444)         // Red 500
val Red400 = Color(0xFFF87171)         // Red 400
val Red50 = Color(0xFFFEF2F2)          // Red 50
val RedDarkBg = Color(0xFF7F1D1D)      // Dark red container

val Cyan500 = Color(0xFF06B6D4)        // Cyan 500
val Cyan400 = Color(0xFF22D3EE)        // Cyan 400
val Cyan50 = Color(0xFFECFEFF)         // Cyan 50
val CyanDarkBg = Color(0xFF164E63)     // Dark cyan container

// ── Light Surfaces (Cool Slate) ──────────────────────────────
val BgLight = Color(0xFFF8FAFC)        // Slate 50
val SurfaceWhite = Color(0xFFFFFFFF)   // Pure White
val SurfaceLight = Color(0xFFF1F5F9)   // Slate 100
val DividerColor = Color(0xFFE2E8F0)   // Slate 200

// ── Dark Surfaces (Deep Rich Obsidian / Slate) ───────────────
val BgDark = Color(0xFF0B0F19)         // Obsidian Slate
val SurfaceDark = Color(0xFF131B2E)    // Card Surface Dark
val SurfaceDarkElevated = Color(0xFF1E293B) // Slate 800
val DividerDark = Color(0xFF26334D)    // Dark Border/Divider

// ── Light Text ───────────────────────────────────────────────
val TextPrimary = Color(0xFF0F172A)    // Slate 900 (High contrast: >15:1)
val TextSecondary = Color(0xFF475569)  // Slate 600 (High contrast: >6:1)
val TextHint = Color(0xFF94A3B8)       // Slate 400

// ── Dark Text ────────────────────────────────────────────────
val TextPrimaryDark = Color(0xFFF8FAFC) // Slate 50 (High contrast: >15:1)
val TextSecondaryDark = Color(0xFF94A3B8) // Slate 400 (High contrast: >6:1)
val TextHintDark = Color(0xFF64748B)    // Slate 500

// ── Gradients ────────────────────────────────────────────────
val GradientStart = Primary
val GradientEnd = Secondary
val GradientStartDark = Color(0xFF3730A3)
val GradientEndDark = Color(0xFF5B21B6)

// ── Shadow tints ─────────────────────────────────────────────
val ShadowIndigo = Color(0x0D4F46E5)

// ── Legacy aliases for backward compatibility ────────────────
val Purple600 = Primary
val Purple400 = Color(0xFF7986CB)
val Purple800 = PrimaryDark
val Cyan600 = Tertiary
