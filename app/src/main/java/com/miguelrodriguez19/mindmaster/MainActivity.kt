package com.miguelrodriguez19.mindmaster

import android.content.Intent
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
import com.miguelrodriguez19.mindmaster.models.utils.FirebaseManager.getAuth
import com.miguelrodriguez19.mindmaster.models.utils.Preferences
import com.miguelrodriguez19.mindmaster.models.utils.Preferences.getToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        drawerLayout = binding.drawerLayout

        val user = Preferences.getUser()
        val curToken = getToken() ?: "current"
        val supToken = runBlocking {
            withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                getAuth().currentUser?.getIdToken(false)?.await()?.token ?: "supposed"
            }
        }

        val initialFragment: Int = if (user != null && curToken == supToken && Preferences.getSecurePhrase() != null) {
            loadUserData(user)
            R.id.calendarFragment
        } else {
            R.id.logInFragment
        }

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

    private fun loadUserData(user: UserResponse) {
        val navHeaderBinding = DrawerHeaderBinding.bind(binding.navView.getHeaderView(0))
        navHeaderBinding.tvName.text = user.firstName

        Glide.with(this)
            .load(user.photoUrl)
            .into(navHeaderBinding.civDrawerUserPhoto)
    }

    fun setUpUser(user: UserResponse, token: String) {
        Preferences.setUser(user)
        Preferences.setToken(token)

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
        Preferences.clearAll()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

}