package com.example.classschedule.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 课程实体类
 *
 * 每门课程对应课表上的一个时间段，存储所有必要信息：
 * - 所属学期、课程名、教师、地点
 * - 星期几（1=周一, 7=周日）
 * - 周次范围（如第 1-16 周）
 * - 单双周类型（0=全部, 1=单周, 2=双周）
 * - 节次范围（如第 2-3 节）
 * - 卡片颜色索引（0-9，对应预设颜色数组）
 *
 * 外键关联到学期表，删除学期时级联删除该学期下所有课程。
 */
@Entity(
    tableName = "courses",
    indices = [Index(value = ["semester_id"])],
    foreignKeys = [ForeignKey(
        entity = Semester::class,
        parentColumns = ["id"],
        childColumns = ["semester_id"],
        onDelete = ForeignKey.CASCADE  // 删除学期时自动删除关联课程
    )]
)
data class Course(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 关联的学期 ID */
    @ColumnInfo(name = "semester_id")
    val semesterId: Long,

    /** 课程名称（必填） */
    val name: String,

    /** 教师姓名（可选） */
    val teacher: String? = null,

    /** 上课地点/教室（可选） */
    val location: String? = null,

    /** 星期几：1=周一, 2=周二, ..., 7=周日 */
    @ColumnInfo(name = "day_of_week")
    val dayOfWeek: Int,

    /** 起始周次 (1-30) */
    @ColumnInfo(name = "start_week")
    val startWeek: Int,

    /** 结束周次 (1-30) */
    @ColumnInfo(name = "end_week")
    val endWeek: Int,

    /** 周类型：0=全部周, 1=单周, 2=双周 */
    @ColumnInfo(name = "week_type")
    val weekType: Int = 0,

    /** 起始节次 (1-12) */
    @ColumnInfo(name = "start_section")
    val startSection: Int,

    /** 结束节次 (1-12)，必须 >= startSection */
    @ColumnInfo(name = "end_section")
    val endSection: Int,

    /** 预设颜色索引 (0-9) */
    @ColumnInfo(name = "color_index")
    val colorIndex: Int = 0
)
