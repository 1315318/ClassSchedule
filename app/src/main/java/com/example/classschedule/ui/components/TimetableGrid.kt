package com.example.classschedule.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classschedule.data.model.Course
import kotlin.math.roundToInt

/**
 * 课表网格组件
 *
 * 核心渲染逻辑：
 * 1. 顶部：星期标题行（周一至周五/周日，由 dayCount 控制）
 * 2. 左侧：节次序号列（1-12）
 * 3. 主体：Canvas 绘制网格线 + 绝对定位的课程卡片
 * 4. 透明覆盖层：检测空白格子的点击事件，用于添加课程
 *
 * 布局示意：
 * ┌────┬─────┬─────┬─────┬─────┬─────┐
 * │    │ 一  │ 二  │ 三  │ 四  │ 五  │  ← 星期标题
 * ├────┼─────┼─────┼─────┼─────┼─────┤
 * │ 1  │     │ 数学│     │     │     │  ← 课程卡片
 * │    │     │ 2-3节│     │     │     │
 * ├────┼─────┤     ├─────┼─────┼─────┤
 * │ 2  │     │     │     │     │     │
 * ├────┼─────┼─────┼─────┼─────┼─────┤
 * │... │     │     │     │     │     │
 * └────┴─────┴─────┴─────┴─────┴─────┘
 */
