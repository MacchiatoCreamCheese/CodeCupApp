package com.example.codecupapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.databinding.ItemPromoCardBinding


/**
 * Adapter for displaying promotional images in a ViewPager or RecyclerView.
 * @param promos List of drawable resource IDs representing promotional banners.
 */
class PromoAdapter(
    private val promos: List<Int>
) : RecyclerView.Adapter<PromoAdapter.PromoViewHolder>() {

    // 🧱 ViewHolder using ViewBinding for safer UI references
    inner class PromoViewHolder(val binding: ItemPromoCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    // 🔧 Inflate layout using ViewBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoViewHolder {
        val binding = ItemPromoCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PromoViewHolder(binding)
    }

    // 🖼️ Bind promo image to ViewHolder
    override fun onBindViewHolder(holder: PromoViewHolder, position: Int) {
        val imageRes = promos[position]
        holder.binding.promoImage.setImageResource(imageRes)
    }

    // 📦 Total number of promo items
    override fun getItemCount(): Int = promos.size
}