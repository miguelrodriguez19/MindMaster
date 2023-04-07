package com.miguelrodriguez19.mindmaster.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.button.MaterialButton
import com.miguelrodriguez19.mindmaster.databinding.FragmentHelpBinding
import com.miguelrodriguez19.mindmaster.models.FAQ
import com.miguelrodriguez19.mindmaster.models.GroupPasswordsResponse

class HelpFragment : Fragment() {
    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchView: SearchView
    private lateinit var btnGoToWebsite: MaterialButton
    private lateinit var btnEmailUs: MaterialButton
    private lateinit var btnTerms: MaterialButton
    private lateinit var rvFAQs: RecyclerView
    private lateinit var adapter: FAQsAdapter
    var data: ArrayList<FAQ> = ArrayList()
    private var dataFiltered: ArrayList<FAQ> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
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
        createFakeData()
        searchView = binding.searchView
        rvFAQs = binding.rvFaqs
        btnGoToWebsite = binding.btnGoToWebsite
        btnEmailUs = binding.btnEmailUs
        btnTerms = binding.btnTermsAndConditions

        val mLayoutManager = StaggeredGridLayoutManager(1, 1)
        rvFAQs.layoutManager = mLayoutManager

        adapter = FAQsAdapter(data) {

        }
        dataFiltered.addAll(data)

        rvFAQs.adapter = adapter
    }

    private fun createFakeData() {
        val faqs = arrayListOf(
            FAQ(
                "1",
                "What is the cost of the product?",
                "The cost of the product varies depending on the size and color you choose."
            ),
            FAQ(
                "2",
                "What is your return policy?",
                "We accept returns within 30 days of purchase as long as the item is in its original condition."
            ),
            FAQ(
                "3",
                "How long does shipping take?",
                "Shipping usually takes 3-5 business days within the United States. International shipping times may vary."
            ),
            FAQ(
                "4",
                "Do you offer gift wrapping?",
                "Yes, we offer gift wrapping for an additional fee of $5 per item."
            ),
            FAQ(
                "5",
                "What forms of payment do you accept?",
                "We accept all major credit cards, PayPal, and Apple Pay."
            )
        )
        data = faqs
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
