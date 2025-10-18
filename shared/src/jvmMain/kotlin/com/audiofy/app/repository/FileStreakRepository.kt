package com.audiofy.app.repository

import com.audiofy.app.data.CheckInStatus
import com.audiofy.app.data.ListeningStreak
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Calendar

/**
 * JVM平台的打卡数据存储实现
 */
class FileStreakRepository(private val dataDir: File) : StreakRepository {

    private val streakFile = File(dataDir, "streak.json")
    private val _streakFlow = MutableStateFlow(loadStreak())

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = true
        }
    }

    init {
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
    }

    private fun loadStreak(): ListeningStreak {
        return if (streakFile.exists()) {
            try {
                val jsonString = streakFile.readText()
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
        try {
            val jsonString = json.encodeToString(streak)
            streakFile.writeText(jsonString)
            _streakFlow.value = streak
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        streakFile.delete()
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
        return formatDate(System.currentTimeMillis())
    }

    private fun getYesterdayDate(today: String): String {
        val parts = today.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()
        
        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, day)
            add(Calendar.DAY_OF_MONTH, -1)
        }
        
        return formatDate(calendar.timeInMillis)
    }

    private fun formatDate(millis: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = millis
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }

    private fun getDayOfWeek(date: String): Int {
        val parts = date.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()
        
        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, day)
        }
        
        val javaDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return if (javaDayOfWeek == 1) 7 else javaDayOfWeek - 1
    }

    private fun isNewWeek(oldDate: String?, newDate: String): Boolean {
        if (oldDate == null) return false
        
        val oldDayOfWeek = getDayOfWeek(oldDate)
        val newDayOfWeek = getDayOfWeek(newDate)
        
        return newDayOfWeek == 1 && oldDayOfWeek != 1
    }
}
