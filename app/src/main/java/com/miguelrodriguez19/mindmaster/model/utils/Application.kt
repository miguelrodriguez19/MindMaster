package com.miguelrodriguez19.mindmaster.model.utils

import android.app.Application

class MainApplication : Application() {
   init {
       instance = this
   }

   companion object {
       lateinit var instance: Application
   }

}