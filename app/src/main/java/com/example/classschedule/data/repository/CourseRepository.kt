package com.example.classschedule.data.repository

import com.example.classschedule.data.database.CourseDao
import com.example.classschedule.data.model.Course
import kotlinx.coroutines.flow.Flow

/**
 * 课程数据仓库
 *
 * 封装 CourseDao 操作，提供课程 CRUD 接口。
 * 所有查询返回 Flow，UI 层通过 collect 自动响应数据变更。
 */
class CourseRepository(private val courseDao: CourseDao) {

    /** 获取指定学期下所有课程的 Flow */
    fun getCoursesBySemester(semesterId: Long): Flow<List<Course>> =
        courseDao.getCoursesBySemester(semesterId)

    /** 根据 ID 获取单个课程 */
    suspend fun getCourseById(id: Long): Course? = courseDao.getCourseById(id)

    /** 插入课程，返回新 ID */
    suspend fun insert(course: Course): Long = courseDao.insert(course)

    /** 更新课程 */
    suspend fun update(course: Course) = courseDao.update(course)

    /** 删除课程 */
    suspend fun delete(course: Course) = courseDao.delete(course)

    /** 删除指定学期下所有课程（批量导入前清理） */
    suspend fun deleteBySemester(semesterId: Long) = courseDao.deleteBySemester(semesterId)

    /** 批量插入课程 */
    suspend fun insertAll(courses: List<Course>) = courseDao.insertAll(courses)
}
