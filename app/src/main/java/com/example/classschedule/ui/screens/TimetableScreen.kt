package com.example.classschedule.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classschedule.data.model.Course
import com.example.classschedule.ui.components.TimetableGrid
import com.example.classschedule.ui.components.WeekSelector
import com.example.classschedule.ui.viewmodel.SemesterViewModel
import com.example.classschedule.ui.viewmodel.TimetableEvent
import com.example.classschedule.ui.viewmodel.TimetableViewModel

/**
 * 主课表界面
 *
 * 完整组装课表的所有 UI 组件：
 * - TopAppBar：学期名称（可点击切换）、周末开关、添加按钮、更多菜单
 * - WeekSelector：周次切换
 * - TimetableGrid：课表网格
 * - 空状态提示
 * - 各种对话框：添加/编辑课程、学期管理、冲突提示、导入确认
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    timetableViewModel: TimetableViewModel,
    semesterViewModel: SemesterViewModel
) {
    val context = LocalContext.current
    val uiState by timetableViewModel.uiState.collectAsState()
    val filteredCourses by timetableViewModel.filteredCourses.collectAsState()
    val currentWeek by timetableViewModel.currentWeek.collectAsState()
    val showWeekend by timetableViewModel.showWeekend.collectAsState()
    val semesters by semesterViewModel.semesters.collectAsState()
    val currentSemester by semesterViewModel.currentSemester.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // 对话框状态
    var showAddDialog by remember { mutableStateOf(false) }
    var editCourse by remember { mutableStateOf<Course?>(null) }
    var showSemesterDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    // 添加对话框的预设值（点击空白格子时的 day 和 section）
    var presetDay by remember { mutableIntStateOf(1) }
    var presetSection by remember { mutableIntStateOf(1) }

    // 冲突对话框状态
    var conflictData by remember { mutableStateOf<Pair<Course, List<Course>>?>(null) }

    // 导入确认对话框状态
    var importData by remember { mutableStateOf<com.example.classschedule.utils.BackupData?>(null) }

    // SAF 文件选择器
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { timetableViewModel.exportBackup(context, it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { timetableViewModel.importBackup(context, it) }
    }

    // 处理一次性事件（Snackbar、冲突对话框等）
    LaunchedEffect(Unit) {
        timetableViewModel.events.collect { event ->
            when (event) {
                is TimetableEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is TimetableEvent.ClearSnackbar -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
                is TimetableEvent.ShowConflictDialog -> {
                    conflictData = Pair(event.course, event.conflicts)
                }
                is TimetableEvent.ShowImportConfirmDialog -> {
                    importData = event.backupData
                }
            }
        }
    }

    // 当前学期变化时自动加载对应课程
    LaunchedEffect(currentSemester?.id) {
        currentSemester?.id?.let { timetableViewModel.loadCourses(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentSemester?.name ?: "极简课表",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    // 周末显示切换按钮
                    IconButton(onClick = { timetableViewModel.toggleWeekend() }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = if (showWeekend) "隐藏周末" else "显示周末",
                            tint = if (showWeekend) Color.White else Color.White.copy(alpha = 0.5f)
                        )
                    }

                    // 添加课程按钮
                    IconButton(onClick = {
                        presetDay = 1
                        presetSection = 1
                        showAddDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加课程"
                        )
                    }

                    // 更多菜单
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "更多操作"
                            )
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            // 学期管理
                            DropdownMenuItem(
                                text = { Text("学期管理") },
                                onClick = {
                                    showOverflowMenu = false
                                    showSemesterDialog = true
                                }
                            )
                            // 导出备份
                            DropdownMenuItem(
                                text = { Text("导出备份") },
                                onClick = {
                                    showOverflowMenu = false
                                    exportLauncher.launch("classschedule_backup.json")
                                }
                            )
                            // 导入备份
                            DropdownMenuItem(
                                text = { Text("导入备份") },
                                onClick = {
                                    showOverflowMenu = false
                                    importLauncher.launch(arrayOf("application/json"))
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        containerColor = Color(0xFFFAFAFA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ---- 周次选择器 ----
            WeekSelector(
                currentWeek = currentWeek,
                onWeekChanged = { timetableViewModel.setCurrentWeek(it) }
            )

            // ---- 课表网格 / 空状态 ----
            if (filteredCourses.isEmpty()) {
                // 空状态
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "点击右上角 + 添加课程",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                }
            } else {
                // 课表网格
                TimetableGrid(
                    courses = filteredCourses,
                    showWeekend = showWeekend,
                    sectionCount = 12,
                    onEmptyCellTap = { day, section ->
                        presetDay = day
                        presetSection = section
                        showAddDialog = true
                    },
                    onCourseLongClick = { course ->
                        editCourse = course
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // ========== 添加课程对话框 ==========
    if (showAddDialog) {
        AddEditCourseDialog(
            initialCourse = null,
            defaultDayOfWeek = presetDay,
            defaultSection = presetSection,
            semesterId = currentSemester?.id ?: 0,
            onDismiss = { showAddDialog = false },
            onSave = { course ->
                timetableViewModel.addCourse(course)
                showAddDialog = false
            }
        )
    }

    // ========== 编辑课程对话框 ==========
    editCourse?.let { course ->
        AddEditCourseDialog(
            initialCourse = course,
            semesterId = currentSemester?.id ?: 0,
            onDismiss = { editCourse = null },
            onSave = { updatedCourse ->
                timetableViewModel.updateCourse(updatedCourse)
                editCourse = null
            }
        )
    }

    // ========== 课程长按操作菜单 ==========
    editCourse?.let { course ->
        // 使用 AlertDialog 实现编辑/删除菜单
        AlertDialog(
            onDismissRequest = { editCourse = null },
            title = { Text(course.name) },
            text = {
                Column {
                    Text("教师: ${course.teacher ?: "无"}")
                    Text("地点: ${course.location ?: "无"}")
                    Text("周次: ${course.startWeek}-${course.endWeek}周")
                    Text("节次: 第${course.startSection}-${course.endSection}节")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val current = editCourse
                        editCourse = null
                        // 重新打开编辑对话框
                        current?.let {
                            editCourse = it
                            // 直接使用编辑模式
                        }
                    }
                ) {
                    Text("编辑")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        timetableViewModel.deleteCourse(course)
                        editCourse = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    // ========== 学期管理对话框 ==========
    if (showSemesterDialog) {
        SemesterManagementDialog(
            semesters = semesters,
            currentSemester = currentSemester,
            onSwitchSemester = { semesterViewModel.switchSemester(it) },
            onAddSemester = { semesterViewModel.addSemester(it) },
            onRenameSemester = { semester, newName ->
                semesterViewModel.renameSemester(semester, newName)
            },
            onDeleteSemester = { semesterViewModel.deleteSemester(it) },
            onDismiss = { showSemesterDialog = false }
        )
    }

    // ========== 冲突确认对话框 ==========
    conflictData?.let { (newCourse, conflicts) ->
        AlertDialog(
            onDismissRequest = { conflictData = null },
            title = { Text("时间冲突") },
            text = {
                Column {
                    Text("以下课程与当前课程时间冲突：")
                    conflicts.forEach { c ->
                        Text(
                            text = "· ${c.name}（周${c.startWeek}-${c.endWeek}，第${c.startSection}-${c.endSection}节）",
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "\n是否仍然保存？",
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCourse.id == 0L) {
                            timetableViewModel.forceAddCourse(newCourse)
                        } else {
                            timetableViewModel.forceUpdateCourse(newCourse)
                        }
                        conflictData = null
                    }
                ) {
                    Text("仍然保存", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { conflictData = null }) {
                    Text("取消")
                }
            }
        )
    }

    // ========== 导入确认对话框 ==========
    importData?.let { data ->
        AlertDialog(
            onDismissRequest = { importData = null },
            title = { Text("导入备份") },
            text = {
                Column {
                    Text("即将导入以下数据：")
                    Text("学期数: ${data.semesters.size}")
                    Text("课程数: ${data.courses.size}")
                    Text("导出时间: ${data.exportTime}")
                    Text(
                        text = "\n导入模式：",
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        timetableViewModel.confirmImport(data, overwrite = false)
                        importData = null
                    }
                ) {
                    Text("合并导入")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            timetableViewModel.confirmImport(data, overwrite = true)
                            importData = null
                        }
                    ) {
                        Text("覆盖导入")
                    }
                    TextButton(onClick = { importData = null }) {
                        Text("取消")
                    }
                }
            }
        )
    }
}
