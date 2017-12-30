package de.masitec.secundus

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Created by Marv on 30.12.17.
 */
class SecundusPageAdapter(fm: FragmentManager, val fragments: List<() -> Fragment>) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return fragments[position]()
    }

    override fun getCount(): Int {
        return fragments.count()
    }
}
