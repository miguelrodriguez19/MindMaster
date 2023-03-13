package com.miguelrodriguez19.mindmaster.expenses

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentExpensesBinding

class ExpensesFragment : Fragment() {
    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    fun init() {
        val adapter = TabsFragmentAdapter(
            parentFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        adapter.addItem(PieChartFragment(), resources.getString(R.string.monthly))
        adapter.addItem(BarsChartFragment(), resources.getString(R.string.annual))

        val viewPager = binding.viewPagerGraphs
        viewPager.adapter = adapter

        val tabsGraphLayout = binding.tlTabsGraph
        tabsGraphLayout.setupWithViewPager(viewPager)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class TabsFragmentAdapter(fm: FragmentManager, behavior: Int) :
        FragmentPagerAdapter(fm, behavior) {

        private val listFragment: MutableList<Fragment> = ArrayList()
        private val tittleList: MutableList<String> = ArrayList()

        fun addItem(fragment: Fragment, title: String) {
            listFragment.add(fragment)
            tittleList.add(title)
        }

        override fun getCount(): Int {
            return listFragment.size
        }

        override fun getItem(position: Int): Fragment {
            return listFragment[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return tittleList[position]
        }
    }
}