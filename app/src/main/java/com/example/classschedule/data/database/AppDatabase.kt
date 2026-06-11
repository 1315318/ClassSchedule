package com.example.classschedule.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.classschedule.data.model.Course
import com.example.classschedule.data.model.Semester

/**
 * Room 数据库类
 *
 * 单例模式，确保整个应用共用一个数据库实例。
 * 版本 1 — 初始版本，包含学期表和课程表。
 *
 * fallbackToDestructiveMigration()：开发阶段若 schema 变更则重建数据库，
 * 正式发布前应替换为 Migration 策略。
 */
@Database(
    entities = [Course::class, Semester::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao
    abstract fun semesterDao(): SemesterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 获取数据库单例
         * @param context 应用上下文（自动转为 applicationContext 避免泄漏）
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "classschedule.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
