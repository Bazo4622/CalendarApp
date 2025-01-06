package com.usj.calendarapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.usj.calendarapp.R
import com.usj.calendarapp.model.Account

class AccountAdapter(private var accounts: List<Account>, private val layoutId: Int) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    private var selectedAccount: Account? = null

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.username)
        val logoutButton: Button? = itemView.findViewById(R.id.logoutButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return AccountViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val currentAccount = accounts[position]
        holder.username.text = currentAccount.username

        holder.logoutButton?.setOnClickListener {
            val database = FirebaseDatabase.getInstance()
            val accountsRef = database.getReference("accounts")
            accountsRef.child(currentAccount.id.toString()).child("loggedIn").setValue(false)
            selectedAccount = null
            Toast.makeText(holder.itemView.context, "Logout successful", Toast.LENGTH_SHORT).show()
            updateAccounts(accounts.filter { it.id != currentAccount.id })
        }

        holder.itemView.setOnClickListener {
            selectedAccount = currentAccount
            notifyDataSetChanged()
        }

        if (selectedAccount == currentAccount) {
            holder.itemView.setBackgroundColor(Color.GRAY)
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    fun getSelectedAccount(): Account? = selectedAccount

    override fun getItemCount() = accounts.size

    fun updateAccounts(newAccounts: List<Account>) {
        accounts = newAccounts
        notifyDataSetChanged()
    }
}