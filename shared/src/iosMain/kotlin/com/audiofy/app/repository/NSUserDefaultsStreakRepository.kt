package com.audiofy.app.repository

import com.audiofy.app.data.CheckInStatus
import com.audiofy.app.data.ListeningStreak
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.*

/**
 * iOS平台的打卡数据存储实现
 */
class NSUserDefaultsStreakRepository : StreakRepository {

    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val _streakFlow = MutableStateFlow(loadStreak())

    companion object {
        private const val KEY_STREAK = "listening_streak"
        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    private fun loadStreak(): ListeningStreak {
        val jsonString = userDefaults.stringForKey(KEY_STREAK)
        return if (jsonString != null) {
            try {
                json.decodeFromString<ListeningStreak>(jsonString)
            } catch (e: Exception) {
                ListeningStreak.DEFAULT
            }
        } else {
            ListeningStreak.DEFAULT
        }
    }

    override fun getStreakFlow(): Flow<ListeningStreak> {
        return _streakFlow.asStateFlow()
    }

    override suspend fun getStreak(): ListeningStreak {
        return _streakFlow.value
    }

    override suspend fun saveStreak(streak: ListeningStreak) {
        val jsonString = json.encodeToString(streak)
        userDefaults.setObject(jsonString, forKey = KEY_STREAK)
        userDefaults.synchronize()
        _streakFlow.value = streak
    }

    override suspend fun recordListening(date: String?) {
        val currentStreak = getStreak()
        val today = date ?: getCurrentDate()
        
        if (currentStreak.lastListenDate == today) {
            return
        }
        
        val yesterday = getYesterdayDate(today)
        val dayOfWeek = getDayOfWeek(today)
        
        val newStreak = when (currentStreak.lastListenDate) {
            null -> 1
            yesterday -> currentStreak.currentStreak + 1
            today -> currentStreak.currentStreak
            else -> 1
        }
        
        val newThisWeekDays = if (isNewWeek(currentStreak.lastListenDate, today)) {
            setOf(dayOfWeek)
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
        userDefaults.removeObjectForKey(KEY_STREAK)
        userDefaults.synchronize()
        _streakFlow.value = ListeningStreak.DEFAULT
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
        val now = NSDate()
        return formatDate(now)
    }

    private fun getYesterdayDate(today: String): String {
        val parts = today.split("-")
        val calendar = NSCalendar.currentCalendar
        val components = NSDateComponents().apply {
            year = parts[0].toLong()
            month = parts[1].toLong()
            day = parts[2].toLong()
        }
        val date = calendar.dateFromComponents(components) ?: NSDate()
        val yesterday = calendar.dateByAddingUnit(
            NSCalendarUnitDay,
            -1,
            date,
            0u
        ) ?: date
        return formatDate(yesterday)
    }

    private fun formatDate(date: NSDate): String {
        val calendar = NSCalendar.currentCalendar
        val components = calendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
            date
        )
        return "${components.year.toString().padStart(4, '0')}-${components.month.toString().padStart(2, '0')}-${components.day.toString().padStart(2, '0')}"
    }

    private fun getDayOfWeek(date: String): Int {
        val parts = date.split("-")
        val calendar = NSCalendar.currentCalendar
        val components = NSDateComponents().apply {
            year = parts[0].toLong()
            month = parts[1].toLong()
            day = parts[2].toLong()
        }
        val nsDate = calendar.dateFromComponents(components) ?: NSDate()
        val weekday = calendar.component(NSCalendarUnitWeekday, nsDate)
        
        return if (weekday == 1L) 7 else (weekday - 1).toInt()
    }

    private fun isNewWeek(oldDate: String?, newDate: String): Boolean {
        if (oldDate == null) return false
        
        val oldDayOfWeek = getDayOfWeek(oldDate)
        val newDayOfWeek = getDayOfWeek(newDate)
        
        return newDayOfWeek == 1 && oldDayOfWeek != 1
    }
}
