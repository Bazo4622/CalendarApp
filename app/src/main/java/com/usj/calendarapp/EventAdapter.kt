package com.usj.calendarapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.usj.calendarapp.R
import com.usj.calendarapp.model.Event
import java.text.SimpleDateFormat
import java.util.Date

class EventAdapter(private var events: List<Event>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventName)
        val eventDate: TextView = itemView.findViewById(R.id.eventDate)
        val eventDescription: TextView = itemView.findViewById(R.id.eventDescription)
        val accountName: TextView = itemView.findViewById(R.id.accountName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val currentEvent = events[position]
        holder.eventName.text = currentEvent.title

        val date = Date(currentEvent.date)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm")
        holder.eventDate.text = format.format(date)
        holder.eventDescription.text = currentEvent.description
        holder.accountName.text = currentEvent.accountId.toString()
    }

    override fun getItemCount() = events.size

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }
}