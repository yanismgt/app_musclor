package com.example.app_lunabee

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

data class Exercise(val name: String, val repetitions: Int)
data class Seance(val id: Long, val nomSeance: String)
