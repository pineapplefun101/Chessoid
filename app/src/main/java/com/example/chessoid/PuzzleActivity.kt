package com.example.chessoid

import android.os.Bundle
import android.widget.GridLayout
import android.widget.ImageView
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

            val boardGrid = findViewById<GridLayout>(R.id.chessBoardGrid)

            boardGrid.post {
                val squareSize = boardGrid.width / 8
                var squareIndex = 0

                // A 64-item array representing the standard chess starting position.
                // 0 represents an empty square.
                val initialBoard = arrayOf(
                    R.drawable.b_rook, R.drawable.b_knight, R.drawable.b_bishop, R.drawable.b_queen, R.drawable.b_king, R.drawable.b_bishop, R.drawable.b_knight, R.drawable.b_rook,
                    R.drawable.b_pawn, R.drawable.b_pawn, R.drawable.b_pawn, R.drawable.b_pawn, R.drawable.b_pawn, R.drawable.b_pawn, R.drawable.b_pawn, R.drawable.b_pawn,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    R.drawable.w_pawn, R.drawable.w_pawn, R.drawable.w_pawn, R.drawable.w_pawn, R.drawable.w_pawn, R.drawable.w_pawn, R.drawable.w_pawn, R.drawable.w_pawn,
                    R.drawable.w_rook, R.drawable.w_knight, R.drawable.w_bishop, R.drawable.w_queen, R.drawable.w_king, R.drawable.w_bishop, R.drawable.w_knight, R.drawable.w_rook
                )

                for (row in 0..7) {
                    for (col in 0..7) {
                        val square = ImageView(this)
                        square.tag = squareIndex

                        square.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        // Optional: Adjust padding if the pieces look too large or small inside their squares
                        square.setPadding(8, 8, 8, 8)

                        // Inject the piece asset if the array says this square shouldn't be empty
                        val pieceImageId = initialBoard[squareIndex]
                        if (pieceImageId != 0) {
                            square.setImageResource(pieceImageId)
                        }

                        val params = GridLayout.LayoutParams()
                        params.width = squareSize
                        params.height = squareSize
                        square.layoutParams = params

                        boardGrid.addView(square)
                        squareIndex++
                    }
                }
            }
        }
    }
}