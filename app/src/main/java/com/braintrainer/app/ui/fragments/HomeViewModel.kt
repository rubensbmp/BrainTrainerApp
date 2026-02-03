package com.braintrainer.app.ui.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.braintrainer.app.data.local.AppDatabase
import com.braintrainer.app.data.local.User
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()

    // Get current user (might be null initially)
    val user: LiveData<User?> = userDao.getUser().asLiveData()

    private val resultDao = AppDatabase.getDatabase(application).gameResultDao()
    
    private val _dailyResult = androidx.lifecycle.MutableLiveData<com.braintrainer.app.data.local.GameResult?>()
    val dailyResult: LiveData<com.braintrainer.app.data.local.GameResult?> = _dailyResult
    
    private val _suggestedDifficulty = androidx.lifecycle.MutableLiveData<String>("MEDIUM")
    val suggestedDifficulty: LiveData<String> = _suggestedDifficulty

    fun refreshDailyResult() {
        viewModelScope.launch {
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            
            val start = calendar.timeInMillis
            val end = start + 86400000L
            
            val res = resultDao.getDailyTestForDate(start, end)
            _dailyResult.value = res
            
            // Also update difficulty based on history
            val lastAge = resultDao.getLastBrainAge()
            val diff = when {
                lastAge == null -> "MEDIUM"
                lastAge <= 30 -> "HARD" // If you're doing well (low age), it gets harder
                lastAge >= 60 -> "EASY" // If specific struggle, easier
                else -> "MEDIUM"
            }
            _suggestedDifficulty.value = diff
        }
    }

    fun updateUser(name: String, avatarId: Int) {
        viewModelScope.launch {
            val existing = user.value
            val id = existing?.id ?: 0
            val birthDate = existing?.birthDate ?: System.currentTimeMillis()
            
            val newUser = User(
                id = id,
                name = name,
                birthDate = birthDate,
                avatarId = avatarId
            )
            userDao.insertUser(newUser)

            // INTERNET SYNC: If logged in, update Firebase
            val socialRepo = com.braintrainer.app.data.SocialRepository()
            if (socialRepo.getCurrentUser() != null) {
                try {
                    val stats = resultDao.getGlobalStats()
                    socialRepo.syncUserProfile(
                        name = name,
                        brainAge = (stats.avgBrainAge ?: 40.0).toInt(),
                        totalMatches = stats.totalGames,
                        avgGrade = stats.avgGrade ?: "F",
                        avatarId = avatarId
                    )
                } catch (e: Exception) {
                    // Fail silently
                }
            }
        }
    }

    fun getTimeRemainingUntilNextDay(): String {
        val calendar = java.util.Calendar.getInstance()
        val now = calendar.timeInMillis
        
        // Target: Tomorrow at 00:00
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        val diff = calendar.timeInMillis - now
        val hours = (diff / 3600000L)
        val minutes = (diff % 3600000L) / 60000L
        val seconds = (diff % 60000L) / 1000L
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
