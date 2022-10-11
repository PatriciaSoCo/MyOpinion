package com.patriciasoco.myopinion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentContainerView
import com.patriciasoco.myopinion.fragment.SearchFragment
import com.patriciasoco.myopinion.fragment.HomeFragment
import com.patriciasoco.myopinion.fragment.LoginFragment
import com.patriciasoco.myopinion.fragment.MyProfileFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val fragmentContainerView = findViewById<FragmentContainerView>(R.id.fragment_container)
        val navigationMenu = findViewById<NavigationView>(R.id.navigation_menu)

        this.toggle = ActionBarDrawerToggle( this, drawerLayout,
            R.string.open_menu,
            R.string.close_menu
        )
        drawerLayout.addDrawerListener(toggle)
        toggle. syncState()

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, HomeFragment())
            commit()
        }

        navigationMenu.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.item_home -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.fragment_container, HomeFragment())
                        commit()
                    }
                    drawerLayout.closeDrawers()
                }
                R.id.item_login -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.fragment_container, LoginFragment())
                        commit()
                    }
                    drawerLayout.closeDrawers()
                }
                R.id.my_profile_item -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.fragment_container, MyProfileFragment())
                        commit()
                    }
                    drawerLayout.closeDrawers()
                }
                R.id.item_search -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.fragment_container, SearchFragment())
                        commit()
                    }
                    drawerLayout.closeDrawers()
                }
            }
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}