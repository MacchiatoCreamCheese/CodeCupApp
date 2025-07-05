package com.example.codecupapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.codecupapp.databinding.FragmentOrderSuccessBinding

class OrderSuccessFragment : Fragment() {

    private var _binding: FragmentOrderSuccessBinding? = null
    private val binding get() = _binding!!

    // Shared ViewModel for tracking loyalty progress
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()

    // Flag to prevent adding stamp multiple times on fragment re-creation
    private var stampAdded = false

    // Inflate layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Handle stamp logic and button navigation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ensure stamp is only added once per successful order
        if (!stampAdded) {
            loyaltyViewModel.addStamp()
            stampAdded = true
        }

        // Navigate to orders page
        binding.btnTrackOrder.setOnClickListener {
            findNavController().navigate(R.id.ordersFragment)
        }
    }

    // Prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
