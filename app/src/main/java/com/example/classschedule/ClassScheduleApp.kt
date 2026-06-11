package com.example.classschedule

import android.app.Application
import com.example.classschedule.data.database.AppDatabase
import com.example.classschedule.data.repository.CourseRepository
import com.example.classschedule.data.repository.SemesterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 极简课表 Application 类
 *
 * 使用手动依赖注入（无第三方 DI 框架），在 Application 层创建
 * 数据库实例和 Repository 单例，通过 ViewModelFactory 注入到 ViewModel。
 *
 * 应用启动时自动检查并创建默认学期（若数据库为空）。
 */
class ClassScheduleApp : Application() {

    /** 数据库单例 */
    lateinit var database: AppDatabase
        private set

    /** 课程数据仓库 */
    lateinit var courseRepository: CourseRepository
        private set

    /** 学期数据仓库 */
    lateinit var semesterRepository: SemesterRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // 初始化数据库和仓库
        database = AppDatabase.getInstance(this)
        courseRepository = CourseRepository(database.courseDao())
        semesterRepository = SemesterRepository(database.semesterDao())

        // 异步确保默认学期存在（首次启动时自动创建）
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            semesterRepository.ensureDefaultSemester()
        }
    }
}
