package com.miguelrodriguez19.mindmaster.models.utils

import android.app.Application

class MainApplication : Application() {
   init {
       instance = this
   }

   companion object {
       lateinit var instance: Application
   }
}
