package com.example.chessoid

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageButton
import android.content.Intent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Find the Puzzles button by its ID
        val btnPuzzles = findViewById<ImageButton>(R.id.btnPuzzles)

        // 2. Set what happens when the button is clicked
        btnPuzzles.setOnClickListener {
            // 3. Create an Intent to transition to the PuzzleActivity
            val intent = Intent(this, PuzzleActivity::class.java)
            startActivity(intent)
        }
    }
}