package com.mathhelper.mathhelperserver.services.log

import com.fasterxml.jackson.databind.ObjectMapper
import com.mathhelper.mathhelperserver.constants.ActionTypes
import com.mathhelper.mathhelperserver.constants.CommonException
import com.mathhelper.mathhelperserver.constants.NoReadPermissionForNamespaceException
import com.mathhelper.mathhelperserver.constants.NotFoundException
import com.mathhelper.mathhelperserver.datatables.log.*
import com.mathhelper.mathhelperserver.datatables.namespaces.Namespace
import com.mathhelper.mathhelperserver.datatables.tasks.AutoSubTask
import com.mathhelper.mathhelperserver.datatables.tasks.AutoSubTaskRepository
import com.mathhelper.mathhelperserver.datatables.taskset.TasksetRepository
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.mathhelper.mathhelperserver.datatables_history.tasks.TaskHistory
import com.mathhelper.mathhelperserver.datatables_history.tasks.TaskHistoryRepository
import com.mathhelper.mathhelperserver.datatables_history.taskset.TasksetHistory
import com.mathhelper.mathhelperserver.datatables_history.taskset.TasksetHistoryRepository
import com.mathhelper.mathhelperserver.forms.log.*
import com.mathhelper.mathhelperserver.services.namespace.NamespaceService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException
import java.math.BigInteger
import java.sql.Timestamp
import java.text.SimpleDateFormat

@Service
class LogService {
    @Autowired
    private lateinit var activityLogRepository: ActivityLogRepository

    @Autowired
    private lateinit var lastStepLogRepository: LastStepLogRepository

    @Autowired
    private lateinit var resultLogRepository: ResultLogRepository

    @Autowired
    private lateinit var appRepository: AppRepository

    @Autowired
    private lateinit var activityTypeRepository: ActivityTypeRepository

    @Autowired
    private lateinit var taskHistoryRepository: TaskHistoryRepository

    @Autowired
    private lateinit var tasksetHistoryRepository: TasksetHistoryRepository

    @Autowired
    private lateinit var tasksetRepository: TasksetRepository

    @Autowired
    private lateinit var autoSubTaskRepository: AutoSubTaskRepository

    @Autowired
    private lateinit var namespaceService: NamespaceService

    companion object {
        private val logger by lazy { LoggerFactory.getLogger("logic-logs") }
    }

    fun getActivityForm(activityLog: ActivityLog): ActivityLogForm {
        logger.info("getActivityForm")
        return ActivityLogForm(
            appCode = activityLog.app.code, activityTypeCode = activityLog.activityType.code,
            clientActionTs = activityLog.clientActionTs, serverActionTs = activityLog.serverActionTs,
            userCode = activityLog.user.code,
            tasksetCode = activityLog.taskset?.id?.code, tasksetVersion = activityLog.taskset?.id?.version,
            taskCode = activityLog.task?.id?.code, taskVersion = activityLog.task?.id?.version,
            autoSubTaskCode = activityLog.autoSubTask?.code,
            originalExpression = activityLog.originalExpression,
            goalExpression = activityLog.goalExpression, goalPattern = activityLog.goalPattern,
            difficulty = activityLog.difficulty, currSolution = activityLog.currSolution,
            currExpression = activityLog.currExpression, nextExpression = activityLog.nextExpression,
            appliedRule = activityLog.appliedRule, selectedPlace = activityLog.selectedPlace,
            currTimeMs = activityLog.currTimeMs, timeFromLastActionMs = activityLog.timeFromLastActionMs,
            currStepsNumber = activityLog.currStepsNumber, nextStepsNumber = activityLog.nextStepsNumber,
            subActionNumber = activityLog.subActionNumber,
            subActionsAfterLastTransformation = activityLog.subActionsAfterLastTransformation,
            otherData = activityLog.otherData, otherGameStepData = activityLog.otherGameStepData,
            otherSolutionStepData = activityLog.otherSolutionStepData
        )
    }

