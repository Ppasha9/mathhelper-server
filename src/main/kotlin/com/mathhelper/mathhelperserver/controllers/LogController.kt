package com.mathhelper.mathhelperserver.controllers

import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.forms.log.*
import com.mathhelper.mathhelperserver.services.log.LogService
import com.mathhelper.mathhelperserver.services.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.lang.RuntimeException
import java.sql.Timestamp
import javax.validation.Valid

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/log")
class LogController {
    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var logService: LogService

    companion object {
        private val logger by lazy { LoggerFactory.getLogger("logic-logs") }
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/activity/create")
    fun createActivityLog(@Valid @RequestBody activityLogForm: ActivityLogForm?): ResponseEntity<String> {
        logger.info("createActivityLog")
        logger.info("activityLogForm = {}".format(activityLogForm))
        if (activityLogForm == null) {
            return ResponseEntity(Constants.EXCEPTION_INVALID_BODY_STRING, HttpStatus.BAD_REQUEST)
        }
        val user = userService.getCurrentAuthorizedUser()!!
        try {
            logService.createFrom(activityLogForm, user)
        } catch (e: RuntimeException) {
            return ResponseEntity(e.message!!, HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity("Activity log was successfully created", HttpStatus.CREATED)
    }

    @PostMapping("/activity/find")
    fun findActivityLog(@Valid @RequestBody activityLogSearchForm: ActivityLogSearchForm?): ResponseEntity<Any> {
        logger.info("findActivityLog")
        logger.info("activityLogSearchForm = {}".format(activityLogSearchForm))
        val res = if (activityLogSearchForm == null)
            logService.findActivityAll()
        else
            logService.findActivityByForm(activityLogSearchForm)
        val all = arrayListOf<ActivityLogForm>()
        return if (res.isEmpty())
            ResponseEntity("Logs with these params not found", HttpStatus.NOT_FOUND)
        else {
            res.forEach { all.add(logService.getActivityForm(it)) }
            ResponseEntity.ok(all)
        }
    }

    @PostMapping("/result/find/tasksets")
    fun findTasksetsByResult(@Valid @RequestBody resultTasksetSearchForm: ResultTasksetSearchForm?): ResponseEntity<Any> {
        logger.info("findTasksetsByResult")
        logger.info("resultTasksetSearchForm = {}".format(resultTasksetSearchForm))
        val res = logService.findResultTasksetsByForm(resultTasksetSearchForm)
        return if (res.isEmpty())
            ResponseEntity("Tasksets result info with these params not found", HttpStatus.NOT_FOUND)
        else
            ResponseEntity.ok(res)
    }

    @PostMapping("/result/find/tasks")
    fun findTasksByResult(@Valid @RequestBody resultTaskSearchForm: ResultTaskSearchForm?): ResponseEntity<Any> {
        logger.info("findTasksByResult")
        logger.info("resultTaskSearchForm = {}".format(resultTaskSearchForm))
        val res = logService.findResultTasksByForm(resultTaskSearchForm)
        return if (res.isEmpty())
            ResponseEntity("Tasks result info with these params not found", HttpStatus.NOT_FOUND)
        else
            ResponseEntity.ok(res)
    }

    @PostMapping("/result/find/users")
    fun findUsersByResult(@Valid @RequestBody resultUserSearchForm: ResultUserSearchForm?): ResponseEntity<Any> {
        logger.info("findUsersByResult")
        logger.info("resultUserSearchForm = {}".format(resultUserSearchForm))
        val res = logService.findResultUsersByForm(resultUserSearchForm)
        return if (res.isEmpty())
            ResponseEntity("Users result info with these params not found", HttpStatus.NOT_FOUND)
        else
            ResponseEntity.ok(res)
    }

    @PostMapping("/result/find")
    fun findResultLog(@Valid @RequestBody activityLogSearchForm: ActivityLogSearchForm?): ResponseEntity<Any> {
        logger.info("findResultLog")
        logger.info("activityLogSearchForm = {}".format(activityLogSearchForm))
        val res = if (activityLogSearchForm == null)
            logService.findResultAll()
        else
            logService.findResultByForm(activityLogSearchForm)
        val all = arrayListOf<ResultLogForm>()
        return if (res.isEmpty())
            ResponseEntity("Results with these params not found", HttpStatus.NOT_FOUND)
        else {
            res.forEach { all.add(logService.getResultForm(it)) }
            ResponseEntity.ok(all)
        }
    }

    @RequestMapping("/result/all", method = [RequestMethod.GET, RequestMethod.POST])
    fun findAllResultLog(): ResponseEntity<Any> {
        val res = logService.findResultAll()
        val all = arrayListOf<ResultLogForm>()
        return if (res.isEmpty())
            ResponseEntity("Results with these params not found", HttpStatus.NOT_FOUND)
        else {
            res.forEach { all.add(logService.getResultForm(it)) }
            ResponseEntity.ok(all)
        }
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/user_statistics")
    fun getUserStatistics(@RequestParam(name = "app", required = true) appCode: String): ResponseEntity<Any> {
        logger.info("getUserStatistics")
        val user = userService.getCurrentAuthorizedUser()!!
        return try {
            val stat = logService.getUserStatistics(appCode, user.code)
            if (stat.tasksetStatistics.isEmpty()) {
                ResponseEntity("User's statistics for $appCode not found", HttpStatus.NOT_FOUND)
            } else {
                ResponseEntity.ok(stat)
            }
        } catch (e: RuntimeException) {
            ResponseEntity(e.message!!, HttpStatus.BAD_REQUEST)
        }
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @DeleteMapping("/user_statistics")
    fun clearUserStatistics(@RequestParam(name = "app", required = true) appCode: String): ResponseEntity<Any> {
        logger.info("clearUserStatistics")
        val user = userService.getCurrentAuthorizedUser()!!
        return try {
            logService.clearUserStatistics(appCode, user.code)
            ResponseEntity.ok("User statistics were successfully cleared")
        } catch (e: RuntimeException) {
            ResponseEntity(e.message!!, HttpStatus.BAD_REQUEST)
        }
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @GetMapping("/statistics_for_report")
    fun getUserStatisticsForReport(
        @RequestParam(name = "namespace", required = false) namespace: String? = "",
        @RequestParam(name = "taskset", required = false) taskset: String? = "",
        @RequestParam(name = "start_date", required = false) startDate: Timestamp? = null,
        @RequestParam(name = "end_date", required = false) endDate: Timestamp? = null
    ): ResponseEntity<Any> {
        logger.info("Getting statistics for report")
        logger.info("Params: namespace=$namespace, taskset=$taskset, start_date=$startDate, end_date=$endDate")

        if ((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
            return ResponseEntity("You should request both dates, not only one", HttpStatus.BAD_REQUEST)
        }

        val user = userService.getCurrentAuthorizedUser()!!
        return try {
            val res = logService.getStatisticsForReport(user, namespace ?: "", taskset ?: "", startDate, endDate)
            ResponseEntity.ok(res)
        } catch (e: RuntimeException) {
            ResponseEntity(e.message!!, HttpStatus.BAD_REQUEST)
        }
    }
}