package com.miguelrodriguez19.mindmaster.view.fragments.passwordVault

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
import com.miguelrodriguez19.mindmaster.model.structures.dto.accountVault.PasswordGroupResponse
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade
import com.miguelrodriguez19.mindmaster.model.structures.dto.accountVault.Account
import com.miguelrodriguez19.mindmaster.view.adapters.passwordVault.GroupAdapter
import com.miguelrodriguez19.mindmaster.view.bottomSheets.CustomBottomSheet
import com.miguelrodriguez19.mindmaster.view.bottomSheets.PasswordGroupBS

class PasswordsFragment : Fragment() {
    private var _binding: FragmentPasswordsBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchView: SearchView
    private lateinit var btnAddGroup: ExtendedFloatingActionButton
    private lateinit var rvAccountsGroups: RecyclerView
    private lateinit var adapter: GroupAdapter
    private lateinit var progressBarAllAccounts: ProgressBar
    private val data: ArrayList<PasswordGroupResponse> = ArrayList()

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
            val passwordsBS = CustomBottomSheet.get<PasswordGroupResponse>(PasswordGroupBS::class.java.name)

            passwordsBS?.showViewDetailBS(requireContext(), null) {
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
        progressBarAllAccounts.visibility = View.VISIBLE
        data.clear()
        FirestoreManagerFacade.loadAllGroups() { accountsGroupsList ->
            data.addAll(accountsGroupsList)
            adapter.setData(accountsGroupsList)
            progressBarAllAccounts.visibility = View.GONE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun search(text: String) {
        val filteredData = ArrayList<PasswordGroupResponse>()
        for (group in data) {
            val filteredAccount = ArrayList<Account>()
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
