package com.usj.calendarapp.utils

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

data class Account(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val password: String = ""
)

data class Event(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val date: Long = 0L,
    val time: Long = 0L,
    val accountId: Int = 0
)

fun addHolidaysToFirebase() {
    val database: FirebaseDatabase = Firebase.database
    val accountsRef = database.getReference("accounts")
    val eventsRef = database.getReference("events")

    // Create holidays account
    val holidaysAccount = Account(
        id = 1,
        username = "Holidays",
        email = "holidays@example.com",
        password = "holidays"
    )
    accountsRef.child(holidaysAccount.id.toString()).setValue(holidaysAccount)

    // Create holidays events
    val holidays = listOf(
        Event(id = 1, title = "New Year's Day", description = "New Year's Day", date = 1672531200000, time = 0, accountId = holidaysAccount.id),
        Event(id = 2, title = "Independence Day", description = "Independence Day", date = 1688476800000, time = 0, accountId = holidaysAccount.id),
        Event(id = 3, title = "Christmas Day", description = "Christmas Day", date = 1704067200000, time = 0, accountId = holidaysAccount.id),
        Event(id = 4, title = "Labor Day", description = "Labor Day", date = 1693708800000, time = 0, accountId = holidaysAccount.id),
        Event(id = 5, title = "Thanksgiving Day", description = "Thanksgiving Day", date = 1700870400000, time = 0, accountId = holidaysAccount.id),
        Event(id = 6, title = "Memorial Day", description = "Memorial Day", date = 1685404800000, time = 0, accountId = holidaysAccount.id),
        Event(id = 7, title = "Veterans Day", description = "Veterans Day", date = 1700265600000, time = 0, accountId = holidaysAccount.id),
        Event(id = 8, title = "Halloween", description = "Halloween", date = 1698710400000, time = 0, accountId = holidaysAccount.id),
        Event(id = 9, title = "Valentine's Day", description = "Valentine's Day", date = 1676332800000, time = 0, accountId = holidaysAccount.id),
        Event(id = 10, title = "Easter", description = "Easter", date = 1682208000000, time = 0, accountId = holidaysAccount.id)
    )

    holidays.forEach { event ->
        eventsRef.child(event.id.toString()).setValue(event)
    }
}