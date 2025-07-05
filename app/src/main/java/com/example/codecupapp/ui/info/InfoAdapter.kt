package com.example.codecupapp.ui.info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.data.InfoItem
import com.example.codecupapp.databinding.ItemInfoRowBinding

class InfoAdapter(
    private val items: List<InfoItem>
) : RecyclerView.Adapter<InfoAdapter.InfoViewHolder>() {

    inner class InfoViewHolder(val binding: ItemInfoRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InfoItem) {
            binding.title.text = item.title
            binding.subtitle.text = item.subtitle
            binding.icon.setImageResource(item.icon)
            binding.root.setOnClickListener { item.action() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        val binding = ItemInfoRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
