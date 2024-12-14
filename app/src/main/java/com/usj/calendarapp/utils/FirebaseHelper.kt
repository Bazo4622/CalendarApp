package com.usj.calendarapp.utils

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Calendar

data class Account(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val loggedIn: Boolean = false
)

data class Event(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val date: Long = 0L,
    val time: Long = 0L,
    val accountId: Int = 0,
    val repeating: Boolean = false
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
        password = "holidays",
        loggedIn = true
    )
    accountsRef.child(holidaysAccount.id.toString()).setValue(holidaysAccount)

    // Create holidays events with correct dates
    val holidays = listOf(
        Event(id = 1, title = "New Year's Day", description = "New Year's Day", date = 0L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 2, title = "Independence Day", description = "Independence Day", date = 15897600000L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 3, title = "Christmas Day", description = "Christmas Day", date = 30931200000L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 4, title = "Labor Day", description = "Labor Day", date = 20995200000L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 5, title = "Thanksgiving Day", description = "Thanksgiving Day", date = 28598400000L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 6, title = "Memorial Day", description = "Memorial Day", date = 12441600000L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 7, title = "Veterans Day", description = "Veterans Day", date = 27129600000L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 8, title = "Halloween", description = "Halloween", date = 26265600000L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 9, title = "Valentine's Day", description = "Valentine's Day", date = 3801600000L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 10, title = "Easter", description = "Easter", date = 7516800000L, time = 0, accountId = holidaysAccount.id, repeating = true)
    )

    holidays.forEach { event ->
        for (year in 0 until 60) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = event.date
                set(Calendar.YEAR, 1970 + year)
            }
            val newEvent = event.copy(date = calendar.timeInMillis)
            eventsRef.child("${event.id}_${1970 + year}").setValue(newEvent)
        }
    }
}