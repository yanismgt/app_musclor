package com.example.app_lunabee

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding

class PageExo : AppCompatActivity() {
    private lateinit var titleImagePage: LinearLayout
    private lateinit var titleSeancePage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        DatabaseManager.initialize(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.file_exo)

        val seanceNom = intent.getStringExtra("seanceNom") ?: "ValeurParDefaut"
        handlingTitle(seanceNom)
        listExo(seanceNom)
    }

    private fun handlingTitle(nomSeance :String) {
        titleImagePage = findViewById(R.id.image_seance_page)
        titleSeancePage = findViewById(R.id.title_seance_page)
        val imagePath = DatabaseManager.dbHelper.getImageResourceForSeance(nomSeance)

        if (DatabaseManager.dbHelper.isImageDefinedForSeance(nomSeance)) {
            titleImagePage.setBackgroundResource(imagePath)
        } else {
            titleImagePage.setBackgroundColor(Color.LTGRAY)
        }
        val shape = GradientDrawable()
        shape.setColor(Color.argb((0.5 * 255).toInt(), 150, 150, 150))
        shape.cornerRadius = 30f
        titleSeancePage.setPadding(15.toPx())
        titleSeancePage.text = nomSeance
        titleSeancePage.background = shape
    }

    private fun listExo(nomSeance: String) {
        val linearLayoutExo = findViewById<LinearLayout>(R.id.list_exo_layout)
        val exercices = DatabaseManager.dbHelper.getRepetAndExo(nomSeance)

        linearLayoutExo.removeAllViews()
        linearLayoutExo.orientation = LinearLayout.VERTICAL

        if (exercices.isNotEmpty()) {
            for (exercise in exercices) {
                val exerciseLayout = LinearLayout(this)
                val paramsExerciseLayout = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsExerciseLayout.bottomMargin = 40
                exerciseLayout.layoutParams = paramsExerciseLayout
                exerciseLayout.orientation = LinearLayout.HORIZONTAL

                val shape = GradientDrawable()
                shape.setColor(ContextCompat.getColor(this, R.color.back_choice))
                shape.setStroke(2, ContextCompat.getColor(this, R.color.back_choice))
                shape.cornerRadius = 10f

                val textView = TextView(this)
                val paramsTextView = LinearLayout.LayoutParams(
                    200.toPx(),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textView.setPadding(5.toPx())
                textView.text = exercise.name
                textView.textSize = 20f
                textView.layoutParams = paramsTextView

                val layout_Rep = LinearLayout(this)
                val paramsLayoutRep = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    50.toPx()
                )
                paramsLayoutRep.gravity = Gravity.CENTER_VERTICAL
                paramsLayoutRep.weight = 1f
                layout_Rep.layoutParams = paramsLayoutRep

                val textRep = TextView(this)
                val paramsTextRep = LinearLayout.LayoutParams(
                    50.toPx(),
                    50.toPx()
                )
                paramsTextRep.leftMargin = 220
                textRep.setPadding(15.toPx(), 10.toPx(), 0, 0 )
                textRep.background = shape
                textRep.textSize = 20f
                textRep.setTextColor(Color.WHITE)
                textRep.text = "x${exercise.repetitions}"
                textRep.layoutParams = paramsTextRep

                exerciseLayout.addView(textView)
                layout_Rep.addView(textRep)
                linearLayoutExo.addView(exerciseLayout)
                exerciseLayout.addView(layout_Rep)
            }
        } else {
            val noExoTextView = TextView(this)
            noExoTextView.text = "Aucun exercice pour cette séance."
            noExoTextView.textSize = 20f
            noExoTextView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            linearLayoutExo.addView(noExoTextView)
        }
    }
    fun Int.toPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale).toInt()
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