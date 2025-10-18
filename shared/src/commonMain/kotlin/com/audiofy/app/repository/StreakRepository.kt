package com.audiofy.app.repository

import com.audiofy.app.data.CheckInStatus
import com.audiofy.app.data.ListeningStreak
import kotlinx.coroutines.flow.Flow

/**
 * 打卡数据仓库接口
 */
interface StreakRepository {
    /**
     * 获取打卡数据（响应式）
     */
    fun getStreakFlow(): Flow<ListeningStreak>
    
    /**
     * 获取打卡数据
     */
    suspend fun getStreak(): ListeningStreak
    
    /**
     * 保存打卡数据
     */
    suspend fun saveStreak(streak: ListeningStreak)
    
    /**
     * 记录一次收听（自动更新打卡状态）
     * @param date 收听日期 (YYYY-MM-DD)，默认为今天
     */
    suspend fun recordListening(date: String? = null)
    
    /**
     * 清空打卡数据
     */
    suspend fun clearStreak()
    
    /**
     * 获取今天的打卡状态
     */
    suspend fun getTodayCheckInStatus(): CheckInStatus
}
