package com.example.codecupapp

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codecupapp.data.CartItem
import com.example.codecupapp.databinding.CartBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CartBottomSheet(
    private val cartItems: List<CartItem>,
    private val onConfirm: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: CartBottomSheetBinding? = null
    private val binding get() = _binding!!

    // Inflate layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CartBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Setup view components
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupConfirmButton()
    }

    // Configure bottom sheet appearance
    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.5f)
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }

    // Optional: define dynamic height (currently unused)
    private fun getDialogHeight(): Int {
        val displayMetrics = resources.displayMetrics
        return (displayMetrics.heightPixels * 0.33).toInt()
    }

    // Setup cart preview list
    private fun setupRecyclerView() {
        binding.recyclerCartPreview.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCartPreview.adapter = CartPreviewAdapter(cartItems)
    }

    // Handle confirm button click
    private fun setupConfirmButton() {
        binding.btnConfirm.setOnClickListener {
            dismiss()
            onConfirm()
        }
    }

    // Prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
