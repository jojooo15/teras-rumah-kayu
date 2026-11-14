package com.jojo.terasrumah

import android.app.Application
import com.jojo.terasrumah.data.repository.AuthRepository

class TerasRumahApplication : Application() {
    
    // Lazy initialization of repositories
    val authRepository: AuthRepository by lazy {
        AuthRepository(applicationContext)
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        @Volatile
        private var instance: TerasRumahApplication? = null
        
        fun getInstance(): TerasRumahApplication {
            return instance ?: synchronized(this) {
                instance ?: throw IllegalStateException("Application not initialized")
            }
        }
    }
}
