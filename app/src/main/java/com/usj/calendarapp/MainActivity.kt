package com.usj.calendarapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.usj.calendarapp.adapter.AccountAdapter
import com.usj.calendarapp.model.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import com.usj.calendarapp.utils.addHolidaysToFirebase

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var accountsRecyclerView: RecyclerView
    private lateinit var accountAdapter: AccountAdapter
    private lateinit var calendarView: MaterialCalendarView
    private val applicationScope = CoroutineScope(Job() + Dispatchers.Main)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addHolidaysToFirebase()

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        accountsRecyclerView = findViewById(R.id.accountsRecyclerView)
        calendarView = findViewById(R.id.calendarView)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        navigationView.setNavigationItemSelectedListener(this)

        accountsRecyclerView.layoutManager = LinearLayoutManager(this)
        accountAdapter = AccountAdapter(getLoggedInAccounts())
        accountsRecyclerView.adapter = accountAdapter

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            // Handle login button click
        }

        findViewById<Button>(R.id.signUpButton).setOnClickListener {
            // Handle sign up button click
        }

        calendarView.setOnDateChangedListener { widget, date, selected ->
            widget.removeDecorators()
            widget.addDecorator(object : DayViewDecorator {
                override fun shouldDecorate(day: CalendarDay): Boolean {
                    return day == date
                }

                override fun decorate(view: DayViewFacade) {
                    if (selected) {
                        view.setBackgroundDrawable(getDrawable(R.drawable.selected_date_background) ?: return)
                        view.addSpan { textPaint: android.text.TextPaint ->
                            textPaint.color = getColor(R.color.calendar_date_selected)
                        }
                    } else {
                        view.setBackgroundDrawable(getDrawable(R.drawable.transparent_background) ?: return)
                        view.addSpan { textPaint: android.text.TextPaint ->
                            textPaint.color = getColor(R.color.calendar_date_unselected)
                        }
                    }
                }
            })
            widget.invalidateDecorators()
        }
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
                    if (account != null && account.id != 1) {
                        accountsList.add(account)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        return accountsList
    }
}