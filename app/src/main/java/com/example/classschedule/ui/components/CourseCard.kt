package com.example.classschedule.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classschedule.data.model.Course
import com.example.classschedule.ui.theme.CourseColors

/**
 * 课程卡片组件
 *
 * 在课表网格中显示单门课程，包含课程名、地点、教师等信息。
 * 背景色根据课程的 colorIndex 从预设颜色数组中选取。
 *
 * 支持单击和长按操作：
 * - 单击：查看课程详情（预留）
 * - 长按：弹出编辑/删除菜单
 *
 * 文字溢出时自动省略，适应不同卡片高度。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CourseCard(
    course: Course,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    // 根据 colorIndex 获取背景色，越界时回退到第一个颜色
    val bgColor = CourseColors.getOrElse(course.colorIndex) { CourseColors[0] }
    // 文字颜色：白色（深色背景上保持可读性）
    val textColor = Color.White

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(6.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column {
            // 课程名称 — 粗体，最多 2 行
            Text(
                text = course.name,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            // 上课地点 — 若有则显示，半透明
            if (!course.location.isNullOrBlank()) {
                Text(
                    text = course.location,
                    color = textColor.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }

            // 教师姓名 — 若有则显示，半透明
            if (!course.teacher.isNullOrBlank()) {
                Text(
                    text = course.teacher,
                    color = textColor.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
