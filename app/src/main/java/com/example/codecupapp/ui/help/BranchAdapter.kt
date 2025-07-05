package com.example.codecupapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.data.BranchLocation


class BranchAdapter(
    private val branches: List<BranchLocation>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<BranchAdapter.BranchViewHolder>() {

    inner class BranchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.branchName)

        fun bind(branch: BranchLocation) {
            title.text = branch.name
            itemView.setOnClickListener {
                onClick(branch.uriString)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BranchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_branch, parent, false)
        return BranchViewHolder(view)
    }

    override fun onBindViewHolder(holder: BranchViewHolder, position: Int) {
        holder.bind(branches[position])
    }

    override fun getItemCount(): Int = branches.size
}
