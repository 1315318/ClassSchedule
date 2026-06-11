package com.example.classschedule.data.repository

import com.example.classschedule.data.database.SemesterDao
import com.example.classschedule.data.model.Semester
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * 学期数据仓库
 *
 * 封装 SemesterDao 操作，同时负责首次启动时创建默认学期。
 * 业务逻辑层通过 Repository 访问数据，不直接操作 DAO。
 */
class SemesterRepository(private val semesterDao: SemesterDao) {

    /** 获取所有学期的 Flow */
    fun getAllSemesters(): Flow<List<Semester>> = semesterDao.getAllSemesters()

    /** 获取当前选中学期 */
    fun getCurrentSemester(): Flow<Semester?> = semesterDao.getCurrentSemester()

    /** 插入新学期 */
    suspend fun insert(semester: Semester): Long = semesterDao.insert(semester)

    /** 更新学期 */
    suspend fun update(semester: Semester) = semesterDao.update(semester)

    /** 删除学期 */
    suspend fun delete(semester: Semester) = semesterDao.delete(semester)

    /** 切换当前学期 */
    suspend fun setCurrentSemester(id: Long) {
        semesterDao.clearCurrentFlag()
        semesterDao.setCurrent(id)
    }

    /** 根据名称查找学期 */
    suspend fun getByName(name: String): Semester? = semesterDao.getByName(name)

    /**
     * 确保至少存在一个默认学期
     * 在应用首次启动时调用，若无学期则自动创建一个。
     */
    suspend fun ensureDefaultSemester() {
        val current = semesterDao.getCurrentSemester().firstOrNull()
        if (current == null) {
            val defaultSemester = Semester(
                name = "2025年秋季学期",
                isCurrent = true
            )
            semesterDao.insert(defaultSemester)
        }
    }
}
