package com.braintrainer.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.braintrainer.app.data.local.AppDatabase
import com.braintrainer.app.data.local.GameResult
import kotlinx.coroutines.flow.map

class TrainingViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).gameResultDao()

    // Map of GameType -> Best (Lowest) Brain Age
    val bestScores: LiveData<Map<String, Int>> = dao.getAllResults().map { results ->
        val map = mutableMapOf<String, Int>()
        results.forEach { result ->
            if (result.brainAge != null) {
                val currentBest = map[result.gameType]
                if (currentBest == null || result.brainAge < currentBest) {
                    map[result.gameType] = result.brainAge
                }
            }
        }
        map
    }.asLiveData()
}
