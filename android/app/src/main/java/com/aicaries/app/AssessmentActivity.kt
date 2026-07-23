package com.aicaries.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class AssessmentActivity : AppCompatActivity() {

    private var currentQuestion = 0
    private val answers = mutableMapOf<String, String>()

    data class Question(
        val category: String,
        val question: String,
        val options: List<String>,
        val key: String
    )

    private val questions = listOf(

        Question(
            "Demographic Information",
            "What is your age group?",
            listOf("Under 18", "18–30", "31–45", "46–60", "60+"),
            "age"
        ),

        Question(
            "Demographic Information",
            "What is your gender?",
            listOf("Male", "Female", "Other", "Prefer not to say"),
            "gender"
        ),

        Question(
            "Demographic Information",
            "Place of residence?",
            listOf("Urban", "Rural"),
            "residence"
        ),

        Question(
            "Demographic Information",
            "Level of education?",
            listOf(
                "No formal education",
                "Primary school",
                "Secondary school",
                "College/University"
            ),
            "education"
        ),

        Question(
            "Socio-Economic Indicators",
            "Monthly household income range?",
            listOf(
                "Below ₹10,000",
                "₹10,000–₹30,000",
                "₹30,000–₹60,000",
                "Above ₹60,000"
            ),
            "income"
        ),

        Question(
            "Socio-Economic Indicators",
            "Do you have access to regular dental care?",
            listOf("Yes", "No"),
            "dentalAccess"
        ),

        Question(
            "Dietary Habits",
            "How often do you consume sugary foods or drinks?",
            listOf(
                "Rarely",
                "1–2 times/week",
                "3–5 times/week",
                "Daily"
            ),
            "sugar"
        ),

        Question(
            "Dietary Habits",
            "Do you snack between meals?",
            listOf(
                "No",
                "Sometimes",
                "Daily",
                "Multiple times daily"
            ),
            "snacking"
        ),

        Question(
            "Hygiene Practices",
            "How many times do you brush daily?",
            listOf(
                "Never",
                "Once",
                "Twice",
                "3 or more times"
            ),
            "brushing"
        ),

        Question(
            "Hygiene Practices",
            "Do you use floss or mouthwash?",
            listOf(
                "No",
                "Floss only",
                "Mouthwash only",
                "Both"
            ),
            "hygieneAids"
        ),

        Question(
            "Dental History",
            "Have you been diagnosed with dental caries before?",
            listOf("Yes", "No"),
            "previousCaries"
        ),

        Question(
            "Dental History",
            "Tooth pain or sensitivity in the past 6 months?",
            listOf("Yes", "No"),
            "toothPain"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment)

        showQuestion(0)
    }

    private fun showQuestion(index: Int) {

        if (index >= questions.size) {
            calculateAndSave()
            return
        }

        currentQuestion = index
        val q = questions[index]

        val total = questions.size

        findViewById<ProgressBar>(R.id.progressBar).progress =
            ((index + 1) * 100) / total

        findViewById<TextView>(R.id.tvQuestionNumber).text =
            "Question ${index + 1} of $total"

        findViewById<TextView>(R.id.tvCategory).text = q.category
        findViewById<TextView>(R.id.tvQuestion).text = q.question

        val container = findViewById<LinearLayout>(R.id.optionsContainer)
        container.removeAllViews()

        q.options.forEach { option ->

            val optView = layoutInflater.inflate(
                R.layout.item_option,
                container,
                false
            )

            optView.findViewById<TextView>(R.id.tvOption).text = option

            optView.setOnClickListener {

                for (i in 0 until container.childCount) {
                    container.getChildAt(i).background =
                        resources.getDrawable(
                            R.drawable.bg_option_unselected,
                            null
                        )
                }

                optView.background =
                    resources.getDrawable(
                        R.drawable.bg_option_selected,
                        null
                    )

                answers[q.key] = option

                val btnNext = findViewById<Button>(R.id.btnNext)
                btnNext.isEnabled = true
                btnNext.alpha = 1.0f
            }

            container.addView(optView)
        }

        findViewById<View>(R.id.btnBack).setOnClickListener {

            if (currentQuestion > 0) {
                showQuestion(currentQuestion - 1)
            } else {
                finish()
            }
        }

        val btnNext = findViewById<Button>(R.id.btnNext)

        btnNext.isEnabled = false
        btnNext.alpha = 0.5f

        btnNext.setOnClickListener {
            showQuestion(currentQuestion + 1)
        }
    }

    private fun calculateAndSave() {

        // DEMOGRAPHIC
        var demographic = 0

        demographic += when (answers["age"]) {
            "Under 18" -> 5
            "18–30" -> 2
            "31–45" -> 4
            "46–60" -> 6
            "60+" -> 8
            else -> 0
        }

        demographic += when (answers["gender"]) {
            "Female" -> 2
            else -> 0
        }

        demographic += when (answers["residence"]) {
            "Rural" -> 5
            else -> 0
        }

        demographic += when (answers["education"]) {
            "No formal education" -> 8
            "Primary school" -> 5
            "Secondary school" -> 3
            else -> 0
        }

        demographic = minOf(demographic, 100)

        // SOCIOECONOMIC
        var socioeconomic = 0

        socioeconomic += when (answers["income"]) {
            "Below ₹10,000" -> 30
            "₹10,000–₹30,000" -> 20
            "₹30,000–₹60,000" -> 10
            "Above ₹60,000" -> 0
            else -> 0
        }

        socioeconomic += when (answers["dentalAccess"]) {
            "No" -> 25
            else -> 0
        }

        socioeconomic = minOf(socioeconomic, 100)

        // DIETARY
        var dietary = 0

        dietary += when (answers["sugar"]) {
            "Rarely" -> 0
            "1–2 times/week" -> 15
            "3–5 times/week" -> 30
            "Daily" -> 50
            else -> 0
        }

        dietary += when (answers["snacking"]) {
            "No" -> 0
            "Sometimes" -> 10
            "Daily" -> 20
            "Multiple times daily" -> 35
            else -> 0
        }

        dietary = minOf(dietary, 100)

        // HYGIENE
        var hygiene = 0

        hygiene += when (answers["brushing"]) {
            "Never" -> 60
            "Once" -> 30
            "Twice" -> 0
            "3 or more times" -> 0
            else -> 0
        }

        hygiene += when (answers["hygieneAids"]) {
            "No" -> 25
            "Floss only" -> 10
            "Mouthwash only" -> 10
            "Both" -> 0
            else -> 0
        }

        hygiene = minOf(hygiene, 100)

        // DENTAL HISTORY
        var dentalHistory = 0

        dentalHistory += when (answers["previousCaries"]) {
            "Yes" -> 50
            else -> 0
        }

        dentalHistory += when (answers["toothPain"]) {
            "Yes" -> 30
            else -> 0
        }

        dentalHistory = minOf(dentalHistory, 100)

        // OVERALL SCORE
        val overall = minOf(
            (
                    demographic * 0.10 +
                            socioeconomic * 0.25 +
                            dietary * 0.25 +
                            hygiene * 0.30 +
                            dentalHistory * 0.10
                    ).toInt(),
            100
        )

        val risk = when {
            overall < 30 -> "Low"
            overall < 60 -> "Moderate"
            else -> "High"
        }

        // RECOMMENDATIONS
        val recommendations = mutableListOf<String>()

        if (hygiene > 40) {
            recommendations.add(
                "Brush your teeth at least twice daily using fluoride toothpaste."
            )
        }

        if (answers["hygieneAids"] == "No") {
            recommendations.add(
                "Start flossing daily to remove plaque between teeth."
            )
        }

        if (dietary > 30) {
            recommendations.add(
                "Reduce sugary foods and soft drinks."
            )
        }

        if (
            answers["snacking"] == "Daily" ||
            answers["snacking"] == "Multiple times daily"
        ) {
            recommendations.add(
                "Avoid frequent snacking between meals."
            )
        }

        if (answers["dentalAccess"] == "No") {
            recommendations.add(
                "Try to access regular dental care services."
            )
        }

        if (answers["previousCaries"] == "Yes") {
            recommendations.add(
                "You have a history of caries. Visit a dentist soon."
            )
        }

        if (answers["toothPain"] == "Yes") {
            recommendations.add(
                "Tooth pain may indicate active decay."
            )
        }

        recommendations.add(
            "Visit your dentist every 6 months."
        )

        recommendations.add(
            "Drink fluoridated water regularly."
        )

        // SAVE
        val session = SessionManager(this)
        session.saveAssessmentScore(overall)

        val params = JSONObject().apply {

            put("user_id", session.getUserId())
            put("overall_score", overall)
            put("risk_level", risk)

            put("demographic_score", demographic)
            put("socioeconomic_score", socioeconomic)
            put("dietary_score", dietary)
            put("hygiene_score", hygiene)
            put("dental_history_score", dentalHistory)

            put("answers", JSONObject(answers.toMap()))

            put("result_type", "assessment")

            put(
                "recommendations",
                JSONArray(recommendations)
            )
        }

        ApiClient.post("save_result.php", params) { response ->

            runOnUiThread {

                val resultId = response
                    ?.optJSONObject("data")
                    ?.optInt("result_id") ?: 0

                val intent = Intent(
                    this,
                    ResultDetailActivity::class.java
                ).apply {

                    putExtra("result_id", resultId)
                    putExtra("score", overall)
                    putExtra("risk_level", risk)
                    putExtra("result_type", "assessment")

                    putExtra("demographic", demographic)
                    putExtra("socioeconomic", socioeconomic)
                    putExtra("dietary", dietary)
                    putExtra("hygiene", hygiene)
                    putExtra("dentalHistory", dentalHistory)

                    putStringArrayListExtra(
                        "recommendations",
                        ArrayList(recommendations)
                    )
                }

                startActivity(intent)
                finish()
            }
        }
    }
}