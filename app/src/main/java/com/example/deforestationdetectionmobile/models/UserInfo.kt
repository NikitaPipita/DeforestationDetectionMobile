package com.example.deforestationdetectionmobile.models

import kotlin.properties.Delegates

class UserInfo {
    companion object {
        var id by Delegates.notNull<Int>()
        var email by Delegates.notNull<String>()
        var name by Delegates.notNull<String>()
        var role by Delegates.notNull<String>()
        var accessToken by Delegates.notNull<String>()
        var refreshToken by Delegates.notNull<String>()
    }
}