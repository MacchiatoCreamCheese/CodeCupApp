package com.example.codecupapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.codecupapp.databinding.FragmentOrderSuccessBinding

class OrderSuccessFragment : Fragment(R.layout.fragment_order_success) {

    private var _binding: FragmentOrderSuccessBinding? = null
    private val binding get() = _binding!!

    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()
    private var stampAdded = false  // Prevents multiple increments on recreation

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentOrderSuccessBinding.bind(view)

        if (!stampAdded) {
            loyaltyViewModel.addStamp()
            stampAdded = true
        }

        binding.btnTrackOrder.setOnClickListener {
            findNavController().navigate(R.id.ordersFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
