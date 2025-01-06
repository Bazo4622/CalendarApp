package com.usj.calendarapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.usj.calendarapp.adapter.AccountAdapter
import com.usj.calendarapp.adapter.EventAdapter
import com.usj.calendarapp.model.Account
import com.usj.calendarapp.model.Event
import com.usj.calendarapp.utils.addHolidaysToFirebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var accountsRecyclerView: RecyclerView
    private lateinit var accountAdapter: AccountAdapter
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private val applicationScope = CoroutineScope(Job() + Dispatchers.Main)
    private val eventsList = mutableListOf<Event>()
    private val markedDates = mutableSetOf<CalendarDay>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addHolidaysToFirebase()

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        accountsRecyclerView = findViewById(R.id.accountsRecyclerView)
        calendarView = findViewById(R.id.calendarView)
        eventsRecyclerView = findViewById(R.id.recyclerView)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        navigationView.setNavigationItemSelectedListener(this)

        accountsRecyclerView.layoutManager = LinearLayoutManager(this)
        getLoggedInAccounts { accounts ->
            accountAdapter = AccountAdapter(accounts)
            accountsRecyclerView.adapter = accountAdapter
        }

        eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        eventAdapter = EventAdapter(emptyList(), R.layout.item_event_edit)
        eventsRecyclerView.adapter = eventAdapter

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            showLoginDialog()
        }

        findViewById<Button>(R.id.signUpButton).setOnClickListener {
            showSignUpDialog()
        }

        fetchEventsOfLoggedInAccounts()

        val manageEventsButton = findViewById<Button>(R.id.manageEventsButton)
        manageEventsButton.setOnClickListener {
            showManageEventsDialog()
        }
    }

    private fun fetchEventsOfLoggedInAccounts() {
        val database = FirebaseDatabase.getInstance()
        val accountsRef = database.getReference("accounts")
        val eventsRef = database.getReference("events")

        accountsRef.orderByChild("loggedIn").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val loggedInAccountIds = snapshot.children.mapNotNull { it.getValue(Account::class.java)?.id }
                    eventsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            eventsList.clear()
                            markedDates.clear()
                            for (eventSnapshot in snapshot.children) {
                                val event = eventSnapshot.getValue(Event::class.java)
                                if (event != null && event.accountId in loggedInAccountIds) {
                                    eventsList.add(event)
                                    val eventDate = Calendar.getInstance().apply {
                                        timeInMillis = event.date
                                    }
                                    markedDates.add(CalendarDay.from(eventDate))
                                }
                            }
                            eventsList.sortBy { it.date }
                            calendarView.addDecorator(EventDayDecorator(markedDates, calendarView.context))

                            calendarView.setOnDateChangedListener { widget, date, selected ->
                                if (selected) {
                                    val selectedEvents = eventsList.filter {
                                        val eventDate = CalendarDay.from(Date(it.date))
                                        eventDate == date
                                    }
                                    eventAdapter.updateEvents(selectedEvents)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getLoggedInAccounts(callback: (List<Account>) -> Unit) {
        val accountsList = mutableListOf<Account>()
        val database = FirebaseDatabase.getInstance()
        val accountsRef = database.getReference("accounts")

        accountsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (accountSnapshot in snapshot.children) {
                    val account = accountSnapshot.getValue(Account::class.java)
                    if (account != null && account.loggedIn && account.username != "Holidays") {
                        accountsList.add(account)
                    }
                }
                callback(accountsList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun refreshAccountsList() {
        val database = FirebaseDatabase.getInstance()
        val accountsRef = database.getReference("accounts")

        accountsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val accountsList = mutableListOf<Account>()
                for (accountSnapshot in snapshot.children) {
                    val account = accountSnapshot.getValue(Account::class.java)
                    if (account != null && account.loggedIn && account.username != "Holidays") {
                        accountsList.add(account)
                    }
                }
                accountAdapter.updateAccounts(accountsList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun showLoginDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_login, null)
        val usernameEditText = dialogView.findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.passwordEditText)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Login")
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.loginSubmitButton).setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            } else {
                validateLogin(username, password) { isValid, message ->
                    if (isValid) {
                        dialog.dismiss()
                        fetchEventsOfLoggedInAccounts()
                        refreshAccountsList()
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialogView.findViewById<Button>(R.id.backButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showSignUpDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_signup, null)
        val usernameEditText = dialogView.findViewById<EditText>(R.id.usernameEditText)
        val emailEditText = dialogView.findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.passwordEditText)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Sign Up")
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.signupSubmitButton).setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            } else if (password.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT)
                    .show()
            } else {
                checkUniqueUsernameAndEmail(username, email) { isUnique ->
                    if (isUnique) {
                        createAccount(username, email, password)
                        dialog.dismiss()
                        refreshAccountsList()
                    } else {
                        Toast.makeText(this, "Username or email already exists", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        dialogView.findViewById<Button>(R.id.backButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun validateLogin(
        username: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance()
        val accountsRef = database.getReference("accounts")

        accountsRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val account = snapshot.children.mapNotNull { it.getValue(Account::class.java) }
                        .firstOrNull { it.username != "Holidays" }
                    when {
                        account == null -> callback(false, "Account does not exist")
                        account.password != password -> callback(false, "Invalid password")
                        account.loggedIn -> callback(false, "Account is already logged in")
                        else -> {
                            accountsRef.child(account.id.toString()).child("loggedIn")
                                .setValue(true)
                            callback(true, "Login successful")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, "Login failed due to database error")
                }
            })
    }

    private fun checkUniqueUsernameAndEmail(
        username: String,
        email: String,
        callback: (Boolean) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance()
        val accountsRef = database.getReference("accounts")

        accountsRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        callback(false)
                    } else {
                        accountsRef.orderByChild("email").equalTo(email)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    callback(!snapshot.exists())
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    callback(false)
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }

    private fun createAccount(username: String, email: String, password: String) {
        val database = FirebaseDatabase.getInstance()
        val accountsRef = database.getReference("accounts")

        accountsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newId = (snapshot.children.mapNotNull { it.getValue(Account::class.java)?.id }
                    .maxOrNull() ?: 0) + 1
                val newAccount = Account(
                    id = newId,
                    username = username,
                    email = email,
                    password = password,
                    loggedIn = true
                )
                accountsRef.child(newId.toString()).setValue(newAccount)
                Toast.makeText(
                    this@MainActivity,
                    "Account created successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to create account", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private class EventDayDecorator(
        private val dates: Set<CalendarDay>,
        private val context: Context
    ) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade) {
            view.setBackgroundDrawable(
                context.getDrawable(R.drawable.event_date_background) ?: return
            )
        }
    }

    private fun showManageEventsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.manage_events, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.addEventButton).setOnClickListener {
            showAddEventDialog()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.editEventButton).setOnClickListener {
            showSelectAccountDialog { account ->
                showSelectEventDialog(account, { event ->
                    showEditEventDialog(event)
                })
            }
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.deleteEventButton).setOnClickListener {
            showSelectAccountDialog { account ->
                showSelectEventDialog(account, { event ->
                    deleteEvent(event.id)
                }, isDelete = true)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAddEventDialog() {
        val dialogView = layoutInflater.inflate(R.layout.add_event, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val eventTitleEditText = dialogView.findViewById<EditText>(R.id.eventTitleEditText)
        val eventDescriptionEditText =
            dialogView.findViewById<EditText>(R.id.eventDescriptionEditText)
        val eventDateEditText = dialogView.findViewById<EditText>(R.id.eventDateEditText)
        val eventTimeEditText = dialogView.findViewById<EditText>(R.id.eventTimeEditText)

        dialogView.findViewById<Button>(R.id.saveEventButton).setOnClickListener {
            val title = eventTitleEditText.text.toString().trim()
            val description = eventDescriptionEditText.text.toString().trim()
            val date = eventDateEditText.text.toString().trim()
            val time = eventTimeEditText.text.toString().trim()

            if (title.isEmpty() || description.isEmpty() || date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            } else {
                // Save the event to Firebase
                saveEvent(title, description, date, time)
                dialog.dismiss()
            }
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveEvent(title: String, description: String, date: String, time: String) {
        val database = FirebaseDatabase.getInstance()
        val eventsRef = database.getReference("events")

        val newEventId = eventsRef.push().key ?: return
        val newEvent = Event(
            id = newEventId.hashCode(), // Ensure unique ID
            title = title,
            description = description,
            date = date.toLong(),
            time = time.toLong(),
            accountId = 0,
            repeating = false
        )
        eventsRef.child(newEventId).setValue(newEvent)
        Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show()
    }

    private fun showSelectAccountDialog(onAccountSelected: (Account) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.select_account, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val accountsRecyclerView = dialogView.findViewById<RecyclerView>(R.id.accountsRecyclerView)
        accountsRecyclerView.layoutManager = LinearLayoutManager(this)

        getLoggedInAccounts { accounts ->
            val accountAdapter = AccountAdapter(accounts)
            accountsRecyclerView.adapter = accountAdapter

            dialogView.findViewById<Button>(R.id.nextButton).setOnClickListener {
                val selectedAccount = accountAdapter.getSelectedAccount()
                if (selectedAccount != null) {
                    onAccountSelected(selectedAccount)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Please select an account", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.show()
        }
    }

    private fun showSelectEventDialog(account: Account, onEventSelected: (Event) -> Unit, isDelete: Boolean = false) {
        val dialogView = layoutInflater.inflate(R.layout.select_event, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val eventsRecyclerView = dialogView.findViewById<RecyclerView>(R.id.eventsRecyclerView)
        eventsRecyclerView.layoutManager = LinearLayoutManager(this)

        getEventsForAccount(account) { events ->
            val layoutId = if (isDelete) R.layout.item_event_delete else R.layout.item_event_edit
            val eventAdapter = EventAdapter(events, layoutId)
            eventsRecyclerView.adapter = eventAdapter

            dialogView.findViewById<Button>(R.id.nextButton).setOnClickListener {
                val selectedEvent = eventAdapter.getSelectedEvent()
                if (selectedEvent != null) {
                    onEventSelected(selectedEvent)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Please select an event", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.show()
        }
    }

    private fun showEditEventDialog(event: Event) {
        val dialogView = layoutInflater.inflate(R.layout.add_event, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val eventTitleEditText = dialogView.findViewById<EditText>(R.id.eventTitleEditText)
        val eventDescriptionEditText =
            dialogView.findViewById<EditText>(R.id.eventDescriptionEditText)
        val eventDateEditText = dialogView.findViewById<EditText>(R.id.eventDateEditText)
        val eventTimeEditText = dialogView.findViewById<EditText>(R.id.eventTimeEditText)

        eventTitleEditText.setText(event.title)
        eventDescriptionEditText.setText(event.description)
        eventDateEditText.setText(event.date.toString())
        eventTimeEditText.setText(event.time.toString())

        dialogView.findViewById<Button>(R.id.saveEventButton).setOnClickListener {
            val title = eventTitleEditText.text.toString().trim()
            val description = eventDescriptionEditText.text.toString().trim()
            val date = eventDateEditText.text.toString().trim()
            val time = eventTimeEditText.text.toString().trim()

            if (title.isEmpty() || description.isEmpty() || date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            } else {
                // Update the event in Firebase
                updateEvent(event.id, title, description, date, time)
                dialog.dismiss()
            }
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateEvent(eventId: Int, title: String, description: String, date: String, time: String) {
        val database = FirebaseDatabase.getInstance()
        val eventsRef = database.getReference("events").child(eventId.toString())

        val updatedEvent = Event(
            id = eventId,
            title = title,
            description = description,
            date = date.toLong(),
            time = time.toLong(),
            accountId = 0,
            repeating = false
        )
        eventsRef.setValue(updatedEvent)
        Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show()
    }

    private fun deleteEvent(eventId: Int) {
        val database = FirebaseDatabase.getInstance()
        val eventsRef = database.getReference("events").child(eventId.toString())

        eventsRef.removeValue()
        Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show()
    }

    private fun getEventsForAccount(account: Account, callback: (List<Event>) -> Unit) {
        val eventsList = mutableListOf<Event>()
        val database = FirebaseDatabase.getInstance()
        val eventsRef = database.getReference("events")

        eventsRef.orderByChild("accountId").equalTo(account.id.toDouble()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(Event::class.java)
                    if (event != null) {
                        eventsList.add(event)
                    }
                }
                callback(eventsList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}