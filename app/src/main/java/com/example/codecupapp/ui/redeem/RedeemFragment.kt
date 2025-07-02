package com.example.codecupapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codecupapp.databinding.FragmentRedeemBinding

class RedeemFragment : Fragment(R.layout.fragment_redeem) {

    private var _binding: FragmentRedeemBinding? = null
    private val binding get() = _binding!!

    private val rewardsViewModel: RewardsViewModel by activityViewModels()
    private lateinit var adapter: RedeemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentRedeemBinding.bind(view)

        binding.recyclerRedeem.layoutManager = LinearLayoutManager(requireContext())
        adapter = RedeemAdapter(emptyList())
        binding.recyclerRedeem.adapter = adapter

        // Observe transaction history for both add/subtract
        rewardsViewModel.transactionHistory.observe(viewLifecycleOwner) { history ->
            adapter.updateData(history)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
