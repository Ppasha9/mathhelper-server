package com.mathhelper.mathhelperserver.controllers

import com.google.gson.Gson
import com.mathhelper.mathhelperserver.constants.*
import com.mathhelper.mathhelperserver.forms.rule_pack.RulePackForm
import com.mathhelper.mathhelperserver.forms.task.FilterTasksForm
import com.mathhelper.mathhelperserver.forms.task.OneTaskResponseForm
import com.mathhelper.mathhelperserver.forms.task.TaskForm
import com.mathhelper.mathhelperserver.services.namespace.NamespaceService
import com.mathhelper.mathhelperserver.services.request.EditRequestService
import com.mathhelper.mathhelperserver.services.request.GameSolveRequestService
import com.mathhelper.mathhelperserver.services.response.EditResponseService
import com.mathhelper.mathhelperserver.services.response.GameSolveResponseService
import com.mathhelper.mathhelperserver.services.rule_pack.RulePackService
import com.mathhelper.mathhelperserver.services.task.TaskService
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

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/task")
class TaskController {
    @Autowired
    private lateinit var taskService: TaskService

    @Autowired
    private lateinit var editRequestService: EditRequestService
    @Autowired
    private lateinit var editResponseService: EditResponseService

    @Autowired
    private lateinit var gameSolveRequestService: GameSolveRequestService
    @Autowired
    private lateinit var gameSolveResponseService: GameSolveResponseService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var namespaceService: NamespaceService

    @Autowired
    private lateinit var rulePackService: RulePackService

    @Autowired
    private lateinit var request: HttpServletRequest

