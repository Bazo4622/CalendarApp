package com.usj.calendarapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.usj.calendarapp.data.AppDatabase
import com.usj.calendarapp.model.Account
import com.usj.calendarapp.repository.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AccountRepository
    val accounts: LiveData<List<Account>>
    private val viewModelJob = Job()
    private val customScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        val accountDao = AppDatabase.getDatabase(application).accountDao()
        repository = AccountRepository(accountDao)
        accounts = repository.getAllAccounts()
    }

    fun insert(account: Account) = customScope.launch {
        repository.insert(account)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}