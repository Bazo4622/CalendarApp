package com.usj.calendarapp.data

import com.usj.calendarapp.model.Event
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event)

    @Query("SELECT * FROM events WHERE date = :date ORDER BY time")
    fun getEventsByDate(date: Long): LiveData<List<Event>>

    @Query("SELECT * FROM events WHERE accountId = :accountId ORDER BY date, time")
    fun getEventsByAccount(accountId: Int): LiveData<List<Event>>
}