package com.aicaries.app

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import org.json.JSONObject

class ProfileFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionManager(requireContext())
        val tvInitial = view.findViewById<TextView>(R.id.tvInitial)
        val tvEmail   = view.findViewById<TextView>(R.id.tvEmail)
        val etName    = view.findViewById<EditText>(R.id.etName)
        val etPhone   = view.findViewById<EditText>(R.id.etPhone)

        tvEmail.text = session.getEmail()
        etName.setText(session.getName())
        tvInitial.text = session.getName().firstOrNull()?.uppercase() ?: "U"

        view.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val params = JSONObject()
            params.put("user_id", session.getUserId())
            params.put("name",    etName.text.toString())
            params.put("phone",   etPhone.text.toString())
            params.put("language", session.getLanguage())

            ApiClient.post("update_profile.php", params) { response ->
                requireActivity().runOnUiThread {
                    if (response != null && response.getBoolean("success")) {
                        Toast.makeText(requireContext(), "Profile saved!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        view.findViewById<CardView>(R.id.cardLanguage).setOnClickListener {
            startActivity(Intent(requireContext(), LanguageActivity::class.java))
        }

        view.findViewById<CardView>(R.id.cardSignOut).setOnClickListener {
            session.logout()
            startActivity(Intent(requireContext(), SplashActivity::class.java))
            requireActivity().finish()
        }
    }
}