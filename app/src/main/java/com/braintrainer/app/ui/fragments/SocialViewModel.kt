package com.braintrainer.app.ui.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.braintrainer.app.data.SocialRepository
import com.braintrainer.app.data.local.AppDatabase
import com.braintrainer.app.model.Group
import com.braintrainer.app.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SocialViewModel(application: Application) : AndroidViewModel(application) {
    private val socialRepo = SocialRepository()
    private val db = AppDatabase.getDatabase(application)

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _userGroups = MutableLiveData<List<Group>>()
    val userGroups: LiveData<List<Group>> = _userGroups

    private val _ranking = MutableLiveData<List<User>>()
    val ranking: LiveData<List<User>> = _ranking

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _status = MutableLiveData<String>("Iniciando...")
    val status: LiveData<String> = _status

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        val user = FirebaseAuth.getInstance().currentUser
        _isLoggedIn.value = user != null
        if (user != null) {
            _status.value = "Usuário logado: ${user.email}"
            loadInitialData()
        } else {
            _status.value = "Aguardando login..."
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                
                // 1. SYNC PROFILE (Avatar, Name)
                // Fetch remote profile first
                val remoteUser = socialRepo.fetchUserProfile()
                // Fix: Create DB User, not Model User
                val localUser = db.userDao().getUser().firstOrNull() ?: com.braintrainer.app.data.local.User(name = firebaseUser.displayName ?: "User", birthDate = System.currentTimeMillis(), uid = firebaseUser.uid)
                
                if (remoteUser != null) {
                    // Remote exists: Update local with remote Avatar if remote is valid
                    val mergedUser = localUser.copy(
                        avatarId = remoteUser.avatarId, // Trust remote avatar
                        uid = remoteUser.uid,
                        name = if (remoteUser.name.isNotEmpty()) remoteUser.name else localUser.name
                    )
                    // Note: groupIds are not synced to local User table because it doesn't support list.
                    // We rely on SocialViewModel caching them from Firestore.
                    db.userDao().insertUser(mergedUser) // Insert/Update
                } else {
                    // Remote doesn't exist: Push local (handled later by generic sync)
                    // Ensure local has UID
                    if (localUser.uid != firebaseUser.uid) {
                        db.userDao().insertUser(localUser.copy(uid = firebaseUser.uid))
                    }
                }

                // 2. SYNC GAME RESULTS
                _status.value = "Sincronizando histórico..."
                
                // A. Upload Local -> Remote
                val allLocalResults = db.gameResultDao().getAllResultsSync() // Need Sync version in DAO or convert flow
                socialRepo.uploadGameResults(allLocalResults)
                
                // B. Download Remote -> Local
                val remoteResults = socialRepo.fetchGameResults()
                
                // Deduplicate: Don't insert if date/type matches existing
                // Since autogenerate ID, we rely on content match
                val existingSignatures = allLocalResults.map { "${it.date}_${it.gameType}" }.toSet()
                
                val newResults = remoteResults.filter { 
                    val sig = "${it.date}_${it.gameType}"
                    !existingSignatures.contains(sig)
                }
                
                if (newResults.isNotEmpty()) {
                    db.gameResultDao().insertResults(newResults) // Need batch insert in DAO
                }
                
                // 3. UPDATE STATS SUMMARY (Global)
                val updatedStats = db.gameResultDao().getGlobalStats()
                val brainAge = (updatedStats.avgBrainAge ?: 40.0).toInt()
                val totalMatches = updatedStats.totalGames
                val avgGrade = updatedStats.avgGrade ?: "F"
                val finalAvatarId = db.userDao().getUser().firstOrNull()?.avatarId ?: 1

                socialRepo.syncUserProfile(
                    firebaseUser.displayName ?: "User", 
                    brainAge, totalMatches, avgGrade, finalAvatarId
                )
                
                _status.value = "Sincronização completa!"
                refreshGroups()
            } catch (e: Exception) {
                _status.value = "Erro Sync: ${e.message}"
                _error.value = "Erro Sync: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun refreshGroups() {
        viewModelScope.launch {
            try {
                _status.value = "Buscando grupos..."
                val groups = socialRepo.getUserGroups()
                _userGroups.value = groups
                if (groups.isNotEmpty()) {
                    loadRanking(groups[0].groupId)
                    _status.value = "${groups.size} grupos carregados."
                } else {
                    _status.value = "Nenhum grupo encontrado."
                }
            } catch (e: Exception) {
                _status.value = "Erro Refresh: ${e.message}"
                _error.value = "Erro Refresh: ${e.message}"
            }
        }
    }

    fun loadRanking(groupId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val list = socialRepo.getGroupRanking(groupId)
                _ranking.value = list
                _loading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _loading.value = false
            }
        }
    }

    fun createGroup(name: String) {
        viewModelScope.launch {
            try {
                _status.value = "Criando grupo '$name'..."
                socialRepo.createGroup(name)
                _status.value = "Grupo criado! Atualizando..."
                refreshGroups()
            } catch (e: Exception) {
                _status.value = "Erro ao criar: ${e.message}"
                _error.value = "Erro Social: ${e.message}"
            }
        }
    }

    fun joinGroup(id: String) {
        viewModelScope.launch {
            try {
                socialRepo.joinGroup(id)
                refreshGroups()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            try {
                socialRepo.deleteGroup(groupId)
                refreshGroups()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun kickMember(groupId: String, memberUid: String) {
        viewModelScope.launch {
            try {
                socialRepo.kickMember(groupId, memberUid)
                loadRanking(groupId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun transferAdmin(groupId: String, newAdminUid: String) {
        viewModelScope.launch {
            try {
                socialRepo.transferAdmin(groupId, newAdminUid)
                loadRanking(groupId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
