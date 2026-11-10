package com.example.chessoid

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.SeekBar
import android.widget.RadioGroup
import android.widget.RadioButton
import android.widget.Button
import android.content.Intent


class BotSettingsActivity : AppCompatActivity() {

    private var selectedElo = 1200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bot_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvDifficultyLabel = findViewById<TextView>(R.id.tvDifficultyLabel)
        val seekDifficulty = findViewById<SeekBar>(R.id.seekDifficulty)
        val radioGroupColor = findViewById<RadioGroup>(R.id.radioGroupColor)
        val btnStartMatch = findViewById<Button>(R.id.btnStartMatch)

        // 1. Dynamic SeekBar Logic (Maps 0-25 directly to 500-3000 Elo)
        seekDifficulty.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedElo = 500 + (progress * 100)
                tvDifficultyLabel.text = "Bot Difficulty: $selectedElo Elo"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 2. Start Match Logic
        btnStartMatch.setOnClickListener {

            // Find out which color they checked
            val selectedColorId = radioGroupColor.checkedRadioButtonId
            val playerColor = when (selectedColorId) {
                R.id.radioBlack -> "black"
                R.id.radioRandom -> "random"
                else -> "white"
            }

            // Pack the choices into an Intent and send them to the Match screen
            val intent = Intent(this, BotMatchActivity::class.java)
            intent.putExtra("PLAYER_COLOR", playerColor)
            intent.putExtra("BOT_ELO", selectedElo)
            startActivity(intent)
        }
    }
}