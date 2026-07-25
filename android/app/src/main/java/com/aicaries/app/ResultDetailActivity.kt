package com.aicaries.app

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class ResultDetailActivity : AppCompatActivity() {
    private var serverResultData: org.json.JSONObject? = null
    private var recommendationsList: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_detail)

        findViewById<CardView>(R.id.btnBack)
            .setOnClickListener {
                finish()
            }

        findViewById<CardView>(R.id.btnDownload)
            .setOnClickListener {
                downloadReport()
            }

        val resultId =
            intent.getIntExtra("result_id", -1)

        val score =
            intent.getIntExtra(
                "score",
                intent.getIntExtra("overall_score", 0)
            )

        val risk =
            intent.getStringExtra("risk_level")
                ?: "Unknown"

        val resultType =
            intent.getStringExtra("result_type")
                ?: "assessment"

        val confidence =
            intent.getFloatExtra("confidence", 0f)

        val prediction =
            intent.getStringExtra("prediction")
                ?: ""

        val timestamp =
            intent.getStringExtra("timestamp")
                ?: ""

        val dScore =
            intent.getIntExtra(
                "demographic_score",
                intent.getIntExtra("demographic", 0)
            )

        val seScore =
            intent.getIntExtra(
                "socioeconomic_score",
                intent.getIntExtra("socioeconomic", 0)
            )

        val dietScore =
            intent.getIntExtra(
                "dietary_score",
                intent.getIntExtra("dietary", 0)
            )

        val hygScore =
            intent.getIntExtra(
                "hygiene_score",
                intent.getIntExtra("hygiene", 0)
            )

        val dhScore =
            intent.getIntExtra(
                "dental_history_score",
                intent.getIntExtra("dentalHistory", 0)
            )

        val recs =
            intent.getStringArrayListExtra(
                "recommendations"
            ) ?: arrayListOf()
        recommendationsList.addAll(recs)

        // DATE
        val tvDate =
            findViewById<TextView>(R.id.tvDate)

        try {

            val sdf = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            )

            val date = sdf.parse(timestamp)

            val out = SimpleDateFormat(
                "MMM dd, yyyy • hh:mm a",
                Locale.getDefault()
            )

            tvDate.text =
                if (date != null)
                    out.format(date)
                else
                    out.format(Date())

        } catch (e: Exception) {

            tvDate.text =
                SimpleDateFormat(
                    "MMM dd, yyyy • hh:mm a",
                    Locale.getDefault()
                ).format(Date())
        }

        // SCORE
        findViewById<TextView>(R.id.tvScore)
            .text = "$score%"

        findViewById<TextView>(R.id.tvRiskLevel)
            .text = "$risk Risk"

        val textColorRes = when (risk.lowercase()) {

            "low" ->
                R.color.risk_low

            "high" ->
                R.color.risk_high

            else ->
                R.color.risk_moderate
        }

        val color =
            ContextCompat.getColor(
                this,
                textColorRes
            )

        findViewById<TextView>(R.id.tvScore)
            .setTextColor(color)

        findViewById<TextView>(R.id.tvRiskLevel)
            .setTextColor(color)

        // VIEWS
        val tvScanInfo =
            findViewById<TextView>(R.id.tvScanInfo)

        val cardBreakdown =
            findViewById<CardView>(R.id.cardBreakdown)

        // =========================
        // SCAN RESULT
        // =========================

        if (resultType == "scan") {

            tvScanInfo.visibility = View.VISIBLE

            cardBreakdown.visibility = View.GONE

            val detected =
                if (prediction == "caries")
                    "⚠️ Caries Detected!"
                else
                    "✅ No Caries Detected"

            val confText =
                if (confidence > 0)
                    "\nAI Confidence: ${
                        String.format(
                            "%.1f",
                            confidence
                        )
                    }%"
                else
                    ""

            tvScanInfo.text =
                "$detected$confText"

            tvScanInfo.setTextColor(

                if (prediction == "caries")

                    ContextCompat.getColor(
                        this,
                        R.color.risk_high
                    )

                else

                    ContextCompat.getColor(
                        this,
                        R.color.risk_low
                    )
            )

        }

        // =========================
        // FINAL VALIDATED RESULT
        // =========================

        else if (resultType == "final") {

            tvScanInfo.visibility = View.VISIBLE

            cardBreakdown.visibility = View.VISIBLE

            val assessmentScore =
                intent.getIntExtra(
                    "assessment_score",
                    0
                )

            val aiScore =
                intent.getIntExtra(
                    "ai_score",
                    0
                )

            val finalText = """
🧠 AI Scan Score: $aiScore%

📋 Assessment Score: $assessmentScore%

✅ Final Validated Result Generated
            """.trimIndent()

            tvScanInfo.text = finalText

            tvScanInfo.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.teal_700
                )
            )

            setRow(
                R.id.rowDemographic,
                "AI Scan Weight",
                aiScore
            )

            setRow(
                R.id.rowSocioEconomic,
                "Assessment Weight",
                assessmentScore
            )

            setRow(
                R.id.rowDietary,
                "Combined Risk",
                score
            )

            val confidenceVal = if (confidence > 1f) confidence.toInt() else (confidence * 100f).toInt()
            setRow(
                R.id.rowHygiene,
                "AI Confidence",
                confidenceVal
            )

            setRow(
                R.id.rowDentalHistory,
                "Validation",
                100
            )
        }

        // =========================
        // ASSESSMENT RESULT
        // =========================

        else {

            tvScanInfo.visibility = View.GONE

            cardBreakdown.visibility = View.VISIBLE

            setRow(
                R.id.rowDemographic,
                "Demographic",
                dScore
            )

            setRow(
                R.id.rowSocioEconomic,
                "Socio-Economic",
                seScore
            )

            setRow(
                R.id.rowDietary,
                "Dietary",
                dietScore
            )

            setRow(
                R.id.rowHygiene,
                "Hygiene",
                hygScore
            )

            setRow(
                R.id.rowDentalHistory,
                "Dental History",
                dhScore
            )
        }

        // RECOMMENDATIONS
        val finalRecs =
            if (recs.isNotEmpty())
                recs
            else
                buildRecommendations(risk, score)

        val rv =
            findViewById<RecyclerView>(
                R.id.rvRecommendations
            )

        rv.layoutManager =
            LinearLayoutManager(this)

        rv.adapter =
            RecommendationAdapter(finalRecs)

        rv.isNestedScrollingEnabled = false

        // LOAD SERVER DATA
        if (resultId != -1)
            loadFromServer(resultId, resultType)
    }

    private fun setRow(
        viewId: Int,
        label: String,
        percent: Int
    ) {

        val row = findViewById<View>(viewId)

        row.findViewById<TextView>(
            R.id.tvLabel
        ).text = label

        row.findViewById<TextView>(
            R.id.tvPercent
        ).text = "$percent%"

        row.findViewById<ProgressBar>(
            R.id.progressBar
        ).progress = percent
    }

    private fun buildRecommendations(
        risk: String,
        score: Int
    ): List<String> {

        val base = mutableListOf(

            "Brush your teeth at least twice daily with fluoride toothpaste.",

            "Floss between your teeth every day to remove plaque.",

            "Reduce sugary foods and soft drinks in your diet.",

            "Visit your dentist for a check-up every 6 months.",

            "Drink fluoridated water and rinse after meals."
        )

        if (
            risk.lowercase() == "high"
            || score > 60
        ) {

            base.add(
                "Schedule a dental appointment as soon as possible."
            )

            base.add(
                "Avoid tobacco and alcohol which worsen dental health."
            )
        }

        if (
            risk.lowercase() == "moderate"
        ) {

            base.add(
                "Use an antibacterial mouthwash daily."
            )
        }

        return base
    }

    private fun loadFromServer(
        resultId: Int,
        resultType: String
    ) {

        val session = SessionManager(this)

        val params = org.json.JSONObject()

        params.put(
            "user_id",
            session.getUserId()
        )

        params.put(
            "result_id",
            resultId
        )

        ApiClient.post(
            "get_result_detail.php",
            params
        ) { response ->

            runOnUiThread {

                if (
                    response != null
                    &&
                    response.optBoolean("success")
                ) {

                    val data =
                        response.optJSONObject("result")
                            ?: return@runOnUiThread
                    serverResultData = data

                    if (resultType == "assessment") {

                        setRow(
                            R.id.rowDemographic,
                            "Demographic",
                            data.optInt("demographic_score")
                        )

                        setRow(
                            R.id.rowSocioEconomic,
                            "Socio-Economic",
                            data.optInt("socioeconomic_score")
                        )

                        setRow(
                            R.id.rowDietary,
                            "Dietary",
                            data.optInt("dietary_score")
                        )

                        setRow(
                            R.id.rowHygiene,
                            "Hygiene",
                            data.optInt("hygiene_score")
                        )

                        setRow(
                            R.id.rowDentalHistory,
                            "Dental History",
                            data.optInt("dental_history_score")
                        )
                    }

                    val recsArray =
                        response.optJSONArray(
                            "recommendations"
                        )

                    if (
                        recsArray != null
                        &&
                        recsArray.length() > 0
                    ) {

                        val recList =
                            (0 until recsArray.length())
                                .map {
                                    recsArray.getString(it)
                                }
                        val rv =
                            findViewById<RecyclerView>(
                                R.id.rvRecommendations
                            )

                        rv.adapter =
                            RecommendationAdapter(recList)
                    }
                }
            }
        }
    }

    private fun downloadReport() {
        val session = SessionManager(this)
        val patientName = session.getName().ifEmpty { "Patient" }
        val patientEmail = session.getEmail().ifEmpty { "N/A" }
        
        val score = intent.getIntExtra("score", intent.getIntExtra("overall_score", 0))
        val risk = intent.getStringExtra("risk_level") ?: "Unknown"
        val resultType = intent.getStringExtra("result_type") ?: "assessment"
        val confidence = intent.getFloatExtra("confidence", 0f)
        val prediction = intent.getStringExtra("prediction") ?: ""
        val intentTimestamp = intent.getStringExtra("timestamp") ?: ""
        
        val reportDate = if (intentTimestamp.isNotEmpty()) {
            try {
                val parsedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(intentTimestamp)
                if (parsedDate != null) {
                    SimpleDateFormat("MMMM d, yyyy 'at' hh:mm a", Locale.getDefault()).format(parsedDate)
                } else {
                    SimpleDateFormat("MMMM d, yyyy 'at' hh:mm a", Locale.getDefault()).format(Date())
                }
            } catch (e: Exception) {
                SimpleDateFormat("MMMM d, yyyy 'at' hh:mm a", Locale.getDefault()).format(Date())
            }
        } else {
            SimpleDateFormat("MMMM d, yyyy 'at' hh:mm a", Locale.getDefault()).format(Date())
        }
        
        val isFinal = resultType == "final" || resultType == "scan"
        val diagnosticMode = if (isFinal) "AI Scanner + Assessment" else "Dental Risk Assessment"
        
        val riskColor = when (risk.lowercase(Locale.ROOT)) {
            "high" -> "#ef4444"
            "moderate" -> "#f59e0b"
            else -> "#10b981"
        }
        
        val dScore = serverResultData?.optInt("demographic_score") ?: intent.getIntExtra("demographic_score", intent.getIntExtra("demographic", 0))
        val seScore = serverResultData?.optInt("socioeconomic_score") ?: intent.getIntExtra("socioeconomic_score", intent.getIntExtra("socioeconomic", 0))
        val dietScore = serverResultData?.optInt("dietary_score") ?: intent.getIntExtra("dietary_score", intent.getIntExtra("dietary", 0))
        val hygScore = serverResultData?.optInt("hygiene_score") ?: intent.getIntExtra("hygiene_score", intent.getIntExtra("hygiene", 0))
        val dhScore = serverResultData?.optInt("dental_history_score") ?: intent.getIntExtra("dental_history_score", intent.getIntExtra("dentalHistory", 0))
        
        val answers = serverResultData?.optJSONObject("answers")
        val finalPrediction = answers?.optString("prediction") ?: prediction
        val finalConfidence = answers?.optDouble("confidence")?.toFloat() ?: confidence
        val imagePath = answers?.optString("image") ?: ""
        
        val imageUrl = if (imagePath.isNotEmpty()) {
            "${ApiClient.BASE_URL}$imagePath"
        } else {
            ""
        }
        
        val confidencePercent = if (finalConfidence > 1f) Math.round(finalConfidence) else Math.round(finalConfidence * 100f)
        val aiBlock = if (isFinal && finalPrediction.isNotEmpty()) {
            val detectedText = if (finalPrediction == "caries") "⚠️ Caries Detected" else "✅ No Caries Detected"
            val textColor = if (finalPrediction == "caries") "#ef4444" else "#10b981"
            """
            <div class="breakdown">
              <h3>AI Teeth Scan Analysis</h3>
              <div class="breakdown-row">
                <span class="breakdown-label">AI Model Prediction</span>
                <span class="breakdown-val" style="color: $textColor; font-size: 1rem; width: auto; font-weight: 800;">
                  $detectedText
                </span>
              </div>
              <div class="breakdown-row">
                <span class="breakdown-label">AI Confidence Level</span>
                <span class="breakdown-val">$confidencePercent%</span>
              </div>
            </div>
            """.trimIndent()
        } else {
            ""
        }
        
        val imageBlock = if (imageUrl.isNotEmpty()) {
            """
            <div class="scan-image-container">
              <h3>Scanned Teeth Photo</h3>
              <img src="$imageUrl" alt="Teeth Scan" class="scan-img" />
            </div>
            """.trimIndent()
        } else {
            ""
        }
        
        val recsHtml = StringBuilder()
        for (rec in recommendationsList) {
            recsHtml.append("<li>").append(rec).append("</li>")
        }
        if (recsHtml.isEmpty()) {
            recsHtml.append("""
                <li>Brush your teeth at least twice daily with fluoride toothpaste.</li>
                <li>Floss between your teeth every day to remove plaque.</li>
                <li>Reduce sugary foods and soft drinks in your diet.</li>
                <li>Visit your dentist for a check-up every 6 months.</li>
            """.trimIndent())
        }
        
        val htmlContent = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>AICaries Diagnostic Report - $patientName</title>
              <style>
                body {
                  font-family: sans-serif;
                  background-color: #f3f4f6;
                  color: #1f2937;
                  margin: 0;
                  padding: 20px;
                  display: flex;
                  justify-content: center;
                }
                .report-card {
                  background: white;
                  max-width: 700px;
                  width: 100%;
                  border-radius: 16px;
                  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
                  padding: 40px;
                  box-sizing: border-box;
                }
                .header {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  border-bottom: 2px solid #e5e7eb;
                  padding-bottom: 20px;
                  margin-bottom: 30px;
                }
                .header h1 {
                  font-size: 1.8rem;
                  margin: 0;
                  color: #6366f1;
                  font-weight: 800;
                }
                .header .date {
                  font-size: 0.9rem;
                  color: #6b7280;
                }
                .patient-info {
                  background: #f5f3ff;
                  border-left: 4px solid #6366f1;
                  padding: 16px;
                  border-radius: 0 8px 8px 0;
                  margin-bottom: 30px;
                }
                .patient-info p {
                  margin: 4px 0;
                  font-size: 0.95rem;
                }
                .score-container {
                  display: flex;
                  align-items: center;
                  gap: 30px;
                  background: #fafafa;
                  border: 1px solid #e5e7eb;
                  border-radius: 12px;
                  padding: 24px;
                  margin-bottom: 30px;
                }
                .score-circle {
                  width: 100px;
                  height: 100px;
                  border-radius: 50%;
                  background: conic-gradient($riskColor ${score}%, #e5e7eb 0);
                  display: flex;
                  justify-content: center;
                  align-items: center;
                  position: relative;
                }
                .score-circle::after {
                  content: '';
                  position: absolute;
                  width: 80px;
                  height: 80px;
                  border-radius: 50%;
                  background: white;
                }
                .score-value {
                  position: absolute;
                  font-size: 1.8rem;
                  font-weight: 800;
                  z-index: 1;
                  color: #111827;
                }
                .score-details h2 {
                  margin: 0 0 6px 0;
                  font-size: 1.3rem;
                  color: #111827;
                }
                .badge {
                  display: inline-block;
                  padding: 6px 16px;
                  border-radius: 20px;
                  font-size: 0.85rem;
                  font-weight: 700;
                  text-transform: uppercase;
                  color: white;
                  background-color: $riskColor;
                }
                .breakdown {
                  margin-bottom: 30px;
                }
                .breakdown h3 {
                  font-size: 1.1rem;
                  margin-bottom: 16px;
                  border-bottom: 1px solid #e5e7eb;
                  padding-bottom: 8px;
                  color: #374151;
                }
                .breakdown-row {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 12px;
                }
                .breakdown-label {
                  font-size: 0.95rem;
                  color: #4b5563;
                }
                .breakdown-bar-container {
                  width: 200px;
                  background: #e5e7eb;
                  height: 8px;
                  border-radius: 4px;
                  overflow: hidden;
                  margin: 0 16px;
                  flex-grow: 1;
                }
                .breakdown-bar-fill {
                  height: 100%;
                  background: #6366f1;
                  border-radius: 4px;
                }
                .breakdown-val {
                  width: 40px;
                  text-align: right;
                  font-weight: 700;
                  font-size: 0.95rem;
                }
                .scan-image-container {
                  text-align: center;
                  margin-bottom: 30px;
                  background: #fafafa;
                  border: 1px solid #e5e7eb;
                  border-radius: 12px;
                  padding: 24px;
                }
                .scan-image-container h3 {
                  margin-top: 0;
                  font-size: 1.1rem;
                  color: #374151;
                  margin-bottom: 16px;
                  text-align: left;
                  border-bottom: 1px solid #e5e7eb;
                  padding-bottom: 8px;
                }
                .scan-img {
                  max-width: 100%;
                  max-height: 350px;
                  border-radius: 8px;
                  border: 1px solid #d1d5db;
                  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
                }
                .recs {
                  background: #fafafa;
                  border: 1px solid #e5e7eb;
                  border-radius: 12px;
                  padding: 24px;
                  margin-bottom: 30px;
                }
                .recs h3 {
                  margin-top: 0;
                  color: #6366f1;
                  margin-bottom: 16px;
                }
                .recs ul {
                  margin: 0;
                  padding-left: 20px;
                }
                .recs li {
                  margin-bottom: 10px;
                  font-size: 0.95rem;
                  line-height: 1.5;
                }
                @media print {
                  body {
                    background-color: white;
                    padding: 0;
                  }
                  .report-card {
                    box-shadow: none;
                    padding: 0;
                  }
                }
              </style>
            </head>
            <body>
              <div class="report-card">
                <div class="header">
                  <h1>AICaries Diagnostic Report</h1>
                  <div class="date">$reportDate</div>
                </div>

                <div class="patient-info">
                  <p><strong>Patient Name:</strong> $patientName</p>
                  <p><strong>Email Address:</strong> $patientEmail</p>
                  <p><strong>Diagnostic Mode:</strong> $diagnosticMode</p>
                </div>

                <div class="score-container">
                  <div class="score-circle">
                    <span class="score-value">$score%</span>
                  </div>
                  <div class="score-details">
                    <h2>Risk Score</h2>
                    <span class="badge">$risk Risk</span>
                  </div>
                </div>

                <div class="breakdown">
                  <h3>Diagnostic Breakdown</h3>
                  <div class="breakdown-row">
                    <span class="breakdown-label">Demographic Risk Rating</span>
                    <div class="breakdown-bar-container">
                      <div class="breakdown-bar-fill" style="width: $dScore%"></div>
                    </div>
                    <span class="breakdown-val">$dScore%</span>
                  </div>
                  <div class="breakdown-row">
                    <span class="breakdown-label">Socioeconomic Influence</span>
                    <div class="breakdown-bar-container">
                      <div class="breakdown-bar-fill" style="width: $seScore%"></div>
                    </div>
                    <span class="breakdown-val">$seScore%</span>
                  </div>
                  <div class="breakdown-row">
                    <span class="breakdown-label">Sugary Dietary Load</span>
                    <div class="breakdown-bar-container">
                      <div class="breakdown-bar-fill" style="width: $dietScore%"></div>
                    </div>
                    <span class="breakdown-val">$dietScore%</span>
                  </div>
                  <div class="breakdown-row">
                    <span class="breakdown-label">Hygiene Practices Score</span>
                    <div class="breakdown-bar-container">
                      <div class="breakdown-bar-fill" style="width: $hygScore%"></div>
                    </div>
                    <span class="breakdown-val">$hygScore%</span>
                  </div>
                  <div class="breakdown-row">
                    <span class="breakdown-label">Dental History Assessment</span>
                    <div class="breakdown-bar-container">
                      <div class="breakdown-bar-fill" style="width: $dhScore%"></div>
                    </div>
                    <span class="breakdown-val">$dhScore%</span>
                  </div>
                </div>

                $aiBlock

                $imageBlock

                <div class="recs">
                  <h3>Personalized Recommendations</h3>
                  <ul>
                    $recsHtml
                  </ul>
                </div>
              </div>
            </body>
            </html>
        """.trimIndent()

        val webView = WebView(this)
        val settings = webView.settings
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = getSystemService(PRINT_SERVICE) as PrintManager
                val jobName = "AICaries_Diagnostic_Report_${System.currentTimeMillis()}"
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("id", "print", 300, 300))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
                
                printManager.print(jobName, printAdapter, printAttributes)
            }
        }
        
        webView.loadDataWithBaseURL("file:///android_asset/", htmlContent, "text/html", "UTF-8", null)
        Toast.makeText(this, "Generating PDF report...", Toast.LENGTH_SHORT).show()
    }
}
