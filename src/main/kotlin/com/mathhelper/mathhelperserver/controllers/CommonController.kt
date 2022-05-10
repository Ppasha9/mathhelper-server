package com.mathhelper.mathhelperserver.controllers

import com.mathhelper.mathhelperserver.services.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
class CommonController {
    @Autowired
    private lateinit var userService: UserService

    @GetMapping("/api/is_running")
    fun getIsRunning(): ResponseEntity<Any> {
        logger.info("Getting is healthy/running flag")
        return ResponseEntity.ok("I'm running right now!")
    }

    @GetMapping("/api/users")
    fun getUsers(
        @RequestParam(name = "limit", required = false) limit: Int?,
        @RequestParam(name = "offset", required = false) offset: Int?
    ): ResponseEntity<Any> {
        logger.info("Getting users by params: limit=$limit, offset=$offset")
        val res = userService.getAllUsersCuttedForm(limit, offset)
        if (res.isEmpty()) {
            return ResponseEntity("Not found users with params: limit=$limit, offset=$offset", HttpStatus.NOT_FOUND)
        }

        return ResponseEntity.ok(res)
    }

    companion object {
        private val logger by lazy { LoggerFactory.getLogger("logic-logs") }
    }
}