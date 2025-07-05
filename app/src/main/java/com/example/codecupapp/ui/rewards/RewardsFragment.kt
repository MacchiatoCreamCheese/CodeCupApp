package com.example.codecupapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codecupapp.databinding.FragmentRewardsBinding


class RewardsFragment : Fragment() {

    // ViewBinding
    private var _binding: FragmentRewardsBinding? = null
    private val binding get() = _binding!!

    // Shared ViewModels
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()
    private val rewardsViewModel: RewardsViewModel by activityViewModels()

    // Inflate layout with ViewBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRewardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Lifecycle hook: setup UI and observers
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Loyalty Grid Setup
        observeLoyaltyStamps()

        binding.loyaltyGridLayout.setOnClickListener {
            if ((loyaltyViewModel.stamps.value ?: 0) >= 8) showResetDialog()
        }

        // Rewards List Setup
        binding.recyclerRewards.layoutManager = LinearLayoutManager(requireContext())

        rewardsViewModel.initializeRewards()

        rewardsViewModel.points.observe(viewLifecycleOwner) { points ->
            binding.textAvailablePoints.text = "Points: $points"
        }

        rewardsViewModel.rewardList.observe(viewLifecycleOwner) { rewards ->
            binding.recyclerRewards.adapter = RewardsAdapter(rewards) { reward ->
                if (rewardsViewModel.redeem(reward)) {
                    Toast.makeText(
                        requireContext(),
                        "Redeemed: ${reward.title}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(requireContext(), "Not enough points!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        // 🧾 Redeem history navigation
        binding.btnHistory.setOnClickListener {
            findNavController().navigate(R.id.redeemFragment)
        }
    }

    // Observe loyalty stamps and update grid
    private fun observeLoyaltyStamps() {
        loyaltyViewModel.stamps.observe(viewLifecycleOwner) { count ->
            val gridLayout = binding.loyaltyGridLayout
            gridLayout.removeAllViews()

            for (i in 1..8) {
                val icon = ImageView(requireContext()).apply {
                    setImageResource(
                        if (i <= count) R.drawable.local_cafe_40px__1_
                        else R.drawable.local_cafe_40px
                    )
                    val size = 48.dp
                    layoutParams = ViewGroup.MarginLayoutParams(size, size).apply {
                        setMargins(12, 12, 12, 12)
                    }
                }
                gridLayout.addView(icon)
            }
        }
    }

    // Dialog to confirm loyalty reset
    private fun showResetDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Loyalty Card?")
            .setMessage("You've collected all 8 stamps! Do you want to reset your card and redeem your reward?")
            .setPositiveButton("Reset") { _, _ ->
                loyaltyViewModel.resetStamps()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Cleanup
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Extension for dp-to-px conversion
    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}