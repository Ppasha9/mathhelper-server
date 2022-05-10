package com.mathhelper.mathhelperserver.services.task

import com.mathhelper.mathhelperserver.constants.*
import com.mathhelper.mathhelperserver.datatables.subject_type.SubjectTypeRepository
import com.mathhelper.mathhelperserver.datatables.tasks.*
import com.mathhelper.mathhelperserver.datatables.taskset.Taskset
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePackHistoryRepository
import com.mathhelper.mathhelperserver.datatables_history.tasks.*
import com.mathhelper.mathhelperserver.forms.task.TaskForm
import com.mathhelper.mathhelperserver.forms.task.TaskLinkForm
import com.mathhelper.mathhelperserver.services.common.CommonService
import com.mathhelper.mathhelperserver.services.namespace.NamespaceService
import com.mathhelper.mathhelperserver.services.rule_pack.RulePackService
import com.mathhelper.mathhelperserver.services.taskset.TasksetService
import com.mathhelper.mathhelperserver.utils.obtainKeywords
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException
import java.sql.Timestamp
import java.time.OffsetDateTime

@Service
class TaskService {
    @Autowired
    private lateinit var taskRepository: TaskRepository
    @Autowired
    private lateinit var taskHistoryRepository: TaskHistoryRepository
    @Autowired
    private lateinit var autoSubTaskRepository: AutoSubTaskRepository

    @Autowired
    private lateinit var task2RulePackRepository: Task2RulePackRepository
    @Autowired
    private lateinit var task2RulePackHistoryRepository: Task2RulePackHistoryRepository

    @Autowired
    private lateinit var rulePackService: RulePackService

    @Autowired
    private lateinit var namespaceService: NamespaceService

    @Autowired
    private lateinit var tasksetService: TasksetService

    @Autowired
    private lateinit var subjectTypeRepository: SubjectTypeRepository

    @Autowired
    private lateinit var rulePackHistoryRepository: RulePackHistoryRepository

    @Autowired
    private lateinit var tagRepository: TagRepository

    @Autowired
    private lateinit var task2TagRepository: Task2TagRepository

    @Autowired
    private lateinit var commonService: CommonService

    fun existsByCode(taskCode: String): Boolean = taskRepository.existsByCode(taskCode)

    @Transactional
    fun findByCode(taskCode: String): Task? = taskRepository.findByCode(taskCode)

    @Transactional
    fun findActiveHistoryByCode(taskCode: String): TaskHistory? = taskHistoryRepository.findByIdCodeAndIsActiveTrue(taskCode)

    @Transactional
    fun save(task: Task) = taskRepository.save(task)

    @Transactional
    fun filterTasks(limit: Int?, offset: Int?, substring: String?, keywords: String?, sortBy: String?, descending: Boolean?,
                    namespace: String?, authorUserCode: String?, subjectType: String?, currUser: User?): List<Task> {
        logger.info("Filtering tasks in TaskService")
        if (sortBy != null && !(sortBy == "name_ru" || sortBy == "name_en")) {
            return listOf()
        }

        if (subjectType != null && !subjectTypeRepository.existsByName(subjectType)) {
            return listOf()
        }

        val tasks: ArrayList<Task>
        if (keywords.isNullOrBlank()) {
            tasks = taskRepository.findByLimitAndOffsetAndSubstrAndSortByAndDescendingAndNamespaceAndAuthorUserCodeAndSubjectTypeNative(
                limit = limit?.toString() ?: "all",
                offset = offset ?: 0,
                substring = if (substring.isNullOrBlank()) "" else substring.replace("\"", ""),
                sortBy = if (sortBy.isNullOrBlank()) "name_ru" else sortBy.replace("\"", ""),
                sortByType = if (descending == true) "desc" else "asc",
                namespace = if (namespace.isNullOrBlank()) "" else namespace.replace("\"", ""),
                authorUserCode = if (authorUserCode.isNullOrBlank()) "" else authorUserCode.replace("\"", ""),
                subjectType = if (subjectType.isNullOrBlank()) null else subjectTypeRepository.findByName(subjectType)
            ) as ArrayList<Task>
        } else {
            val limitSearch = Math.min(Math.max(limit
                ?: 0, Constants.KEYWORDS_SEARCHING_LIMIT_MIN_SIZE), Constants.KEYWORDS_SEARCHING_LIMIT_MAX_SIZE)
            val offsetSearch = offset ?: 0
            val obtainKeywords = obtainKeywords(keywords, taskRepository)

            if (obtainKeywords.keywordsFormatted == null) {
                return listOf()
            }

            tasks = findByKeywords(
                keywords = obtainKeywords.keywordsFormatted,
                rowsLimit = limitSearch,
                offset = offsetSearch,
                namespace = if (namespace.isNullOrBlank()) "" else namespace.replace("\"", ""),
                authorUserCode = if (authorUserCode.isNullOrBlank()) "" else authorUserCode.replace("\"", ""),
                subjectType = if (subjectType.isNullOrBlank()) "" else subjectTypeRepository.findByName(subjectType)!!.name
            )
        }

