package com.aicaries.app

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ScanFragment : Fragment() {

    private var photoUri: Uri? = null
    private var selectedUri: Uri? = null
    private lateinit var ivPreview: ImageView
    private lateinit var btnAnalyse: Button

    private val SCAN_URL = "http://10.219.5.63/aicaries/api/scan_analysis.php"
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            showPreview(it)
        }
    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            photoUri?.let {
                selectedUri = it
                showPreview(it)
            }
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted)
            openCamera()
        else
            Toast.makeText(
                requireContext(),
                "Camera permission required.",
                Toast.LENGTH_SHORT
            ).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_scan, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivPreview = view.findViewById(R.id.ivPreview)
        btnAnalyse = view.findViewById(R.id.btnAnalyse)

        view.findViewById<CardView>(R.id.btnBack)
            .setOnClickListener {
                requireActivity()
                    .onBackPressedDispatcher
                    .onBackPressed()
            }

        view.findViewById<CardView>(R.id.btnCamera)
            .setOnClickListener {
                checkCameraAndOpen()
            }

        view.findViewById<CardView>(R.id.btnGallery)
            .setOnClickListener {
                pickImage.launch("image/*")
            }

        view.findViewById<CardView>(R.id.cardImageArea)
            .setOnClickListener {
                pickImage.launch("image/*")
            }

        btnAnalyse.setOnClickListener {

            if (selectedUri != null)
                analyseImage(selectedUri!!)
            else
                Toast.makeText(
                    requireContext(),
                    "Please select a photo first!",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    private fun checkCameraAndOpen() {

        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            requestCameraPermission.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    private fun openCamera() {

        val photoFile = createImageFile()

        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )

        takePicture.launch(photoUri)
    }

    private fun createImageFile(): File {

        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())

        val storageDir = requireContext()
            .getExternalFilesDir(
                Environment.DIRECTORY_PICTURES
            )

        return File.createTempFile(
            "DENTAL_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun showPreview(uri: Uri) {

        view?.apply {

            ivPreview.setImageURI(uri)

            ivPreview.visibility = View.VISIBLE

            findViewById<LinearLayout>(
                R.id.layoutPlaceholder
            ).visibility = View.GONE

            try {
                findViewById<TextView>(
                    R.id.tvChangeBadge
                ).visibility = View.VISIBLE
            } catch (e: Exception) {
            }

            btnAnalyse.isEnabled = true
            btnAnalyse.alpha = 1.0f
        }
    }

    private fun analyseImage(imageUri: Uri) {

        val progress = ProgressDialog(requireContext())

        progress.setMessage(
            "🔍 Analysing your teeth with AI...\nPlease wait 10-20 seconds..."
        )

        progress.setCancelable(false)

        progress.show()

        val session = SessionManager(requireContext())

        try {

            val inputStream =
                requireContext()
                    .contentResolver
                    .openInputStream(imageUri)

            val file = File(
                requireContext().cacheDir,
                "scan_image.jpg"
            )

            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)

            inputStream?.close()

            outputStream.close()

            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            val requestBody =
                MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "user_id",
                        session.getUserId().toString()
                    )
                    .addFormDataPart(
                        "image",
                        "scan.jpg",
                        RequestBody.create(
                            "image/jpeg".toMediaType(),
                            file
                        )
                    )
                    .build()

            val request = Request.Builder()
                .url(SCAN_URL)
                .post(requestBody)
                .build()

            client.newCall(request)
                .enqueue(object : Callback {

                    override fun onFailure(
                        call: Call,
                        e: java.io.IOException
                    ) {

                        requireActivity().runOnUiThread {

                            progress.dismiss()

                            Toast.makeText(
                                requireContext(),
                                "❌ Connection failed!\nCheck:\n1. XAMPP running\n2. Flask API running\n3. Same WiFi\nError: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onResponse(
                        call: Call,
                        response: Response
                    ) {

                        val responseBody =
                            response.body?.string()

                        requireActivity().runOnUiThread {

                            progress.dismiss()

                            try {

                                val json =
                                    JSONObject(responseBody ?: "")

                                if (json.getBoolean("success")) {

                                    val data =
                                        json.getJSONObject("data")

                                    // AI score
                                    val aiScore =
                                        data.getInt("overall_score")

                                    // Assessment score
                                    val assessmentScore =
                                        session.getAssessmentScore()

                                    // Final combined score
                                    val finalScore =
                                        (
                                                (assessmentScore * 0.4f)
                                                        +
                                                        (aiScore * 0.6f)
                                                ).toInt()

                                    // Final risk
                                    val finalRisk = when {

                                        finalScore >= 70 ->
                                            "High"

                                        finalScore >= 40 ->
                                            "Moderate"

                                        else ->
                                            "Low"
                                    }

                                    val intent = Intent(
                                        requireContext(),
                                        ResultDetailActivity::class.java
                                    )

                                    intent.putExtra(
                                        "result_id",
                                        data.getInt("result_id")
                                    )

                                    intent.putExtra(
                                        "score",
                                        finalScore
                                    )

                                    intent.putExtra(
                                        "risk_level",
                                        finalRisk
                                    )

                                    intent.putExtra(
                                        "result_type",
                                        "final"
                                    )

                                    intent.putExtra(
                                        "confidence",
                                        data.optDouble(
                                            "confidence",
                                            0.0
                                        ).toFloat()
                                    )

                                    intent.putExtra(
                                        "prediction",
                                        data.optString(
                                            "prediction",
                                            ""
                                        )
                                    )

                                    intent.putExtra(
                                        "assessment_score",
                                        assessmentScore
                                    )

                                    intent.putExtra(
                                        "ai_score",
                                        aiScore
                                    )

                                    // Breakdown rows
                                    intent.putExtra(
                                        "demographic",
                                        aiScore
                                    )

                                    intent.putExtra(
                                        "socioeconomic",
                                        assessmentScore
                                    )

                                    intent.putExtra(
                                        "dietary",
                                        finalScore
                                    )

                                    intent.putExtra(
                                        "hygiene",
                                        data.optDouble(
                                            "confidence",
                                            0.0
                                        ).toInt()
                                    )

                                    intent.putExtra(
                                        "dentalHistory",
                                        100
                                    )

                                    val recs =
                                        data.getJSONArray(
                                            "recommendations"
                                        )

                                    val recList =
                                        ArrayList<String>()

                                    for (i in 0 until recs.length()) {

                                        recList.add(
                                            recs.getString(i)
                                        )
                                    }

                                    intent.putStringArrayListExtra(
                                        "recommendations",
                                        recList
                                    )

                                    startActivity(intent)

                                } else {

                                    Toast.makeText(
                                        requireContext(),
                                        "❌ ${json.getString("message")}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            } catch (e: Exception) {

                                Toast.makeText(
                                    requireContext(),
                                    "Error: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                })

        } catch (e: Exception) {

            progress.dismiss()

            Toast.makeText(
                requireContext(),
                "Error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}