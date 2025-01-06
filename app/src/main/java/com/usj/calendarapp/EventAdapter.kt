package com.usj.calendarapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.usj.calendarapp.R
import com.usj.calendarapp.model.Event
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EventAdapter(private var events: List<Event>, private val accountMap: Map<Int, String>, itemEventEdit: Int) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private var selectedEvent: Event? = null

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

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm z", Locale.getDefault())

        val date = Date(currentEvent.date)
        val time = Date(currentEvent.time)

        holder.eventDate.text = "${dateFormat.format(date)} ${timeFormat.format(time)}"
        holder.eventDescription.text = currentEvent.description
        holder.accountName.text = accountMap[currentEvent.accountId] ?: currentEvent.accountId.toString()

        holder.itemView.setOnClickListener {
            selectedEvent = currentEvent
            notifyDataSetChanged()
        }

        if (selectedEvent == currentEvent) {
            holder.itemView.setBackgroundColor(Color.GRAY)
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    fun getSelectedEvent(): Event? = selectedEvent

    override fun getItemCount() = events.size

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }
}