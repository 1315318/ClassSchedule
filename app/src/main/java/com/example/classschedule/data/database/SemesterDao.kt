package com.example.classschedule.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.classschedule.data.model.Semester
import kotlinx.coroutines.flow.Flow

/**
 * 学期数据访问对象（DAO）
 *
 * 提供学期管理的数据库操作。
 * is_current 字段通过 clearCurrentFlag + setCurrent 组合来保证
 * 同一时间只有一个学期为当前选中状态。
 */
@Dao
interface SemesterDao {

    /** 获取所有学期，按 ID 升序排列 */
    @Query("SELECT * FROM semesters ORDER BY id ASC")
    fun getAllSemesters(): Flow<List<Semester>>

    /** 获取当前选中的学期（可能为 null，首次启动时） */
    @Query("SELECT * FROM semesters WHERE is_current = 1 LIMIT 1")
    fun getCurrentSemester(): Flow<Semester?>

    /** 插入学期，返回新生成的 ID */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(semester: Semester): Long

    /** 更新学期信息 */
    @Update
    suspend fun update(semester: Semester)

    /** 删除学期（关联课程由 Room 外键级联删除） */
    @Delete
    suspend fun delete(semester: Semester)

    /** 清除所有学期的"当前"标记 */
    @Query("UPDATE semesters SET is_current = 0")
    suspend fun clearCurrentFlag()

    /** 设置指定学期为当前选中 */
    @Query("UPDATE semesters SET is_current = 1 WHERE id = :id")
    suspend fun setCurrent(id: Long)

    /** 根据名称获取学期（用于导入时判断是否存在） */
    @Query("SELECT * FROM semesters WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Semester?
}