    fun getResultForm(resultLog: ResultLog): ResultLogForm {
        logger.info("getResultForm")
        return ResultLogForm(
            appCode = resultLog.app.code,
            tasksetCode = resultLog.taskset?.id?.code, tasksetVersion = resultLog.taskset?.id?.version,
            taskCode = resultLog.task?.id?.code, taskVersion = resultLog.task?.id?.version,
            autoSubTaskCode = resultLog.autoSubTask?.code, userCode = resultLog.user.code,
            difficulty = resultLog.difficulty, baseAward = resultLog.baseAward, currTimeMs = resultLog.currTimeMs,
            currStepsNumber = resultLog.currStepsNumber, clientActionTs = resultLog.clientActionTs,
            serverActionTs = resultLog.serverActionTs, qualityData = resultLog.qualityData
        )
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun createFrom(activityLogForm: ActivityLogForm, user: User) {
        logger.info("createFrom")
        // validate links
        val app = appRepository.findByCode(activityLogForm.appCode) ?:
            throw NotFoundException("App with code = ${activityLogForm.appCode}")
        val activityType = activityTypeRepository.findByCode(activityLogForm.activityTypeCode) ?:
            throw NotFoundException("Activity type with code = ${activityLogForm.activityTypeCode}")
        val action = ActionTypes.valueOf(activityType.code.toUpperCase())
        var tasksetHistory: TasksetHistory? = null
        if (activityLogForm.tasksetCode != null && activityLogForm.tasksetVersion != null) {
            val id = HistoryId(activityLogForm.tasksetCode!!, activityLogForm.tasksetVersion!!)
            val tasksetHistoryOpt = tasksetHistoryRepository.findById(id)
            if (!tasksetHistoryOpt.isPresent)
                throw NotFoundException("TasksetHistory with code = ${activityLogForm.tasksetCode}")
            tasksetHistory = tasksetHistoryOpt.get()
        }
        var taskHistory: TaskHistory? = null
        if (activityLogForm.taskCode != null && activityLogForm.taskVersion != null) {
            val id = HistoryId(activityLogForm.taskCode!!, activityLogForm.taskVersion!!)
            val taskHistoryOpt = taskHistoryRepository.findById(id)
            if (!taskHistoryOpt.isPresent)
                throw NotFoundException("TaskHistory with code = ${activityLogForm.taskCode}")
            taskHistory = taskHistoryOpt.get()
        }
        var autoSubTask: AutoSubTask? = null
        if (activityLogForm.autoSubTaskCode != null) {
            val autoTaskOpt = autoSubTaskRepository.findByCode(activityLogForm.autoSubTaskCode!!) ?:
                throw NotFoundException("AutoSubTask with code = ${activityLogForm.autoSubTaskCode}")
            autoSubTask = autoTaskOpt
        }
        // create ActivityLog
        val activityLog = ActivityLog(
            app = app, activityType = activityType, taskset = tasksetHistory, task = taskHistory,
            autoSubTask = autoSubTask, user = user, originalExpression = activityLogForm.originalExpression,
            goalExpression = activityLogForm.goalExpression, goalPattern = activityLogForm.goalPattern,
            difficulty = activityLogForm.difficulty, currSolution = activityLogForm.currSolution,
            currExpression = activityLogForm.currExpression, nextExpression = activityLogForm.nextExpression,
            appliedRule = activityLogForm.appliedRule, selectedPlace = activityLogForm.selectedPlace,
            currTimeMs = activityLogForm.currTimeMs, timeFromLastActionMs = activityLogForm.timeFromLastActionMs,
            currStepsNumber = activityLogForm.currStepsNumber, nextStepsNumber = activityLogForm.nextStepsNumber,
            subActionNumber = activityLogForm.subActionNumber,
            subActionsAfterLastTransformation = activityLogForm.subActionsAfterLastTransformation,
            clientActionTs = activityLogForm.clientActionTs, serverActionTs = Timestamp(System.currentTimeMillis()),
            otherData = activityLogForm.otherData,
            otherGameStepData = activityLogForm.otherGameStepData,
            otherSolutionStepData = activityLogForm.otherSolutionStepData
        )
        // check if step -> create/update LastStepLog
        if (action.isStep()) {
            // get current last step
            if (tasksetHistory == null && autoSubTask == null && taskHistory == null)
                throw CommonException("Fail. Both Task and AutoTask are not specified")
            val currentLastStep = if (autoSubTask != null)
                lastStepLogRepository.findByAppCodeAndTasksetIdAndAutoSubTaskCodeAndUserCode(app.code, tasksetHistory!!.id, autoSubTask.code, user.code)
            else lastStepLogRepository.findByAppCodeAndTasksetIdAndTaskIdAndUserCode(app.code, tasksetHistory!!.id, taskHistory!!.id, user.code)
            // check clientActionTs
            if (currentLastStep == null) {
                val lastStepLog = LastStepLog(
                    app = app, activityType = activityType, taskset = tasksetHistory, task = taskHistory,
                    autoSubTask = autoSubTask, user = user, currSolution = activityLogForm.currSolution,
                    currExpression = activityLogForm.nextExpression, currTimeMs = activityLogForm.currTimeMs,
                    currStepsNumber = activityLogForm.nextStepsNumber, clientActionTs = activityLogForm.clientActionTs,
                    otherData = activityLogForm.otherData, otherGameStepData = activityLogForm.otherGameStepData,
                    otherSolutionStepData = activityLogForm.otherSolutionStepData,
                    serverActionTs = Timestamp(System.currentTimeMillis())
                )
                lastStepLogRepository.save(lastStepLog)
            } else if (activityLogForm.clientActionTs!!.after(currentLastStep.clientActionTs)) {
                currentLastStep.activityType = activityType
                currentLastStep.currSolution = activityLogForm.currSolution
                currentLastStep.currExpression = activityLogForm.nextExpression
                currentLastStep.currTimeMs = activityLogForm.currTimeMs
                currentLastStep.currStepsNumber = activityLogForm.nextStepsNumber
                currentLastStep.clientActionTs = activityLogForm.clientActionTs
                currentLastStep.otherData = activityLogForm.otherData
                currentLastStep.otherGameStepData = activityLogForm.otherGameStepData
                currentLastStep.otherSolutionStepData = activityLogForm.otherSolutionStepData
                currentLastStep.serverActionTs = Timestamp(System.currentTimeMillis())
                currentLastStep.userCleared = false
                lastStepLogRepository.save(currentLastStep)
            }
        } else if (action.isEnd()) { // check if result -> create ResultLog
            val resultLog = ResultLog(
                app = app, taskset = tasksetHistory, task = taskHistory, autoSubTask = autoSubTask,
                user = user, difficulty = activityLogForm.difficulty, baseAward = activityLogForm.baseAward,
                currTimeMs = activityLogForm.currTimeMs, currStepsNumber = activityLogForm.currStepsNumber,
                clientActionTs = activityLogForm.clientActionTs, qualityData = activityLogForm.qualityData,
                serverActionTs = Timestamp(System.currentTimeMillis())
            )
            resultLogRepository.save(resultLog)
        }
        activityLogRepository.save(activityLog)
    }

    @Transactional
    fun findActivityByForm(activityLogSearchForm: ActivityLogSearchForm): List<ActivityLog> =
        activityLogRepository.findByAppAndTasksetAndTaskAndAutoSubTaskAndUserNative(
            appCode = activityLogSearchForm.appCode,
            tasksetCode = activityLogSearchForm.tasksetCode,
            tasksetVersion = activityLogSearchForm.tasksetVersion,
            taskCode = activityLogSearchForm.taskCode,
            taskVersion = activityLogSearchForm.taskVersion,
            autoSubTaskCode = activityLogSearchForm.autoSubTaskCode,
            userCode = activityLogSearchForm.userCode,
            limit = activityLogSearchForm.limit,
            offset = activityLogSearchForm.offset
        )

    @Transactional
    fun findResultByForm(activityLogSearchForm: ActivityLogSearchForm): List<ResultLog> =
        resultLogRepository.findByAppAndTasksetAndTaskAndAutoSubTaskAndUserNative(
            appCode = activityLogSearchForm.appCode,
            tasksetCode = activityLogSearchForm.tasksetCode,
            tasksetVersion = activityLogSearchForm.tasksetVersion,
            taskCode = activityLogSearchForm.taskCode,
            taskVersion = activityLogSearchForm.taskVersion,
            autoSubTaskCode = activityLogSearchForm.autoSubTaskCode,
            userCode = activityLogSearchForm.userCode,
            limit = activityLogSearchForm.limit,
            offset = activityLogSearchForm.offset
        )

    @Transactional
    fun findActivityAll(): List<ActivityLog> = activityLogRepository.findAll()

    @Transactional
    fun findResultAll(): List<ResultLog> = resultLogRepository.findAll()

    @Transactional
    fun findResultTasksetsByForm(resultTasksetSearchForm: ResultTasksetSearchForm?): List<ResultTasksetForm> {
        val res = arrayListOf<ResultTasksetForm>()
        val resAny = resultLogRepository.findResultTasksetByParamsNative(
            appCode = resultTasksetSearchForm?.appCode,
            userCode = resultTasksetSearchForm?.userCode,
            // TODO: remove oldrefmap
            sortBy = TasksetOldRefMap[resultTasksetSearchForm?.sortedBy],
            sortByType = if (resultTasksetSearchForm?.descending == true) "desc" else "asc",
            limit = resultTasksetSearchForm?.limit ?: 1000,
            offset = resultTasksetSearchForm?.offset ?: 0,
            onlyNew = resultTasksetSearchForm?.onlyNew ?: false
        )
        resAny.forEach {
            val arr = ObjectMapper().convertValue(it, List::class.java)
            res.add(ResultTasksetForm(
                appCode = arr[0] as? String,
                gameCode = arr[1] as? String,
                levelsCount = (arr[2] as? BigInteger)?.toInt(),
                tasksDifficulty = arr[3] as? Double,
                usersCount = (arr[4] as? BigInteger)?.toLong(),
                gameName = arr[5] as? String // TODO: add russian name (, arr[6] as String)
            ))
        }
        return res
    }

    @Transactional
    fun findResultTasksByForm(resultTaskSearchForm: ResultTaskSearchForm?): List<ResultTaskForm> {
        val res = arrayListOf<ResultTaskForm>()
        val resAny = resultLogRepository.findResultTaskByParamsNative(
            appCode = resultTaskSearchForm?.appCode,
            tasksetCode = resultTaskSearchForm?.gameCode,
            userCode = resultTaskSearchForm?.userCode,
            // TODO: remove oldrefmap
            sortBy = TaskOldRefMap[resultTaskSearchForm?.sortedBy],
            sortByType = if (resultTaskSearchForm?.descending == true) "desc" else "asc",
            limit = resultTaskSearchForm?.limit ?: 1000,
            offset = resultTaskSearchForm?.offset ?: 0,
            onlyNew = resultTaskSearchForm?.onlyNew ?: false
        )
        resAny.forEach {
            val arr = ObjectMapper().convertValue(it, List::class.java)
            res.add(ResultTaskForm(
                appCode = arr[0] as? String,
                gameCode = arr[1] as? String,
                gameName = arr[2] as? String,
                // TODO (tasksetNameRu): arr[3] as String,
                levelCode = arr[4] as? String,
                taskNameEn = arr[5] as? String,
                taskNameRu = arr[6] as? String,
                difficulty = (arr[7] as? Double)?.toFloat(),
                steps = (arr[8] as? Double)?.toInt(),
                usersCount = (arr[9] as? BigInteger)?.toLong()
            ))
        }
        return res
    }

    @Transactional
    fun findResultUsersByForm(resultUserSearchForm: ResultUserSearchForm?): List<ResultUserForm> {
        val res = arrayListOf<ResultUserForm>()
        val resAny = resultLogRepository.findResultUserByParamsNative(
                appCode = resultUserSearchForm?.appCode,
                tasksetCode = resultUserSearchForm?.gameCode,
                // TODO: taskCode
                taskCode = resultUserSearchForm?.levelCode,
                // TODO: remove oldrefmap
                sortBy = UserOldRefMap[resultUserSearchForm?.sortedBy] ?: "rating",
                sortByType = if (resultUserSearchForm?.descending == false) "asc" else "desc",
                limit = resultUserSearchForm?.limit ?: 1000,
                offset = resultUserSearchForm?.offset ?: 0,
                onlyNew = resultUserSearchForm?.onlyNew ?: false
        )
        resAny.forEach {
            val arr = ObjectMapper().convertValue(it, List::class.java)
            res.add(ResultUserForm(
                userCode = arr[0] as? String,
                userLogin = arr[1] as? String,
                userName = arr[2] as? String,
                userFullName = arr[3] as? String,
                additionalInfo = arr[4] as? String,
                levelsCount = (arr[5] as? BigInteger)?.toLong(),
                tasksDifficulty = arr[6] as? Double,
                rating = if (arr[7] != null) arr[7] as Double else 0.0
            ))
        }
        return res
    }

    @Transactional
    fun getUserStatistics(appCode: String, userCode: String): UserStatForm {
        val statRows = resultLogRepository.getUserStatisticsNative(appCode, userCode)
        val tasksetStat = arrayListOf<TasksetStat>()
        var curTasksetStat: TasksetStat?
        for (row in statRows) {
            val filterRes = tasksetStat.filter { it.code == row.tasksetCode }
            if (filterRes.isEmpty()) {
                curTasksetStat = TasksetStat(row.tasksetCode, row.passedTotal, row.pausedTotal, arrayListOf())
                tasksetStat.add(curTasksetStat)
            } else {
                curTasksetStat = filterRes[0]
            }
            curTasksetStat.tasksStat.add(TaskStat(
                row.taskCode,
                row.steps,
                row.time,
                if (row.passedNotPaused) StateType.DONE.name else StateType.PAUSED.name,
                row.expression)
            )
        }
        return UserStatForm(tasksetStat)
    }

    @Transactional
    fun clearUserStatistics(appCode: String, userCode: String) {
        lastStepLogRepository.resetLastStepLogByAppAndUser(appCode, userCode)
        resultLogRepository.resetResultLogByAppAndUser(appCode, userCode)
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun getStatisticsForReport(user: User, namespaceCode: String, tasksetCode: String, startDate: Timestamp?, endDate: Timestamp?): List<StatisticForReport> {
        val namespace: Namespace = if (namespaceCode.isNotBlank()) {
            namespaceService.findByCode(namespaceCode)
                ?: throw NotFoundException("Namespace with code $namespaceCode")
        } else {
            val taskset = tasksetRepository.findByCode(tasksetCode)
                ?: throw NotFoundException("Taskset with code $tasksetCode")
            taskset.namespace
        }

        if (!namespaceService.isUserHaveReadAccessToNamespace(namespace, user)) {
            throw NoReadPermissionForNamespaceException(namespaceCode, user.fullName)
        }

        val resAny = resultLogRepository.getStatisticsForReport(namespaceCode, tasksetCode, startDate, endDate)
        val res = arrayListOf<StatisticForReport>()
        resAny.forEach {
            val objectMapper = ObjectMapper()
            objectMapper.setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S"))
            val arr = objectMapper.convertValue(it, List::class.java)
            res.add(
                StatisticForReport(
                    tasksetCode = arr[0] as String,
                    tasksetVersion = arr[1] as Int,
                    taskCode = arr[2] as String,
                    taskVersion = arr[3] as Int,
                    taskNameRu = arr[4] as String,
                    taskNameEn = arr[5] as String,
                    userCode = arr[6] as String,
                    userLogin = arr[7] as String,
                    userFullName = arr[8] as String,
                    userAdditional = arr[9] as String,
                    stepsNumber = if (arr[10] != null) arr[10] as Double else 0.0,
                    timeMS = if (arr[11] != null) (arr[11] as BigInteger).toLong() else 0,
                    difficulty = arr[12] as Double,
                    clientActionTS = Timestamp.valueOf(arr[13] as String),
                    appName = arr[14] as String
                )
            )
        }

        return res
    }
}