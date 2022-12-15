package com.example.readtrack

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.readtrack.dialogs.AddBookCoverDialog
import com.example.readtrack.util.ImageUtils
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    init {
        instance = this
    }

    companion object {
        private var instance: MainActivity? = null

        // To provide a static way of getting context (e.g. converters)
        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Grab the container fragment as NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.containerFragment) as NavHostFragment
        // Initialize navController in this activity as navController in the fragment
        this.navController = navHostFragment.navController

        // Tell the bottom navigation bar to use the nav controller
        findViewById<BottomNavigationView>(R.id.bottom_nav).setupWithNavController(this.navController)
    }
}