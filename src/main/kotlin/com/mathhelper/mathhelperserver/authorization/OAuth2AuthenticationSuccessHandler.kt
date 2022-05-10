package com.mathhelper.mathhelperserver.authorization

import com.mathhelper.mathhelperserver.services.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.net.URLEncoder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class OAuth2AuthenticationSuccessHandler : AuthenticationSuccessHandler {
    @Autowired
    private lateinit var userService: UserService

    override fun onAuthenticationSuccess(p0: HttpServletRequest?, p1: HttpServletResponse?, p2: Authentication?) {
        val signInUrl = "/api/auth/signin?login=%s&password=%s"  // TODO: check safety, may be problems while sending the string with open password to user device
        val user = userService.findByLogin(p2!!.name)
        val cred = URLEncoder.encode(user!!.externalCode, "UTF-8")
        val name = URLEncoder.encode(user.email, "UTF-8")

        p1!!.sendRedirect(signInUrl.format(name, cred))
    }
}