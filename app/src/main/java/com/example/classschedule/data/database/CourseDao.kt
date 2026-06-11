package com.example.classschedule.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.classschedule.data.model.Course
import kotlinx.coroutines.flow.Flow

/**
 * 课程数据访问对象（DAO）
 *
 * 提供课程表的所有数据库操作。
 * 查询操作返回 Flow，实现响应式数据更新。
 * 写操作为挂起函数，在协程中执行。
 */
@Dao
interface CourseDao {

    /** 获取指定学期下的所有课程（响应式 Flow，数据变更自动通知 UI） */
    @Query("SELECT * FROM courses WHERE semester_id = :semesterId ORDER BY day_of_week, start_section")
    fun getCoursesBySemester(semesterId: Long): Flow<List<Course>>

    /** 根据 ID 获取单个课程 */
    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Long): Course?

    /** 插入课程，返回新生成的 ID */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: Course): Long

    /** 更新课程 */
    @Update
    suspend fun update(course: Course)

    /** 删除课程 */
    @Delete
    suspend fun delete(course: Course)

    /** 删除指定学期下的所有课程（用于批量导入覆盖） */
    @Query("DELETE FROM courses WHERE semester_id = :semesterId")
    suspend fun deleteBySemester(semesterId: Long)

    /** 批量插入课程（用于备份导入） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<Course>)
}
