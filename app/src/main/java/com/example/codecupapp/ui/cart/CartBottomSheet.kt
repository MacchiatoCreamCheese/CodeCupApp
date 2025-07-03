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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CartBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerCartPreview.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCartPreview.adapter = CartPreviewAdapter(cartItems)

        binding.btnConfirm.setOnClickListener {
            dismiss()
            onConfirm()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // Full width
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            // Transparent background
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // Elevate above nav bar
            setDimAmount(0.5f)
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }

    private fun getDialogHeight(): Int {
        val displayMetrics = resources.displayMetrics
        return (displayMetrics.heightPixels * 0.33).toInt() // 1/3 of screen
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
