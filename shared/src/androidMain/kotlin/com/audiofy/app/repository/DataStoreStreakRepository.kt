package com.audiofy.app.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.audiofy.app.data.CheckInStatus
import com.audiofy.app.data.ListeningStreak
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Android平台的打卡数据存储实现
 */
class DataStoreStreakRepository(private val context: Context) : StreakRepository {

    private val Context.streakDataStore: DataStore<Preferences> by preferencesDataStore(name = "audiofy_streak")

    companion object {
        private val KEY_STREAK = stringPreferencesKey("listening_streak")
        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    override fun getStreakFlow(): Flow<ListeningStreak> {
        return context.streakDataStore.data.map { preferences ->
            val jsonString = preferences[KEY_STREAK]
            if (jsonString != null) {
                try {
                    json.decodeFromString<ListeningStreak>(jsonString)
                } catch (e: Exception) {
                    ListeningStreak.DEFAULT
                }
            } else {
                ListeningStreak.DEFAULT
            }
        }
    }

    override suspend fun getStreak(): ListeningStreak {
        return getStreakFlow().first()
    }

    override suspend fun saveStreak(streak: ListeningStreak) {
        context.streakDataStore.edit { preferences ->
            val jsonString = json.encodeToString(streak)
            preferences[KEY_STREAK] = jsonString
        }
    }

    override suspend fun recordListening(date: String?) {
        val currentStreak = getStreak()
        val today = date ?: getCurrentDate()
        
        // 如果今天已经记录过，直接返回
        if (currentStreak.lastListenDate == today) {
            return
        }
        
        val yesterday = getYesterdayDate(today)
        val dayOfWeek = getDayOfWeek(today)
        
        // 计算新的连续天数
        val newStreak = when (currentStreak.lastListenDate) {
            null -> 1 // 第一次收听
            yesterday -> currentStreak.currentStreak + 1 // 连续
            today -> currentStreak.currentStreak // 今天已记录（不应该发生）
            else -> 1 // 中断，重新开始
        }
        
        // 更新本周收听天数
        val newThisWeekDays = if (isNewWeek(currentStreak.lastListenDate, today)) {
            setOf(dayOfWeek) // 新的一周，重置
        } else {
            currentStreak.thisWeekDays + dayOfWeek
        }
        
        val updatedStreak = ListeningStreak(
            currentStreak = newStreak,
            longestStreak = maxOf(newStreak, currentStreak.longestStreak),
            lastListenDate = today,
            totalListeningDays = currentStreak.totalListeningDays + 1,
            thisWeekDays = newThisWeekDays
        )
        
        saveStreak(updatedStreak)
    }

    override suspend fun clearStreak() {
        context.streakDataStore.edit { preferences ->
            preferences.remove(KEY_STREAK)
        }
    }

    override suspend fun getTodayCheckInStatus(): CheckInStatus {
        val streak = getStreak()
        val today = getCurrentDate()
        
        return when {
            streak.lastListenDate == today -> CheckInStatus.CHECKED_TODAY
            streak.lastListenDate == null -> CheckInStatus.NOT_YET
            streak.lastListenDate == getYesterdayDate(today) -> CheckInStatus.NOT_YET
            else -> CheckInStatus.STREAK_BROKEN
        }
    }

    private fun getCurrentDate(): String {
        val millis = System.currentTimeMillis()
        return formatDate(millis)
    }

    private fun getYesterdayDate(today: String): String {
        val parts = today.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()
        
        val millis = java.util.Calendar.getInstance().apply {
            set(year, month - 1, day)
            add(java.util.Calendar.DAY_OF_MONTH, -1)
        }.timeInMillis
        
        return formatDate(millis)
    }

    private fun formatDate(millis: Long): String {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = millis
        }
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }

    private fun getDayOfWeek(date: String): Int {
        val parts = date.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()
        
        val calendar = java.util.Calendar.getInstance().apply {
            set(year, month - 1, day)
        }
        
        // Calendar.DAY_OF_WEEK: 1=Sunday, 2=Monday, ..., 7=Saturday
        // Convert to: 1=Monday, ..., 7=Sunday
        val javaDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        return if (javaDayOfWeek == 1) 7 else javaDayOfWeek - 1
    }

    private fun isNewWeek(oldDate: String?, newDate: String): Boolean {
        if (oldDate == null) return false
        
        val oldDayOfWeek = getDayOfWeek(oldDate)
        val newDayOfWeek = getDayOfWeek(newDate)
        
        // 如果新日期是周一，且旧日期不是周一，则是新的一周
        return newDayOfWeek == 1 && oldDayOfWeek != 1
    }
}
