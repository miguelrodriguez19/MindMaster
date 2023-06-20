package com.miguelrodriguez19.mindmaster.views.expenses.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.miguelrodriguez19.mindmaster.views.expenses.fragments.BarsChartFragment
import com.miguelrodriguez19.mindmaster.views.expenses.fragments.PieChartFragment

class ViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<String>()

    override fun getCount() = mFragmentList.size
    override fun getItem(position: Int) = mFragmentList[position]
    override fun getPageTitle(position: Int) = mFragmentTitleList[position]
    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }
}
