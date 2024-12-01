package com.usj.calendarapp.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.usj.calendarapp.model.Account

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account)

    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): LiveData<List<Account>>
}