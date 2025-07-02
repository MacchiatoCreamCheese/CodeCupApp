package com.example.codecupapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RedeemAdapter(private var items: List<PointTransaction>) :
    RecyclerView.Adapter<RedeemAdapter.RedeemViewHolder>() {

    inner class RedeemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.textRedeemTitle)
        val date: TextView = view.findViewById(R.id.textRedeemDate)
        val points: TextView = view.findViewById(R.id.textRedeemPoints)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RedeemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_redeem, parent, false)
        return RedeemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RedeemViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.source
        holder.date.text = item.date
        holder.points.text = if (item.amount >= 0) "+${item.amount} pts" else "${item.amount} pts"
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<PointTransaction>) {
        items = newItems
        notifyDataSetChanged()
    }
}
