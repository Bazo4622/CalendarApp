package com.usj.calendarapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.usj.calendarapp.R
import com.usj.calendarapp.model.Account

class AccountAdapter(private var accounts: List<Account>) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    private var selectedAccount: Account? = null

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.username)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_account_select, parent, false)
        return AccountViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val currentAccount = accounts[position]
        holder.username.text = currentAccount.username

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