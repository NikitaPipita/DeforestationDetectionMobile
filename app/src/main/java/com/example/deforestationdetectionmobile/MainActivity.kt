package com.example.deforestationdetectionmobile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.deforestationdetectionmobile.models.UserInfo
import com.example.deforestationdetectionmobile.presentation.iot.ActiveIotsList
import com.example.deforestationdetectionmobile.presentation.navigation.MainNavigationPage
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
    }

    fun login(view: View) {
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        Log.d("POST", "starting")
        val queue = Volley.newRequestQueue(this)
        val url = "https://deforestation-proj.herokuapp.com/sessions"
        val postData = JSONObject()
        postData.put("email", emailEditText.text.toString())
        postData.put("password", passwordEditText.text.toString())

        val jsonRequest = JsonObjectRequest(Request.Method.POST, url, postData,
            Response.Listener<JSONObject> {
                    response ->
                Log.d("OK", "login")
                UserInfo.id = response["user_id"] as Int
                UserInfo.email = response["email"] as String
                UserInfo.name = response["full_name"] as String
                UserInfo.role = response["user_role"] as String
                UserInfo.accessToken = response["access_token"] as String
                UserInfo.refreshToken = response["refresh_token"] as String
                enterFunctionality()
            },
            Response.ErrorListener {
                    error ->
                Log.d("Error", "error")
            }
        )

        queue.add(jsonRequest)
    }

    private fun enterFunctionality() {
        if (UserInfo.role != "locked") {
            if (UserInfo.role == "employee" || UserInfo.role == "observer") {
                val intent = Intent(this, ActiveIotsList::class.java)
                startActivity(intent)
            } else if (UserInfo.role == "admin" || UserInfo.role == "manager") {
                val intent = Intent(this, MainNavigationPage::class.java)
                startActivity(intent)
            }
        }
    }
}