        return namespaceService.filterEntitiesByNamespacesGrantsAndUserForRead(tasks, currUser)
    }

    fun getTaskForm(task: Task): TaskForm {
        logger.info("GetTaskForm")
        val taskHistoryActive = taskHistoryRepository.findByIdCodeAndIsActiveTrue(task.code)!!
        val form = TaskForm(
            namespaceCode = task.namespace.code,
            code = task.code,
            version = taskHistoryActive.id.version,
            nameEn = task.nameEn,
            nameRu = task.nameRu,
            descriptionEn = task.descriptionEn,
            descriptionRu = task.descriptionRu,
            descriptionShortEn = task.descriptionShortEn,
            descriptionShortRu = task.descriptionShortRu,
            subjectType = task.subjectType?.name ?: "",
            originalExpressionPlainText = task.originalExpressionPlainText,
            originalExpressionStructureString = task.originalExpressionStructureString,
            originalExpressionTex = task.originalExpressionTex,
            originalExpression = task.originalExpression,
            goalType = task.goalType.code,
            goalExpressionStructureString = task.goalExpressionStructureString,
            goalExpressionPlainText = task.goalExpressionPlainText,
            goalExpressionTex = task.goalExpressionTex,
            goalExpression = task.goalExpression,
            goalPattern = task.goalPattern,
            otherGoalData = task.otherGoalData,
            stepsNumber = task.stepsNumber,
            time = task.time,
            difficulty = task.difficulty,
            solution = task.solution,
            solutionsStepsTree = task.solutionsStepsTree,
            hints = task.hints,
            otherCheckSolutionData = task.otherCheckSolutionData,
            countOfAutoGeneratedTasks = task.countOfAutoGeneratedTasks ?: 0,
            otherAutoGenerationData = task.otherAutoGenerationData,
            interestingFacts = task.interestingFacts,
            otherAwardData = task.otherAwardData,
            nextRecommendedTasks = task.nextRecommendedTasks,
            otherData = task.otherData,
            rules = task.rules,
            rulePacks = arrayListOf(),
            tags = arrayListOf()
        )

        // fill rule packs
        task2RulePackRepository.findByTaskCode(task.code).forEach {
            form.rulePacks?.add(rulePackService.getRulePackLinkForm(it.rulePack))
        }

        task2TagRepository.findByTaskCode(task.code).forEach {
            form.tags?.add(it.tag.code)
        }

        return form
    }

