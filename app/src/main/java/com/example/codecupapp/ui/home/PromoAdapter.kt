package com.example.codecupapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.databinding.ItemPromoCardBinding

/**
 * Adapter for displaying promotional banners using a RecyclerView.
 * @param promos List of drawable resource IDs for each promotion image.
 */
class PromoAdapter(
    private val promos: List<Int>
) : RecyclerView.Adapter<PromoAdapter.PromoViewHolder>() {

    /**
     * ViewHolder class that holds reference to the layout views
     * for a single promotional card.
     */
    inner class PromoViewHolder(val binding: ItemPromoCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * Inflates the layout for a single item using view binding.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPromoCardBinding.inflate(inflater, parent, false)
        return PromoViewHolder(binding)
    }

    /**
     * Binds the image resource at the given position to the view holder.
     */
    override fun onBindViewHolder(holder: PromoViewHolder, position: Int) {
        val imageResId = promos[position]
        holder.binding.promoImage.setImageResource(imageResId)
    }

    /**
     * Returns the total number of promotional items to display.
     */
    override fun getItemCount(): Int = promos.size
}
