# 极简课表 (Minimal Timetable)

一个极简、无广告、启动快的 Android 课表应用，采用 Kotlin + Jetpack Compose + MVVM + Room 架构。

## 功能

- **周视图课表**：周一至周日（可切换显示周六日），每天 1-12 节
- **课程卡片**：彩色背景区分课程，显示课程名、教师、地点
- **添加/编辑/删除**：点击空白格子添加课程，长按编辑或删除
- **学期管理**：支持多学期切换，各学期独立课表
- **单双周支持**：课程可标记为全部/单周/双周
- **冲突检测**：添加课程时自动检测时间冲突
- **备份导出/导入**：JSON 格式备份，支持合并或覆盖导入
- **本地存储**：Room 数据库持久化，无需网络

## 技术栈

| 组件 | 技术 |
|------|------|
| 语言 | Kotlin 2.0 |
| UI | Jetpack Compose (Material3) |
| 架构 | MVVM (ViewModel + Repository) |
| 数据库 | Room |
| 异步 | Kotlin Coroutines + Flow |
| 序列化 | Gson |
| 编译 | KSP (Room annotation processing) |

## 项目结构

```
app/src/main/java/com/example/classschedule/
├── MainActivity.kt              # 唯一 Activity
├── ClassScheduleApp.kt          # Application（手动 DI）
├── data/
│   ├── model/
│   │   ├── Course.kt            # 课程实体
│   │   └── Semester.kt          # 学期实体
│   ├── database/
│   │   ├── AppDatabase.kt       # Room 数据库单例
│   │   ├── CourseDao.kt         # 课程 DAO
│   │   └── SemesterDao.kt       # 学期 DAO
│   └── repository/
│       ├── CourseRepository.kt
│       └── SemesterRepository.kt
├── ui/
│   ├── theme/
│   │   ├── Color.kt             # 课程卡片 10 色 + Material3 色板
│   │   ├── Type.kt              # 排版定义
│   │   └── Theme.kt             # 主题
│   ├── components/
│   │   ├── TimetableGrid.kt     # 课表网格（Canvas + 绝对定位）
│   │   ├── CourseCard.kt        # 课程卡片
│   │   └── WeekSelector.kt      # 周次切换器
│   ├── screens/
│   │   ├── TimetableScreen.kt   # 主界面
│   │   ├── AddEditCourseDialog.kt       # 添加/编辑课程弹窗
│   │   └── SemesterManagementDialog.kt  # 学期管理弹窗
│   └── viewmodel/
│       ├── TimetableViewModel.kt
│       └── SemesterViewModel.kt
└── utils/
    ├── ConflictChecker.kt       # 课程冲突检测
    └── BackupHelper.kt          # JSON 备份导入导出
```

## 构建

1. 安装 Android Studio 或 Android SDK 命令行工具
2. 安装 JDK 17+ 和 Gradle 8.5+
3. 设置 `ANDROID_HOME` 环境变量
4. 在项目根目录执行：

```bash
./gradlew assembleDebug
```

APK 生成位置：`app/build/outputs/apk/debug/app-debug.apk`

## 许可

GPL V3 License
