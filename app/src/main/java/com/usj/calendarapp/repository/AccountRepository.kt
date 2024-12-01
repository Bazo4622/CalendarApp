package com.usj.calendarapp.repository

import androidx.lifecycle.LiveData
import com.usj.calendarapp.data.AccountDao
import com.usj.calendarapp.model.Account

class AccountRepository(private val accountDao: AccountDao) {
    fun getAllAccounts(): LiveData<List<Account>> {
        return accountDao.getAllAccounts()
    }

    suspend fun insert(account: Account) {
        accountDao.insert(account)
    }
}