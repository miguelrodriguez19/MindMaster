package com.miguelrodriguez19.mindmaster.views.settings.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.button.MaterialButton
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentFaqsBinding
import com.miguelrodriguez19.mindmaster.databinding.FragmentHelpBinding
import com.miguelrodriguez19.mindmaster.models.structures.FAQ
import com.miguelrodriguez19.mindmaster.views.settings.FAQsAdapter

class FaqsFragment : Fragment() {
    private var _binding: FragmentFaqsBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchView: SearchView
    private lateinit var rvFAQs: RecyclerView
    private lateinit var adapter: FAQsAdapter
    var data: ArrayList<FAQ> = ArrayList()
    private var dataFiltered: ArrayList<FAQ> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaqsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidget()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { search(it) }
                return true
            }
        })
    }

    private fun search(text: String) {
        val filteredData = ArrayList<FAQ>()
        if (data.isNotEmpty()) {
            for (item in data) {
                if (item.question.contains(text, true)) {
                    filteredData.add(item)
                }
            }
            adapter.data = filteredData
            adapter.notifyDataSetChanged()
        }
    }

    private fun initWidget() {
        searchView = binding.searchView
        rvFAQs = binding.rvFaqs

        val mLayoutManager = StaggeredGridLayoutManager(1, 1)
        rvFAQs.layoutManager = mLayoutManager

        adapter = FAQsAdapter(data) {

        }
        dataFiltered.addAll(data)

        rvFAQs.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
