package com.example.classschedule.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 周次选择器
 *
 * 左右箭头 + 当前周次显示，支持 1-30 周的快速切换。
 * 用于课表过滤：根据选中的周次和课程的单双周规则，决定显示哪些课程。
 */
@Composable
fun WeekSelector(
    currentWeek: Int,
    onWeekChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 上一周按钮（最小 1）
        IconButton(
            onClick = { if (currentWeek > 1) onWeekChanged(currentWeek - 1) },
            enabled = currentWeek > 1
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                contentDescription = "上一周"
            )
        }

        // 当前周次标签
        TextButton(
            onClick = { } // 点击周次文字不做操作（可扩展为周次选择弹窗）
        ) {
            Text(
                text = "第 $currentWeek 周",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }

        // 下一周按钮（最大 30）
        IconButton(
            onClick = { if (currentWeek < 30) onWeekChanged(currentWeek + 1) },
            enabled = currentWeek < 30
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                contentDescription = "下一周"
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 快速跳转到当前周（预留）
        Text(
            text = "周次",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
