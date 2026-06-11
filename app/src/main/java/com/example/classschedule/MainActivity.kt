package com.example.classschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classschedule.ui.screens.TimetableScreen
import com.example.classschedule.ui.theme.ClassScheduleTheme
import com.example.classschedule.ui.viewmodel.SemesterViewModel
import com.example.classschedule.ui.viewmodel.TimetableViewModel

/**
 * 极简课表 — 唯一 Activity
 *
 * 使用 Jetpack Compose 渲染整个界面，无需 Fragment。
 * 通过手动 DI（Application 单例）创建 ViewModel 实例。
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ClassScheduleApp

        setContent {
            ClassScheduleTheme {
                // 创建 TimetableViewModel（手动工厂注入 Repository）
                val timetableViewModel: TimetableViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return TimetableViewModel(
                                app.courseRepository,
                                app.semesterRepository
                            ) as T
                        }
                    }
                )

                // 创建 SemesterViewModel
                val semesterViewModel: SemesterViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return SemesterViewModel(app.semesterRepository) as T
                        }
                    }
                )

                // 渲染主界面
                TimetableScreen(
                    timetableViewModel = timetableViewModel,
                    semesterViewModel = semesterViewModel
                )
            }
        }
    }
}
