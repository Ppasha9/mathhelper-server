package com.mathhelper.mathhelperserver.controllers

import com.google.gson.Gson
import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.constants.NetworkError
import com.mathhelper.mathhelperserver.datatables.tasks.GoalType
import com.mathhelper.mathhelperserver.forms.rule_pack.RulePackForm
import com.mathhelper.mathhelperserver.forms.taskset.*
import com.mathhelper.mathhelperserver.services.namespace.NamespaceService
import com.mathhelper.mathhelperserver.services.request.EditRequestService
import com.mathhelper.mathhelperserver.services.request.GameSolveRequestService
import com.mathhelper.mathhelperserver.services.response.EditResponseService
import com.mathhelper.mathhelperserver.services.response.GameSolveResponseService
import com.mathhelper.mathhelperserver.services.rule_pack.RulePackService
import com.mathhelper.mathhelperserver.services.taskset.TasksetService
import com.mathhelper.mathhelperserver.services.user.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.lang.RuntimeException
import java.sql.Timestamp
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

enum class FormType {
    FULL,
    LINK,
    CUTTED_LINK;

    companion object {
        private val map = values().associateBy(FormType::name)
        fun fromString(str: String) = map[str.uppercase()]
    }
}

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/taskset")
class TasksetController {
    @Autowired
    private lateinit var tasksetService: TasksetService

    @Autowired
    private lateinit var rulePackService: RulePackService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var namespaceService: NamespaceService

    @Autowired
    private lateinit var editRequestService: EditRequestService
    @Autowired
    private lateinit var editResponseService: EditResponseService

    @Autowired
    private lateinit var gameSolveRequestService: GameSolveRequestService
    @Autowired
    private lateinit var gameSolveResponseService: GameSolveResponseService

    @Autowired
    private lateinit var request: HttpServletRequest

