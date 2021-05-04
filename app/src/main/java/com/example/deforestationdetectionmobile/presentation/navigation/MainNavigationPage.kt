package com.example.deforestationdetectionmobile.presentation.navigation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.deforestationdetectionmobile.R
import com.example.deforestationdetectionmobile.presentation.iot.ActiveIotsList
import com.example.deforestationdetectionmobile.presentation.iot.AllIotsList

class MainNavigationPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_navigation_page)
    }

    fun navigateToActiveIots(view: View) {
        val intent = Intent(this, ActiveIotsList::class.java)
        startActivity(intent)
    }

    fun navigateToAllIots(view: View) {
        val intent = Intent(this, AllIotsList::class.java)
        startActivity(intent)
    }
}