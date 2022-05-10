package com.mathhelper.mathhelperserver.controllers

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.mathhelper.mathhelperserver.authorization.JwtProvider
import com.mathhelper.mathhelperserver.authorization.JwtResponse
import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables.users.UserTypeRepository
import com.mathhelper.mathhelperserver.forms.authorization.GoogleSignInForm
import com.mathhelper.mathhelperserver.forms.authorization.LoginUserForm
import com.mathhelper.mathhelperserver.forms.authorization.SignUpUserForm
import com.mathhelper.mathhelperserver.services.user.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
class AuthController {
    @Autowired
    private lateinit var authenticationManager: AuthenticationManager
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder
    @Autowired
    private lateinit var jwtProvider: JwtProvider

    @Autowired
    private lateinit var userService: UserService
    @Autowired
    private lateinit var userTypeRepository: UserTypeRepository

    private val jacksonFactory = JacksonFactory()

    @RequestMapping("/signin", method = [RequestMethod.GET, RequestMethod.POST])
    fun singIn(
        @RequestParam(name = "login", required = false, defaultValue = "") loginOrEmail: String,
        @RequestParam(name = "password", required = false, defaultValue = "") password: String,
        @Valid @RequestBody loginRequest: LoginUserForm?
    ): ResponseEntity<Any> {
        var rPass = password
        var rLogin = loginOrEmail
        if (loginRequest != null) {
            rPass = loginRequest.password
            rLogin = loginRequest.loginOrEmail
        }

        logger.info("Sign In. Login or Email: {}. Password: {}",
            rLogin,
            passwordEncoder.encode(rPass))

        val user = userService.findByLoginOrEmail(rLogin)
        if (user == null) {
            logger.info("Sign in failed. No such user by login or email: {}", rLogin)
            return ResponseEntity("Fail. No such user.", HttpStatus.BAD_REQUEST)
        }

        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(user.login, rPass))
        SecurityContextHolder.getContext().authentication = authentication

        val jwt = jwtProvider.generateJwtToken((authentication.principal as UserDetails).username)

        if (user.isOauth) {
            user.password = userService.generateRandPassword()
            user.externalCode = ""
            userService.save(user)
        }

        return ResponseEntity.ok(JwtResponse(jwt, user))
    }

    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody signUpRequest: SignUpUserForm?): ResponseEntity<Any> {
        logger.info("Signing up new user started!!!")
        if (signUpRequest == null) {
            logger.error(Constants.EXCEPTION_INVALID_BODY_STRING)
            return ResponseEntity(Constants.EXCEPTION_INVALID_BODY_STRING, HttpStatus.BAD_REQUEST)
        }

        logger.info("Trying to sign up new user!")
        try {
            userService.signUp(signUpRequest)
        } catch (e: RuntimeException) {
            logger.error(e.message!!)
            return ResponseEntity(e.message!!, HttpStatus.BAD_REQUEST)
        }

        logger.info("Check that user was added to database")
        val loginOrEmail = signUpRequest.login ?: signUpRequest.email!!
        val user = userService.findByLoginOrEmail(loginOrEmail)
            ?: return ResponseEntity("Cannot register user by Login or Email $loginOrEmail", HttpStatus.BAD_REQUEST)

        logger.info("Register user. Request: $signUpRequest. User code: ${user.code}")

        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(user.login, signUpRequest.password))
        SecurityContextHolder.getContext().authentication = authentication

        val jwt = jwtProvider.generateJwtToken((authentication.principal as UserDetails).username)
        return ResponseEntity.ok(JwtResponse(jwt, user))
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/edit")
    fun edit(@Valid @RequestBody editRequest: SignUpUserForm?): ResponseEntity<Any> {
        if (editRequest == null) {
            logger.error(Constants.EXCEPTION_INVALID_BODY_STRING)
            return ResponseEntity(Constants.EXCEPTION_INVALID_BODY_STRING, HttpStatus.BAD_REQUEST)
        }

        val authorizedUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("No permission. Please, log in before editing.", HttpStatus.BAD_REQUEST)

        logger.info("Edit user. Request: $editRequest. User code: ${authorizedUser.code}")

        try {
            userService.edit(authorizedUser, editRequest)
        } catch (e: RuntimeException) {
            logger.error(e.message!!)
            return ResponseEntity(e.message!!, HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity.ok("User edited successfully.")
    }

    @RequestMapping("/google_sign_in", method = [RequestMethod.POST, RequestMethod.GET])
    fun googleSignIn(
        @RequestParam(name = "idTokenString", required = false) inputIdTokenString: String?,
        @Valid @RequestBody googleSignInRequest: GoogleSignInForm?
    ): ResponseEntity<Any> {
        val tokenVerifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), jacksonFactory)
            // Specify the CLIENT_ID of the app that accesses the google api backend:
            .setAudience(Arrays.asList("977771799310-42c14i973bbuo8nnquld6houe6mfa2t1.apps.googleusercontent.com")) // https://console.developers.google.com/apis/credentials/oauthclient/977771799310-42c14i973bbuo8nnquld6houe6mfa2t1.apps.googleusercontent.com?hl=ru&project=mathhelper-server
            .build()

        logger.info("[Google Sign In.] Request param. inputIdTokenString=$inputIdTokenString")
        logger.info("[Google Sign In.] Request body. googleSignInRequest.idTokenString=${googleSignInRequest?.idTokenString}")

        val idTokenString = inputIdTokenString ?: googleSignInRequest?.idTokenString ?:
            return ResponseEntity("No idTokenString in input request", HttpStatus.BAD_REQUEST)
        logger.info("Google Sign In. idTokenString=$idTokenString")
        val idToken = tokenVerifier.verify(idTokenString) ?: return ResponseEntity("Invalid idTokenString", HttpStatus.BAD_REQUEST)

        val payload = idToken.payload
        val userExternalCode = payload.subject
        logger.info("Google Sign In. User external code: $userExternalCode")

        val password = userService.generateRandPassword()
        val encodedPassword = passwordEncoder.encode(password)

        var user = userService.findByExternalCode(userExternalCode) ?: userService.findByLoginOrEmail(payload.email)
        if (user == null) {
            val login = payload.email.replace("@gmail.com", "")
            val name = payload["given_name"] as String
            val familyName = payload["family_name"] as String
            val fullName = if (familyName.isNotBlank()) "$familyName $name" else name

            logger.info("Goolge Sign In. Create new user: $login $name $familyName $fullName")

            user = User(
                code = "",
                userType = userTypeRepository.findByCode(Constants.DEFAULT_USER_TYPE_CODE)!!,
                login = login,
                email = payload.email,
                name = name,
                fullName = fullName,
                locale = "eng",
                password = encodedPassword,
                isOauth = true,
                externalCode = userExternalCode
            )

            userService.save(user)
            user = userService.findByExternalCode(userExternalCode)!!
        } else {
            user.password = encodedPassword
            userService.save(user)
        }

        logger.info("Google Sign In. New user login: ${user.login}")

        return try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(user.login, password)
            )

            SecurityContextHolder.getContext().authentication = authentication
            val jwt = jwtProvider.generateJwtToken((authentication.principal as UserDetails).username)
            ResponseEntity(JwtResponse(jwt, user), HttpStatus.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity(e.toString(), HttpStatus.BAD_REQUEST)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("logic-logs")
    }
}