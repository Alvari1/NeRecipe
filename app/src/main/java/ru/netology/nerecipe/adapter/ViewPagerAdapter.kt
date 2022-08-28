package ru.netology.nerecipe.adapter

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.netology.nerecipe.dto.FragmentList


class ViewPagerAdapter(
    list: List<FragmentList>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragmentList = list

    override fun getItemId(position: Int) = fragmentList[position].fragmentId.toLong()

    override fun containsItem(itemId: Long): Boolean {
        return !fragmentList.none { it.fragmentId.toLong() == itemId }
    }

    override fun getItemCount() = fragmentList.size
    override fun createFragment(position: Int) =
        fragmentList[position].fragment
}