package com.example.classschedule.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.classschedule.data.model.Course
import com.example.classschedule.data.model.Semester
import com.example.classschedule.data.repository.CourseRepository
import com.example.classschedule.data.repository.SemesterRepository
import com.example.classschedule.utils.BackupData
import com.example.classschedule.utils.BackupHelper
import com.example.classschedule.utils.ConflictChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 课表 UI 状态数据类
 *
 * 单一数据类包含课表界面所需的所有状态，
 * 通过 StateFlow 暴露，避免多 Flow 状态不一致问题。
 */
data class TimetableUiState(
    val currentSemester: Semester? = null,
    val currentWeek: Int = 1,
    val showWeekend: Boolean = true,
    val isSemesterLoading: Boolean = true
)

/**
 * 课表主 ViewModel
 *
 * 管理课表的核心状态：学期切换、课程 CRUD、周次过滤、周末显示切换。
 * 同时负责备份导入导出（通过 SAF 文件选择器读写）。
 */
class TimetableViewModel(
    private val courseRepository: CourseRepository,
    private val semesterRepository: SemesterRepository
) : ViewModel() {

    /** 当前选中的周次 (1-30) */
    private val _currentWeek = MutableStateFlow(1)
    val currentWeek: StateFlow<Int> = _currentWeek.asStateFlow()

    /** 是否显示周末（周六、周日） */
    private val _showWeekend = MutableStateFlow(true)
    val showWeekend: StateFlow<Boolean> = _showWeekend.asStateFlow()

    /**
     * 单次事件通道 — 用于向 UI 发送一次性消息
     * 如：冲突检测结果、导入导出结果提示
     */
    private val _events = MutableSharedFlow<TimetableEvent>()
    val events = _events.asSharedFlow()

    /** 主 UI 状态（学期信息 + 周次 + 周末开关） */
    val uiState: StateFlow<TimetableUiState> = combine(
        semesterRepository.getCurrentSemester(),
        _currentWeek,
        _showWeekend
    ) { semester, week, weekend ->
        TimetableUiState(
            currentSemester = semester,
            currentWeek = week,
            showWeekend = weekend,
            isSemesterLoading = semester == null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TimetableUiState()
    )

    /** 当前学期课程的内部缓存 */
    private val _courses = MutableStateFlow<List<Course>>(emptyList())

    /** 过滤后的课程（根据当前周次和单双周规则） */
    val filteredCourses: StateFlow<List<Course>> = combine(
        _courses,
        _currentWeek,
        _showWeekend
    ) { allCourses, week, weekend ->
        allCourses.filter { course ->
            // 周末过滤：若不显示周末，则过滤掉周六(6)和周日(7)
            if (!weekend && course.dayOfWeek >= 6) return@filter false
            // 周次过滤（含单双周逻辑）
            ConflictChecker.isWeekActive(course, week)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    /** 课程加载 Job，用于在切换学期时取消旧的加载任务 */
    private var courseLoaderJob: Job? = null

    /**
     * 加载指定学期的课程数据
     *
     * 切换学期时自动取消旧的收集协程，避免多个 Flow 同时收集。
     */
    fun loadCourses(semesterId: Long) {
        // 取消上一次加载任务
        courseLoaderJob?.cancel()
        courseLoaderJob = viewModelScope.launch {
            courseRepository.getCoursesBySemester(semesterId).collect { courseList ->
                _courses.value = courseList
            }
        }
    }

    /** 设置当前周次 */
    fun setCurrentWeek(week: Int) {
        _currentWeek.value = week.coerceIn(1, 30)
    }

    /** 切换周末显示/隐藏 */
    fun toggleWeekend() {
        _showWeekend.value = !_showWeekend.value
    }

    /**
     * 添加课程（含冲突检测）
     *
     * 流程：
     * 1. 检测冲突
     * 2. 若有冲突，通过事件发送冲突信息，由 UI 弹窗确认
     * 3. 若无冲突，直接保存
     */
    fun addCourse(course: Course) {
        viewModelScope.launch {
            val conflicts = ConflictChecker.findConflicts(_courses.value, course)
            if (conflicts.isNotEmpty()) {
                _events.emit(TimetableEvent.ShowConflictDialog(course, conflicts))
            } else {
                doInsertCourse(course)
            }
        }
    }

    /** 强制添加课程（忽略冲突） */
    fun forceAddCourse(course: Course) {
        viewModelScope.launch {
            doInsertCourse(course)
        }
    }

    private suspend fun doInsertCourse(course: Course) {
        courseRepository.insert(course)
        _events.emit(TimetableEvent.ShowSnackbar("课程已添加"))
    }

    /** 更新课程（含冲突检测） */
    fun updateCourse(course: Course) {
        viewModelScope.launch {
            val conflicts = ConflictChecker.findConflicts(_courses.value, course)
            if (conflicts.isNotEmpty()) {
                _events.emit(TimetableEvent.ShowConflictDialog(course, conflicts))
            } else {
                doUpdateCourse(course)
            }
        }
    }

    /** 强制更新课程（忽略冲突） */
    fun forceUpdateCourse(course: Course) {
        viewModelScope.launch {
            doUpdateCourse(course)
        }
    }

    private suspend fun doUpdateCourse(course: Course) {
        courseRepository.update(course)
        _events.emit(TimetableEvent.ShowSnackbar("课程已更新"))
    }

    /** 删除课程 */
    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            courseRepository.delete(course)
            _events.emit(TimetableEvent.ShowSnackbar("课程已删除"))
        }
    }

    // ============= 备份导入导出 =============

    /**
     * 导出备份到指定 URI（SAF 文件选择器返回）
     *
     * 流程：
     * 1. 从数据库获取所有学期和课程
     * 2. 序列化为 JSON
     * 3. 通过 ContentResolver 写入 URI
     */
    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val semesters = semesterRepository.getAllSemesters().first()
                    val allCourses = mutableListOf<Course>()
                    for (semester in semesters) {
                        allCourses.addAll(
                            courseRepository.getCoursesBySemester(semester.id).first()
                        )
                    }
                    val json = BackupHelper.exportToJson(semesters, allCourses)
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(json.toByteArray(Charsets.UTF_8))
                    }
                }
                _events.emit(TimetableEvent.ShowSnackbar("导出成功"))
            } catch (e: Exception) {
                _events.emit(TimetableEvent.ShowSnackbar("导出失败: ${e.message}"))
            }
        }
    }

    /**
     * 从指定 URI 导入备份（SAF 文件选择器返回）
     *
     * 读取 JSON 并反序列化，然后提示用户选择导入方式（合并/覆盖）。
     */
    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                        ?: throw Exception("无法读取文件")
                }
                val data = BackupHelper.importFromJson(json)
                    ?: throw Exception("备份文件格式无效")

                _events.emit(TimetableEvent.ShowImportConfirmDialog(data))
            } catch (e: Exception) {
                _events.emit(TimetableEvent.ShowSnackbar("导入失败: ${e.message}"))
            }
        }
    }

    /**
     * 确认导入备份数据
     * @param overwrite true=覆盖现有数据（同名学期先删后加）, false=合并（同名学期跳过，新增学期追加）
     */
    fun confirmImport(data: BackupData, overwrite: Boolean) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    for (semester in data.semesters) {
                        val existing = semesterRepository.getByName(semester.name)
                        if (existing != null) {
                            if (overwrite) {
                                // 覆盖模式：删除旧学期及其课程，重新导入
                                courseRepository.deleteBySemester(existing.id)
                                semesterRepository.delete(existing)
                            } else {
                                // 合并模式：同名学期跳过，仅导入新课
                                // 课程直接插入到已有学期下
                                val semesterCourses = data.courses.filter {
                                    it.semesterId == semester.id
                                }.map {
                                    it.copy(id = 0, semesterId = existing.id)
                                }
                                courseRepository.insertAll(semesterCourses)
                                continue
                            }
                        }
                        // 插入新学期 + 课程
                        val newSemesterId = semesterRepository.insert(
                            semester.copy(id = 0, isCurrent = false)
                        )
                        val semesterCourses = data.courses.filter {
                            it.semesterId == semester.id
                        }.map {
                            it.copy(id = 0, semesterId = newSemesterId)
                        }
                        courseRepository.insertAll(semesterCourses)
                    }
                }
                _events.emit(TimetableEvent.ShowSnackbar("导入成功"))
            } catch (e: Exception) {
                _events.emit(TimetableEvent.ShowSnackbar("导入失败: ${e.message}"))
            }
        }
    }
}

/**
 * 课表相关的一次性事件
 *
 * SharedFlow 事件通道用于 UI 和 ViewModel 之间的单次通信，
 * 例如 Snackbar 提示、对话框触发等。
 */
sealed class TimetableEvent {
    /** 显示 Snackbar 消息 */
    data class ShowSnackbar(val message: String) : TimetableEvent()
    /** 清除 Snackbar */
    data object ClearSnackbar : TimetableEvent()
    /** 显示冲突确认对话框 */
    data class ShowConflictDialog(
        val course: Course,
        val conflicts: List<Course>
    ) : TimetableEvent()
    /** 显示导入确认对话框 */
    data class ShowImportConfirmDialog(
        val backupData: BackupData
    ) : TimetableEvent()
}