    @GetMapping("")
    fun findTasks(
        @RequestParam(name = "limit", required = false) limit: Int?,
        @RequestParam(name = "offset", required = false) offset: Int?,
        @RequestParam(name = "substr", required = false) substring: String?,
        @RequestParam(name = "keywords", required = false) keywords: String?,
        @RequestParam(name = "sort-by", required = false) sortBy: String?,
        @RequestParam(name = "descending", required = false) descending: Boolean?,
        @RequestParam(name = "namespace", required = false) namespace: String?,
        @RequestParam(name = "author-user-code", required = false) authorUserCode: String?,
        @RequestParam(name = "subjectType", required = false) subjectType: String?
    ): ResponseEntity<Any> {
        logger.info("Filter tasks")
        logger.info("Params: limit=$limit, offset=$offset, substr=$substring, keywords=$keywords, sort-by=$sortBy," +
            " descending=$descending, namespace=$namespace, author-user-code=$authorUserCode, subjectType=$subjectType")

        val currUser = userService.getCurrentAuthorizedUser()
        val filteredTasks = taskService.filterTasks(limit, offset, substring, keywords, sortBy, descending, namespace,
            authorUserCode, subjectType, currUser)
        if (filteredTasks.isEmpty())
            return ResponseEntity("Tasks by this params not found", HttpStatus.NOT_FOUND)

        val taskForms = arrayListOf<TaskForm>()
        filteredTasks.forEach { taskForms.add(taskService.getTaskForm(it)) }

        val res = FilterTasksForm(tasks = taskForms)
        return ResponseEntity.ok(res)
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @GetMapping("/edit/{code}")
    fun getTaskForEditing(
        @PathVariable(name = "code", required = true) taskCode: String,
        @RequestParam(name = "client-ts", required = false) clientTS: Timestamp?,
        @RequestParam(name = "action-type", required = false) actionType: String?,
        @RequestParam(name = "app", required = false) appName: String?
    ): ResponseEntity<Any> {
        logger.info("Get task for editing")
        logger.info("Params: task code=$taskCode, client-ts=$clientTS, action-type=$actionType, app=$appName")

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

        val task = taskService.findByCode(taskCode)
        if (task == null) {
            val errMsg = "Task with code '$taskCode' not found"
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
        if (!namespaceService.isNamespacePublicWrite(task.namespace) && !namespaceService.isUserHaveWriteAccessToNamespace(task.namespace, currUser)) {
            return ResponseEntity("Current authorized user doesn't have write access to task's namespace", HttpStatus.FORBIDDEN)
        }

        val taskForm = taskService.getTaskForm(task)
        val taskRulePacks = rulePackService.getByTaskCodes(listOf(taskForm.code!!)) as ArrayList<RulePackForm>

        val res = OneTaskResponseForm(task = taskForm, rulePacks = taskRulePacks)

        val gson = Gson()
        editResponseService.addNew(
            requestID = editRequestId,
            task = taskService.findActiveHistoryByCode(task.code),
            taskset = null,
            rulePack = null,
            headers = null,
            responseBody = mutableMapOf("task" to gson.toJson(taskForm), "rulePacks" to gson.toJson(taskRulePacks)),
            returnCode = HttpStatus.OK.value(),
            validated = true
        )

        return ResponseEntity.ok(res)
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @GetMapping("/play/{app}/{solving-mode}/{code}")
    fun getTaskForPlay(
        @PathVariable(name = "app", required = true) appName: String,
        @PathVariable(name = "solving-mode", required = true) solvingMode: String,
        @PathVariable(name = "code", required = true) taskCode: String,
        @RequestParam(name = "client-ts", required = false) clientTS: Timestamp?
    ): ResponseEntity<Any> {
        logger.info("Get task for play")
        logger.info("Params: app=$appName, solving-mode=$solvingMode, code=$taskCode, client-ts=$clientTS")

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

        val task = taskService.findByCode(taskCode)
        if (task == null) {
            val errMsg = "Task with code '$taskCode' not found"
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
            task = task,
            autoSubTask = null,
            clientTS = clientTS
        )

        val currUser = userService.getCurrentAuthorizedUser()!!
        if (!namespaceService.isNamespacePublicRead(task.namespace) && !namespaceService.isUserHaveReadAccessToNamespace(task.namespace, currUser)) {
            return ResponseEntity("Current authorized user doesn't have read access to task's namespace", HttpStatus.FORBIDDEN)
        }

        val taskForm = taskService.getTaskForm(task)
        val taskRulePacks = rulePackService.getByTaskCodes(listOf(taskForm.code!!)) as ArrayList<RulePackForm>

        val res = OneTaskResponseForm(task = taskForm, rulePacks = taskRulePacks)

        val gson = Gson()
        gameSolveResponseService.addNew(
            requestID = gameRequestId,
            activityLog = null,
            headers = null,
            responseBody = mutableMapOf("task" to gson.toJson(taskForm), "rulePacks" to gson.toJson(taskRulePacks)),
            returnCode = HttpStatus.OK.value()
        )

        return ResponseEntity.ok(res)
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @GetMapping("/solve/{app}/{solving-mode}/{code}")
    fun getTaskForSolve(
        @PathVariable(name = "app", required = true) appName: String,
        @PathVariable(name = "solving-mode", required = true) solvingMode: String,
        @PathVariable(name = "code", required = true) taskCode: String,
        @RequestParam(name = "client-ts", required = false) clientTS: Timestamp?
    ): ResponseEntity<Any> {
        logger.info("Get task for solve")
        logger.info("Params: app=$appName, solving-mode=$solvingMode, code=$taskCode, client-ts=$clientTS")

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

        val task = taskService.findByCode(taskCode)
        if (task == null) {
            val errMsg = "Task with code '$taskCode' not found"
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
            task = task,
            autoSubTask = null,
            clientTS = clientTS
        )

        val currUser = userService.getCurrentAuthorizedUser()!!
        if (!namespaceService.isNamespacePublicRead(task.namespace) && !namespaceService.isUserHaveReadAccessToNamespace(task.namespace, currUser)) {
            return ResponseEntity("Current authorized user doesn't have read access to task's namespace", HttpStatus.FORBIDDEN)
        }

        val taskForm = taskService.getTaskForm(task)
        val taskRulePacks = rulePackService.getByTaskCodes(listOf(taskForm.code!!)) as ArrayList<RulePackForm>

        val res = OneTaskResponseForm(task = taskForm, rulePacks = taskRulePacks)

        val gson = Gson()
        gameSolveResponseService.addNew(
            requestID = gameRequestId,
            activityLog = null,
            headers = null,
            responseBody = mutableMapOf("task" to gson.toJson(taskForm), "rulePacks" to gson.toJson(taskRulePacks)),
            returnCode = HttpStatus.OK.value()
        )

        return ResponseEntity.ok(res)
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/create")
    fun createTask(@Valid @RequestBody taskForm: TaskForm?): ResponseEntity<Any> {
        logger.info("Create new task")
        logger.info("Form: $taskForm")

        if (taskForm == null)
            return ResponseEntity(Constants.EXCEPTION_INVALID_BODY_STRING, HttpStatus.BAD_REQUEST)

        val currUser = userService.getCurrentAuthorizedUser()!!
        val namespace = namespaceService.findByCode(taskForm.namespaceCode) ?:
            return ResponseEntity(NetworkError.notFound("Namespace with code ${taskForm.namespaceCode}"), HttpStatus.NOT_FOUND)
        if (!namespaceService.isUserHaveWriteAccessToNamespace(namespace, currUser)) {
            return ResponseEntity("Current authorized user doesn't have write access to task's namespace", HttpStatus.FORBIDDEN)
        }

        try {
            taskService.createFrom(taskForm, currUser)
        } catch (e: RuntimeException) {
            return ResponseEntity(e.message!!, HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity("Task was successfully created", HttpStatus.CREATED)
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/update")
    fun patchTask(@Valid @RequestBody taskForm: TaskForm?): ResponseEntity<Any> {
        logger.info("Patch task")
        logger.info("Params: $taskForm")

        if (taskForm == null)
            return ResponseEntity(Constants.EXCEPTION_INVALID_BODY_STRING, HttpStatus.BAD_REQUEST)

        val currUser = userService.getCurrentAuthorizedUser()!!
        val namespace = namespaceService.findByCode(taskForm.namespaceCode) ?:
            return ResponseEntity(NetworkError.notFound("Namespace with code ${taskForm.namespaceCode}"), HttpStatus.NOT_FOUND)
        if (!namespaceService.isUserHaveWriteAccessToNamespace(namespace, currUser)) {
            return ResponseEntity("Current authorized user doesn't have write access to task's namespace", HttpStatus.FORBIDDEN)
        }

        try {
            taskService.updateFromController(taskForm, currUser)
        } catch (e: RuntimeException) {
            return ResponseEntity(e.message!!, HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity.ok("Task was successfully updated")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("logic-logs")
    }
}