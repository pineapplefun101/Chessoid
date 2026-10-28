package com.example.chessoid

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

class PuzzleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_puzzle)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val engineFile = File(filesDir, "stockfish")
        if (!engineFile.exists()) {
            // Copy from assets to internal storage
            assets.open("stockfish").use { input ->
                engineFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            // Grant Linux execution permissions
            engineFile.setExecutable(true)
        }
    }
}