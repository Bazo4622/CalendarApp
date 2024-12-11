package com.usj.calendarapp.model

data class Account(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val loggedIn: Boolean = false
) {
    // No-argument constructor for Firebase
    constructor() : this(0, "", "", "", false)
}