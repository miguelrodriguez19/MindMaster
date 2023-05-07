package com.miguelrodriguez19.mindmaster.passwords

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
import com.miguelrodriguez19.mindmaster.expenses.AllMovementsAdapter
import com.miguelrodriguez19.mindmaster.models.GroupPasswordsResponse
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse
import com.miguelrodriguez19.mindmaster.utils.AllBottomSheets.Companion.showPasswordsBS

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
            showPasswordsBS(requireContext(), null)
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
        createFakeData()
        searchView = binding.searchView
        btnAddGroup = binding.btnAddGroup
        rvAccountsGroups = binding.rvAccountsGroups

        val mLayoutManager = StaggeredGridLayoutManager(1, 1)
        rvAccountsGroups.layoutManager = mLayoutManager

        adapter = GroupAdapter(requireContext(), data)
        dataFiltered.addAll(data)

        rvAccountsGroups.adapter = adapter
    }

    private fun createFakeData(){
        val group1 = GroupPasswordsResponse(
            codGroup = 1,
            name = "Netflix",
            accountsList = listOf(
                GroupPasswordsResponse.Account(
                    codAccount = 1,
                    name = "Account",
                    username = "user1",
                    email = "user1@gmail.com",
                    password = "password1",
                    description = "",
                    note = null,
                    type = GroupPasswordsResponse.Type.GOOGLE
                ),
                GroupPasswordsResponse.Account(
                    codAccount = 2,
                    name = "Account",
                    username = "miguel",
                    email = "user2@yahoo.com",
                    password = "password2",
                    description = "",
                    note = "Note for account 2",
                    type = GroupPasswordsResponse.Type.OTHER
                )
            )
        )

        val group2 = GroupPasswordsResponse(
            codGroup = 2,
            name = "Amazon",
            accountsList = listOf(
                GroupPasswordsResponse.Account(
                    codAccount = 1,
                    name = "Account",
                    username = "user3",
                    email = "example@gmail.com",
                    password = "password3",
                    description = null,
                    note = null,
                    type = GroupPasswordsResponse.Type.OTHER
                )
            )
        )

        val group3 = GroupPasswordsResponse(
            codGroup = 3,
            name = "Instagram",
            accountsList = listOf(
                GroupPasswordsResponse.Account(
                    codAccount = 1,
                    name = "Account",
                    username = "user4",
                    email = "user4@hotmail.com",
                    description = null,
                    password = "password4",
                    note = null,
                    type = GroupPasswordsResponse.Type.OTHER
                ),
                GroupPasswordsResponse.Account(
                    codAccount = 2,
                    username = "user5",
                    name = "Account",
                    email = "user5@gmail.com",
                    password = "password5",
                    description = null,
                    note = "Note for account 5",
                    type = GroupPasswordsResponse.Type.EMAIL
                ),
                GroupPasswordsResponse.Account(
                    codAccount = 3,
                    username = "user6",
                    name = "Account",
                    email = "user6@yahoo.com",
                    password = "password6",
                    description = null,
                    note = null,
                    type = GroupPasswordsResponse.Type.GOOGLE
                )
            )
        )

        data = arrayListOf(group1, group2, group3)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}