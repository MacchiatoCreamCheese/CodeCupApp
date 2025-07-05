package com.example.codecupapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.databinding.ItemCoffeeCardBinding

/**
 * Adapter for displaying coffee items in a RecyclerView grid.
 *
 * @param coffeeList List of coffee items to display.
 * @param onItemClick Callback when a coffee item is clicked.
 */
class CoffeeAdapter(
    private val coffeeList: List<CoffeeItem>,
    private val onItemClick: (CoffeeItem) -> Unit
) : RecyclerView.Adapter<CoffeeAdapter.CoffeeViewHolder>() {

    // ViewHolder uses ViewBinding for safe view reference
    inner class CoffeeViewHolder(val binding: ItemCoffeeCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Inflate item layout and create ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoffeeViewHolder {
        val binding = ItemCoffeeCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CoffeeViewHolder(binding)
    }

    // Bind data to UI
    override fun onBindViewHolder(holder: CoffeeViewHolder, position: Int) {
        val coffee = coffeeList[position]
        holder.binding.apply {
            textCoffeeName.text = coffee.name
            textCoffeePrice.text = "$%.2f".format(coffee.price)
            imageCoffee.setImageResource(coffee.imageResId)

            root.setOnClickListener { onItemClick(coffee) }
        }
    }

    // Number of items
    override fun getItemCount(): Int = coffeeList.size
}
