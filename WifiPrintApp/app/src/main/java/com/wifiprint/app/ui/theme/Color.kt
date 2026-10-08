package com.wifiprint.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary — Emerald & Forest Green (Printora Website Brand) ────────
val Primary = Color(0xFF0A7746)        // Forest Emerald Green 700 (Website --primary)
val PrimaryDark = Color(0xFF08633A)    // Deep Forest Green (Website --primary-hover)
val PrimaryLight = Color(0xFFE8F5EE)   // Soft Emerald Container
val PrimaryLuminous = Color(0xFF10B981) // Emerald 500 (Website Dark --primary)

// ── Secondary — Luminous Mint & Emerald ──────────────────────────────
val Secondary = Color(0xFF10B981)      // Emerald 500 (Website --secondary)
val SecondaryLight = Color(0xFFECFDF5) // Soft Mint 50
val SecondaryLuminous = Color(0xFF34D399) // Mint 400 (Website Dark --accent / --success)

// ── Tertiary — Deep Teal / Emerald Accent ────────────────────────────
val Tertiary = Color(0xFF059669)       // Emerald 600 (Website --accent)
val TertiaryLight = Color(0xFFF2F6F3)  // Website Muted Container
val TertiaryLuminous = Color(0xFF6EE7B7) // Mint 300

// ── Accent (Legacy Aliases) ──────────────────────────────────────────
val Accent = Secondary
val AccentLight = SecondaryLight

// ── Status Colors (WCAG AA Compliant) ────────────────────────────────
val Green500 = Color(0xFF10B981)       // Emerald 500
val Green400 = Color(0xFF34D399)       // Emerald 400
val Green50 = Color(0xFFECFDF5)        // Emerald 50
val GreenDarkBg = Color(0xFF064E3B)    // Dark green container

val Orange500 = Color(0xFFF59E0B)      // Amber 500 (Website --warning)
val Orange400 = Color(0xFFFBBF24)      // Amber 400 (Website Dark --warning)
val Orange50 = Color(0xFFFFFBEB)       // Amber 50
val OrangeDarkBg = Color(0xFF78350F)   // Dark amber container

val Red500 = Color(0xFFEF4444)         // Red 500 (Website --destructive)
val Red400 = Color(0xFFF87171)         // Red 400 (Website Dark --destructive)
val Red50 = Color(0xFFFEF2F2)          // Red 50
val RedDarkBg = Color(0xFF7F1D1D)      // Dark red container

val Cyan500 = Color(0xFF06B6D4)        // Cyan 500
val Cyan400 = Color(0xFF22D3EE)        // Cyan 400
val Cyan50 = Color(0xFFECFEFF)         // Cyan 50
val CyanDarkBg = Color(0xFF164E63)     // Dark cyan container

// ── Light Surfaces (Website --background: #FBFCF9) ───────────────────
val BgLight = Color(0xFFFBFCF9)        // Warm off-white (Website --background)
val SurfaceWhite = Color(0xFFFFFFFF)   // Pure White Card (Website --card)
val SurfaceLight = Color(0xFFF2F6F3)   // Website Muted Gray-Green (Website --muted)
val DividerColor = Color(0xFFE2E8E3)   // Website Border (Website --border)

// ── Dark Surfaces (Website --background: #08120D, --card: #0E1B13) ──
val BgDark = Color(0xFF08120D)         // Forest Midnight (Website Dark --background)
val SurfaceDark = Color(0xFF0E1B13)    // Deep Forest Card (Website Dark --card)
val SurfaceDarkElevated = Color(0xFF13241A) // Muted Forest Surface (Website Dark --muted)
val DividerDark = Color(0xFF182E21)    // Forest Border (Website Dark --border)

// ── Light Text (Website --foreground: #0E171B) ───────────────────────
val TextPrimary = Color(0xFF0E171B)    // Charcoal Slate (Website --foreground)
val TextSecondary = Color(0xFF5E6D65)  // Forest Muted Slate (Website --muted-foreground)
val TextHint = Color(0xFF8FA397)       // Subdued Sage

// ── Dark Text (Website --foreground: #F4FAF6) ────────────────────────
val TextPrimaryDark = Color(0xFFF4FAF6) // Crisp Off-White (Website Dark --foreground)
val TextSecondaryDark = Color(0xFF8FA397) // Sage Muted Slate (Website Dark --muted-foreground)
val TextHintDark = Color(0xFF5E6D65)    // Muted Sage

// ── Gradients ────────────────────────────────────────────────────────
val GradientStart = Primary            // #0A7746
val GradientEnd = Secondary            // #10B981
val GradientStartDark = Color(0xFF0A7746)
val GradientEndDark = Color(0xFF059669)

// ── Shadow tints ─────────────────────────────────────────────────────
val ShadowIndigo = Color(0x140A7746)

// ── Legacy aliases for backward compatibility ────────────────────────
val Purple600 = Primary
val Purple400 = Color(0xFF34D399)
val Purple800 = PrimaryDark
val Cyan600 = Tertiary
