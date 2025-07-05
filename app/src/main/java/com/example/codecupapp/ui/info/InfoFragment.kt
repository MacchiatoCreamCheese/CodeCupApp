package com.example.codecupapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codecupapp.data.InfoItem
import com.example.codecupapp.databinding.FragmentInfoBinding
import com.example.codecupapp.ui.info.InfoAdapter

class InfoFragment : Fragment() {

    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var infoAdapter: InfoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        val items = listOf(
            InfoItem("Hot line", R.drawable.ac_unit_40px, "(028) 7300 1009") {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:02873001009")))
            },
            InfoItem("Email", R.drawable.ac_unit_40px, "cs@katinat.vn") {
                startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:cs@katinat.vn")))
            },
            InfoItem("Website", R.drawable.ac_unit_40px, "http://katinat.vn/") {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://katinat.vn/")))
            },
            InfoItem("Facebook", R.drawable.ac_unit_40px, "https://www.facebook.com/katinat.vn") {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.facebook.com/katinat.vn")
                    )
                )
            },
            InfoItem("Feedback and support", R.drawable.ac_unit_40px, "Let us know!") {
                Toast.makeText(context, "Feedback feature coming soon!", Toast.LENGTH_SHORT).show()
            }
        )

        infoAdapter = InfoAdapter(items)
        binding.recyclerInfo.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = infoAdapter
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
