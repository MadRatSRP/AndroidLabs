package com.androidlabs.activity;

import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.androidlabs.R;
import com.androidlabs.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    NavController navController;
    Toolbar toolbar;

    ActivityMainBinding binding;

    static Integer appPrecision;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding initialization
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        drawerLayout = binding.drawerLayout;
        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // NavigationView initialization
        navigationView = binding.navigationView;

        Menu menu = navigationView.getMenu();

        // Добавляем айтемы в список и перерисовываем NavigationView
        menu.add(0, R.id.figures, 0,
                getApplicationContext().getString(R.string.figuresTitle));
        menu.add(0, R.id.history, 0,
                getApplicationContext().getString(R.string.historyTitle));
        navigationView.invalidate();

        navController = Navigation.findNavController(this, R.id.navHostFragment);

        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout);

        binding.showSettings.setOnClickListener(view -> {
            navController.navigate(R.id.showSettings);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
