package com.example.deforestationdetectionmobile.presentation.iot

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.deforestationdetectionmobile.R
import com.example.deforestationdetectionmobile.models.Iot
import com.example.deforestationdetectionmobile.models.UserInfo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ActiveIotsList : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var addFab: FloatingActionButton

    private var iots: MutableList<Iot> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iots_list)

        listView = findViewById(R.id.active_iots_list)
        addFab = findViewById(R.id.add_fab)

        if (UserInfo.role == "observer") {
            addFab.visibility = FloatingActionButton.GONE
        }

        listView.onItemClickListener =
            AdapterView.OnItemClickListener {
                    parent, view, position, id ->
                val uri: String =
                    java.lang.String.format(
                        Locale.ENGLISH,
                        "geo:%f,%f?z=%d&q=%f,%f (%s)",
                        iots[id.toInt()].latitude,
                        iots[id.toInt()].longitude,
                        10,
                        iots[id.toInt()].latitude,
                        iots[id.toInt()].longitude,
                        "coordinates"
                    )
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(intent)
            }
    }

    override fun onResume() {
        super.onResume()
        getIots(true)
    }

    private fun getIots(acceptRefresh: Boolean) {
        val url = "https://deforestation-proj.herokuapp.com/iot/signal"

        val jsonResponses: MutableList<Iot> = ArrayList()

        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = object : StringRequest(
            Request.Method.GET,
            url,
            Response.Listener<String> { response ->
                try {
                    val jsonArray = JSONArray(response)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        jsonResponses.add(Iot(jsonObject))
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                iots = jsonResponses
                updateIotsList()
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
                    getIots(false)
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
        ){}

        requestQueue.add(jsonObjectRequest)
    }

    private fun updateIotsList() {
        val iotsInfo: MutableList<HashMap<String, String>> = ArrayList()
        for (iot in iots) {
            val map: HashMap<String, String> = HashMap()
            map["position"] = getString(R.string.longitude) + ": " + iot.longitude.toString() + " " +
                    getString(R.string.latitude) + ": " + iot.latitude.toString()
            if (iot.state == "nothing") {
                map["state"] = getString(R.string.nothing)
            } else if (iot.state == "active"){
                map["state"] = getString(R.string.active)
            } else if (iot.state == "lost"){
                map["state"] = getString(R.string.lost)
            }
            iotsInfo.add(map)
        }
        val adapter = SimpleAdapter(
            this,
            iotsInfo,
            android.R.layout.simple_list_item_2,
            arrayOf("position", "state"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        listView.adapter = adapter
    }

    fun addNew(view: View) {
        val intent = Intent(this, AddActivity::class.java)
        startActivity(intent)
    }
}