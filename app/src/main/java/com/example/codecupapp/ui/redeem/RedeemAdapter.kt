package com.example.codecupapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.databinding.ItemRedeemBinding


/**
 * Adapter for displaying redeemed rewards or point transactions in a RecyclerView.
 *
 * @param items List of point transactions to display.
 */
class RedeemAdapter(
    private var items: List<PointTransaction>
) : RecyclerView.Adapter<RedeemAdapter.RedeemViewHolder>() {

    // 📦 ViewHolder uses ViewBinding to access views safely and cleanly
    inner class RedeemViewHolder(val binding: ItemRedeemBinding) :
        RecyclerView.ViewHolder(binding.root)

    // 🔧 Inflate item layout with ViewBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RedeemViewHolder {
        val binding = ItemRedeemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RedeemViewHolder(binding)
    }

    // 🖼️ Bind each point transaction to its item view
    override fun onBindViewHolder(holder: RedeemViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            textRedeemTitle.text = item.source
            textRedeemDate.text = item.date
            textRedeemPoints.text = if (item.amount >= 0) "+${item.amount} pts" else "${item.amount} pts"
        }
    }

    // 🔢 Return number of items in the list
    override fun getItemCount(): Int = items.size

    // 🔁 Update list data and refresh UI
    fun updateData(newItems: List<PointTransaction>) {
        items = newItems
        notifyDataSetChanged()
    }
}