package com.mathhelper.mathhelperserver.api_requests.authorization

import com.mathhelper.mathhelperserver.services.user.UserService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

private class KPostgreSQLContainer(image: String): PostgreSQLContainer<KPostgreSQLContainer>(image)

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AuthenticationTests {
    companion object {
        @Value("#{systemProperties['tests.databasename']}")
        private val dbName: String = ""

        @Container
        private val container = KPostgreSQLContainer("postgres:12").apply {
            withDatabaseName(dbName)
            withUsername(username)
            withPassword(password)
        }
    }

    @Autowired
    private lateinit var mvc: MockMvc
    @Autowired
    private lateinit var userService: UserService
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private fun getSignUpRequestString(login: String, email: String): String {
        return """
            {
                "login": "$login",
                "email": "$email",
                "password": "test_pass",
                "name": "test_user_name",
                "fullName": "test_user_full_name",
                "locale": "rus"
            }
        """.trimIndent()
    }

    private fun getSignInRequestString(email: String): String {
        return """
            {
                "loginOrEmail": "$email",
                "password": "test_pass"
            }
        """.trimIndent()
    }

    @Test
    @DisplayName("Signing up new user test")
    fun testSignUpNewUser() {
        val userLogin = "user1"
        val userEmail = "email1@gmail.com"

        // Perform the request and check response status
        mvc.perform(
                MockMvcRequestBuilders.post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(getSignUpRequestString(login = userLogin, email = userEmail))
            )
            .andExpect(status().isOk)

        // Check that user was added
        assertNotEquals(userService.findAll().count(), 0)
        assertTrue(userService.existsByLogin(userLogin))

        val user = userService.findByLogin(userLogin)!!

        assertEquals(user.email, userEmail)
        assertEquals(user.name, "test_user_name")
        assertEquals(user.fullName, "test_user_full_name")
        assertEquals(user.locale, "rus")
    }

    @Test
    @DisplayName("Signing up an existed user test")
    fun testSignUpExistedUser() {
        val userLogin = "user2"
        val userEmail = "email2@gmail.com"

        // Perform the request and check response status
        mvc.perform(
                MockMvcRequestBuilders.post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(getSignUpRequestString(login = userLogin, email = userEmail))
            )
            .andExpect(status().isOk)

        // Perform second sign up request. It should failed
        mvc.perform(
                MockMvcRequestBuilders.post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(getSignUpRequestString(login = userLogin, email = userEmail))
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("Validating email in request form test")
    fun testEmailValidation() {
        val login = "user3"
        val validEmail = "email3@gmail.com"
        val invalidEmail = "email"

        // This should fail
        mvc.perform(
                MockMvcRequestBuilders.post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(getSignUpRequestString(login = login, email = invalidEmail))
            )
            .andExpect(status().isBadRequest)

        // This should succeeded
        mvc.perform(
                MockMvcRequestBuilders.post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(getSignUpRequestString(login = login, email = validEmail))
            )
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("Signing in new registered user test")
    fun testSignInNewUser() {
        val login = "user4"
        val email = "email4@gmail.com"

        // Firstly - register new user
        mvc.perform(
                MockMvcRequestBuilders.post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(getSignUpRequestString(login = login, email = email))
            )
            .andExpect(status().isOk)

        // Secondly - try to sign in using `login` and `email`
        val signInResult = mvc.perform(
                MockMvcRequestBuilders.post("/api/auth/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(getSignInRequestString(email = email))
            )
            .andExpect(status().isOk)
            .andDo(MockMvcResultHandlers.print())
            .andReturn()
    }

    @Test
    @DisplayName("Check required fields for `signup` request")
    fun testSignUpRequiredFields() {
        val login = "user5"
        val email = "email5@gmail.com"
        val password = "test_pass"
        val locale = "rus"

        // Check required fields (login or email, locale and password)

        // 1. login or email
        var requestContent = """
            {
                "password": "$password",
                "locale": "$locale"
            }
        """.trimIndent()
        mvc.perform(
                MockMvcRequestBuilders.post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestContent)
            )
            .andExpect(status().isBadRequest)
            .andDo(MockMvcResultHandlers.print())

        // 2. password
        requestContent = """
            {
                "login": "$login",
                "locale": "$locale"
            }
        """.trimIndent()
        mvc.perform(
                MockMvcRequestBuilders.post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestContent)
            )
            .andExpect(status().isBadRequest)
            .andDo(MockMvcResultHandlers.print())

        // 3. locale
        requestContent = """
            {
                "login": "$login",
                "password": "$password"
            }
        """.trimIndent()
        mvc.perform(
            MockMvcRequestBuilders.post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent)
            )
            .andExpect(status().isBadRequest)
            .andDo(MockMvcResultHandlers.print())

        // 4. success request with login or email

        // With just login
        requestContent = """
            {
                "login": "$login",
                "password": "$password",
                "locale": "$locale"
            }
        """.trimIndent()
        mvc.perform(
            MockMvcRequestBuilders.post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent)
            )
            .andExpect(status().isOk)
            .andDo(MockMvcResultHandlers.print())

        // With just email
        requestContent = """
            {
                "email": "$email",
                "password": "$password",
                "locale": "$locale"
            }
        """.trimIndent()
        mvc.perform(
            MockMvcRequestBuilders.post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent)
            )
            .andExpect(status().isOk)
            .andDo(MockMvcResultHandlers.print())
    }
}