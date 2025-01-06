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
import kotlinx.coroutines.launch
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
        accountAdapter = AccountAdapter(getLoggedInAccounts(), this)
        accountsRecyclerView.adapter = accountAdapter

        eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        eventAdapter = EventAdapter(emptyList())
        eventsRecyclerView.adapter = eventAdapter

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            showLoginDialog()
        }

        findViewById<Button>(R.id.signUpButton).setOnClickListener {
            showSignUpDialog()
        }

        fetchEventsOfLoggedInAccounts()
    }

    private fun fetchEventsOfLoggedInAccounts() {
        val database = FirebaseDatabase.getInstance()
        val accountsRef = database.getReference("accounts")
        val eventsRef = database.getReference("events")

        accountsRef.orderByChild("loggedIn").equalTo(true).addListenerForSingleValueEvent(object : ValueEventListener {
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
                                    timeInMillis = event.date // Date is already in milliseconds
                                }
                                markedDates.add(CalendarDay.from(eventDate))
                            }
                        }
                        // Sort events by date
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

    private fun getLoggedInAccounts(): List<Account> {
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
                accountAdapter.updateAccounts(accountsList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        return accountsList
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
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            } else {
                checkUniqueUsernameAndEmail(username, email) { isUnique ->
                    if (isUnique) {
                        createAccount(username, email, password)
                        dialog.dismiss()
                        refreshAccountsList()
                    } else {
                        Toast.makeText(this, "Username or email already exists", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialogView.findViewById<Button>(R.id.backButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun validateLogin(username: String, password: String, callback: (Boolean, String) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val accountsRef = database.getReference("accounts")

        accountsRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val account = snapshot.children.mapNotNull { it.getValue(Account::class.java) }
                    .firstOrNull { it.username != "Holidays" }
                when {
                    account == null -> callback(false, "Account does not exist")
                    account.password != password -> callback(false, "Invalid password")
                    account.loggedIn -> callback(false, "Account is already logged in")
                    else -> {
                        accountsRef.child(account.id.toString()).child("loggedIn").setValue(true)
                        callback(true, "Login successful")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Login failed due to database error")
            }
        })
    }

    private fun checkUniqueUsernameAndEmail(username: String, email: String, callback: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val accountsRef = database.getReference("accounts")

        accountsRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    callback(false)
                } else {
                    accountsRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
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
                val newId = (snapshot.children.mapNotNull { it.getValue(Account::class.java)?.id }.maxOrNull() ?: 0) + 1
                val newAccount = Account(id = newId, username = username, email = email, password = password, loggedIn = true)
                accountsRef.child(newId.toString()).setValue(newAccount)
                Toast.makeText(this@MainActivity, "Account created successfully", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to create account", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private class EventDayDecorator(private val dates: Set<CalendarDay>, private val context: Context) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade) {
            view.setBackgroundDrawable(context.getDrawable(R.drawable.event_date_background) ?: return)
        }
    }
}