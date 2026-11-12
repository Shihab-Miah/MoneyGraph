package com.miah_shihab.moneygraph.ui.theme

import androidx.compose.ui.graphics.Color

// Default brand colors
val PrimaryBlue = Color(0xFF2563EB)
val PrimaryBlueDark = Color(0xFF1D4ED8)
val SecondaryTeal = Color(0xFF0D9488)
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF64748B)

val BackgroundDark = Color(0xFF0B1120)
val SurfaceDark = Color(0xFF1E293B)
val TextPrimaryDark = Color(0xFFF1F5F9)
val TextSecondaryDark = Color(0xFF94A3B8)

// Semantic financial colors
val IncomeGreen = Color(0xFF16A34A)
val IncomeGreenContainer = Color(0xFFDCFCE7)
val ExpenseRed = Color(0xFFDC2626)
val ExpenseRedContainer = Color(0xFFFEE2E2)
val TransferBlue = Color(0xFF2563EB)
val TransferBlueContainer = Color(0xFFDBEAFE)
val LoanOrange = Color(0xFFD97706)
val LoanOrangeContainer = Color(0xFFFEF3C7)

// Color scheme palettes for user selection
data class ThemePalette(
    val id: Long,
    val name: String,
    val primary: Color,
    val secondary: Color
)

val AvailablePalettes = listOf(
    ThemePalette(0xFF2563EBL, "Ocean Blue", Color(0xFF2563EB), Color(0xFF0D9488)),
    ThemePalette(0xFF7C3AEDL, "Royal Violet", Color(0xFF7C3AED), Color(0xFFDB2777)),
    ThemePalette(0xFF059669L, "Emerald Green", Color(0xFF059669), Color(0xFF0284C7)),
    ThemePalette(0xFFEA580CL, "Sunset Orange", Color(0xFFEA580C), Color(0xFFE11D48)),
    ThemePalette(0xFF0284C7L, "Sky Cyan", Color(0xFF0284C7), Color(0xFF4F46E5)),
    ThemePalette(0xFF4F46E5L, "Indigo Night", Color(0xFF4F46E5), Color(0xFF9333EA)),
    ThemePalette(0xFFDB2777L, "Rose Pink", Color(0xFFDB2777), Color(0xFFF59E0B)),
    ThemePalette(0xFF475569L, "Slate Grey", Color(0xFF475569), Color(0xFF0EA5E9))
)
