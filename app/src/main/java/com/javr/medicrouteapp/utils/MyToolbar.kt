package com.javr.medicrouteapp.utils

import androidx.appcompat.app.AppCompatActivity
import com.javr.medicrouteapp.R

class MyToolbar {
    fun showToolbar(activities : AppCompatActivity, titulo: String, isBotonVolver: Boolean){
        activities.setSupportActionBar(activities.findViewById(R.id.toolbar))
        activities.supportActionBar?.title = titulo
        activities.supportActionBar?.setDisplayHomeAsUpEnabled(isBotonVolver)
    }
}