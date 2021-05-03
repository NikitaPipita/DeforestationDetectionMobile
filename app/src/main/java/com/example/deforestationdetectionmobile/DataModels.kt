package com.example.deforestationdetectionmobile

import org.json.JSONObject
import kotlin.properties.Delegates

class Iot() {
    var id: Int? = null
    var userId: Int? = null
    var groupId: Int? = null
    var longitude by Delegates.notNull<Double>()
    var latitude by Delegates.notNull<Double>()
    var state by Delegates.notNull<String>()
    var type: String? = null

    constructor(jsonObject: JSONObject) : this() {
        id = jsonObject.getInt("iot_id")
        groupId = jsonObject.getJSONObject("group").getInt("group_id")
        longitude = jsonObject.getDouble("longitude")
        latitude = jsonObject.getDouble("latitude")
        state = jsonObject.getString("iot_state")
        type = jsonObject.getString("iot_type")
    }
}

class Group() {
    var id by Delegates.notNull<Int>()

    constructor(jsonObject: JSONObject) : this() {
        id = jsonObject.getInt("group_id")
    }
}

class SuitablePosition() {
    var isSuitable by Delegates.notNull<Boolean>()
    var minimumDistanceToMoveAway by Delegates.notNull<Double>()

    constructor(jsonObject: JSONObject) : this() {
        isSuitable = jsonObject.getBoolean("suitable")
        minimumDistanceToMoveAway = jsonObject.getDouble("minimum_distance_to_move_away")
    }
}