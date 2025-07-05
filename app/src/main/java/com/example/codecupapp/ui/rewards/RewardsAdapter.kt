package com.example.codecupapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.data.RewardItem
import com.example.codecupapp.databinding.ItemRewardCardBinding

/**
 * Adapter for displaying redeemable rewards in a RecyclerView.
 * Handles UI binding and redeem button click logic.
 */
class RewardsAdapter(
    private val rewards: List<RewardItem>,
    private val onRedeem: (RewardItem) -> Unit
) : RecyclerView.Adapter<RewardsAdapter.RewardViewHolder>() {

    // ViewHolder using ViewBinding for reward card layout.
    inner class RewardViewHolder(val binding: ItemRewardCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Inflate view using ViewBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val binding = ItemRewardCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RewardViewHolder(binding)
    }

    // Bind data and set click listener
    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        val reward = rewards[position]
        with(holder.binding) {
            textRewardTitle.text = reward.title
            textRewardPoints.text = "Requires: ${reward.pointsRequired} points"
            btnRedeem.setOnClickListener {
                onRedeem(reward)
            }
        }
    }

    override fun getItemCount(): Int = rewards.size
}