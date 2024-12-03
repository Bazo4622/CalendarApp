package com.usj.calendarapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.usj.calendarapp.data.AppDatabase
import com.usj.calendarapp.model.Event
import com.usj.calendarapp.repository.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EventRepository
    private val viewModelJob = Job()
    private val customScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        val eventDao = AppDatabase.getDatabase(application, customScope).eventDao()
        repository = EventRepository(eventDao)
    }

    fun getEventsByDate(date: Long): LiveData<List<Event>> {
        return repository.getEventsByDate(date)
    }

    fun insert(event: Event) = customScope.launch {
        repository.insert(event)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}