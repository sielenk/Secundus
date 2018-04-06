package de.masitec.secundus

import android.content.res.Resources
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import de.masitec.secundus.fragments.CameraFragment
import de.masitec.secundus.fragments.SmellButtonFragment

class SecundusPageAdapter(fm: FragmentManager, private val resources: Resources)
    : FragmentStatePagerAdapter(fm) {

    private companion object {
        val fragments = listOf(
                Pair(R.string.smell_button_tab, { SmellButtonFragment() }),
                Pair(R.string.camera_tab, { CameraFragment() })
        )
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position].second()
    }

    override fun getPageTitle(position: Int): CharSequence {
        return resources.getText(fragments[position].first)
    }

    override fun getCount(): Int {
        return fragments.count()
    }
}
