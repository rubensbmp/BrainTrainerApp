package com.braintrainer.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameResultDao {
    @Insert
    suspend fun insertResult(result: GameResult)

    @Query("SELECT * FROM game_results ORDER BY date DESC")
    fun getAllResults(): Flow<List<GameResult>>

    @Query("SELECT * FROM game_results WHERE gameType = 'DAILY_TEST' ORDER BY date DESC LIMIT 30")
    fun getDailyTestHistory(): Flow<List<GameResult>>
    
    @Query("SELECT * FROM game_results WHERE date >= :startOfDay AND date < :endOfDay AND gameType = 'DAILY_TEST' LIMIT 1")
    suspend fun getDailyTestForDate(startOfDay: Long, endOfDay: Long): GameResult?

    @Query("SELECT * FROM game_results WHERE gameType = 'DAILY_TEST' ORDER BY date ASC")
    fun getDailyTestHistoryData(): Flow<List<GameResult>>

    @Query("SELECT brainAge FROM game_results WHERE gameType = 'DAILY_TEST' AND brainAge IS NOT NULL ORDER BY date DESC LIMIT 1")
    suspend fun getLastBrainAge(): Int?

    // Preserves DAILY_TEST history
    @Query("DELETE FROM game_results WHERE gameType != 'DAILY_TEST'")
    suspend fun deleteAllResults()

    @Query("SELECT AVG(brainAge) as avgBrainAge, COUNT(*) as totalGames, (SELECT grade FROM game_results WHERE brainAge IS NOT NULL ORDER BY date DESC LIMIT 1) as avgGrade FROM game_results WHERE brainAge IS NOT NULL")
    suspend fun getGlobalStats(): GlobalStats
    
    @Query("SELECT * FROM game_results ORDER BY date DESC")
    suspend fun getAllResultsSync(): List<GameResult>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<GameResult>)
}

data class GlobalStats(
    val avgBrainAge: Double?,
    val totalGames: Int,
    val avgGrade: String?
)
