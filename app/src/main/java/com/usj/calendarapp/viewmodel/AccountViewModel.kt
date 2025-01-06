package com.usj.calendarapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.usj.calendarapp.model.Account

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> get() = _accounts

    init {
        fetchAccountsFromFirebase()
    }

    private fun fetchAccountsFromFirebase() {
        val database = FirebaseDatabase.getInstance()
        val accountsRef = database.getReference("accounts")
        accountsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val accountsList = mutableListOf<Account>()
                for (accountSnapshot in snapshot.children) {
                    val account = accountSnapshot.getValue(Account::class.java)
                    if (account != null) {
                        accountsList.add(account)
                    }
                }
                _accounts.value = accountsList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}