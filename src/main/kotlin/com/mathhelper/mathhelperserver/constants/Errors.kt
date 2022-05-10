package com.mathhelper.mathhelperserver.constants

import java.lang.RuntimeException

class NetworkError {
    companion object {
        private const val fail = "Fail."
        const val invalidBody = "$fail Invalid request body."
        const val badRequest = "$fail Bad Request."
        fun notFound(entity: String) = "$fail No such entity: $entity"
        fun badRequestWithError(error: String) = "$badRequest Error: $error"
        fun alreadyExistsWithCode(entity: String, code: String) = "$fail Entity: $entity with code = $code already exists"
        fun alreadyExistsWithParams(entity: String, params: String) = "$fail Entity: $entity with these params: $params already exists"
        fun noPermissionForNamespace(namespaceCode: String, userFullName: String, activity: String) = "$fail User $userFullName dosn't have $activity permission for namespace $namespaceCode"
        fun noWritePermissionForNamespace(namespaceCode: String, userFullName: String) = noPermissionForNamespace(namespaceCode, userFullName, "write")
        fun noReadPermissionForNamespace(namespaceCode: String, userFullName: String) = noPermissionForNamespace(namespaceCode, userFullName, "read")
        fun noReadWritePermissionForNamespace(namespaceCode: String, userFullName: String) = noPermissionForNamespace(namespaceCode, userFullName, "read/write")
    }
}

class NotFoundException(entity: String) : RuntimeException(NetworkError.notFound(entity))
class BadRequestWithErrorException(error: String) : RuntimeException(NetworkError.badRequestWithError(error))
class AlreadyExistsWithCodeException(entity: String, code: String) : RuntimeException(NetworkError.alreadyExistsWithCode(entity, code))
class AlreadyExistsWithParamsException(entity: String, params: String) : RuntimeException(NetworkError.alreadyExistsWithParams(entity, params))
class NoWritePermissionForNamespaceException(namespaceCode: String, userFullName: String) : RuntimeException(NetworkError.noWritePermissionForNamespace(namespaceCode, userFullName))
class NoReadPermissionForNamespaceException(namespaceCode: String, userFullName: String) : RuntimeException(NetworkError.noReadPermissionForNamespace(namespaceCode, userFullName))
class NoReadWritePermissionForNamespaceException(namespaceCode: String, userFullName: String) : RuntimeException(NetworkError.noReadWritePermissionForNamespace(namespaceCode, userFullName))

class CommonException(msg: String) : RuntimeException(msg)
