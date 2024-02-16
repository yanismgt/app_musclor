package com.example.app_lunabee

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PageSetting : AppCompatActivity() {
    private lateinit var titlePage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        DatabaseManager.initialize(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_setting)

        titlePage = findViewById(R.id.title_page)
        titlePage.text = "Paramètre"
    }

    fun switchNav(view: View) {
        when (view.id) {
            R.id.button_add -> {
                val intent = Intent(this, PageAdd::class.java)
                startActivity(intent)
            }
            R.id.button_seance -> {
                val intent = Intent(this, PageProg::class.java)
                startActivity(intent)
            }
            R.id.button_param -> {
                val intent = Intent(this, PageSetting::class.java)
                startActivity(intent)
            }
        }
    }
}