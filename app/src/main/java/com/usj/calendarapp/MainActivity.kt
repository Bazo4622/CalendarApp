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
import com.usj.calendarapp.adapter.AccountAdapter
import com.usj.calendarapp.data.AppDatabase
import com.usj.calendarapp.model.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var accountsRecyclerView: RecyclerView
    private lateinit var accountAdapter: AccountAdapter
    private val applicationScope = CoroutineScope(Job() + Dispatchers.Main)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        accountsRecyclerView = findViewById(R.id.accountsRecyclerView)

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
        // Replace with actual logic to get logged-in accounts
        return listOf(
            Account(1, "admin", "admin@example.com", "admin"),
            Account(2, "default", "default@example.com", "default")
        )
    }
}