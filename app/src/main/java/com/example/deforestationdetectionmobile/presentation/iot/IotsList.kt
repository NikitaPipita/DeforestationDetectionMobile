package com.example.deforestationdetectionmobile.presentation.iot

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.deforestationdetectionmobile.R
import com.example.deforestationdetectionmobile.models.Group
import com.example.deforestationdetectionmobile.models.Iot
import com.example.deforestationdetectionmobile.models.UserInfo
import org.json.JSONArray
import org.json.JSONException

class IotsList : AppCompatActivity() {

    private lateinit var listView: ListView

    private var iots: MutableList<Iot> = ArrayList()
    private var groups: MutableList<Group> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iots_list)

        listView = findViewById(R.id.iots_list)
    }

    override fun onResume() {
        super.onResume()
        getIots()
    }

    private fun getIots() {
        val url = "https://deforestation-proj.herokuapp.com/iots"

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
            Response.ErrorListener { error -> error.printStackTrace() }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer " + UserInfo.accessToken
                return headers
            }
        }

        requestQueue.add(jsonObjectRequest)
    }

    private fun updateIotsList() {
        val iotsInfo: MutableList<HashMap<String, String>> = ArrayList()
        for (iot in iots) {
            val map: HashMap<String, String> = HashMap()
            map["position"] = "Longitude: " + iot.longitude.toString() + " Latitude: " + iot.latitude.toString()
            map["state"] = iot.state
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
}