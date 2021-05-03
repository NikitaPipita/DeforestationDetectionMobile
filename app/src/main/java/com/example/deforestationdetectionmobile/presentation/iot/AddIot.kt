package com.example.deforestationdetectionmobile.presentation.iot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.deforestationdetectionmobile.R
import com.example.deforestationdetectionmobile.models.Group
import com.example.deforestationdetectionmobile.models.UserInfo
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class AddIot : AppCompatActivity() {
    private var groups: MutableList<Group> = ArrayList()

    private lateinit var longitude: EditText
    private lateinit var latitude: EditText
    private lateinit var groupSpinner: Spinner
    private lateinit var groupLabel: TextView
    private lateinit var typeSpinner: Spinner
    private lateinit var addButton: Button
    private lateinit var addAnywayButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_iot)

        longitude = findViewById(R.id.longitudeEditText)
        latitude = findViewById(R.id.latitudeEditText)
        groupSpinner = findViewById(R.id.groupSpinner)
        groupLabel = findViewById(R.id.groupText)
        typeSpinner = findViewById(R.id.typeSpinner)
        addButton = findViewById(R.id.addIotButton)
        addAnywayButton = findViewById(R.id.addIotAnywayButton)

        setTypesMenu()
    }

    override fun onResume() {
        super.onResume()
        getGroups(true)
    }

    fun onCheckPosition(view: View) {
        addAnywayButton.visibility = Button.GONE
        checkIsIotSuitable(true)
    }

    fun onAddIot(view: View) {
        addAnywayButton.visibility = Button.GONE
        addIot(true)
    }

    private fun checkIsIotSuitable(acceptRefresh: Boolean) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://deforestation-proj.herokuapp.com/iot/check"
        val postData = JSONObject()
        postData.put("group_id", groupSpinner.selectedItem.toString().toInt())
        postData.put("longitude", longitude.text.toString().toDouble())
        postData.put("latitude", latitude.text.toString().toDouble())
        postData.put("iot_type", typeSpinner.selectedItem.toString())


        val jsonRequest = object : JsonObjectRequest(
            Request.Method.POST, url, postData,
            Response.Listener<JSONObject> { response ->
                val isSuitable = response["suitable"] as Boolean
                if (!isSuitable) {
                    val minimumDistanceToMoveAway =
                        response["minimum_distance_to_move_away"] as Double
                    Toast.makeText(
                        applicationContext,
                        "Failed! Step back at least $minimumDistanceToMoveAway meters",
                        Toast.LENGTH_LONG
                    ).show()
                }
                addAnywayButton.visibility = Button.VISIBLE
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
                    checkIsIotSuitable(false)
                }
                return super.parseNetworkResponse(response)
            }
        }

        queue.add(jsonRequest)
    }

    private fun addIot(acceptRefresh: Boolean) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://deforestation-proj.herokuapp.com/iots/create"
        val postData = JSONObject()
        postData.put("user_id", UserInfo.id)
        postData.put("group_id", groupSpinner.selectedItem.toString().toInt())
        postData.put("longitude", longitude.text.toString().toDouble())
        postData.put("latitude", latitude.text.toString().toDouble())
        postData.put("iot_state", "nothing")
        postData.put("iot_type", typeSpinner.selectedItem.toString())


        val jsonRequest = object : JsonObjectRequest(
            Request.Method.POST, url, postData,
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
                    addIot(false)
                }
                return super.parseNetworkResponse(response)
            }
        }

        queue.add(jsonRequest)
    }

    private fun getGroups(acceptRefresh: Boolean) {
        val url = "https://deforestation-proj.herokuapp.com/groups"

        val jsonResponses: MutableList<Group> = ArrayList()

        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = object : StringRequest(
            Request.Method.GET,
            url,
            Response.Listener<String> { response ->
                try {
                    val jsonArray = JSONArray(response)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        jsonResponses.add(Group(jsonObject))
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                groups = jsonResponses
                updateGroupsMenu()
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

            override fun parseNetworkResponse(response: NetworkResponse): Response<String>? {
                val mStatusCode = response.statusCode
                if (mStatusCode in 400..499 && acceptRefresh) {
                    refreshToken()
                    getGroups(false)
                }
                return super.parseNetworkResponse(response)
            }
        }

        requestQueue.add(jsonObjectRequest)
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

    private fun updateGroupsMenu() {
        val groupsInfo: MutableList<Int> = ArrayList()
        for (group in groups) {
            groupsInfo.add(group.id)
        }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, groupsInfo
        )
        groupSpinner.adapter = adapter
        if (groupsInfo.isNotEmpty()) {
            groupSpinner.setSelection(0)
        }
    }

    private fun setTypesMenu() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, listOf("gyroscope", "microphone")
        )
        typeSpinner.adapter = adapter
        typeSpinner.setSelection(0)
    }
}