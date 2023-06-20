package com.miguelrodriguez19.mindmaster.views.passwords

import android.annotation.SuppressLint
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
import com.miguelrodriguez19.mindmaster.models.firebase.FirebaseManager
import com.miguelrodriguez19.mindmaster.views.passwords.adapters.GroupAdapter

class PasswordsFragment : Fragment() {
    private var _binding: FragmentPasswordsBinding? = null
    private val binding get() = _binding!!
    private val data: ArrayList<GroupPasswordsResponse> = ArrayList()
    private lateinit var adapter: GroupAdapter

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

        binding.btnAddGroup.setOnClickListener {
            showPasswordsBS(requireContext(), null) {
                adapter.addItem(it)
            }
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
        data.clear()
        FirebaseManager.loadAllGroups() { accountsGroupsList ->
            data.addAll(accountsGroupsList)
            adapter.setData(accountsGroupsList)
            binding.progressBarAllAccounts.visibility = View.GONE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun search(text: String) {
        val filteredData = ArrayList<GroupPasswordsResponse>()
        for (group in data) {
            val filteredAccount = ArrayList<GroupPasswordsResponse.Account>()
            if (group.name.contains(text, true)) {
                filteredAccount.addAll(group.accountsList)
            } else {
                for (account in group.accountsList) {
                    if (account.name.contains(text, true)) {
                        filteredAccount.add(account)
                    }
                }
            }
            if (filteredAccount.isNotEmpty()) {
                filteredData.add(group.copy(accountsList = filteredAccount))
            }
        }
        adapter.data = filteredData
        adapter.notifyDataSetChanged()
    }

    private fun initWidget() {
        binding.rvAccountsGroups.layoutManager = StaggeredGridLayoutManager(1, 1)
        adapter = GroupAdapter(requireContext(), data)
        binding.rvAccountsGroups.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
