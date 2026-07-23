package com.aicaries.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.json.JSONObject

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        val session =
            SessionManager(requireContext())

        val firstName =
            session.getName()
                .split(" ")
                .firstOrNull() ?: "User"

        // USERNAME

        view.findViewById<TextView>(
            R.id.tvUsername
        ).text = "$firstName 👋"

        // LAST RESULT

        val params = JSONObject()

        params.put(
            "user_id",
            session.getUserId()
        )

        ApiClient.post(
            "get_results.php",
            params
        ) { response ->

            requireActivity().runOnUiThread {

                if (
                    response != null &&
                    response.getBoolean("success")
                ) {

                    val data =
                        response.getJSONArray("data")

                    if (data.length() > 0) {

                        val latest =
                            data.getJSONObject(0)

                        val score =
                            latest.getInt("overall_score")

                        val risk =
                            latest.getString("risk_level")

                        view.findViewById<TextView>(
                            R.id.tvLastScore
                        ).text = "$score%"

                        view.findViewById<TextView>(
                            R.id.tvLastRisk
                        ).text = risk
                    }
                }
            }
        }

        // START NOW BUTTON

        view.findViewById<Button>(
            R.id.btnStartNow
        ).setOnClickListener {

            findNavController().navigate(
                R.id.scanFragment
            )
        }

        // ASSESSMENT

        view.findViewById<CardView>(
            R.id.cardAssessment
        ).setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    AssessmentActivity::class.java
                )
            )
        }

        // AI SCAN

        view.findViewById<CardView>(
            R.id.cardAIScan
        ).setOnClickListener {

            findNavController().navigate(
                R.id.scanFragment
            )
        }

        // HISTORY

        view.findViewById<CardView>(
            R.id.cardHistory
        ).setOnClickListener {

            findNavController().navigate(
                R.id.historyFragment
            )
        }

        // TIPS

        view.findViewById<CardView>(
            R.id.cardTips
        ).setOnClickListener {

            findNavController().navigate(
                R.id.moreFragment
            )
        }

        // FIND DENTIST

        val btnFindDentist =
            view.findViewById<Button>(
                R.id.btnFindDentist
            )

        btnFindDentist.setOnClickListener {

            val gmmIntentUri =
                Uri.parse(
                    "geo:0,0?q=dentist"
                )

            val mapIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    gmmIntentUri
                )

            mapIntent.setPackage(
                "com.google.android.apps.maps"
            )

            startActivity(mapIntent)
        }
    }
}