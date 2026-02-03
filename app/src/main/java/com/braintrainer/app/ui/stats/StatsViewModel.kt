package com.braintrainer.app.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import com.braintrainer.app.data.local.AppDatabase
import com.braintrainer.app.data.local.GameResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).gameResultDao()

    val allResults: LiveData<List<GameResult>> = dao.getAllResults().asLiveData()

    val totalGames: LiveData<Int> = allResults.map { it.size }

    val averageBrainAge: LiveData<String> = allResults.map { results ->
        val valid = results.filter { it.brainAge != null }
        if (valid.isNotEmpty()) {
            val avg = valid.map { it.brainAge!! }.average()
            String.format("%.1f", avg) // e.g. "24.5"
        } else {
            "--"
        }
    }
    
    // Recent 20 items
    val recentHistory: LiveData<List<GameResult>> = allResults.map { list ->
        list.take(20)
    }
    
    val dailyTestHistory: LiveData<List<GameResult>> = dao.getDailyTestHistoryData().asLiveData()

    fun clearHistory() {
        viewModelScope.launch {
            dao.deleteAllResults()
        }
    }
}
