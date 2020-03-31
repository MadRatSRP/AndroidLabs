package com.androidlabs.activity

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.androidlabs.R
import com.androidlabs.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        var appPrecision: Int? = null
    }

    // ViewBinding variable
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding initialization
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar initialization
        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                binding.toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // NavigationView initialization
        val menu = binding.navigationView.menu

        // Добавляем айтемы в список и перерисовываем NavigationView
        menu.add(0, R.id.figures, 0,
                applicationContext.getString(R.string.figuresTitle))
        menu.add(0, R.id.history, 0,
                applicationContext.getString(R.string.historyTitle))
        binding.navigationView.invalidate()

        val navController = Navigation.findNavController(this, R.id.navHostFragment)
        NavigationUI.setupWithNavController(binding.navigationView, navController)
        NavigationUI.setupActionBarWithNavController(this, navController, binding.drawerLayout)

        // ShowSettings button clickListener
        binding.showSettings.setOnClickListener { navController.navigate(R.id.showSettings) }
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

    fun setNewToolbarTitle(@StringRes titleId: Int) {
        binding.toolbar.setTitle(titleId)
    }
}