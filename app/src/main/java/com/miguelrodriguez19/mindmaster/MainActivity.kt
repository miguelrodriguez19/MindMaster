package com.miguelrodriguez19.mindmaster

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.miguelrodriguez19.mindmaster.databinding.ActivityMainBinding
import com.miguelrodriguez19.mindmaster.databinding.DrawerHeaderBinding
import com.miguelrodriguez19.mindmaster.models.structures.UserResponse
import com.miguelrodriguez19.mindmaster.models.utils.AESEncripter
import com.miguelrodriguez19.mindmaster.models.utils.Preferences

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        screenSplash.setKeepOnScreenCondition {
            AESEncripter.init(applicationContext)
            false
        }
        drawerLayout = binding.drawerLayout

        val user = Preferences.getUser()
        val initialFragment:Int = if (user != null) {
            userSetUp(user)
            R.id.calendarFragment
        }else {
            R.id.logInFragment
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        navController.navigate(initialFragment)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.calendarFragment,
                R.id.diaryFragment,
                R.id.expensesFragment,
                R.id.passwordsFragment,
                R.id.settingsFragment,
                R.id.helpFragment
            ),
            drawerLayout
        )

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.navView.setupWithNavController(navController)
    }

    fun userSetUp(user: UserResponse) {
        val navHeaderBinding = DrawerHeaderBinding.bind(binding.navView.getHeaderView(0))
        Glide.with(this)
            .load(user.photoUrl)
            .into(navHeaderBinding.civDrawerUserPhoto)

        navHeaderBinding.tvName.text = user.firstName
    }

    fun lockDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    fun unlockDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}