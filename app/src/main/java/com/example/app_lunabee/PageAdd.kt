package com.example.app_lunabee

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class PageAdd : AppCompatActivity() {
    private lateinit var titlePage: TextView
    private lateinit var zoneTextSeance: AutoCompleteTextView
    private lateinit var buttonAddExo: Button
    private lateinit var buttonSupSeance: Button
    private lateinit var textViewExo: TextView
    private lateinit var spinnerListExo: Spinner
    private lateinit var spinnerListRep: Spinner
    private lateinit var linearExo: LinearLayout
    private lateinit var listeExo: Array<String>
    private lateinit var listeRep: Array<String>
    private lateinit var exercicesAdapter: ArrayAdapter<String>
    private lateinit var repAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        DatabaseManager.initialize(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_add)

        titlePage = findViewById(R.id.title_page)

        titlePage.text = "Nouveau programme"

        //gestion de la list des exos
        listeExo = resources.getStringArray(R.array.list_exercice)
        listeRep = resources.getStringArray(R.array.list_repet)
        exercicesAdapter = ArrayAdapter(this, R.layout.style_spinner_add, listeExo)
        repAdapter = ArrayAdapter(this, R.layout.style_spinner_add, listeRep)
        spinnerListExo = findViewById(R.id.list_exo)
        spinnerListRep = findViewById(R.id.list_rep)
        spinnerListExo.adapter = exercicesAdapter
        spinnerListRep.adapter = repAdapter

        zoneTextSeance = findViewById(R.id.name_seance)
        buttonAddExo = findViewById(R.id.buttonAddExo)
        buttonSupSeance = findViewById(R.id.buttonSupSeance)
        textViewExo = findViewById(R.id.all_exo)

        exercicesAdapter.setDropDownViewResource(R.layout.style_spinner_drop)
        spinnerListExo.adapter = exercicesAdapter

        repAdapter.setDropDownViewResource(R.layout.style_spinner_drop)
        spinnerListRep.adapter = repAdapter

        val seanceNames = DatabaseManager.dbHelper.getAllSeanceNames()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, seanceNames)
        zoneTextSeance.setAdapter(adapter)
        zoneTextSeance.threshold = 1
        buttonSupSeance.setOnClickListener {
            val nomSeance = zoneTextSeance.text.toString().trim()
            if (nomSeance.isNotEmpty()) {
                supSeance(nomSeance)
                zoneTextSeance.text.clear()
            }else {
                Toast.makeText(this, "Séance vide", Toast.LENGTH_SHORT).show()
            }
        }
        linearExo = findViewById(R.id.linear_list_exo)
        buttonAddExo.setOnClickListener {
            val nomExercice = spinnerListExo.selectedItem.toString()
            val nbrRep = spinnerListRep.selectedItem.toString()
            val nomSeance = zoneTextSeance.text.toString().trim()

            if (nomSeance.isNotEmpty() && nomExercice.isNotEmpty()) {
                addExoToSeance(nomSeance, nomExercice, nbrRep)
            }
            addExercisesToLayout(nomSeance)
        }
        val btnSelectPhoto = findViewById<ImageButton>(R.id.selectPhoto)

        btnSelectPhoto.setOnClickListener {
            val nomSeance = zoneTextSeance.text.toString().trim()
            showImageListDialog(nomSeance)
        }
        val editText = findViewById<EditText>(R.id.name_seance)

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                seanceEmpty()
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString().trim()
                if (text.isNotEmpty()) {
                    addExercisesToLayout(text)
                } else {
                    seanceEmpty()
                }
            }
        })
    }

    private fun supSeance(nomSeance: String) {
        if (DatabaseManager.dbHelper.seanceNameExists(nomSeance)) {
            DatabaseManager.dbHelper.supSeance(nomSeance)
            addExercisesToLayout(nomSeance)
            Toast.makeText(this, "Séance supprimée avec Succès", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Séance inexistante", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImageListDialog(nomSeance: String) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, gViewImage.imageTitles)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choisir une image")
        builder.setAdapter(adapter) { dialog: DialogInterface, position: Int ->
            val selectedImage = gViewImage.imageResources[position]

            if (nomSeance.isNotEmpty()) {
                DatabaseManager.dbHelper.addImageToSeance(nomSeance, selectedImage)
                Toast.makeText(this, "Image sélectionnée et associée à la séance: $nomSeance", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Aucune séance sélectionnée.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun addSeance(nomSeance: String) {
        if (!DatabaseManager.dbHelper.seanceNameExists(nomSeance)) {
            DatabaseManager.dbHelper.addSeance(nomSeance)
        }
    }

    private fun addExoToSeance(nomSeance: String, nomExercice: String, nbrRep: String) {
        addSeance(nomSeance)
        DatabaseManager.dbHelper.addExerciceToSeance(nomSeance, nomExercice, nbrRep)
    }

    fun addExercisesToLayout(nomSeance: String) {
        val linearLayoutExo = findViewById<LinearLayout>(R.id.linear_list_exo)
        val id_seance = DatabaseManager.dbHelper.getSeanceId(nomSeance)
        val exercices = DatabaseManager.dbHelper.getExercicesForSeance(id_seance)

        linearLayoutExo.removeAllViews()
        linearLayoutExo.orientation = LinearLayout.VERTICAL
        if (exercices.isNotEmpty()) {
            Log.d("DEBUG", "Nombre d'exercices: ${exercices.size}")
            for (exercise in exercices) {
                val exerciseLayout = LinearLayout(this)
                exerciseLayout.orientation = LinearLayout.HORIZONTAL

                val textView = TextView(this)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
                textView.text = "- $exercise"
                textView.textSize = 20f
                textView.layoutParams = params

                val deleteButton = Button(this)
                val id_exo = DatabaseManager.dbHelper.getExerciceId(exercise, id_seance)

                deleteButton.text = "X"
                deleteButton.setOnClickListener {
                    DatabaseManager.dbHelper.deleteExercice(id_exo)
                    addExercisesToLayout(nomSeance)
                }

                exerciseLayout.addView(textView)
                exerciseLayout.addView(deleteButton)
                linearLayoutExo.addView(exerciseLayout)
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

    fun seanceEmpty() {
        val linearLayoutExo = findViewById<LinearLayout>(R.id.linear_list_exo)

        linearLayoutExo.removeAllViews()

        val textView = TextView(this)
        textView.text = "Aucune séance sélectionnée !"
        textView.textSize = 25f
        textView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        linearLayoutExo.addView(textView)
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

