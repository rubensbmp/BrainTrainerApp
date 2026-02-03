package com.braintrainer.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.braintrainer.app.data.local.AppDatabase
import com.braintrainer.app.data.local.User
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileSetupViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()

    private val _navigateToMenu = MutableLiveData<Boolean>()
    val navigateToMenu: LiveData<Boolean> = _navigateToMenu

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun saveProfile(name: String, dobString: String) {
        if (name.isBlank()) {
            _error.value = "Por favor, insira seu nome."
            return
        }

        val dob = parseDate(dobString)
        if (dob == null) {
            _error.value = "Data inválida. Use o formato DD/MM/AAAA."
            return
        }

        viewModelScope.launch {
            val user = User(name = name, birthDate = dob)
            userDao.insertUser(user)
            _navigateToMenu.value = true
        }
    }

    private fun parseDate(dateString: String): Long? {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.parse(dateString)?.time
        } catch (e: Exception) {
            null
        }
    }
}