    @GetMapping("")
    fun findTasksets(
        @RequestParam(name = "limit", required = false) limit: Int?,
        @RequestParam(name = "offset", required = false) offset: Int?,
        @RequestParam(name = "substr", required = false) substring: String?,
        @RequestParam(name = "keywords", required = false) keywords: String?,
        @RequestParam(name = "sort-by", required = false) sortBy: String?,
        @RequestParam(name = "descending", required = false) descending: Boolean?,
        @RequestParam(name = "namespace", required = false) namespace: String?,
        @RequestParam(name = "author-user-code", required = false) authorUserCode: String?,
        @RequestParam(name = "subjectType", required = false) subjectType: String?,
        @RequestParam(name = "form", required = false) form: String?,
        @RequestParam(name = "code", required = false) code: String?
    ): ResponseEntity<Any> {
        logger.info("Filter tasks")
        logger.info("""Params: limit=$limit, offset=$offset, substr=$substring, keywords=$keywords, sort-by=$sortBy
           descending=$descending, namespace=$namespace, author-user-code=$authorUserCode, 
           subjectType=$subjectType, form=$form, code=$code
        """)

        val currUser = userService.getCurrentAuthorizedUser()
        val filteredTasksets = tasksetService.filterTasksets(limit, offset, substring, keywords, sortBy, descending, namespace,
            authorUserCode, subjectType, currUser, code)
        if (filteredTasksets.isEmpty())
            return ResponseEntity("Tasksets by this params not found", HttpStatus.NOT_FOUND)

        val formType = if (form.isNullOrBlank()) FormType.FULL else {
            FormType.fromString(form) ?: FormType.FULL
        }
        val res = when (formType) {
            FormType.FULL -> {
                val tasksetForms = arrayListOf<TasksetForm>()
                filteredTasksets.forEach { tasksetForms.add(tasksetService.getTasksetForm(it)) }
                FilterTasksetsForm(tasksets = tasksetForms)
            }
            FormType.LINK -> {
                val tasksetForms = arrayListOf<TasksetLinkForm>()
                filteredTasksets.forEach { tasksetForms.add(tasksetService.getTasksetLinkForm(it)) }
                FilterTasksetsLinkForm(tasksets = tasksetForms)
            }
            FormType.CUTTED_LINK -> {
                val tasksetForms = arrayListOf<TasksetCuttedLinkForm>()
                filteredTasksets.forEach { tasksetForms.add(tasksetService.getTasksetCuttedLinkForm(it)) }
                FilterTasksetsCuttedLinkForm(tasksets = tasksetForms)
            }
        }
        return ResponseEntity.ok(res)
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @GetMapping("/edit/{code}")
    fun getTasksetForEditing(
        @PathVariable(name = "code", required = true) tasksetCode: String,
        @RequestParam(name = "client-ts", required = false) clientTS: Timestamp?,
        @RequestParam(name = "action-type", required = false) actionType: String?,
        @RequestParam(name = "app", required = false) appName: String?
    ): ResponseEntity<Any> {
        logger.info("Get taskset for editing")
        logger.info("Params: taskset code=$tasksetCode, client-ts=$clientTS, action-type=$actionType, app=$appName")

        // save current request to database
        val editRequestId = editRequestService.addNew(
            method = "GET",
            path = request.requestURI.toString(),
            appName = appName ?: "",
            user = userService.getCurrentAuthorizedUser(),
            userIP = request.remoteAddr.toString(),
            clientTS = clientTS,
            actionType = actionType ?: ""
        )

        val taskset = tasksetService.findByCode(tasksetCode)
        if (taskset == null) {
            val errMsg = "Taskset with code '$tasksetCode' not found"
            val retCode = HttpStatus.NOT_FOUND
            val responseEntity = ResponseEntity<Any>(errMsg, retCode)

            editResponseService.addNew(
                requestID = editRequestId,
                task = null,
                taskset = null,
                rulePack = null,
                headers = responseEntity.headers.toMutableMap(),
                responseBody = mutableMapOf("Message" to errMsg, "returnCode" to retCode.value()),
                returnCode = retCode.value(),
                validated = true
            )

            return responseEntity
        }

        val currUser = userService.getCurrentAuthorizedUser()!!
        if (!namespaceService.isNamespacePublicWrite(taskset.namespace) && !namespaceService.isUserHaveWriteAccessToNamespace(taskset.namespace, currUser)) {
            return ResponseEntity("Current authorized user doesn't have write access to taskset's namespace", HttpStatus.FORBIDDEN)
        }

        val tasksetForm = tasksetService.getTasksetForm(taskset)
        val tasksetTasksCodes = tasksetForm.tasks.map { it.code!! }
        val tasksetRulePacks = rulePackService.getByTaskCodes(tasksetTasksCodes) as ArrayList<RulePackForm>

        val res = OneTasksetResponseForm(taskset = tasksetForm, rulePacks = tasksetRulePacks)

        val gson = Gson()
        editResponseService.addNew(
            requestID = editRequestId,
            taskset = tasksetService.findActiveHistoryByCode(taskset.code),
            task = null,
            rulePack = null,
            headers = null,
            responseBody = mutableMapOf("taskset" to gson.toJson(tasksetForm), "rulePacks" to gson.toJson(tasksetRulePacks)),
            returnCode = HttpStatus.OK.value(),
            validated = true
        )

        return ResponseEntity.ok(res)
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @GetMapping("/play/{app}/{solving-mode}/{code}")
    fun getTasksetForPlay(
        @PathVariable(name = "app", required = true) appName: String,
        @PathVariable(name = "solving-mode", required = true) solvingMode: String,
        @PathVariable(name = "code", required = true) tasksetCode: String,
        @RequestParam(name = "client-ts", required = false) clientTS: Timestamp?
    ): ResponseEntity<Any> {
        logger.info("Get taskset for play")
        logger.info("Params: app=$appName, solving-mode=$solvingMode, code=$tasksetCode, client-ts=$clientTS")

        // save current request to database without task
        var gameRequestId = gameSolveRequestService.addNew(
            method = "GET",
            path = request.requestURI.toString(),
            appName = appName,
            userIP = request.remoteAddr.toString(),
            user = userService.getCurrentAuthorizedUser()!!,
            taskType = "",
            task = null,
            autoSubTask = null,
            clientTS = clientTS
        )

        val taskset = tasksetService.findByCode(tasksetCode)
        if (taskset == null) {
            val errMsg = "Taskset with code '$tasksetCode' not found"
            val retCode = HttpStatus.NOT_FOUND
            val responseEntity = ResponseEntity<Any>(errMsg, retCode)

            gameSolveResponseService.addNew(
                requestID = gameRequestId,
                activityLog = null,
                headers = null,
                responseBody = mutableMapOf("Message" to errMsg, "returnCode" to retCode.value()),
                returnCode = retCode.value()
            )

            return responseEntity
        }

        // save current request to database WITH task
        gameRequestId = gameSolveRequestService.addNew(
            method = "GET",
            path = request.requestURI.toString(),
            appName = appName,
            userIP = request.remoteAddr.toString(),
            user = userService.getCurrentAuthorizedUser()!!,
            taskType = "",
            task = null,
            autoSubTask = null,
            clientTS = clientTS
        )

        val currUser = userService.getCurrentAuthorizedUser()!!
        if (!namespaceService.isNamespacePublicRead(taskset.namespace) && !namespaceService.isUserHaveReadAccessToNamespace(taskset.namespace, currUser)) {
            return ResponseEntity("Current authorized user doesn't have read access to taskset's namespace", HttpStatus.FORBIDDEN)
        }

        val tasksetForm = tasksetService.getTasksetForm(taskset)
        val tasksetTasksCodes = tasksetForm.tasks.map { it.code!! }
        val tasksetRulePacks = rulePackService.getByTaskCodes(tasksetTasksCodes) as ArrayList<RulePackForm>

        val res = OneTasksetResponseForm(taskset = tasksetForm, rulePacks = tasksetRulePacks)

        val gson = Gson()
        gameSolveResponseService.addNew(
            requestID = gameRequestId,
            activityLog = null,
            headers = null,
            responseBody = mutableMapOf("taskset" to gson.toJson(tasksetForm), "rulePacks" to gson.toJson(tasksetRulePacks)),
            returnCode = HttpStatus.OK.value()
        )

        return ResponseEntity.ok(res)
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @GetMapping("/solve/{app}/{solving-mode}/{code}")
    fun getTasksetForSolve(
        @PathVariable(name = "app", required = true) appName: String,
        @PathVariable(name = "solving-mode", required = true) solvingMode: String,
        @PathVariable(name = "code", required = true) tasksetCode: String,
        @RequestParam(name = "client-ts", required = false) clientTS: Timestamp?
    ): ResponseEntity<Any> {
        logger.info("Get taskset for solve")
        logger.info("Params: app=$appName, solving-mode=$solvingMode, code=$tasksetCode, client-ts=$clientTS")

        // save current request to database without task
        var gameRequestId = gameSolveRequestService.addNew(
            method = "GET",
            path = request.requestURI.toString(),
            appName = appName,
            userIP = request.remoteAddr.toString(),
            user = userService.getCurrentAuthorizedUser()!!,
            taskType = "",
            task = null,
            autoSubTask = null,
            clientTS = clientTS
        )

        val taskset = tasksetService.findByCode(tasksetCode)
        if (taskset == null) {
            val errMsg = "Taskset with code '$tasksetCode' not found"
            val retCode = HttpStatus.NOT_FOUND
            val responseEntity = ResponseEntity<Any>(errMsg, retCode)

            gameSolveResponseService.addNew(
                requestID = gameRequestId,
                activityLog = null,
                headers = null,
                responseBody = mutableMapOf("Message" to errMsg, "returnCode" to retCode.value()),
                returnCode = retCode.value()
            )

            return responseEntity
        }

        // save current request to database WITH task
        gameRequestId = gameSolveRequestService.addNew(
            method = "GET",
            path = request.requestURI.toString(),
            appName = appName,
            userIP = request.remoteAddr.toString(),
            user = userService.getCurrentAuthorizedUser()!!,
            taskType = "",
            task = null,
            autoSubTask = null,
            clientTS = clientTS
        )

        val currUser = userService.getCurrentAuthorizedUser()!!
        if (!namespaceService.isNamespacePublicRead(taskset.namespace) && !namespaceService.isUserHaveReadAccessToNamespace(taskset.namespace, currUser)) {
            return ResponseEntity("Current authorized user doesn't have read access to taskset's namespace", HttpStatus.FORBIDDEN)
        }

        val tasksetForm = tasksetService.getTasksetForm(taskset)
        val tasksetTasksCodes = tasksetForm.tasks.map { it.code!! }
        val tasksetRulePacks = rulePackService.getByTaskCodes(tasksetTasksCodes) as ArrayList<RulePackForm>

        val res = OneTasksetResponseForm(taskset = tasksetForm, rulePacks = tasksetRulePacks)

        val gson = Gson()
        gameSolveResponseService.addNew(
            requestID = gameRequestId,
            activityLog = null,
            headers = null,
            responseBody = mutableMapOf("taskset" to gson.toJson(tasksetForm), "rulePacks" to gson.toJson(tasksetRulePacks)),
            returnCode = HttpStatus.OK.value()
        )

        return ResponseEntity.ok(res)
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/create")
    fun createTaskset(@Valid @RequestBody tasksetForm: TasksetForm?): ResponseEntity<Any> {
        logger.info("Create new taskset")
        logger.info("Form: $tasksetForm")

        if (tasksetForm == null)
            return ResponseEntity(Constants.EXCEPTION_INVALID_BODY_STRING, HttpStatus.BAD_REQUEST)

        val currUser = userService.getCurrentAuthorizedUser()!!
        val namespace = namespaceService.findByCode(tasksetForm.namespaceCode) ?:
            return ResponseEntity(NetworkError.notFound("Namespace with code ${tasksetForm.namespaceCode}"), HttpStatus.NOT_FOUND)
        if (!namespaceService.isUserHaveWriteAccessToNamespace(namespace, currUser)) {
            return ResponseEntity("Current authorized user doesn't have write access to taskset's namespace", HttpStatus.FORBIDDEN)
        }

        try {
            tasksetService.createFrom(tasksetForm, currUser)
        } catch (e: RuntimeException) {
            return ResponseEntity(e.message!!, HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity("Taskset was successfully created", HttpStatus.CREATED)
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/update")
    fun patchTaskset(@Valid @RequestBody tasksetForm: TasksetForm?): ResponseEntity<Any> {
        logger.info("Patch taskset")
        logger.info("Params: $tasksetForm")

        if (tasksetForm == null)
            return ResponseEntity(Constants.EXCEPTION_INVALID_BODY_STRING, HttpStatus.BAD_REQUEST)

        val currUser = userService.getCurrentAuthorizedUser()!!
        val namespace = namespaceService.findByCode(tasksetForm.namespaceCode) ?:
            return ResponseEntity(NetworkError.notFound("Namespace with code ${tasksetForm.namespaceCode}"), HttpStatus.NOT_FOUND)
        if (!namespaceService.isUserHaveWriteAccessToNamespace(namespace, currUser)) {
            return ResponseEntity("Current authorized user doesn't have write access to taskset's namespace", HttpStatus.FORBIDDEN)
        }

        try {
            tasksetService.updateWith(tasksetForm, currUser)
        } catch (e: RuntimeException) {
            return ResponseEntity(e.message!!, HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity.ok("Taskset was successfully updated")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("logic-logs")
    }
}