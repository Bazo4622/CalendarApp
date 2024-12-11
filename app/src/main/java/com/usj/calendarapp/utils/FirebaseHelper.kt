package com.usj.calendarapp.utils

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

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

    // Create holidays events with dates in 1970
    val holidays = listOf(
        Event(id = 1, title = "New Year's Day", description = "New Year's Day", date = 0L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 2, title = "Independence Day", description = "Independence Day", date = 157766400L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 3, title = "Christmas Day", description = "Christmas Day", date = 315532800L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 4, title = "Labor Day", description = "Labor Day", date = 214848000L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 5, title = "Thanksgiving Day", description = "Thanksgiving Day", date = 283996800L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 6, title = "Memorial Day", description = "Memorial Day", date = 124416000L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 7, title = "Veterans Day", description = "Veterans Day", date = 272160000L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 8, title = "Halloween", description = "Halloween", date = 262656000L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 9, title = "Valentine's Day", description = "Valentine's Day", date = 3801600L, time = 0, accountId = holidaysAccount.id, repeating = true),
        Event(id = 10, title = "Easter", description = "Easter", date = 7516800L, time = 0, accountId = holidaysAccount.id, repeating = true)
    )

    holidays.forEach { event ->
        eventsRef.child(event.id.toString()).setValue(event)
    }
}