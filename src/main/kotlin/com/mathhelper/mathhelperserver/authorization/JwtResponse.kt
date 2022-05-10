package com.mathhelper.mathhelperserver.authorization

import com.mathhelper.mathhelperserver.datatables.users.User

class JwtResponse(val accessToken: String, user: User) {
    val token = accessToken
    val tokenType = "Bearer"
    val login = user.login
    val name = user.name
    val fullName = user.fullName
    val additional = user.additional
}