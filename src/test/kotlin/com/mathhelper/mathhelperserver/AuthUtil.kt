package com.mathhelper.mathhelperserver

import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespaceGrant
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespaceGrantsRepository
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespacesRepository
import com.mathhelper.mathhelperserver.datatables.namespaces.UserGrantType
import com.mathhelper.mathhelperserver.services.user.UserService
import org.json.JSONObject
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AuthUtil {
    companion object {
        private var token: String = ""
        var testUserCode: String = ""

        fun getToken(
                mvc: MockMvc? = null, userService: UserService? = null,
                namespaceGrantsRepository: NamespaceGrantsRepository? = null,
                namespacesRepository: NamespacesRepository? = null
        ): String {
            if (token.isNotBlank()) return token
            if (mvc == null || userService == null || namespaceGrantsRepository == null || namespacesRepository == null) return ""
            // Perform the request and check response status
            val signUp = mvc.post("/api/auth/signup") {
                contentType = MediaType.APPLICATION_JSON
                content = signUpBody
            }.andExpect {
                status { isOk }
            }.andReturn()
            // Check that user was added
            assertNotEquals(userService.findAll().count(), 0)
            assertTrue(userService.existsByLogin(testUserLogin))
            val user = userService.findByLogin(testUserLogin)!!
            testUserCode = user.code
            val namespace = namespacesRepository.findByCode("test_namespace_code")!!
            namespaceGrantsRepository.save(NamespaceGrant(namespace = namespace, grantedUser = user, userGrantType = grant, licensorUser = user))
            val namespace2 = namespacesRepository.findByCode("second_test_namespace_code")!!
            namespaceGrantsRepository.save(NamespaceGrant(namespace = namespace2, grantedUser = user, userGrantType = grant, licensorUser = user))
            // Get token
            val response = JSONObject(signUp.response.contentAsString)
            token = response.optString("token", "")
            assertNotEquals("", token)
            return token
        }

        private const val testUserLogin = "math_helper_tester"
        private val signUpBody = """
            {
                "login": "$testUserLogin",
                "email": "$testUserLogin@bk.ru",
                "password": "$testUserLogin",
                "name": "${testUserLogin}_name",
                "fullName": "${testUserLogin}_full_name",
                "locale": "rus"
            }
        """.trimIndent()
        private val grant = UserGrantType(code = Constants.USER_GRANT_TYPE_READ_WRITE)
    }
}