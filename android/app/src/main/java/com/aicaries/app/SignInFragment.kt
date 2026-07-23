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
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import org.json.JSONObject

class SignInFragment : Fragment() {

    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_signin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnSignIn = view.findViewById<Button>(R.id.btnSignIn)
        val btnGoogle = view.findViewById<LinearLayout>(R.id.btnGoogle)
        val tvForgot = view.findViewById<TextView>(R.id.tvForgotPassword)
        val tvCreate = view.findViewById<TextView>(R.id.tvCreateAccount)

        // GOOGLE SIGN IN CONFIG
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient =
            GoogleSignIn.getClient(requireActivity(), gso)

        // NORMAL LOGIN
        btnSignIn.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val progress = ProgressDialog(requireContext())
            progress.setMessage("Signing in...")
            progress.show()

            val params = JSONObject()
            params.put("email", email)
            params.put("password", password)

            ApiClient.post("login.php", params) { response ->

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
                            Intent(requireContext(), LoadingActivity::class.java)
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

        // GOOGLE LOGIN
        btnGoogle.setOnClickListener {

            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        tvForgot.setOnClickListener {
            findNavController().navigate(R.id.action_signin_to_forgot)
        }

        tvCreate.setOnClickListener {
            findNavController().navigate(R.id.action_signin_to_signup)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {

                val account = task.getResult(ApiException::class.java)

                val name = account.displayName ?: "Google User"
                val email = account.email ?: ""

                val session = SessionManager(requireContext())

                session.saveUser(
                    999,
                    name,
                    email,
                    "google_login"
                )

                Toast.makeText(
                    requireContext(),
                    "Google Sign-In Success",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(
                    Intent(requireContext(), LoadingActivity::class.java)
                )

                requireActivity().finish()

            } catch (e: ApiException) {

                Toast.makeText(
                    requireContext(),
                    "Google Sign-In Failed",
                    Toast.LENGTH_LONG
                ).show()

                e.printStackTrace()
            }
        }
    }
}