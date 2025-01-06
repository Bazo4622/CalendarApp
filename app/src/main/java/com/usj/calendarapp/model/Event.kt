package com.usj.calendarapp.model

data class Event(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val date: Long = 0L,
    val time: Long = 0L,
    val accountId: Int = 0,
    val repeating: Boolean = false
) {
    // No-argument constructor for Firebase
    constructor() : this(0, "", "", 0L, 0L, 0, false)
}