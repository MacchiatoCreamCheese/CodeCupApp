package com.example.codecupapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView




class PromoAdapter(private val promos: List<Int>) : RecyclerView.Adapter<PromoAdapter.PromoViewHolder>() {

    inner class PromoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val promoImage: ImageView = itemView.findViewById(R.id.promoImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_promo_card, parent, false)
        return PromoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PromoViewHolder, position: Int) {
        holder.promoImage.setImageResource(promos[position])
    }

    override fun getItemCount(): Int = promos.size
}