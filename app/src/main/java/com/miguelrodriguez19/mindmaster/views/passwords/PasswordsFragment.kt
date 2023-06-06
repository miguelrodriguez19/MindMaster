package com.miguelrodriguez19.mindmaster.views.passwords

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.miguelrodriguez19.mindmaster.databinding.FragmentPasswordsBinding
import com.miguelrodriguez19.mindmaster.models.structures.GroupPasswordsResponse
import com.miguelrodriguez19.mindmaster.models.utils.AllBottomSheets.Companion.showPasswordsBS
import com.miguelrodriguez19.mindmaster.models.utils.FirebaseManager
import com.miguelrodriguez19.mindmaster.views.passwords.adapters.GroupAdapter

class PasswordsFragment : Fragment() {
    private var _binding: FragmentPasswordsBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchView: SearchView
    private lateinit var btnAddGroup: ExtendedFloatingActionButton
    private lateinit var rvAccountsGroups: RecyclerView
    private lateinit var adapter: GroupAdapter
    private lateinit var progressBarAllAccounts: ProgressBar
    private var data: ArrayList<GroupPasswordsResponse> = ArrayList()
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
        setUpData()

        btnAddGroup.setOnClickListener {
            showPasswordsBS(requireContext(), null) {
                adapter.addItem(it)
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

    private fun setUpData() {
        binding.progressBarAllAccounts.visibility = View.VISIBLE
        this@PasswordsFragment.data.clear()
        FirebaseManager.loadAllGroups(requireContext()) { accountsGroupsList ->
            this@PasswordsFragment.data.addAll(accountsGroupsList)
            dataFiltered = data
            adapter.setData(accountsGroupsList)
            binding.progressBarAllAccounts.visibility = View.GONE
        }
    }

    private fun search(text: String) {
        val filteredData = ArrayList<GroupPasswordsResponse>()
        for (item in data) {
            val filteredAccount = ArrayList<GroupPasswordsResponse.Account>()
            for (account in item.accountsList) {
                if (account.name.contains(text, true) || item.name.contains(text, true)) {
                    filteredAccount.add(account)
                }
            }
            if (filteredAccount.isNotEmpty()) {
                filteredData.add(item.copy(accountsList = filteredAccount))
            }
        }
        adapter.data = filteredData
        adapter.notifyDataSetChanged()
    }

    private fun initWidget() {
        searchView = binding.searchView
        btnAddGroup = binding.btnAddGroup
        rvAccountsGroups = binding.rvAccountsGroups
        progressBarAllAccounts = binding.progressBarAllAccounts

        rvAccountsGroups.layoutManager = StaggeredGridLayoutManager(1, 1)
        adapter = GroupAdapter(requireContext(), data)
        rvAccountsGroups.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}