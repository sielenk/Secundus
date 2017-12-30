package de.masitec.secundus

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import de.masitec.secundus.fragments.CameraFragment
import de.masitec.secundus.fragments.SmellButtonFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)!!
        setSupportActionBar(toolbar)

        val fragments = listOf(
                Pair(R.string.smell_button_tab, { SmellButtonFragment() }),
                Pair(R.string.camera_tab, { CameraFragment() })
        )

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        fragments.forEach {
            tabLayout.addTab(tabLayout.newTab().setText(it.first))
        }
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        val viewPager = findViewById<ViewPager>(R.id.pager)
        val adapter = SecundusPageAdapter(supportFragmentManager, fragments.map { it.second })
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) { }

            override fun onTabUnselected(tab: TabLayout.Tab) { }

            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.setCurrentItem(tab.position);
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId

        /*
        when (id) {
          R.id.action_settings ->
                  return true
        }
        */

        return super.onOptionsItemSelected(item)
    }
}
