package com.miguelrodriguez19.mindmaster

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
import com.miguelrodriguez19.mindmaster.model.firebase.FManagerFacade
import com.miguelrodriguez19.mindmaster.model.firebase.FManagerFacade.getAuth
import com.miguelrodriguez19.mindmaster.model.firebase.FManagerFacade.getCurrentUser
import com.miguelrodriguez19.mindmaster.model.structures.dto.UserResponse
import com.miguelrodriguez19.mindmaster.model.utils.AESEncripter
import com.miguelrodriguez19.mindmaster.model.utils.Preferences
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.showToast

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private var lastBackPressedTime: Long = 0L
    private val EXIT_TIME_GAP: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setupBinding()
        splashScreen.setKeepOnScreenCondition { false }

        initContexts()

        setAppTheme(Preferences.getTheme().toInt())
        setupNavigation()
        configureBackButton()
        setupActionBar()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.findNavController()
        drawerLayout = binding.drawerLayout

        val user = Preferences.getUser()

        val firebaseUser = getCurrentUser()

        val initialFragment: Int =
            if (user != null && firebaseUser != null && Preferences.getSecurePhrase() != null) {
                loadUserData(user)
                R.id.calendarFragment
            } else {
                R.id.logInFragment
            }

        navController.navigate(initialFragment)
    }

    private fun configureBackButton() {
        val onBackPressed = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                val exitFragments = arrayOf(
                    R.id.logInFragment, R.id.securityPhraseLoaderFragment,
                    R.id.welcomeFragment, R.id.calendarFragment
                )

                if (exitFragments.contains(navController.currentDestination?.id)) {
                    if (System.currentTimeMillis() - lastBackPressedTime > EXIT_TIME_GAP) {
                        showToast(this@MainActivity, R.string.tap_again_to_exit)
                        lastBackPressedTime = System.currentTimeMillis()
                    } else {
                        this.isEnabled = false
                        finish()
                    }
                } else {
                    navController.popBackStack()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressed)
    }

    private fun setupBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initContexts() {
        Preferences.init(applicationContext)
        AESEncripter.init(applicationContext)
        FManagerFacade.init(applicationContext)
    }

    private fun setAppTheme(theme: Int) {
        when (theme) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // Tema claro
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) // Tema oscuro
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) // Por defecto, sigue el tema del sistema
        }
    }

    private fun loadUserData(user: UserResponse) {
        val navHeaderBinding = DrawerHeaderBinding.bind(binding.navView.getHeaderView(0))
        navHeaderBinding.tvName.text = user.firstName

        Glide.with(this)
            .load(user.photoUrl)
            .into(navHeaderBinding.civDrawerUserPhoto)
    }

    private fun setupActionBar() {
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.calendarFragment, R.id.diaryFragment, R.id.expensesFragment,
                R.id.passwordsFragment, R.id.settingsFragment, R.id.helpFragment
            ),
            drawerLayout
        )

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.navView.setupWithNavController(navController)
    }

    fun setUpUser(user: UserResponse) {
        Preferences.setUser(user)
        loadUserData(user)
    }

    fun updateSecurityPreferences(newSecurePhrase: String, newInitializationVector: String) {
        Preferences.setSecurePhrase(newSecurePhrase)
        Preferences.setInitializationVector(newInitializationVector)
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

    fun logOut() {
        getAuth().signOut()
        Preferences.clearUserPreferences()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

}



