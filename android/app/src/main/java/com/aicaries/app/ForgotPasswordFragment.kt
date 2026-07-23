package com.aicaries.app

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import org.json.JSONObject

class ForgotPasswordFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val btnSend = view.findViewById<Button>(R.id.btnSendReset)
        val tvBack  = view.findViewById<TextView>(R.id.tvBack)

        tvBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnSend.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSend.isEnabled = false
            btnSend.text = "Sending..."

            val params = JSONObject()
            params.put("email", email)

            ApiClient.post("forgot_password.php", params) { response ->
                requireActivity().runOnUiThread {
                    btnSend.isEnabled = true
                    btnSend.text = "Send reset link"

                    val isSuccess = response != null && response.optBoolean("success", false)
                    val title = if (isSuccess) "Success" else "Error"
                    val message = if (isSuccess) {
                        "Reset link sent! Check your email."
                    } else {
                        response?.optString("message") ?: "Error occurred"
                    }

                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            if (isSuccess) {
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        }
                        .show()
                }
            }
        }
    }
}