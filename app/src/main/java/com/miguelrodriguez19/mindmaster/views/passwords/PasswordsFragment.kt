package com.miguelrodriguez19.mindmaster.views.passwords

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.miguelrodriguez19.mindmaster.databinding.FragmentPasswordsBinding
import com.miguelrodriguez19.mindmaster.models.structures.GroupPasswordsResponse
import com.miguelrodriguez19.mindmaster.models.utils.AllBottomSheets.Companion.showPasswordsBS
import com.miguelrodriguez19.mindmaster.views.passwords.adapters.GroupAdapter

class PasswordsFragment : Fragment() {
    private var _binding: FragmentPasswordsBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchView: SearchView
    private lateinit var btnAddGroup: ExtendedFloatingActionButton
    private lateinit var rvAccountsGroups: RecyclerView
    private lateinit var adapter : GroupAdapter
    var data: ArrayList<GroupPasswordsResponse> = ArrayList()
    private var dataFiltered: ArrayList<GroupPasswordsResponse> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidget()
        btnAddGroup.setOnClickListener {
            showPasswordsBS(requireContext(), null){
                // TODO()
            }
        }
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
        val filteredData = ArrayList<GroupPasswordsResponse>()
        if (data.isNotEmpty()) {
            for (item in data) {
                if (item.name.contains(text, true)) {
                    filteredData.add(item)
                }
            }
            adapter.data = filteredData
            adapter.notifyDataSetChanged()
        }
    }

    private fun initWidget() {
        searchView = binding.searchView
        btnAddGroup = binding.btnAddGroup
        rvAccountsGroups = binding.rvAccountsGroups

        val mLayoutManager = StaggeredGridLayoutManager(1, 1)
        rvAccountsGroups.layoutManager = mLayoutManager

        adapter = GroupAdapter(requireContext(), data)
        dataFiltered.addAll(data)

        rvAccountsGroups.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}