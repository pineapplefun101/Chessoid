package com.example.chessoid

import android.os.Bundle
import android.widget.GridLayout
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

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

        // Extract the puzzle database to internal storage
        val dbFile = File(filesDir, "puzzles.db")
        if (!dbFile.exists()) {
            assets.open("puzzles.db").use { input ->
                dbFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        val boardGrid = findViewById<GridLayout>(R.id.chessBoardGrid)

        // Start the Python environment
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        // Fetch your chess_logic.py script
        val py = Python.getInstance()
        val chessLogic = py.getModule("chess_logic")

        // Pass the database path to Python and print the result
        val dbPath = dbFile.absolutePath
        val puzzleTestResult = chessLogic.callAttr("get_test_puzzle", dbPath).toString()
        println("DATABASE TEST: $puzzleTestResult")


        boardGrid.post {

            val squareSize = boardGrid.width / 8
            var squareIndex = 0

            // Memory for the two-tap system
            var selectedIndex = -1
            var selectedSquareName = ""
            var currentLegalMoves = listOf<String>()

            // --- FETCH FEN FROM DATABASE ---
            val dbPath = File(filesDir, "puzzles.db").absolutePath
            val fenString = chessLogic.callAttr("load_random_puzzle", dbPath).toString()

            // Translate the FEN into the 64-item array
            val initialBoard = fenToBoardArray(fenString)
            // -------------------------------
            for (row in 0..7) {
                for (col in 0..7) {
                    val square = ImageView(this)
                    square.tag = squareIndex

                    square.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    square.setPadding(8, 8, 8, 8)

                    val pieceImageId = initialBoard[squareIndex]
                    if (pieceImageId != 0) {
                        square.setImageResource(pieceImageId)
                        square.scaleType = ImageView.ScaleType.FIT_XY
                    }

                    val params = GridLayout.LayoutParams()
                    params.width = squareSize
                    params.height = squareSize
                    square.layoutParams = params

                    // --- THE UPGRADED TOUCH LISTENER ---
                    square.setOnClickListener {
                        val clickedIndex = square.tag as Int
                        val clickedCol = clickedIndex % 8
                        val clickedRow = clickedIndex / 8
                        val clickedSquareName = "${(97 + clickedCol).toChar()}${8 - clickedRow}"

                        // CASE 1: EXECUTE A MOVE
                        // If we already selected a piece, and the tapped square is a valid legal move
                        if (selectedIndex != -1 && currentLegalMoves.contains(clickedSquareName)) {

                            // 1. Tell Python to update the backend board (e.g., "e2e4")
                            val moveUci = selectedSquareName + clickedSquareName
                            chessLogic.callAttr("make_move", moveUci)

                            // 2. Update the UI: Move the image to the new square, clear the old square
                            val sourceSquare = boardGrid.getChildAt(selectedIndex) as ImageView
                            val destSquare = boardGrid.getChildAt(clickedIndex) as ImageView
                            destSquare.setImageDrawable(sourceSquare.drawable)
                            sourceSquare.setImageDrawable(null)

                            // 3. Reset the board highlights and memory
                            for (i in 0..63) {
                                boardGrid.getChildAt(i).setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            }
                            selectedIndex = -1
                            selectedSquareName = ""
                            currentLegalMoves = emptyList()

                        }
                        // CASE 2: SELECT A PIECE
                        else {
                            // 1. Clear any previous highlights
                            for (i in 0..63) {
                                boardGrid.getChildAt(i).setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            }

                            // 2. Ask Python for legal moves for the tapped piece
                            val pyLegalMoves = chessLogic.callAttr("get_legal_moves", clickedSquareName).asList()
                            currentLegalMoves = pyLegalMoves.map { it.toString() }

                            // 3. Highlight destination squares in green
                            for (destName in currentLegalMoves) {
                                val destCol = destName[0] - 'a'
                                val destRow = 8 - destName[1].digitToInt()
                                val destIndex = (destRow * 8) + destCol
                                boardGrid.getChildAt(destIndex).setBackgroundColor(android.graphics.Color.parseColor("#8000FF00"))
                            }

                            // 4. Save state if the piece actually has moves
                            if (currentLegalMoves.isNotEmpty()) {
                                selectedIndex = clickedIndex
                                selectedSquareName = clickedSquareName
                                // Optional: Highlight the tapped piece in yellow so you know it's selected
                                square.setBackgroundColor(android.graphics.Color.parseColor("#80FFFF00"))
                            } else {
                                selectedIndex = -1
                                selectedSquareName = ""
                            }
                        }
                    }
                    // -----------------------------------

                    boardGrid.addView(square)
                    squareIndex++
                }
            }
        }
    }

    private fun charToDrawable(c: Char): Int {
        return when (c) {
            'r' -> R.drawable.b_rook
            'n' -> R.drawable.b_knight
            'b' -> R.drawable.b_bishop
            'q' -> R.drawable.b_queen
            'k' -> R.drawable.b_king
            'p' -> R.drawable.b_pawn
            'R' -> R.drawable.w_rook
            'N' -> R.drawable.w_knight
            'B' -> R.drawable.w_bishop
            'Q' -> R.drawable.w_queen
            'K' -> R.drawable.w_king
            'P' -> R.drawable.w_pawn
            else -> 0
        }
    }

    private fun fenToBoardArray(fen: String): IntArray {
        val board = IntArray(64) { 0 }
        // We only care about the piece placement part (before the first space)
        val piecePlacement = fen.split(" ")[0]
        val rows = piecePlacement.split("/")

        var index = 0
        for (row in rows) {
            for (char in row) {
                if (char.isDigit()) {
                    // Skip empty squares
                    index += char.digitToInt()
                } else {
                    // Assign the drawable ID
                    board[index] = charToDrawable(char)
                    index++
                }
            }
        }
        return board
    }


}