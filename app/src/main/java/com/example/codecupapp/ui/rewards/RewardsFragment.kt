package com.example.codecupapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codecupapp.databinding.FragmentRewardsBinding


class RewardsFragment : Fragment() {

    private var _binding: FragmentRewardsBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRewardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()
    private val rewardsViewModel: RewardsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRewardsBinding.bind(view)

        val gridLayout = view.findViewById<GridLayout>(R.id.loyaltyGridLayout)
        observeLoyaltyStamps(gridLayout)


        gridLayout.setOnClickListener {
            loyaltyViewModel.stamps.value?.let { count ->
                if (count >= 8) {
                    showResetDialog()
                }
            }
        }

        binding.recyclerRewards.layoutManager = LinearLayoutManager(requireContext())
        rewardsViewModel.initializeRewards()
        rewardsViewModel.points.observe(viewLifecycleOwner) { points ->
            binding.textAvailablePoints.text = "Points: $points"
        }
        rewardsViewModel.rewardList.observe(viewLifecycleOwner) { rewards ->
            binding.recyclerRewards.adapter = RewardsAdapter(rewards) { reward ->
                if (rewardsViewModel.redeem(reward)) {
                    Toast.makeText(requireContext(), "Redeemed: ${reward.title}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Not enough points!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.btnHistory.setOnClickListener {
            findNavController().navigate(R.id.redeemFragment)
        }

//
//        rewardsViewModel.points.observe(viewLifecycleOwner) { points ->
//            binding.textAvailablePoints.text = "Points: $points"
//            UserData.points = points
//        }
    }




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


    private fun observeLoyaltyStamps(gridLayout: GridLayout) {
        loyaltyViewModel.stamps.observe(viewLifecycleOwner) { count ->
            gridLayout.removeAllViews()

            for (i in 1..8) {
                val cupIcon = ImageView(requireContext()).apply {
                    setImageResource(
                        if (i <= count) R.drawable.local_cafe_40px__1_
                        else R.drawable.local_cafe_40px // Use separate outline icon
                    )
                    val size = 48.dp
                    layoutParams = ViewGroup.MarginLayoutParams(size, size).apply {
                        setMargins(12, 12, 12, 12)
                    }
                }
                gridLayout.addView(cupIcon)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()


}

