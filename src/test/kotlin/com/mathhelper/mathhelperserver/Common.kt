package com.mathhelper.mathhelperserver

import com.mathhelper.mathhelperserver.datatables.namespaces.Namespace
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespaceGrantType
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables.users.UserType
import java.sql.Timestamp

private val testUserTypeData = mapOf(
    "code" to "__test_user_type_code__",
    "description" to "user_type_descr"
)

fun createTestUserType(
    code: String = testUserTypeData["code"].toString(),
    description: String = testUserTypeData["description"].toString()
) : UserType = UserType(
    code = code,
    description = description
)

fun getDefaultTestUserTypeData() : Map<String, *> = testUserTypeData


private val testUserData = mapOf(
    "code" to "__test_user_code__",
    "login" to "test_login",
    "email" to "test_email",
    "name" to "test_name",
    "fullName" to "test_full_name"
)

fun createTestUser(
    code: String = testUserData["code"].toString(),
    userType: UserType,
    login: String = testUserData["login"].toString(),
    email: String = testUserData["email"].toString(),
    name: String = testUserData["name"].toString(),
    fullName: String = testUserData["fullName"].toString()
) : User = User(
    code = code,
    userType = userType,
    login = login,
    email = email,
    name = name,
    fullName = fullName
)

fun getDefaultTestUserData() : Map<String, *> = testUserData


private val testNamespaceGrantTypeData = mapOf(
    "code" to "__test_code__",
    "description" to "namespace_grant_type_descr"
)

fun createTestNamespaceGrantType(
    code: String = testNamespaceGrantTypeData["code"].toString(),
    description: String = testNamespaceGrantTypeData["description"].toString()
) : NamespaceGrantType = NamespaceGrantType(
    code = code,
    description = description
)

fun getDefaultTestNamespaceGrantTypeData() : Map<String, *> = testNamespaceGrantTypeData


private val testNamespaceData = mapOf(
    "code" to "__test_namespace_code__"
)

fun createTestNamespace(
    code: String = testNamespaceData["code"].toString(),
    authorUser: User, namespaceGrantType: NamespaceGrantType
) : Namespace = Namespace(
    code = code,
    authorUser = authorUser,
    namespaceGrantType = namespaceGrantType,
    serverActionTs = Timestamp(System.currentTimeMillis())
)

fun getDefaultTestNamespaceData() : Map<String, *> = testNamespaceData
