package com.braintrainer.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.braintrainer.app.data.local.AppDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainMenuViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()
    
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val user = userDao.getUser().firstOrNull()
            user?.let {
                _userName.value = it.name
            }
        }
    }
}
