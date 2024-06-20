package com.example.devicemonitorapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.findNavController

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        setupActionBarWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment -> hideToolbar()
                R.id.updateSettingsFragment -> {
                    showToolbar()
                    supportActionBar?.title = getString(R.string.about_app)
                }
                R.id.faqsFragment -> {
                    showToolbar()
                    supportActionBar?.title = getString(R.string.faqs)
                }
                R.id.languageSelection -> {
                    showToolbar()
                    supportActionBar?.title = getString(R.string.language_selection)
                }
                R.id.homeFragment -> {
                    showToolbar()
                    supportActionBar?.title = getString(R.string.device_dashboard)
                }
                R.id.cpuGpuFragment -> {
                    showToolbar()
                    supportActionBar?.title = getString(R.string.cpu_monitoring)
                }
                R.id.GpuFragment -> {
                    showToolbar()
                    supportActionBar?.title = getString(R.string.gpu_monitoring)
                }
                R.id.batteryFragment -> {
                    showToolbar()
                    supportActionBar?.title = getString(R.string.battery_management)
                }
                R.id.performanceBoosterFragment -> {
                    showToolbar()
                    supportActionBar?.title = getString(R.string.performance_booster)
                }
                R.id.refreshRateModsFragment -> {
                    showToolbar()
                    supportActionBar?.title = getString(R.string.refresh_rate_mods)
                }
                R.id.memoryFragment -> {
                    showToolbar()
                    supportActionBar?.title = getString(R.string.memory_management)
                }
                R.id.storageFragment -> {
                    showToolbar()
                    supportActionBar?.title = getString(R.string.storage_management)
                }
                R.id.appManagementFragment -> {
                    showToolbar()
                    supportActionBar?.title = getString(R.string.app_management)
                }
                R.id.networkFragment -> {
                    showToolbar()
                    supportActionBar?.title = getString(R.string.network_usage_stats)
                }
                R.id.taskAutomationFragment -> {
                    showToolbar()
                    supportActionBar?.title = getString(R.string.task_automation)
                }
                else -> showToolbar()
            }
        }
    }

    fun hideToolbar() {
        findViewById<soup.neumorphism.NeumorphCardView>(R.id.toolbar_card).visibility = View.GONE
    }

    fun showToolbar() {
        findViewById<soup.neumorphism.NeumorphCardView>(R.id.toolbar_card).visibility = View.VISIBLE
    }

    override fun onSupportNavigateUp() =
        findNavController(R.id.nav_host_fragment).navigateUp()
}
