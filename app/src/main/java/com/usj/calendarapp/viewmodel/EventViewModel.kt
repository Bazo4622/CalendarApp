package com.usj.calendarapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.usj.calendarapp.model.Event

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events

    fun fetchEventsByDate(date: Long) {
        val database = FirebaseDatabase.getInstance()
        val eventsRef = database.getReference("events")
        eventsRef.orderByChild("date").equalTo(date.toDouble()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventsList = mutableListOf<Event>()
                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(Event::class.java)
                    if (event != null) {
                        eventsList.add(event)
                    }
                }
                _events.value = eventsList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}