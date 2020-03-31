package com.androidlabs.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.androidlabs.R
import com.androidlabs.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    var navigationView: NavigationView? = null
    var drawerLayout: DrawerLayout? = null
    var navController: NavController? = null
    var toolbar: Toolbar? = null
    var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding initialization
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        drawerLayout = binding.drawerLayout
        toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout!!.addDrawerListener(toggle)
        toggle.syncState()

        // NavigationView initialization
        navigationView = binding.navigationView
        val menu = navigationView!!.menu

        // Добавляем айтемы в список и перерисовываем NavigationView
        menu.add(0, R.id.figures, 0,
                applicationContext.getString(R.string.figuresTitle))
        menu.add(0, R.id.history, 0,
                applicationContext.getString(R.string.historyTitle))
        navigationView!!.invalidate()
        navController = Navigation.findNavController(this, R.id.navHostFragment)
        NavigationUI.setupWithNavController(navigationView!!, navController!!)
        NavigationUI.setupActionBarWithNavController(this, navController!!, drawerLayout)
        binding.showSettings.setOnClickListener { view: View? -> navController!!.navigate(R.id.showSettings) }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        var appPrecision: Int? = null
    }
}