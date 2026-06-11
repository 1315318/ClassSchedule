package com.example.classschedule.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.model.Semester

/**
 * 学期管理对话框
 *
 * 功能：
 * - 列出所有学期
 * - 显示当前选中学期（勾选标记）
 * - 点击切换当前学期
 * - 添加学期（底部输入框 + 按钮）
 * - 重命名学期
 * - 删除学期（确认后执行）
 *
 * @param semesters 所有学期列表
 * @param currentSemester 当前选中的学期
 * @param onSwitchSemester 切换学期回调
 * @param onAddSemester 添加学期回调
 * @param onRenameSemester 重命名学期回调
 * @param onDeleteSemester 删除学期回调
 * @param onDismiss 关闭对话框回调
 */
@Composable
fun SemesterManagementDialog(
    semesters: List<Semester>,
    currentSemester: Semester?,
    onSwitchSemester: (Long) -> Unit,
    onAddSemester: (String) -> Unit,
    onRenameSemester: (Semester, String) -> Unit,
    onDeleteSemester: (Semester) -> Unit,
    onDismiss: () -> Unit
) {
    var newSemesterName by remember { mutableStateOf("") }
    var editingSemester by remember { mutableStateOf<Semester?>(null) }
    var editingName by remember { mutableStateOf("") }
    var deleteConfirmId by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "学期管理",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // ---- 学期列表 ----
                semesters.forEach { semester ->
                    val isCurrent = semester.id == currentSemester?.id
                    val isEditing = editingSemester?.id == semester.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSwitchSemester(semester.id) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 选中标记
                        RadioButton(
                            selected = isCurrent,
                            onClick = { onSwitchSemester(semester.id) }
                        )

                        // 学期名称（或编辑输入框）
                        if (isEditing) {
                            OutlinedTextField(
                                value = editingName,
                                onValueChange = { editingName = it },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            // 确认编辑
                            TextButton(
                                onClick = {
                                    onRenameSemester(semester, editingName.trim())
                                    editingSemester = null
                                }
                            ) {
                                Text("确定")
                            }
                            TextButton(
                                onClick = { editingSemester = null }
                            ) {
                                Text("取消")
                            }
                        } else {
                            Text(
                                text = semester.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )

                            // 编辑按钮
                            IconButton(
                                onClick = {
                                    editingSemester = semester
                                    editingName = semester.name
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "重命名",
                                    modifier = Modifier.height(18.dp)
                                )
                            }

                            // 删除按钮
                            IconButton(
                                onClick = {
                                    deleteConfirmId = semester.id
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "删除",
                                    modifier = Modifier.height(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ---- 添加新学期 ----
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newSemesterName,
                        onValueChange = { newSemesterName = it },
                        label = { Text("新学期名称") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newSemesterName.isNotBlank()) {
                                onAddSemester(newSemesterName.trim())
                                newSemesterName = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加学期"
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )

    // ---- 删除确认对话框 ----
    if (deleteConfirmId != null) {
        val semesterToDelete = semesters.find { it.id == deleteConfirmId }
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            title = { Text("确认删除") },
            text = {
                Text("确定要删除学期「${semesterToDelete?.name}」吗？\n该学期下的所有课程也将被删除。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        semesterToDelete?.let { onDeleteSemester(it) }
                        deleteConfirmId = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmId = null }) {
                    Text("取消")
                }
            }
        )
    }
}
