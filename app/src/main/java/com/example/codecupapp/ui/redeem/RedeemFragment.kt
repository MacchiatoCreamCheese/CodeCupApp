package com.example.codecupapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codecupapp.databinding.FragmentRedeemBinding

class RedeemFragment : Fragment() {

    private var _binding: FragmentRedeemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRedeemBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val rewardsViewModel: RewardsViewModel by activityViewModels()
    private lateinit var adapter: RedeemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
