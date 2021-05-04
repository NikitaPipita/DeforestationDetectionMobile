package com.example.deforestationdetectionmobile.presentation.iot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.deforestationdetectionmobile.R
import com.example.deforestationdetectionmobile.models.UserInfo
import org.json.JSONObject
import kotlin.properties.Delegates

class ChangeIotState : AppCompatActivity() {
    var iotId by Delegates.notNull<Int>()

    private lateinit var stateSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_iot_state)

        stateSpinner = findViewById(R.id.stateSpinner)

        iotId = intent.getSerializableExtra("iot_id") as Int

        setStatesMenu()
    }

    fun changeState(view: View) {
        changeIotState(true)
    }

    private fun changeIotState(acceptRefresh: Boolean) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://deforestation-proj.herokuapp.com/iot/state"
        val postData = JSONObject()
        postData.put("iot_id", iotId)
        postData.put("iot_state", stateSpinner.selectedItem.toString())


        val jsonRequest = object : JsonObjectRequest(
            Request.Method.PUT, url, postData,
            Response.Listener<JSONObject> { response ->
                finish()
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
                    changeIotState(false)
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

    private fun setStatesMenu() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, listOf("nothing", "active", "lost")
        )
        stateSpinner.adapter = adapter
        stateSpinner.setSelection(0)
    }
}