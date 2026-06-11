package com.example.classschedule.utils

import com.example.classschedule.data.model.Course
import com.example.classschedule.data.model.Semester
import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 备份数据容器
 *
 * JSON 序列化/反序列化的顶层结构，包含所有学期和课程数据。
 * version 字段用于未来格式兼容性判断。
 */
data class BackupData(
    val version: Int = 1,
    val exportTime: String,
    val semesters: List<Semester>,
    val courses: List<Course>
)

/**
 * 备份导入导出工具
 *
 * 负责 JSON 序列化/反序列化，不涉及文件 I/O。
 * 文件读写由 ViewModel 通过 ContentResolver (SAF) 完成。
 *
 * 导出 JSON 格式示例：
 * {
 *   "version": 1,
 *   "exportTime": "2025-09-01T12:00:00Z",
 *   "semesters": [...],
 *   "courses": [...]
 * }
 */
object BackupHelper {

    /** Gson 实例，使用 pretty printing 提高导出文件可读性 */
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /** 导出时间格式化器（UTC 时间） */
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * 将学期和课程数据导出为 JSON 字符串
     *
     * @param semesters 所有学期列表
     * @param courses 所有课程列表
     * @return 格式化的 JSON 字符串
     */
    fun exportToJson(semesters: List<Semester>, courses: List<Course>): String {
        val data = BackupData(
            version = 1,
            exportTime = dateFormat.format(Date()),
            semesters = semesters,
            courses = courses
        )
        return gson.toJson(data)
    }

    /**
     * 从 JSON 字符串解析备份数据
     *
     * @param json JSON 格式的备份字符串
     * @return 解析后的 BackupData，若解析失败返回 null
     */
    fun importFromJson(json: String): BackupData? {
        return try {
            gson.fromJson(json, BackupData::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
