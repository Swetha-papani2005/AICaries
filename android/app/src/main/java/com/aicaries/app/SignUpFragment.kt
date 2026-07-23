package com.aicaries.app

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.json.JSONObject

class SignUpFragment : Fragment() {

    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_UP_GOOGLE = 2001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<EditText>(R.id.etName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)

        val btnCreate = view.findViewById<Button>(R.id.btnCreateAccount)
        val btnGoogle = view.findViewById<LinearLayout>(R.id.btnGoogle)

        val tvSignIn = view.findViewById<TextView>(R.id.tvSignIn)

        // GOOGLE SIGN IN CONFIG

        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestEmail()
            .build()

        googleSignInClient =
            GoogleSignIn.getClient(requireActivity(), gso)

        // NORMAL SIGN UP

        btnCreate.setOnClickListener {

            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    requireContext(),
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val progress = ProgressDialog(requireContext())
            progress.setMessage("Creating account...")
            progress.show()

            val params = JSONObject()

            params.put("name", name)
            params.put("email", email)
            params.put("password", password)

            ApiClient.post("register.php", params) { response ->

                requireActivity().runOnUiThread {

                    progress.dismiss()

                    if (response == null) {

                        Toast.makeText(
                            requireContext(),
                            "Cannot connect to server",
                            Toast.LENGTH_LONG
                        ).show()

                        return@runOnUiThread
                    }

                    if (response.getBoolean("success")) {

                        val data = response.getJSONObject("data")

                        val session = SessionManager(requireContext())

                        session.saveUser(
                            data.getInt("user_id"),
                            data.getString("name"),
                            data.getString("email"),
                            data.getString("token")
                        )

                        startActivity(
                            Intent(
                                requireContext(),
                                LoadingActivity::class.java
                            )
                        )

                        requireActivity().finish()

                    } else {

                        Toast.makeText(
                            requireContext(),
                            response.getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        // GOOGLE BUTTON

        btnGoogle.setOnClickListener {

            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_UP_GOOGLE)
        }

        // NAVIGATION

        tvSignIn.setOnClickListener {

            findNavController().navigate(
                R.id.action_signup_to_signin
            )
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_UP_GOOGLE) {

            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)

            try {

                val account =
                    task.getResult(ApiException::class.java)

                val name =
                    account.displayName ?: "Google User"

                val email =
                    account.email ?: ""

                val session =
                    SessionManager(requireContext())

                session.saveUser(
                    999,
                    name,
                    email,
                    "google_login"
                )

                Toast.makeText(
                    requireContext(),
                    "Google Sign Up Success",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(
                    Intent(
                        requireContext(),
                        LoadingActivity::class.java
                    )
                )

                requireActivity().finish()

            } catch (e: Exception) {

                Toast.makeText(
                    requireContext(),
                    "Google Sign Up Failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}