package com.example.chessoid

import android.os.Bundle
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
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

            // --- FETCH FEN FROM DATABASE ---
            val dbPath = File(filesDir, "puzzles.db").absolutePath
            val streakText = findViewById<TextView>(R.id.streakText)
            val tvPuzzleStatus = findViewById<TextView>(R.id.tvPuzzleStatus)
            var currentStreak = 0

            var selectedIndex = -1
            var selectedSquareName = ""
            var currentLegalMoves = listOf<String>()

            // --- REUSABLE PUZZLE LOADER ---
            fun loadNewPuzzle() {
                // 1. Reset selection memory
                selectedIndex = -1
                selectedSquareName = ""
                currentLegalMoves = emptyList()

                // 2. Fetch new FEN from Python/SQLite
                val fenString = chessLogic.callAttr("load_random_puzzle", dbPath).toString()
                val boardArray = fenToBoardArray(fenString)

                //Update "White to Move" / "Black to Move"
                val fenParts = fenString.split(" ")
                if (fenParts.size > 1) {
                    if (fenParts[1] == "w") {
                        tvPuzzleStatus.text = "White to Move"
                    } else {
                        tvPuzzleStatus.text = "Black to Move"
                    }
                }


                // 3. Update the 64 existing ImageViews instantly
                for (i in 0..63) {
                    val square = boardGrid.getChildAt(i) as ImageView
                    val pieceImageId = boardArray[i]

                    if (pieceImageId != 0) {
                        square.setImageResource(pieceImageId)
                        square.scaleType = ImageView.ScaleType.FIT_XY
                    } else {
                        square.setImageDrawable(null) // Empty square
                    }
                    // Clear any lingering highlights from the previous puzzle
                    square.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                }
            }

            val btnHint = findViewById<Button>(R.id.btnShowSolution)
            val btnSkip = findViewById<Button>(R.id.btnNextPuzzle)

            // Skip button (|>) - Resets streak and immediately loads next puzzle
            btnSkip.setOnClickListener {
                currentStreak = 0
                streakText.text = "Streak: $currentStreak"
                loadNewPuzzle()
            }

            // Move-by-move hint button (>)
            btnHint.setOnClickListener {
                val result = chessLogic.callAttr("pop_hint_move").toString()

                if (result.startsWith("MOVE:") || result.startsWith("LAST:")) {
                    val moveUci = result.split(":")[1]

                    // Move the piece visually on the board
                    movePieceOnUi(moveUci, boardGrid)

                    // Using a hint breaks the streak
                    currentStreak = 0
                    streakText.text = "Streak: $currentStreak"

                    // Clear any manual piece selections or highlights
                    for (i in 0..63) {
                        boardGrid.getChildAt(i).setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    }
                    selectedIndex = -1
                    selectedSquareName = ""
                    currentLegalMoves = emptyList()

                    // If that was the final move of the puzzle, wait 1 second and load a new one
                    if (result.startsWith("LAST:")) {
                        boardGrid.postDelayed({ loadNewPuzzle() }, 1000)
                    }
                }
            }


            // --- INITIALIZE THE GRID UI ---
            boardGrid.post {
                val squareSize = boardGrid.width / 8
                var squareIndex = 0

                // We only build the 64 empty squares ONCE
                for (row in 0..7) {
                    for (col in 0..7) {
                        val square = ImageView(this)
                        square.tag = squareIndex
                        square.setPadding(8, 8, 8, 8)

                        val params = GridLayout.LayoutParams()
                        params.width = squareSize
                        params.height = squareSize
                        square.layoutParams = params

                        // --- THE TOUCH LISTENER ---
                        square.setOnClickListener {
                            val clickedIndex = square.tag as Int
                            val clickedCol = clickedIndex % 8
                            val clickedRow = clickedIndex / 8
                            val clickedSquareName = "${(97 + clickedCol).toChar()}${8 - clickedRow}"

                            // CASE 1: EXECUTE A MOVE
                            if (selectedIndex != -1 && currentLegalMoves.contains(clickedSquareName)) {
                                val moveUci = selectedSquareName + clickedSquareName
                                val result = chessLogic.callAttr("check_puzzle_move", moveUci).toString()

                                if (result == "WRONG") {
                                    // Flash red and kill the streak
                                    square.setBackgroundColor(android.graphics.Color.parseColor("#80FF0000"))
                                    currentStreak = 0
                                    streakText.text = "Streak: $currentStreak"
                                } else {
                                    // Move is correct! Move piece visually.
                                    movePieceOnUi(moveUci, boardGrid)

                                    if (result.startsWith("CORRECT:")) {
                                        // The bot has a reply
                                        val botReplyUci = result.split(":")[1]
                                        boardGrid.postDelayed({
                                            movePieceOnUi(botReplyUci, boardGrid)
                                        }, 400)
                                    } else if (result == "SOLVED") {
                                        // Puzzle complete! Bump streak and auto-advance.
                                        currentStreak++
                                        streakText.text = "Streak: $currentStreak"

                                        // Wait 1 second so the player can see their winning move land, then reload
                                        boardGrid.postDelayed({
                                            loadNewPuzzle()
                                        }, 1000)
                                    }
                                }

                                // Clean up memory/highlights after ANY move attempt (unless they guessed wrong, leave the red flash)
                                if (result != "WRONG") {
                                    for (i in 0..63) {
                                        boardGrid.getChildAt(i).setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                    }
                                    selectedIndex = -1
                                    selectedSquareName = ""
                                    currentLegalMoves = emptyList()
                                }
                            }
                            // CASE 2: SELECT A PIECE
                            else {
                                // Clear previous highlights (removes the red flash if they tap elsewhere)
                                for (i in 0..63) {
                                    boardGrid.getChildAt(i).setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                }

                                val pyLegalMoves = chessLogic.callAttr("get_legal_moves", clickedSquareName).asList()
                                currentLegalMoves = pyLegalMoves.map { it.toString() }

                                for (destName in currentLegalMoves) {
                                    val destCol = destName[0] - 'a'
                                    val destRow = 8 - destName[1].digitToInt()
                                    val destIndex = (destRow * 8) + destCol
                                    boardGrid.getChildAt(destIndex).setBackgroundColor(android.graphics.Color.parseColor("#8000FF00"))
                                }

                                if (currentLegalMoves.isNotEmpty()) {
                                    selectedIndex = clickedIndex
                                    selectedSquareName = clickedSquareName
                                    square.setBackgroundColor(android.graphics.Color.parseColor("#80FFFF00"))
                                } else {
                                    selectedIndex = -1
                                    selectedSquareName = ""
                                }
                            }
                        }
                        // --------------------------

                        boardGrid.addView(square)
                        squareIndex++
                    }
                }

                // Now that the 64 squares are built and listening, load the very first puzzle!
                loadNewPuzzle()
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

    private fun movePieceOnUi(moveUci: String, boardGrid: GridLayout) {
        // e.g., converts "e2e4" to grid indices
        val sourceCol = moveUci[0] - 'a'
        val sourceRow = 8 - moveUci[1].digitToInt()
        val sourceIndex = (sourceRow * 8) + sourceCol

        val destCol = moveUci[2] - 'a'
        val destRow = 8 - moveUci[3].digitToInt()
        val destIndex = (destRow * 8) + destCol

        val sourceSquare = boardGrid.getChildAt(sourceIndex) as ImageView
        val destSquare = boardGrid.getChildAt(destIndex) as ImageView

        destSquare.setImageDrawable(sourceSquare.drawable)
        sourceSquare.setImageDrawable(null)
    }


}