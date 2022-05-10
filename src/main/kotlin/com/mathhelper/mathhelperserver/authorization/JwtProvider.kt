package com.mathhelper.mathhelperserver.authorization

import com.mathhelper.mathhelperserver.constants.Constants
import io.jsonwebtoken.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException
import java.util.*

@Component
class JwtProvider {
    fun generateJwtToken(username: String): String {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + Constants.JWT_EXPIRATION_TIME_MS))
            .signWith(SignatureAlgorithm.HS512, Constants.JWT_SECRET_KEY)
            .compact()
    }

    fun getUserNameFromJwtToken(token: String): String {
        return Jwts.parser().setSigningKey(Constants.JWT_SECRET_KEY).parseClaimsJws(token).body.subject
    }

    fun validateJwtToken(token: String): Boolean {
        try {
            Jwts.parser().setSigningKey(Constants.JWT_SECRET_KEY).parseClaimsJws(token)
            return true
        } catch (e: SignatureException) {
            logger.error("Invalid JWT signature. Message: {}", e.toString())
        } catch (e: MalformedJwtException) {
            logger.error("Invalid JWT signature. Message: {}", e.toString())
        } catch (e: ExpiredJwtException) {
            logger.error("Expired JWT signature. Message: {}", e.toString())
        } catch (e: UnsupportedJwtException) {
            logger.error("Unsupported JWT signature. Message: {}", e.toString())
        } catch (e: IllegalArgumentException) {
            logger.error("JWT claims string is empty. Message: {}", e.toString())
        }
        return false
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(JwtProvider::class.java)
    }
}