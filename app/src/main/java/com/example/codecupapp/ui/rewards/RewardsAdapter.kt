package com.example.codecupapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.data.RewardItem

class RewardsAdapter(
    private val rewards: List<RewardItem>,
    private val onRedeem: (RewardItem) -> Unit
) : RecyclerView.Adapter<RewardsAdapter.RewardViewHolder>() {

    inner class RewardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.textRewardTitle)
        val points: TextView = view.findViewById(R.id.textRewardPoints)
        val btnRedeem: Button = view.findViewById(R.id.btnRedeem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reward_card, parent, false)
        return RewardViewHolder(view)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        val reward = rewards[position]
        holder.title.text = reward.title
        holder.points.text = "Requires: ${reward.pointsRequired} points"

        holder.btnRedeem.setOnClickListener {
            onRedeem(reward)
        }
    }

    override fun getItemCount(): Int = rewards.size
}
