package com.mathhelper.mathhelperserver.authorization

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtAuthEntryPoint : AuthenticationEntryPoint {
    @Throws(IOException::class, ServletException::class)
    override fun commence(request: HttpServletRequest?,
                          response: HttpServletResponse?,
                          authException: AuthenticationException?) {
        // This is invoked when user tries to access a secured REST resource without supplying any credentials
        // We should just send a 401 Unauthorized response because there is no 'login page' to redirect to
        // Here you can place any message you want
        logger.error("Unauthorized error. Message: {}", authException.toString())
        response?.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException?.message)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(JwtAuthEntryPoint::class.java)
    }
}