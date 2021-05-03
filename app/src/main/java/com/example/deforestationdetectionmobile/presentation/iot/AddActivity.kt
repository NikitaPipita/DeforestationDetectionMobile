package com.example.deforestationdetectionmobile.presentation.iot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.deforestationdetectionmobile.R
import com.example.deforestationdetectionmobile.models.UserInfo
import org.json.JSONObject

class AddActivity : AppCompatActivity() {
    private lateinit var addGroupButton: Button
    private lateinit var addIotButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        addGroupButton = findViewById(R.id.addGroupButton)
        addIotButton = findViewById(R.id.addIotButton)
    }

    fun onAddGroup(view: View) {
        addGroup(true)
    }

    fun onAddIot(view: View) {
        val intent = Intent(this, AddIot::class.java)
        startActivity(intent)
    }

    private fun addGroup(acceptRefresh: Boolean) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://deforestation-proj.herokuapp.com/groups/create"
        val postData = JSONObject()
        postData.put("user_id", UserInfo.id)

        val jsonRequest = object : JsonObjectRequest(
            Request.Method.POST, url, postData,
            Response.Listener<JSONObject> { response ->
                Toast.makeText(applicationContext, "Group is added", Toast.LENGTH_LONG).show()
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer " + UserInfo.accessToken
                return headers
            }

            override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject>? {
                val mStatusCode = response.statusCode
                if (mStatusCode in 400..499 && acceptRefresh) {
                    refreshToken()
                    addGroup(false)
                }
                return super.parseNetworkResponse(response)
            }
        }

        queue.add(jsonRequest)
    }

    private fun refreshToken() {
        val url = "https://deforestation-proj.herokuapp.com/refresh"
        val postData = JSONObject()
        postData.put("token", UserInfo.refreshToken)

        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST,
            url,
            postData,
            Response.Listener<JSONObject> { response ->
                UserInfo.accessToken = response["access_token"] as String
                UserInfo.refreshToken = response["refresh_token"] as String
            },
            Response.ErrorListener { error -> error.printStackTrace() }
        ) {}

        requestQueue.add(jsonObjectRequest)
    }

}