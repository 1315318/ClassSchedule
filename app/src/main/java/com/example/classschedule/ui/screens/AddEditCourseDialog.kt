package com.example.classschedule.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.classschedule.data.model.Course
import com.example.classschedule.ui.theme.CourseColors

/**
 * 添加/编辑课程对话框
 *
 * 使用 AlertDialog 作为容器，内部包含可滚动的表单。
 * 表单字段：
 * - 课程名称（必填）
 * - 教师姓名（可选）
 * - 上课地点（可选）
 * - 星期选择（7 个切换按钮）
 * - 起始周 / 结束周（数字选择器）
 * - 周类型（全部/单周/双周）
 * - 起始节 / 结束节（数字选择器）
 * - 颜色选择（10 种预设颜色）
 *
 * @param initialCourse 编辑模式下传入已有课程，新增时为 null
 * @param defaultDayOfWeek 点击空白格子时预设的星期
 * @param defaultSection 点击空白格子时预设的节次
 * @param onDismiss 关闭对话框回调
 * @param onSave 保存课程回调
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditCourseDialog(
    initialCourse: Course? = null,
    defaultDayOfWeek: Int = 1,
    defaultSection: Int = 1,
    semesterId: Long,
    onDismiss: () -> Unit,
    onSave: (Course) -> Unit
) {
    // 表单状态 — 初始值来自已有课程或默认值
    var name by remember { mutableStateOf(initialCourse?.name ?: "") }
    var teacher by remember { mutableStateOf(initialCourse?.teacher ?: "") }
    var location by remember { mutableStateOf(initialCourse?.location ?: "") }
    var dayOfWeek by remember { mutableIntStateOf(initialCourse?.dayOfWeek ?: defaultDayOfWeek) }
    var startWeek by remember { mutableIntStateOf(initialCourse?.startWeek ?: 1) }
    var endWeek by remember { mutableIntStateOf(initialCourse?.endWeek ?: 16) }
    var weekType by remember { mutableIntStateOf(initialCourse?.weekType ?: 0) }
    var startSection by remember { mutableIntStateOf(initialCourse?.startSection ?: defaultSection) }
    var endSection by remember { mutableIntStateOf(initialCourse?.endSection ?: defaultSection) }
    var colorIndex by remember { mutableIntStateOf(initialCourse?.colorIndex ?: 0) }

    // 验证状态
    var nameError by remember { mutableStateOf(false) }

    val isEditMode = initialCourse != null
    val allDayNames = listOf("一", "二", "三", "四", "五", "六", "日")

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Text(
                text = if (isEditMode) "编辑课程" else "添加课程",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ---- 课程名称（必填） ----
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("课程名称 *") },
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("请输入课程名称") }} else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // ---- 教师姓名 ----
                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text("教师（可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // ---- 上课地点 ----
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("教室/地点（可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // ---- 星期选择 ----
                Text(
                    text = "星期",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    allDayNames.forEachIndexed { index, dayName ->
                        val day = index + 1
                        val isSelected = dayOfWeek == day
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color(0xFFE0E0E0)
                                )
                                .clickable { dayOfWeek = day },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayName,
                                color = if (isSelected) Color.White else Color.DarkGray,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // ---- 周次范围 ----
                Text(
                    text = "周次范围",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberSelector(
                        value = startWeek,
                        onValueChange = { if (it in 1..30) startWeek = it },
                        label = "起始周",
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "—",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    NumberSelector(
                        value = endWeek,
                        onValueChange = { if (it in 1..30) endWeek = it },
                        label = "结束周",
                        modifier = Modifier.weight(1f)
                    )
                }
                // 自动修正：结束周不能小于起始周
                if (endWeek < startWeek) {
                    Text(
                        text = "结束周不能小于起始周",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                // ---- 周类型 ----
                Text(
                    text = "周类型",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                val weekTypes = listOf(0 to "全部", 1 to "单周", 2 to "双周")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    weekTypes.forEach { (type, label) ->
                        val isSelected = weekType == type
                        Button(
                            onClick = { weekType = type },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color(0xFFE0E0E0)
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color.DarkGray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // ---- 节次范围 ----
                Text(
                    text = "节次范围",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberSelector(
                        value = startSection,
                        onValueChange = { if (it in 1..12) startSection = it },
                        label = "起始节",
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "—",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    NumberSelector(
                        value = endSection,
                        onValueChange = { if (it in 1..12) endSection = it },
                        label = "结束节",
                        modifier = Modifier.weight(1f)
                    )
                }
                // 自动修正：结束节不能小于起始节
                if (endSection < startSection) {
                    Text(
                        text = "结束节不能小于起始节",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                // ---- 颜色选择 ----
                Text(
                    text = "卡片颜色",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CourseColors.forEachIndexed { index, color ->
                        val isSelected = colorIndex == index
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (isSelected) Modifier.border(
                                        3.dp,
                                        Color.DarkGray,
                                        CircleShape
                                    ) else Modifier
                                )
                                .clickable { colorIndex = index }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // 表单验证
                    if (name.isBlank()) {
                        nameError = true
                        return@TextButton
                    }
                    if (endSection < startSection || endWeek < startWeek) {
                        return@TextButton
                    }

                    // 构建 Course 对象
                    onSave(
                        Course(
                            id = initialCourse?.id ?: 0,
                            semesterId = initialCourse?.semesterId ?: semesterId,
                            name = name.trim(),
                            teacher = teacher.trim().ifBlank { null },
                            location = location.trim().ifBlank { null },
                            dayOfWeek = dayOfWeek,
                            startWeek = startWeek,
                            endWeek = endWeek.coerceAtLeast(startWeek),
                            weekType = weekType,
                            startSection = startSection,
                            endSection = endSection.coerceAtLeast(startSection),
                            colorIndex = colorIndex
                        )
                    )
                }
            ) {
                Text(
                    text = if (isEditMode) "保存" else "添加",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 数字选择器（+/- 按钮）
 *
 * 用于周次和节次的数值增减，带有范围限制。
 */
@Composable
private fun NumberSelector(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // 减少按钮
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
                    .clickable { onValueChange(value - 1) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "−", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            // 当前值
            Text(
                text = "$value",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            // 增加按钮
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
                    .clickable { onValueChange(value + 1) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
