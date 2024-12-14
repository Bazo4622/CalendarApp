package com.usj.calendarapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.usj.calendarapp.MainActivity
import com.usj.calendarapp.R
import com.usj.calendarapp.model.Account

class AccountAdapter(private var accounts: List<Account>, private val mainActivity: MainActivity) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.username)
        val logoutButton: Button = itemView.findViewById(R.id.logoutButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false)
        return AccountViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val currentAccount = accounts[position]
        holder.username.text = currentAccount.username
        holder.logoutButton.setOnClickListener {
            logoutAccount(currentAccount)
        }
    }

    override fun getItemCount() = accounts.size

    fun updateAccounts(newAccounts: List<Account>) {
        accounts = newAccounts
        notifyDataSetChanged()
    }

    private fun logoutAccount(account: Account) {
        val database = FirebaseDatabase.getInstance()
        val accountsRef = database.getReference("accounts")
        accountsRef.child(account.id.toString()).child("loggedIn").setValue(false).addOnCompleteListener {
            mainActivity.refreshAccountsList()
        }
    }
}