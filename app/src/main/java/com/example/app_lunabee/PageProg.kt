package com.example.app_lunabee

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class YourApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseManager.initialize(this)
    }
}

class PageProg : AppCompatActivity() {
    private lateinit var titlePage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_app)

        DatabaseManager.initialize(this)
        titlePage = findViewById(R.id.title_page)
        titlePage.text = "Programme"
        createButtonsFromDatabase()
    }

    override fun onDestroy() {
        DatabaseManager.closeDatabase()
        super.onDestroy()
    }

    private fun createButtonsFromDatabase() {
        val seanceList = DatabaseManager.dbHelper.getAllSeance()
        val linearLayout = findViewById<LinearLayout>(R.id.Linear_seance)

        linearLayout.removeAllViews()

        seanceList.forEach { seance ->
            val imagePath = DatabaseManager.dbHelper.getImageResourceForSeance(seance.nomSeance)
            val seanceLinearLayout = LinearLayout(this)

            if (imagePath != null) {
                seanceLinearLayout.setBackgroundResource(imagePath)
            } else {
                seanceLinearLayout.setBackgroundColor(Color.GRAY)
            }
            seanceLinearLayout.orientation = LinearLayout.VERTICAL
            val layoutParams = LinearLayout.LayoutParams(800, 400)
            layoutParams.gravity = Gravity.CENTER
            layoutParams.bottomMargin = 150
            seanceLinearLayout.layoutParams = layoutParams

            val button = Button(this)
            button.text = seance.nomSeance
            button.tag = seance

            val params = LinearLayout.LayoutParams(800, 400)
            params.bottomMargin = 100

            val shape = GradientDrawable()
            shape.setColor(Color.argb((0.5 * 255).toInt(), 217, 217, 217))

            button.layoutParams = params
            button.background = shape
            button.textSize = 30F
            button.gravity = Gravity.NO_GRAVITY
            button.setPadding(40, 250, 0, 0)
            button.setTypeface(null, Typeface.BOLD)
            button.setTextColor(Color.WHITE)

            button.setOnClickListener {
                val intent = Intent(this, PageExo::class.java)
                intent.putExtra("seanceNom", seance.nomSeance)
                startActivity(intent)
            }
            seanceLinearLayout.addView(button)
            linearLayout.addView(seanceLinearLayout)
        }
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