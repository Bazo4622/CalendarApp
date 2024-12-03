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

class EventViewModel(application: Application, scope: CoroutineScope) : AndroidViewModel(application) {
    private val repository: EventRepository
    val events: LiveData<List<Event>>
    private val activeAccountId: Int = 1 // Example value, replace with actual logic
    private val viewModelJob = Job()
    private val customScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        val eventDao = AppDatabase.getDatabase(application, scope).eventDao()
        repository = EventRepository(eventDao)
        events = repository.getEventsByAccount(activeAccountId)
    }

    fun insert(event: Event) = customScope.launch {
        repository.insert(event)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}