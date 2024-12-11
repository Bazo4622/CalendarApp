package com.usj.calendarapp.model

data class Event(
    val id: Int = 0,
    val title: String,
    val description: String,
    val date: Long,
    val time: Long,
    val accountId: Int
)