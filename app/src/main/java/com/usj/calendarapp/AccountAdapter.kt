package com.usj.calendarapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.usj.calendarapp.R
import com.usj.calendarapp.model.Account

class AccountAdapter(private val accounts: List<Account>) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

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
            // Handle logout button click
        }
    }

    override fun getItemCount() = accounts.size
}