@Composable
fun TimetableGrid(
    courses: List<Course>,
    showWeekend: Boolean,
    sectionCount: Int = 12,
    onEmptyCellTap: (dayOfWeek: Int, section: Int) -> Unit,
    onCourseLongClick: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    // 根据周末开关决定显示 5 天还是 7 天
    val dayCount = if (showWeekend) 7 else 5

    // 星期标题（中文简写）
    val allDayNames = listOf("一", "二", "三", "四", "五", "六", "日")
    val dayNames = if (showWeekend) allDayNames else allDayNames.take(5)

    // 布局尺寸参数
    val sectionHeight = 80.dp
    val timeColumnWidth = 36.dp
    val headerHeight = 36.dp
    val cardPadding = 2.dp  // 卡片与格子边框的间距

    val density = LocalDensity.current
    val sectionHeightPx = with(density) { sectionHeight.toPx() }
    val timeColumnWidthPx = with(density) { timeColumnWidth.toPx() }
    val headerHeightPx = with(density) { headerHeight.toPx() }
    val cardPaddingPx = with(density) { cardPadding.toPx() }

    val gridLineColor = Color(0xFFE0E0E0)

    // 整体布局：Column 包含标题行 + 可滚动的网格主体
    Column(modifier = modifier) {
        // ========== 星期标题行 ==========
        Row(
            modifier = Modifier
                .background(Color(0xFFF0F4F8))
                .horizontalScroll(rememberScrollState())
        ) {
            // 左上角空白区域
            Box(
                modifier = Modifier
                    .width(timeColumnWidth)
                    .height(headerHeight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "节",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }
            // 星期标题
            dayNames.forEach { name ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(headerHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // ========== 可滚动的网格主体 ==========
        // 外层 horizontalScroll 支持 7 天横向滚动
        val hScrollState = rememberScrollState()
        val vScrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(hScrollState)
                .verticalScroll(vScrollState)
        ) {
            // 总宽度和总高度
            val totalWidth = timeColumnWidthPx + sectionHeightPx * dayCount  // 实际宽度按dp计算
            val totalWidthDp = timeColumnWidth + sectionHeight * dayCount
            val totalHeight = sectionHeight * sectionCount

            // 计算每天列的宽度（等分剩余空间）
            // 使用固定宽度：让每列宽度 = sectionHeight（80dp），这样格子是正方形
            val dayWidthDp = sectionHeight  // 每列 80dp 宽，匹配高度 80dp
            val gridWidthDp = timeColumnWidth + dayWidthDp * dayCount

            Box(
                modifier = Modifier
                    .width(gridWidthDp)
                    .height(totalHeight + headerHeight)
            ) {
                // ---- 第一层：绘制网格线 ----
                Canvas(
                    modifier = Modifier
                        .width(gridWidthDp)
                        .height(totalHeight)
                        .offset(y = headerHeight)
                ) {
                    val dayWidthPx = size.width / dayCount
                    val timeColPx = 0f  // 不再单独画时间列线

                    // 竖线（每天之间的分隔线，包含时间列右侧线）
                    for (i in 0..dayCount) {
                        val x = timeColumnWidthPx + i * dayWidthPx
                        drawLine(
                            color = gridLineColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = if (i == 0) 1.5f else 1f
                        )
                    }

                    // 横线（每节之间的分隔线）
                    for (i in 0..sectionCount) {
                        val y = i * sectionHeightPx
                        drawLine(
                            color = gridLineColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                    }
                }

                // ---- 第二层：节次序号 ----
                Column(
                    modifier = Modifier.offset(y = headerHeight)
                ) {
                    for (section in 1..sectionCount) {
                        Box(
                            modifier = Modifier
                                .width(timeColumnWidth)
                                .height(sectionHeight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$section",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // ---- 第三层：课程卡片（绝对定位） ----
                val dayWidthPx = with(density) { dayWidthDp.toPx() }

                // 按 dayOfWeek 分组课程，检测同一天同一时段是否有重叠课程
                val coursesByDayCell = courses.groupBy { it.dayOfWeek to it.startSection }

                courses.forEach { course ->
                    val dayIndex = course.dayOfWeek - 1  // 转为 0-based 索引
                    if (dayIndex < dayCount) {
                        // 计算卡片位置
                        val courseX = timeColumnWidthPx + dayWidthPx * dayIndex + cardPaddingPx
                        val courseY = sectionHeightPx * (course.startSection - 1) + cardPaddingPx + headerHeightPx
                        val courseW = dayWidthPx - cardPaddingPx * 2
                        val courseH = sectionHeightPx * (course.endSection - course.startSection + 1) - cardPaddingPx * 2

                        // 检测同一天同时段是否有重叠课程（处理冲突覆盖场景）
                        val overlappingCourses = courses.filter { other ->
                            other.id != course.id &&
                                other.dayOfWeek == course.dayOfWeek &&
                                other.startSection <= course.endSection &&
                                other.endSection >= course.startSection
                        }

                        val adjustedWidth = if (overlappingCourses.isNotEmpty()) {
                            // 有重叠课程时，卡片宽度减半，水平偏移
                            courseW / 2
                        } else {
                            courseW
                        }

                        val adjustedX = if (overlappingCourses.isNotEmpty()) {
                            // 判断当前课程在先还是后，决定左侧还是右侧偏移
                            val isFirst = overlappingCourses.all { course.startSection <= it.startSection }
                            if (isFirst) courseX else courseX + courseW / 2
                        } else {
                            courseX
                        }

                        CourseCard(
                            course = course,
                            onClick = { /* 单击暂不做操作，可通过长按编辑 */ },
                            onLongClick = { onCourseLongClick(course) },
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        adjustedX.roundToInt(),
                                        courseY.roundToInt()
                                    )
                                }
                                .width(with(density) { (adjustedWidth / density.density).dp })
                                .height(with(density) { (courseH / density.density).dp })
                        )
                    }
                }

                // ---- 第四层：透明触摸覆盖层，检测空白格子点击 ----
                Box(
                    modifier = Modifier
                        .width(gridWidthDp)
                        .height(totalHeight)
                        .offset(y = headerHeight)
                        .pointerInput(dayCount, sectionCount) {
                            detectTapGestures { offset ->
                                // 将触摸坐标转换为 (day, section)
                                val dayIndex = ((offset.x - timeColumnWidthPx) / dayWidthPx).toInt()
                                val sectionIndex = (offset.y / sectionHeightPx).toInt()

                                val dayOfWeek = dayIndex + 1  // 转回 1-based
                                val section = sectionIndex + 1

                                // 检查点击是否在有效范围内
                                if (dayIndex in 0 until dayCount && section in 1..sectionCount) {
                                    // 检查该位置是否已有课程
                                    val hasCourse = courses.any { course ->
                                        val cDay = course.dayOfWeek - 1
                                        cDay == dayIndex &&
                                            section >= course.startSection &&
                                            section <= course.endSection
                                    }

                                    // 仅当该格子无课程时触发添加
                                    if (!hasCourse) {
                                        onEmptyCellTap(dayOfWeek, section)
                                    }
                                }
                            }
                        }
                )
            }
        }
    }
}
