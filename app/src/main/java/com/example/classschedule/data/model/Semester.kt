package com.example.classschedule.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 学期实体类
 *
 * 每个学期独立存储课程数据，用户可通过下拉菜单切换学期。
 * isCurrent 标记当前选中的学期（同一时间只有一个学期为当前）。
 */
@Entity(tableName = "semesters")
data class Semester(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 学期名称，如 "2025秋季学期" */
    val name: String,

    /** 是否为当前选中的学期 */
    @ColumnInfo(name = "is_current")
    val isCurrent: Boolean = false
)
