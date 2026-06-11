package com.example.classschedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.classschedule.data.model.Semester
import com.example.classschedule.data.repository.SemesterRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 学期管理 ViewModel
 *
 * 管理学期列表的增删改查及切换操作。
 * 数据通过 StateFlow 暴露给 UI 层。
 */
class SemesterViewModel(
    private val repository: SemesterRepository
) : ViewModel() {

    /** 所有学期列表（响应式） */
    val semesters: StateFlow<List<Semester>> = repository.getAllSemesters()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** 当前选中的学期 */
    val currentSemester: StateFlow<Semester?> = repository.getCurrentSemester()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    /** 添加新学期 */
    fun addSemester(name: String) {
        viewModelScope.launch {
            repository.insert(Semester(name = name))
        }
    }

    /** 切换到指定学期 */
    fun switchSemester(id: Long) {
        viewModelScope.launch {
            repository.setCurrentSemester(id)
        }
    }

    /** 删除学期（关联课程由 Room 外键级联删除） */
    fun deleteSemester(semester: Semester) {
        viewModelScope.launch {
            repository.delete(semester)
        }
    }

    /** 重命名学期 */
    fun renameSemester(semester: Semester, newName: String) {
        viewModelScope.launch {
            repository.update(semester.copy(name = newName))
        }
    }
}
