package com.wifiprint.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary — Deep vibrant indigo ──────────────────────────────────
val Primary = Color(0xFF4F46E5)        // Vibrant Indigo
val PrimaryDark = Color(0xFF3525CD)    // Deep Indigo
val PrimaryLight = Color(0xFFE2DFFF)   // Indigo tint

// ── Secondary — Rich violet ────────────────────────────────────────
val Secondary = Color(0xFF7C3AED)      // Violet 600
val SecondaryLight = Color(0xFFEDE9FE) // Violet 50

// ── Tertiary — Teal for connectivity & accents ─────────────────────
val Tertiary = Color(0xFF0D9488)       // Teal 600
val TertiaryLight = Color(0xFFCCFBF1)  // Teal 50

// ── Accent — Teal for secondary actions (legacy alias) ─────────────
val Accent = Tertiary
val AccentLight = TertiaryLight

// ── Gradient helpers ───────────────────────────────────────────────
val GradientStart = Primary             // #4F46E5
val GradientEnd = Secondary             // #7C3AED

// ── Status colors ──────────────────────────────────────────────────
val Green400 = Color(0xFF66BB6A)
val Green50 = Color(0xFFE8F5E9)
val Orange400 = Color(0xFFFFA726)
val Orange50 = Color(0xFFFFF3E0)
val Red400 = Color(0xFFEF5350)
val Red50 = Color(0xFFFFEBEE)
val Cyan400 = Color(0xFF26C6DA)
val Cyan50 = Color(0xFFE0F7FA)

// ── Surfaces — Cool white palette ──────────────────────────────────
val BgLight = Color(0xFFF8FAFC)         // Page background (cooler)
val SurfaceWhite = Color(0xFFFFFFFF)    // Card surfaces
val SurfaceLight = Color(0xFFF1F5F9)    // Muted card variant
val DividerColor = Color(0xFFE2E8F0)    // Borders and dividers

// ── Text ───────────────────────────────────────────────────────────
val TextPrimary = Color(0xFF1B2559)     // Deep navy — high readability
val TextSecondary = Color(0xFF68769F)   // Muted blue-gray
val TextHint = Color(0xFFA3AED0)        // Placeholder text

// ── Shadow tints ───────────────────────────────────────────────────
val ShadowIndigo = Color(0x0D4F46E5)    // 5% indigo for card shadows

// ── Legacy aliases for backward compatibility ──────────────────────
val Purple600 = Primary
val Purple400 = Color(0xFF7986CB)
val Purple800 = PrimaryDark
val Cyan600 = Accent
