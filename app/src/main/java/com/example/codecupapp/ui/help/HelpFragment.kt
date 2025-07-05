package com.example.codecupapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.data.BranchLocation
import com.example.codecupapp.databinding.FragmentHelpBinding

class HelpFragment : Fragment() {

    private lateinit var binding: FragmentHelpBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val branches = listOf(
            BranchLocation("Katinat - Ho Chi Minh", getString(R.string.query_find_branch_1)),
            BranchLocation("Katinat - Binh Duong", getString(R.string.query_find_branch_2)),
            // Add more branches here
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerBranches)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = BranchAdapter(branches) { uri ->
            openMap(uri)
        }


    }

    private fun openMap(uriString: String) {
        val mapUri = uriString.toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        try {
            startActivity(mapIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                getString(R.string.cant_open_map),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

