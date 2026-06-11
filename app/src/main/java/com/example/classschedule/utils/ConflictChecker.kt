package com.example.classschedule.utils

import com.example.classschedule.data.model.Course

/**
 * 课程冲突检测器
 *
 * 检测新课程是否与已有课程在时间上冲突。
 * 冲突判定条件（三个条件同时满足）：
 * 1. 同一天（dayOfWeek 相同）
 * 2. 节次有重叠（如 2-3 节 与 3-4 节的第 3 节重叠）
 * 3. 周次有重叠且周类型兼容（如都在单周上课，且周次范围重叠）
 *
 * 注意：如果两门课程的 weekType 分别为单周和双周，
 * 即使周次范围重叠也不会冲突（因为它们实际不在同一周上课）。
 */
object ConflictChecker {

    /**
     * 查找与新课程冲突的已有课程列表
     *
     * @param existingCourses 同一学期下的所有已有课程
     * @param newCourse 待添加或编辑的课程
     * @return 与新课程冲突的课程列表（空列表表示无冲突）
     */
    fun findConflicts(
        existingCourses: List<Course>,
        newCourse: Course
    ): List<Course> {
        return existingCourses.filter { existing ->
            // 排除自身（编辑场景）
            existing.id != newCourse.id
                // 条件 1：同一天
                && existing.dayOfWeek == newCourse.dayOfWeek
                // 条件 2：节次重叠
                && sectionsOverlap(existing, newCourse)
                // 条件 3：周次重叠且周类型兼容
                && weeksOverlap(existing, newCourse)
        }
    }

    /**
     * 判断两门课的节次是否有重叠
     * 例如：A 为 1-3 节，B 为 3-4 节 → 第 3 节重叠，返回 true
     *        A 为 1-2 节，B 为 3-4 节 → 无重叠，返回 false
     */
    private fun sectionsOverlap(a: Course, b: Course): Boolean {
        return a.startSection <= b.endSection && b.startSection <= a.endSection
    }

    /**
     * 判断两门课在周次维度上是否冲突
     *
     * 需同时满足：
     * - 周次范围有交集（[startWeek, endWeek] 重叠）
     * - 在重叠的周次中，至少有一周两门课都需要上课
     *
     * 单双周逻辑：
     * - weekType=0（全周）：任何周都上课
     * - weekType=1（单周）：仅奇数周上课
     * - weekType=2（双周）：仅偶数周上课
     */
    private fun weeksOverlap(a: Course, b: Course): Boolean {
        // 计算周次范围的交集
        val latestStart = maxOf(a.startWeek, b.startWeek)
        val earliestEnd = minOf(a.endWeek, b.endWeek)

        if (latestStart > earliestEnd) return false

        // 在交集范围内，检查是否存在一周两门课都上课
        for (week in latestStart..earliestEnd) {
            if (isWeekActive(a, week) && isWeekActive(b, week)) {
                return true
            }
        }
        return false
    }

    /**
     * 判断某课程在指定周是否上课
     */
    fun isWeekActive(course: Course, week: Int): Boolean {
        // 周次不在课程范围内
        if (week < course.startWeek || week > course.endWeek) return false

        return when (course.weekType) {
            0 -> true         // 全周：都上课
            1 -> week % 2 != 0  // 单周：仅奇数周
            2 -> week % 2 == 0  // 双周：仅偶数周
            else -> false
        }
    }
}
