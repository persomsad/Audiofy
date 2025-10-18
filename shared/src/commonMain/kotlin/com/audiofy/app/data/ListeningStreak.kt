package com.audiofy.app.data

import kotlinx.serialization.Serializable

/**
 * 连续收听打卡数据模型
 */
@Serializable
data class ListeningStreak(
    val currentStreak: Int = 0,              // 当前连续天数
    val longestStreak: Int = 0,              // 最长连续天数
    val lastListenDate: String? = null,      // 最后收听日期 (YYYY-MM-DD)
    val totalListeningDays: Int = 0,         // 总收听天数
    val thisWeekDays: Set<Int> = emptySet()  // 本周收听的日期 (1-7: 周一到周日)
) {
    companion object {
        val DEFAULT = ListeningStreak()
    }
}

/**
 * 打卡状态
 */
enum class CheckInStatus {
    NOT_YET,        // 今天还未打卡
    CHECKED_TODAY,  // 今天已打卡
    STREAK_BROKEN   // 连续打卡中断
}