    fun getTaskLinkForm(task: Task): TaskLinkForm {
        logger.info("getTaskLinkForm")
        val taskHistoryActive = taskHistoryRepository.findByIdCodeAndIsActiveTrue(task.code)!!
        val form = TaskLinkForm(
            namespaceCode = task.namespace.code,
            code = task.code,
            version = taskHistoryActive.id.version,
            nameEn = task.nameEn,
            nameRu = task.nameRu,
            descriptionEn = task.descriptionEn,
            descriptionRu = task.descriptionRu,
            descriptionShortEn = task.descriptionShortEn,
            descriptionShortRu = task.descriptionShortRu,
            subjectType = task.subjectType?.name ?: "",
            stepsNumber = task.stepsNumber,
            time = task.time,
            difficulty = task.difficulty,
            tags = arrayListOf()
        )
        task2TagRepository.findByTaskCode(task.code).forEach {
            form.tags?.add(it.tag.code)
        }
        return form
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun validateTaskForm(taskForm: TaskForm) {
        val namespace = namespaceService.findByCode(taskForm.namespaceCode) ?:
            throw NotFoundException("Namespace with code = ${taskForm.namespaceCode}")

        if (taskForm.nameEn.isNullOrBlank() && taskForm.nameRu.isNullOrBlank()) {
            throw BadRequestWithErrorException("All names are empty.")
        }
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun createFrom(taskForm: TaskForm, user: User) {
        logger.info("createFrom")
        logger.info("Params: taskForm=$taskForm, user=$user")

        try {
            validateTaskForm(taskForm)
        } catch (e: RuntimeException) {
            throw e
        }

        val namespace = namespaceService.findByCode(taskForm.namespaceCode)!!
        val entityName = if (!taskForm.nameEn.isNullOrBlank()) taskForm.nameEn!! else taskForm.nameRu!!
        val taskCode = if (taskForm.code != null && !taskRepository.existsByCode(taskForm.code!!)) {
            taskForm.code!!
        } else {
            commonService.generateCode(
                    entityName = entityName,
                    entityNamespaceCode = taskForm.namespaceCode,
                    entityRepository = taskRepository
            )
        }

        val keywordsList = taskForm.nameEn!!.split(" ", ",", "-", ";", "/", "\\") +
            taskForm.nameRu!!.split(" ", ",", "-", ";", "/", "\\")
        var keywords = ""
        for (keyword in keywordsList)
        {
            val word = keyword.replace(" ", "")
            if (word !in keywords)
                keywords +=  "$word "
        }

        val now = OffsetDateTime.now()
        val task = Task(
            code = taskCode,
            namespace = namespace,
            keywords = keywords,
            nameEn = taskForm.nameEn ?: "",
            nameRu = taskForm.nameRu ?: "",
            descriptionEn = taskForm.descriptionEn ?: "",
            descriptionRu = taskForm.descriptionRu ?: "",
            descriptionShortEn = taskForm.descriptionShortEn ?: "",
            descriptionShortRu = taskForm.descriptionShortRu ?: "",
            subjectType = if (taskForm.subjectType != null) subjectTypeRepository.findByName(taskForm.subjectType!!) else null,
            originalExpressionTex = taskForm.originalExpressionTex,
            originalExpressionStructureString = taskForm.originalExpressionStructureString,
            originalExpressionPlainText = taskForm.originalExpressionPlainText,
            originalExpression = taskForm.originalExpression,
            goalType = GoalType.fromString(taskForm.goalType.toUpperCase()) ?: GoalType.UNKNOWN,
            goalExpressionTex = taskForm.goalExpressionTex,
            goalExpressionPlainText = taskForm.goalExpressionPlainText,
            goalExpressionStructureString = taskForm.goalExpressionStructureString,
            goalExpression = taskForm.goalExpression,
            goalPattern = taskForm.goalPattern,
            otherGoalData = taskForm.otherGoalData,
            stepsNumber = taskForm.stepsNumber,
            time = taskForm.time,
            difficulty = taskForm.difficulty,
            solution = taskForm.solution,
            solutionsStepsTree = taskForm.solutionsStepsTree,
            rules = taskForm.rules,
            hints = taskForm.hints,
            otherCheckSolutionData = taskForm.otherCheckSolutionData,
            countOfAutoGeneratedTasks = taskForm.countOfAutoGeneratedTasks,
            authorUser = user,
            otherAutoGenerationData = taskForm.otherAutoGenerationData,
            interestingFacts = taskForm.interestingFacts,
            otherAwardData = taskForm.otherAwardData,
            nextRecommendedTasks = taskForm.nextRecommendedTasks,
            otherData = taskForm.otherData,
            serverActionTs = Timestamp(System.currentTimeMillis())
        )

        val taskHistory = TaskHistory(
            id = HistoryId(task.code, 0),
            namespace = namespace,
            keywords = keywords,
            nameEn = task.nameEn,
            nameRu = task.nameRu,
            descriptionEn = task.descriptionEn,
            descriptionRu = task.descriptionRu,
            descriptionShortEn = task.descriptionShortEn,
            descriptionShortRu = task.descriptionShortRu,
            subjectType = task.subjectType,
            originalExpressionTex = task.originalExpressionTex,
            originalExpressionStructureString = task.originalExpressionStructureString,
            originalExpressionPlainText = task.originalExpressionPlainText,
            originalExpression = task.originalExpression,
            goalType = task.goalType,
            goalExpressionTex = task.goalExpressionTex,
            goalExpressionPlainText = task.goalExpressionPlainText,
            goalExpressionStructureString = task.goalExpressionStructureString,
            goalExpression = task.goalExpression,
            goalPattern = task.goalPattern,
            otherGoalData = task.otherGoalData,
            stepsNumber = task.stepsNumber,
            time = task.time,
            difficulty = task.difficulty,
            solution = task.solution,
            solutionsStepsTree = task.solutionsStepsTree,
            rules = task.rules,
            hints = task.hints,
            otherCheckSolutionData = task.otherCheckSolutionData,
            authorUser = user,
            otherAutoGenerationData = task.otherAutoGenerationData,
            countOfAutoGeneratedTasks = task.countOfAutoGeneratedTasks,
            interestingFacts = task.interestingFacts,
            otherAwardData = task.otherAwardData,
            nextRecommendedTasks = task.nextRecommendedTasks,
            otherData = task.otherData,
            isActive = true,
            activeDateFrom = now,
            activeDateTo = Constants.MAX_TIME
        )

        taskRepository.saveAndFlush(task)
        taskHistoryRepository.saveAndFlush(taskHistory)

        taskForm.rulePacks?.forEach {
            val rulePack = rulePackService.findByCode(it.rulePackCode) ?:
                throw NotFoundException("Rule pack with code=${it.rulePackCode}")
                //return NetworkError.notFound()
            val rulePackHistory = rulePackHistoryRepository.findByIdCodeAndIsActiveTrue(it.rulePackCode) ?:
                throw NotFoundException("Active history row of rule pack by code=${it.rulePackCode}")
                //return NetworkError.notFound()

            val task2RulePack = Task2RulePack(
                task = task,
                rulePack = rulePack
            )
            val task2RulePackHistory = Task2RulePackHistory(
                task = taskHistory,
                rulePack = rulePackHistory,
                isActive = true,
                activeDateFrom = now,
                activeDateTo = Constants.MAX_TIME
            )

            task2RulePackRepository.save(task2RulePack)
            task2RulePackHistoryRepository.save(task2RulePackHistory)
        }

        taskForm.tags?.forEach {
            if (!tagRepository.existsByCode(it) || !tagRepository.existsByCodeAndNamespaceCode(it, namespace.code)) {
                tagRepository.saveAndFlush(Tag(
                    code = it,
                    namespace = namespace
                ))
            }

            val tag = tagRepository.findByCodeAndNamespaceCode(it, namespace.code)!!
            task2TagRepository.save(Task2Tag(
                task = task,
                tag = tag
            ))
        }
    }

    fun needToUpdateByForm(task: Task, taskForm: TaskForm): Boolean {
        if (task.nameEn != (taskForm.nameEn ?: "")) {
            return true
        } else if (task.nameRu != (taskForm.nameRu ?: "")) {
            return true
        } else if (task.descriptionEn != (taskForm.descriptionEn ?: "")) {
            return true
        } else if (task.descriptionRu != (taskForm.descriptionRu ?: "")) {
            return true
        } else if (task.descriptionShortEn != (taskForm.descriptionShortEn ?: "")) {
            return true
        } else if (task.descriptionShortRu != (taskForm.descriptionShortRu ?: "")) {
            return true
        } else if (task.subjectType != (if (taskForm.subjectType != null) subjectTypeRepository.findByName(taskForm.subjectType!!) else null)) {
            return true
        } else if (task.originalExpressionTex != taskForm.originalExpressionTex) {
            return true
        } else if (task.originalExpressionStructureString != taskForm.originalExpressionStructureString) {
            return true
        } else if (task.originalExpressionPlainText != taskForm.originalExpressionPlainText) {
            return true
        } else if (task.originalExpression != taskForm.originalExpression) {
            return true
        } else if (task.goalType != (GoalType.fromString(taskForm.goalType.toUpperCase()) ?: GoalType.UNKNOWN)) {
            return true
        } else if (task.goalExpressionTex != taskForm.goalExpressionTex) {
            return true
        } else if (task.goalExpressionPlainText != taskForm.goalExpressionPlainText) {
            return true
        } else if (task.goalExpressionStructureString != taskForm.goalExpressionStructureString) {
            return true
        } else if (task.goalExpression != taskForm.goalExpression) {
            return true
        } else if (task.goalPattern != taskForm.goalPattern) {
            return true
        } else if (task.otherGoalData != taskForm.otherGoalData) {
            return true
        } else if (task.stepsNumber != taskForm.stepsNumber) {
            return true
        } else if (task.time != taskForm.time) {
            return true
        } else if (task.difficulty != taskForm.difficulty) {
            return true
        } else if (task.solution != taskForm.solution) {
            return true
        } else if (task.solutionsStepsTree != taskForm.solutionsStepsTree) {
            return true
        } else if (task.rules != taskForm.rules) {
            return true
        } else if (task.hints != taskForm.hints) {
            return true
        } else if (task.otherCheckSolutionData != taskForm.otherCheckSolutionData) {
            return true
        } else if (task.countOfAutoGeneratedTasks != taskForm.countOfAutoGeneratedTasks) {
            return true
        } else if (task.otherAutoGenerationData != taskForm.otherAutoGenerationData) {
            return true
        } else if (task.interestingFacts != taskForm.interestingFacts) {
            return true
        } else if (task.otherAwardData != taskForm.otherAwardData) {
            return true
        } else if (task.nextRecommendedTasks != taskForm.nextRecommendedTasks) {
            return true
        } else if (task.otherData != taskForm.otherData) {
            return true
        }

        val currTaskRulePacks = task2RulePackRepository.findByTaskCode(task.code)
        val currTaskRulePacksCodes = currTaskRulePacks.map { it.rulePack.code }
        val newRulePacksCodes = if (taskForm.rulePacks == null) arrayListOf() else taskForm.rulePacks!!.map { it.rulePackCode }
        if (!currTaskRulePacksCodes.containsAll(newRulePacksCodes) || !newRulePacksCodes.containsAll(currTaskRulePacksCodes)) {
            return true
        }

        val currTags = task2TagRepository.findByTaskCode(task.code)
        val currTagsCodes = currTags.map { it.tag.code }
        val newTagsCodes = if (taskForm.tags == null) arrayListOf() else taskForm.tags!!
        if (!currTagsCodes.containsAll(newTagsCodes) || !newTagsCodes.containsAll(currTagsCodes)) {
            return true
        }

        return false
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun updateWith(taskForm: TaskForm, user: User) {
        logger.info("updateWith")
        logger.info("Params: taskForm=$taskForm, user=$user")

        if (taskForm.code == null) {
            throw CommonException("You should provide code of Task that you want to update")
        }

        val code = taskForm.code!!
        if (!taskRepository.existsByCode(code))
            throw NotFoundException("Task with code $code")

        val activeTask = findByCode(code) ?: throw NotFoundException("Task with code=$code")
        if (!needToUpdateByForm(activeTask, taskForm)) {
            return
        }

        val activeHistoryTask = taskHistoryRepository.findByIdCodeAndIsActiveTrue(code) ?:
            throw NotFoundException("Active history row of task by code=$code")
        val now = OffsetDateTime.now()

        activeTask.nameEn = taskForm.nameEn ?: ""
        activeTask.nameRu = taskForm.nameRu ?: ""
        activeTask.descriptionEn = taskForm.descriptionEn ?: ""
        activeTask.descriptionRu = taskForm.descriptionRu ?: ""
        activeTask.descriptionShortEn = taskForm.descriptionShortEn ?: ""
        activeTask.descriptionShortRu = taskForm.descriptionShortRu ?: ""
        activeTask.subjectType = if (taskForm.subjectType != null) subjectTypeRepository.findByName(taskForm.subjectType!!) else null
        activeTask.originalExpressionTex = taskForm.originalExpressionTex
        activeTask.originalExpressionStructureString = taskForm.originalExpressionStructureString
        activeTask.originalExpressionPlainText = taskForm.originalExpressionPlainText
        activeTask.originalExpression = taskForm.originalExpression
        activeTask.goalType = GoalType.fromString(taskForm.goalType.toUpperCase()) ?: GoalType.UNKNOWN
        activeTask.goalExpressionTex = taskForm.goalExpressionTex
        activeTask.goalExpressionPlainText = taskForm.goalExpressionPlainText
        activeTask.goalExpressionStructureString = taskForm.goalExpressionStructureString
        activeTask.goalExpression = taskForm.goalExpression
        activeTask.goalPattern = taskForm.goalPattern
        activeTask.otherGoalData = taskForm.otherGoalData
        activeTask.stepsNumber = taskForm.stepsNumber
        activeTask.time = taskForm.time
        activeTask.difficulty = taskForm.difficulty
        activeTask.solution = taskForm.solution
        activeTask.solutionsStepsTree = taskForm.solutionsStepsTree
        activeTask.rules = taskForm.rules
        activeTask.hints = taskForm.hints
        activeTask.otherCheckSolutionData = taskForm.otherCheckSolutionData
        activeTask.countOfAutoGeneratedTasks = taskForm.countOfAutoGeneratedTasks
        activeTask.otherAutoGenerationData = taskForm.otherAutoGenerationData
        activeTask.interestingFacts = taskForm.interestingFacts
        activeTask.otherAwardData = taskForm.otherAwardData
        activeTask.nextRecommendedTasks = taskForm.nextRecommendedTasks
        activeTask.otherData = taskForm.otherData
        activeTask.serverActionTs = Timestamp(System.currentTimeMillis())

        val keywordsList = activeTask.nameEn.split(" ", ",", "-", ";", "/", "\\") +
            activeTask.nameRu.split(" ", ",", "-", ";", "/", "\\")
        var keywords = ""
        for (keyword in keywordsList)
        {
            val word = keyword.replace(" ", "")
            if (word !in keywords)
                keywords +=  "$word "
        }

        activeTask.keywords = keywords

        val newHistoryTask = TaskHistory(
            id = HistoryId(activeTask.code, activeHistoryTask.id.version + 1),
            namespace = activeHistoryTask.namespace,
            keywords = keywords,
            nameEn = activeTask.nameEn,
            nameRu = activeTask.nameRu,
            descriptionEn = activeTask.descriptionEn,
            descriptionRu = activeTask.descriptionRu,
            descriptionShortEn = activeTask.descriptionShortEn,
            descriptionShortRu = activeTask.descriptionShortRu,
            subjectType = activeTask.subjectType,
            originalExpressionTex = activeTask.originalExpressionTex,
            originalExpressionStructureString = activeTask.originalExpressionStructureString,
            originalExpressionPlainText = activeTask.originalExpressionPlainText,
            originalExpression = activeTask.originalExpression,
            goalType = activeTask.goalType,
            goalExpressionTex = activeTask.goalExpressionTex,
            goalExpressionPlainText = activeTask.goalExpressionPlainText,
            goalExpressionStructureString = activeTask.goalExpressionStructureString,
            goalExpression = activeTask.goalExpression,
            goalPattern = activeTask.goalPattern,
            otherGoalData = activeTask.otherGoalData,
            stepsNumber = activeTask.stepsNumber,
            time = activeTask.time,
            difficulty = activeTask.difficulty,
            solution = activeTask.solution,
            solutionsStepsTree = activeTask.solutionsStepsTree,
            rules = activeTask.rules,
            hints = activeTask.hints,
            otherCheckSolutionData = activeTask.otherCheckSolutionData,
            authorUser = user,
            otherAutoGenerationData = activeTask.otherAutoGenerationData,
            countOfAutoGeneratedTasks = activeTask.countOfAutoGeneratedTasks,
            interestingFacts = activeTask.interestingFacts,
            otherAwardData = activeTask.otherAwardData,
            nextRecommendedTasks = activeTask.nextRecommendedTasks,
            otherData = activeTask.otherData,
            isActive = true,
            activeDateFrom = now,
            activeDateTo = Constants.MAX_TIME
        )

        activeHistoryTask.isActive = false
        activeHistoryTask.activeDateTo = now

        taskRepository.saveAndFlush(activeTask)
        taskHistoryRepository.saveAndFlush(activeHistoryTask)
        taskHistoryRepository.saveAndFlush(newHistoryTask)

        // --- handle rule packs ---
        val currTaskRulePacks = task2RulePackRepository.findByTaskCode(code)
        val currTaskRulePacksCodes = currTaskRulePacks.map { it.rulePack.code }
        val newRulePacks = if (taskForm.rulePacks == null) arrayListOf() else taskForm.rulePacks!!.map { it.rulePackCode }
        currTaskRulePacks.forEach {
            if (!newRulePacks.contains(it.rulePack.code)) {
                task2RulePackRepository.delete(it)

                // deactivate history row
                val rulePackHistoryRow = rulePackHistoryRepository.findByIdCodeAndIsActiveTrue(it.rulePack.code) ?:
                    throw NotFoundException("History row for rule pack with code=${it.rulePack.code}")

                val activeHistoryRow = task2RulePackHistoryRepository.findByTaskIdAndRulePackId(
                    taskId = activeHistoryTask.id,
                    rulePackId = rulePackHistoryRow.id
                ) ?: throw NotFoundException("Previous history row for link between task" +
                    " with code=${taskForm.code} and rule pack with code=${it.rulePack.code}")

                activeHistoryRow.isActive = false
                activeHistoryRow.activeDateTo = now
                task2RulePackHistoryRepository.saveAndFlush(activeHistoryRow)
            }
        }

        newRulePacks.forEach {
            if (!currTaskRulePacksCodes.contains(it)) {
                val rulePack = rulePackService.findByCode(it) ?:
                    throw NotFoundException("Rule pack with code=$it")
                task2RulePackRepository.saveAndFlush(Task2RulePack(task = activeTask, rulePack = rulePack))

                // add history row
                val rulePackHistory = rulePackHistoryRepository.findByIdCodeAndIsActiveTrue(it) ?:
                    throw NotFoundException("Current active history row for rule pack with code=$it")

                task2RulePackHistoryRepository.saveAndFlush(Task2RulePackHistory(
                    task = newHistoryTask,
                    rulePack = rulePackHistory,
                    isActive = true,
                    activeDateFrom = now,
                    activeDateTo = Constants.MAX_TIME
                ))
            }
        }

        // --- handle tags ---
        val currTags = task2TagRepository.findByTaskCode(code)
        val currTagsCodes = currTags.map { it.tag.code }
        val newTagsCodes = if (taskForm.tags == null) arrayListOf() else taskForm.tags!!
        currTags.forEach {
            if (!newTagsCodes.contains(it.tag.code)) {
                task2TagRepository.delete(it)
            }
        }

        newTagsCodes.forEach {
            if (!currTagsCodes.contains(it)) {
                if (!tagRepository.existsByCode(it) || !tagRepository.existsByCodeAndNamespaceCode(it, activeTask.namespace.code)) {
                    tagRepository.saveAndFlush(Tag(
                        code = it,
                        namespace = activeTask.namespace
                    ))
                }

                val tag = tagRepository.findByCodeAndNamespaceCode(it, activeTask.namespace.code)!!
                task2TagRepository.saveAndFlush(Task2Tag(
                    task = activeTask,
                    tag = tag
                ))
            }
        }
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun updateFromTaskset(taskset: Taskset, taskForm: TaskForm, user: User) {
        updateWith(taskForm, user)
        tasksetService.updateTasksetsHistoryConnectionsFromTask(taskRepository.findByCode(taskForm.code!!)!!, arrayListOf(taskset.code))
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun updateFromController(taskForm: TaskForm, user: User) {
        updateWith(taskForm, user)
        tasksetService.updateTasksetsHistoryConnectionsFromTask(taskRepository.findByCode(taskForm.code!!)!!, arrayListOf())
    }

    fun findByKeywords(keywords: String, rowsLimit: Int, offset: Int, namespace: String, authorUserCode: String, subjectType: String) : ArrayList<Task> {
        val ids = taskRepository.findByKeywordsAndRowsLimitAndOffsetAndNamespaceAndAuthorUserCodeAndSubjectTypeNative(keywords, rowsLimit, offset, namespace, authorUserCode, subjectType)

        return taskRepository.findByCodeIn(ids) as ArrayList<Task>
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("logic-logs")
    }
}