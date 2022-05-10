package com.mathhelper.mathhelperserver.authorization

import java.lang.RuntimeException

class UserAlreadyExistException : RuntimeException {
    constructor() : super() {}
    constructor(message: String, cause: Throwable) : super(message, cause) {}
    constructor(message: String) : super(message) {}
    constructor(cause: Throwable) : super(cause) {}
}