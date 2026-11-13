package com.example.chessoid

import android.os.Bundle
import android.widget.GridLayout
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File


class BotMatchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bot_match)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val boardGrid = findViewById<GridLayout>(R.id.chessBoardGrid)

        // Start the Python environment
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        // Fetch your chess_logic.py script
        val py = Python.getInstance()
        val botLogic = py.getModule("bot_logic")

        // 1. Unpack the settings
        val playerColor = intent.getStringExtra("PLAYER_COLOR") ?: "white"
        val botElo = intent.getIntExtra("BOT_ELO", 1200)

        // 2. Reset the board and boot the engine with the requested Elo
        val enginePath = "${applicationInfo.nativeLibraryDir}/libstockfish.so"
        botLogic.callAttr("reset_board")
        botLogic.callAttr("init_engine", enginePath, botElo)

        // 3. If the player chose Black, the Bot (White) needs to make the very first move!
        if (playerColor == "black") {
            val firstMove = botLogic.callAttr("get_bot_reply").toString()
            // (We will handle flipping the UI pieces in the next step, for now just let it calculate)
        }

        boardGrid.post {
            val squareSize = boardGrid.width / 8
            var squareIndex = 0

            var selectedIndex = -1
            var selectedSquareName = ""
            var currentLegalMoves = listOf<String>()

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
                            botLogic.callAttr("make_move", moveUci)

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


                            // --- ASK STOCKFISH FOR A REPLY ---
                            boardGrid.postDelayed({
                                val botMoveUci = botLogic.callAttr("get_bot_reply").toString()

                                if (botMoveUci != "GAME_OVER") {
                                    // Convert the bot's UCI string (e.g., "e7e5") to grid indices
                                    val botSourceCol = botMoveUci[0] - 'a'
                                    val botSourceRow = 8 - botMoveUci[1].digitToInt()
                                    val botSourceIndex = (botSourceRow * 8) + botSourceCol

                                    val botDestCol = botMoveUci[2] - 'a'
                                    val botDestRow = 8 - botMoveUci[3].digitToInt()
                                    val botDestIndex = (botDestRow * 8) + botDestCol

                                    // Visually move the bot's piece
                                    val botSourceSquare = boardGrid.getChildAt(botSourceIndex) as ImageView
                                    val botDestSquare = boardGrid.getChildAt(botDestIndex) as ImageView

                                    botDestSquare.setImageDrawable(botSourceSquare.drawable)
                                    botSourceSquare.setImageDrawable(null)
                                }
                            }, 100) // Slight 100ms delay so it feels natural

                        }
                        // CASE 2: SELECT A PIECE
                        else {
                            // 1. Clear any previous highlights
                            for (i in 0..63) {
                                boardGrid.getChildAt(i).setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            }

                            // 2. Ask Python for legal moves for the tapped piece
                            val pyLegalMoves = botLogic.callAttr("get_legal_moves", clickedSquareName).asList()
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
