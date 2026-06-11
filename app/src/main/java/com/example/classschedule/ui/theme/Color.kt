package com.example.classschedule.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 课程卡片预设颜色（10 种）
 *
 * 用户在添加课程时从这些颜色中选择，不同课程用不同颜色区分。
 * 颜色选择参考了柔和、高辨识度的色系，确保白色文字可读。
 */
val CourseColors = listOf(
    Color(0xFF5B8DEF),  // 0 - 柔和蓝 (Cornflower Blue)
    Color(0xFF44BD6F),  // 1 - 翠绿 (Emerald)
    Color(0xFFE8833A),  // 2 - 橘色 (Tangerine)
    Color(0xFFE74C5E),  // 3 - 玫红 (Rose)
    Color(0xFF9B59B6),  // 4 - 紫水晶 (Amethyst)
    Color(0xFF1ABC9C),  // 5 - 青绿 (Teal)
    Color(0xFFF1C40F),  // 6 - 向日葵黄 (Sunflower)
    Color(0xFF34495E),  // 7 - 深灰蓝 (Midnight)
    Color(0xFFE91E63),  // 8 - 珊瑚粉 (Coral Pink)
    Color(0xFF00BCD4),  // 9 - 天蓝 (Sky Blue)
)

/**
 * Material3 浅色主题色板
 * 使用柔和的蓝色作为主色调，保持界面清爽。
 */
val PrimaryLight = Color(0xFF1976D2)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFBBDEFB)
val OnPrimaryContainerLight = Color(0xFF0D47A1)

val SecondaryLight = Color(0xFF546E7A)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFCFD8DC)
val OnSecondaryContainerLight = Color(0xFF263238)

val SurfaceLight = Color(0xFFF5F5F5)
val OnSurfaceLight = Color(0xFF212121)
val SurfaceVariantLight = Color(0xFFFFFFFF)
val OnSurfaceVariantLight = Color(0xFF757575)

val BackgroundLight = Color(0xFFFAFAFA)
val OnBackgroundLight = Color(0xFF212121)

val ErrorLight = Color(0xFFD32F2F)
val OnErrorLight = Color(0xFFFFFFFF)
