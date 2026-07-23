package com.aicaries.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class FinalValidatedResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_validated_result)

        findViewById<CardView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val assessmentScore = intent.getIntExtra("assessment_score", 0)
        val aiScore = intent.getIntExtra("ai_score", 0)

        val aiPrediction = intent.getStringExtra("prediction") ?: "Unknown"
        val aiConfidence = intent.getFloatExtra("confidence", 0f)

        // FINAL COMBINED SCORE
        val finalScore = ((assessmentScore * 0.4f) + (aiScore * 0.6f)).toInt()

        val finalRisk = when {
            finalScore < 20 -> "Low"
            finalScore < 60 -> "Moderate"
            else -> "High"
        }

        findViewById<TextView>(R.id.tvFinalScore).text = "$finalScore%"
        findViewById<TextView>(R.id.tvFinalRisk).text = "$finalRisk Risk"

        val resultText = """
Assessment Score: $assessmentScore%

AI Scan Score: $aiScore%

AI Prediction:
$aiPrediction

AI Confidence:
${String.format("%.1f", aiConfidence)}%
        """.trimIndent()

        findViewById<TextView>(R.id.tvCombinedInfo).text = resultText

        val colorRes = when (finalRisk.lowercase()) {
            "low" -> R.color.risk_low
            "high" -> R.color.risk_high
            else -> R.color.risk_moderate
        }

        val color = ContextCompat.getColor(this, colorRes)

        findViewById<TextView>(R.id.tvFinalScore).setTextColor(color)
        findViewById<TextView>(R.id.tvFinalRisk).setTextColor(color)
    }
}