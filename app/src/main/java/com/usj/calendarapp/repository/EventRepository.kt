package com.usj.calendarapp.repository

import com.usj.calendarapp.model.Event
import androidx.lifecycle.LiveData
import com.usj.calendarapp.data.EventDao

class EventRepository(private val eventDao: EventDao) {
    fun getEventsByAccount(accountId: Int): LiveData<List<Event>> {
        return eventDao.getEventsByAccount(accountId)
    }

    fun getEventsByDate(date: Long): LiveData<List<Event>> {
        return eventDao.getEventsByDate(date)
    }

    suspend fun insert(event: Event) {
        eventDao.insert(event)
    }
}