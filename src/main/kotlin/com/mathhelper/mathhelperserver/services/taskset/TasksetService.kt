package com.mathhelper.mathhelperserver.services.taskset

import com.mathhelper.mathhelperserver.constants.*
import com.mathhelper.mathhelperserver.datatables.subject_type.SubjectTypeRepository
import com.mathhelper.mathhelperserver.datatables.tasks.Tag
import com.mathhelper.mathhelperserver.datatables.tasks.TagRepository
import com.mathhelper.mathhelperserver.datatables.tasks.Task
import com.mathhelper.mathhelperserver.datatables.tasks.TaskRepository
import com.mathhelper.mathhelperserver.datatables.taskset.*
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.mathhelper.mathhelperserver.datatables_history.tasks.TaskHistoryRepository
import com.mathhelper.mathhelperserver.datatables_history.taskset.*
import com.mathhelper.mathhelperserver.forms.taskset.TasksetCuttedLinkForm
import com.mathhelper.mathhelperserver.forms.taskset.TasksetForm
import com.mathhelper.mathhelperserver.forms.taskset.TasksetLinkForm
import com.mathhelper.mathhelperserver.services.common.CommonService
import com.mathhelper.mathhelperserver.services.namespace.NamespaceService
import com.mathhelper.mathhelperserver.services.task.TaskService
import com.mathhelper.mathhelperserver.utils.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.Math.max
import java.lang.Math.min
import java.lang.RuntimeException
import java.sql.Timestamp
import java.time.OffsetDateTime

@Service
class TasksetService {
    @Autowired
    private lateinit var tasksetRepository: TasksetRepository
    @Autowired
    private lateinit var tasksetHistoryRepository: TasksetHistoryRepository

    @Autowired
    private lateinit var taskset2TaskRepository: Taskset2TaskRepository
    @Autowired
    private lateinit var taskset2TaskHistoryRepository: Taskset2TaskHistoryRepository

    @Autowired
    private lateinit var taskset2TagRepository: Taskset2TagRepository
    @Autowired
    private lateinit var tagRepository: TagRepository

    @Autowired
    private lateinit var taskService: TaskService
    @Autowired
    private lateinit var taskRepository: TaskRepository
    @Autowired
    private lateinit var taskHistoryRepository: TaskHistoryRepository

    @Autowired
    private lateinit var namespaceService: NamespaceService

    @Autowired
    private lateinit var subjectTypeRepository: SubjectTypeRepository

    @Autowired
    private lateinit var commonService: CommonService

    @Transactional
    fun findByCode(tasksetCode: String): Taskset? = tasksetRepository.findByCode(tasksetCode)

    @Transactional
    fun findActiveHistoryByCode(code: String): TasksetHistory? = tasksetHistoryRepository.findByIdCodeAndIsActiveTrue(code)

    @Transactional
    fun filterTasksets(limit: Int?, offset: Int?, substring: String?, keywords: String?, sortBy: String?, descending: Boolean?,
                       namespace: String?, authorUserCode: String?, subjectType: String?, currUser: User?, code: String?): List<Taskset> {
        logger.info("Filtering tasksets in TasksetService")
        if (sortBy != null && !(sortBy == "name_ru" || sortBy == "name_en")) {
            return listOf()
        }
        val tasksets: ArrayList<Taskset>
        when {
            !code.isNullOrBlank() -> {
                tasksets = arrayListOf(tasksetRepository.findByCode(code) ?: return listOf())
            }
            keywords.isNullOrBlank() -> {
                tasksets = tasksetRepository.findByLimitAndOffsetAndSubstrAndSortByAndDescendingAndNamespaceAndAuthorUserCodeAndSubjectTypeNative(
                    limit = limit?.toString() ?: "all",
                    offset = offset ?: 0,
                    substring = if (substring.isNullOrBlank()) "" else substring.replace("\"", ""),
                    sortBy = if (sortBy.isNullOrBlank()) "name_ru" else sortBy.replace("\"", ""),
                    sortByType = if (descending == true) "desc" else "asc",
                    namespace = if (namespace.isNullOrBlank()) "" else namespace.replace("\"", ""),
                    authorUserCode = if (authorUserCode.isNullOrBlank()) "" else authorUserCode.replace("\"", ""),
                    subjectType = if (subjectType.isNullOrBlank()) null else subjectTypeRepository.findByName(subjectType)
                ) as ArrayList<Taskset>
            }
            else -> {
                val limitSearch = min(max(limit ?: 0, Constants.KEYWORDS_SEARCHING_LIMIT_MIN_SIZE), Constants.KEYWORDS_SEARCHING_LIMIT_MAX_SIZE)
                val offsetSearch = offset ?: 0
                val obtainKeywords = obtainKeywords(keywords, tasksetRepository)
                if (obtainKeywords.keywordsFormatted == null) {
                    return listOf()
                }
                tasksets = findByKeywords(
                    keywords = obtainKeywords.keywordsFormatted,
                    rowsLimit = limitSearch,
                    offset = offsetSearch,
                    namespace = if (namespace.isNullOrBlank()) "" else namespace.replace("\"", ""),
                    authorUserCode = if (authorUserCode.isNullOrBlank()) "" else authorUserCode.replace("\"", ""),
                    subjectType = if (subjectType.isNullOrBlank()) "" else subjectTypeRepository.findByName(subjectType)!!.name
                )
            }
        }
        return namespaceService.filterEntitiesByNamespacesGrantsAndUserForRead(tasksets, currUser)
    }

    fun getTasksetForm(taskset: Taskset): TasksetForm {
        logger.info("Get taskset form")
        val tasksetHistoryActive = tasksetHistoryRepository.findByIdCodeAndIsActiveTrue(taskset.code)!!
        val form = TasksetForm(
            code = taskset.code,
            version = tasksetHistoryActive.id.version,
            namespaceCode = taskset.namespace.code,
            nameEn = taskset.nameEn,
            nameRu = taskset.nameRu,
            descriptionEn = taskset.descriptionEn,
            descriptionRu = taskset.descriptionRu,
            descriptionShortEn = taskset.descriptionShortEn,
            descriptionShortRu = taskset.descriptionShortRu,
            subjectType = taskset.subjectType?.name ?: "",
            tasks = arrayListOf(),
            recommendedByCommunity = taskset.recommendedByCommunity,
            serverActionTs = taskset.serverActionTs,
            tags = arrayListOf()
        )

        // fill tasks
        taskset2TaskRepository.findByTasksetCode(taskset.code).forEach {
            form.tasks.add(taskService.getTaskForm(it.task))
        }

        taskset2TagRepository.findByTasksetCode(taskset.code).forEach {
            form.tags?.add(it.tag.code)
        }

        return form
    }

    fun getTasksetLinkForm(taskset: Taskset): TasksetLinkForm {
        logger.info("Get taskset link form")
        val tasksetHistoryActive = tasksetHistoryRepository.findByIdCodeAndIsActiveTrue(taskset.code)!!
        val form = TasksetLinkForm(
            code = taskset.code,
            version = tasksetHistoryActive.id.version,
            namespaceCode = taskset.namespace.code,
            nameEn = taskset.nameEn,
            nameRu = taskset.nameRu,
            descriptionEn = taskset.descriptionEn,
            descriptionRu = taskset.descriptionRu,
            descriptionShortEn = taskset.descriptionShortEn,
            descriptionShortRu = taskset.descriptionShortRu,
            subjectType = taskset.subjectType?.name ?: "",
            tasks = arrayListOf(),
            recommendedByCommunity = taskset.recommendedByCommunity,
            tags = arrayListOf()
        )
        // fill tasks links
        taskset2TaskRepository.findByTasksetCode(taskset.code).forEach {
            form.tasks.add(taskService.getTaskLinkForm(it.task))
        }
        taskset2TagRepository.findByTasksetCode(taskset.code).forEach {
            form.tags?.add(it.tag.code)
        }
        return form
    }

    fun getTasksetCuttedLinkForm(taskset: Taskset): TasksetCuttedLinkForm {
        return TasksetCuttedLinkForm(
            code = taskset.code,
            nameRu = taskset.nameRu,
            namespaceCode = taskset.namespace.code
        )
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun createFrom(tasksetForm: TasksetForm, user: User) {
        logger.info("createFrom")
        logger.info("Params: tasksetForm=$tasksetForm, user=$user")

        // Validation
        val namespace = namespaceService.findByCode(tasksetForm.namespaceCode) ?:
            throw NotFoundException("Namespace with code = ${tasksetForm.namespaceCode}")

        val names = tasksetRepository.findNamesOnlyByNamespaceCodeNative(tasksetForm.namespaceCode)
        names.forEach {
            if (tasksetForm.nameEn == it.nameEn || tasksetForm.nameRu == it.nameRu) {
                throw AlreadyExistsWithParamsException("Taskset", "nameRu or nameEn")
            }
        }

        if (tasksetForm.nameEn.isNullOrBlank() && tasksetForm.nameRu.isNullOrBlank()) {
            throw BadRequestWithErrorException("All names are empty.")
        }

        val entityName = if (!tasksetForm.nameEn.isNullOrBlank()) tasksetForm.nameEn!! else tasksetForm.nameRu!!
        val tasksetCode = if (tasksetForm.code != null && !tasksetRepository.existsByCode(tasksetForm.code!!)) {
            tasksetForm.code!!
        } else {
            commonService.generateCode(
                    entityName = entityName,
                    entityNamespaceCode = tasksetForm.namespaceCode,
                    entityRepository = tasksetRepository
            )
        }

        val keywordsList = tasksetForm.nameEn!!.split(" ", ",", "-", ";", "/", "\\") +
            tasksetForm.nameRu!!.split(" ", ",", "-", ";", "/", "\\")
        var keywords = ""
        for (keyword in keywordsList)
        {
            val word = keyword.replace(" ", "")
            if (word !in keywords)
                keywords +=  "$word "
        }

        val now = OffsetDateTime.now()
        val taskset = Taskset(
            code = tasksetCode,
            namespace = namespace,
            keywords = keywords,
            nameRu = tasksetForm.nameRu ?: "",
            nameEn = tasksetForm.nameEn ?: "",
            descriptionShortRu = tasksetForm.descriptionShortRu ?: "",
            descriptionShortEn = tasksetForm.descriptionShortEn ?: "",
            descriptionRu = tasksetForm.descriptionRu ?: tasksetForm.descriptionShortRu ?: "",
            descriptionEn = tasksetForm.descriptionEn ?: tasksetForm.descriptionShortEn ?: "",
            subjectType = if (tasksetForm.subjectType != null) subjectTypeRepository.findByName(tasksetForm.subjectType!!) else null,
            authorUser = user,
            recommendedByCommunity = tasksetForm.recommendedByCommunity,
            otherData = tasksetForm.otherData,
            serverActionTs = Timestamp(System.currentTimeMillis())
        )
        val tasksetHistory = TasksetHistory(
            id = HistoryId(code = taskset.code, version = 0),
            namespace = namespace,
            keywords = keywords,
            nameEn = taskset.nameEn,
            nameRu = taskset.nameRu,
            descriptionShortRu = taskset.descriptionShortRu,
            descriptionShortEn = taskset.descriptionShortEn,
            descriptionRu = taskset.descriptionRu,
            descriptionEn = taskset.descriptionEn,
            subjectType = taskset.subjectType,
            authorUser = user,
            recommendedByCommunity = taskset.recommendedByCommunity,
            otherData = taskset.otherData,
            isActive = true,
            activeDateTo = Constants.MAX_TIME,
            activeDateFrom = now
        )

        tasksetRepository.saveAndFlush(taskset)
        tasksetHistoryRepository.saveAndFlush(tasksetHistory)

        tasksetForm.tasks.forEach {
            try {
                taskService.validateTaskForm(it)
            } catch (e: RuntimeException) {
                throw e
            }

            val taskName = if (!it.nameEn.isNullOrBlank()) it.nameEn!! else it.nameRu!!
            val taskCode = if (it.code != null) {
                it.code!!
            } else {
                commonService.generateCode(
                        entityName = taskName,
                        entityNamespaceCode = it.namespaceCode,
                        entityRepository = taskService
                )
            }

            if (!taskService.existsByCode(taskCode)) {
                try {
                    taskService.createFrom(it, user)
                } catch (e: RuntimeException) {
                    throw CommonException("Cannot create task with code ${taskCode}. Error: ${e.message!!}")
                }
            } else {
                try {
                    taskService.updateFromTaskset(taskset, it, user)
                } catch (e: RuntimeException) {
                    throw CommonException("Cannot update task with code ${taskCode}. Error: ${e.message!!}")
                }
            }

            val task = taskService.findByCode(taskCode) ?: throw NotFoundException("Task with code=${taskCode}")
            val taskHistory = taskService.findActiveHistoryByCode(taskCode)
                ?: throw NotFoundException("History row for task with code=${taskCode}")

            val taskset2Task = Taskset2Task(
                taskset = taskset,
                task = task
            )
            val taskset2TaskHistory = Taskset2TaskHistory(
                taskset = tasksetHistory,
                task = taskHistory,
                isActive = true,
                activeDateFrom = now,
                activeDateTo = Constants.MAX_TIME
            )

            taskset2TaskRepository.save(taskset2Task)
            taskset2TaskHistoryRepository.save(taskset2TaskHistory)
        }

        tasksetForm.tags?.forEach {
            if (!tagRepository.existsByCode(it) || !tagRepository.existsByCodeAndNamespaceCode(it, namespace.code)) {
                tagRepository.saveAndFlush(Tag(
                    code = it,
                    namespace = namespace
                ))
            }

            val tag = tagRepository.findByCodeAndNamespaceCode(it, namespace.code)!!
            taskset2TagRepository.save(Taskset2Tag(
                taskset = taskset,
                tag = tag
            ))
        }
    }

    fun needToUpdateByForm(taskset: Taskset, tasksetForm: TasksetForm): Boolean {
        if (taskset.nameEn != (tasksetForm.nameEn ?: "")) {
            return true
        } else if (taskset.nameRu != (tasksetForm.nameRu ?: "")) {
            return true
        } else if (taskset.descriptionEn != (tasksetForm.descriptionEn ?: "")) {
            return true
        } else if (taskset.descriptionRu != (tasksetForm.descriptionRu ?: "")) {
            return true
        } else if (taskset.descriptionShortEn != (tasksetForm.descriptionShortEn ?: "")) {
            return true
        } else if (taskset.descriptionShortRu != (tasksetForm.descriptionShortRu ?: "")) {
            return true
        } else if (taskset.subjectType != (if (tasksetForm.subjectType != null) subjectTypeRepository.findByName(tasksetForm.subjectType!!) else null)) {
            return true
        } else if (taskset.recommendedByCommunity != tasksetForm.recommendedByCommunity) {
            return true
        } else if (taskset.otherData != tasksetForm.otherData) {
            return true
        }

        val currTasksetTasksCodes = taskset2TaskRepository.findByTasksetCode(taskset.code).map { it.task.code }
        val newTasksetTasksCodes = tasksetForm.tasks.map { it.code!! }
        val newTasksetTasksForms = tasksetForm.tasks

        if (!currTasksetTasksCodes.containsAll(newTasksetTasksCodes) || !newTasksetTasksCodes.containsAll(currTasksetTasksCodes)) {
            return true
        }

        newTasksetTasksForms.forEach {
            if (taskService.needToUpdateByForm(taskService.findByCode(it.code!!)!!, it)) {
                return true
            }
        }

        val currTags = taskset2TagRepository.findByTasksetCode(taskset.code)
        val currTagsCodes = currTags.map { it.tag.code }
        val newTagsCodes = if (tasksetForm.tags == null) arrayListOf() else tasksetForm.tags!!

        if (!currTagsCodes.containsAll(newTagsCodes) || !newTagsCodes.containsAll(currTagsCodes)) {
            return true
        }

        return false
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun updateWith(tasksetForm: TasksetForm, user: User) {
        logger.info("updateWith")
        logger.info("Params: tasksetForm=$tasksetForm, user=$user")

        if (tasksetForm.code == null) {
            throw CommonException("You should provide code of Taskset that you want to update")
        }

        val code = tasksetForm.code!!
        if (!tasksetRepository.existsByCode(code))
            throw NotFoundException("Taskset with code ${tasksetForm.code}")

        val activeTaskset = findByCode(code) ?: throw NotFoundException("Taskset with code=$code")
        if (!needToUpdateByForm(activeTaskset, tasksetForm)) {
            return
        }

        val activeHistoryTaskset = tasksetHistoryRepository.findByIdCodeAndIsActiveTrue(code) ?:
            throw NotFoundException("Active history row of taskset by code=$code")
        val now = OffsetDateTime.now()

        activeTaskset.nameEn = tasksetForm.nameEn ?: ""
        activeTaskset.nameRu = tasksetForm.nameRu ?: ""
        activeTaskset.descriptionEn = tasksetForm.descriptionEn ?: ""
        activeTaskset.descriptionRu = tasksetForm.descriptionRu ?: ""
        activeTaskset.descriptionShortEn = tasksetForm.descriptionShortEn ?: ""
        activeTaskset.descriptionShortRu = tasksetForm.descriptionShortRu ?: ""
        activeTaskset.subjectType = if (tasksetForm.subjectType != null) subjectTypeRepository.findByName(tasksetForm.subjectType!!) else null
        activeTaskset.recommendedByCommunity = tasksetForm.recommendedByCommunity
        activeTaskset.otherData = tasksetForm.otherData
        activeTaskset.serverActionTs = Timestamp(System.currentTimeMillis())

        val keywordsList = activeTaskset.nameEn.split(" ", ",", "-", ";", "/", "\\") +
            activeTaskset.nameRu.split(" ", ",", "-", ";", "/", "\\")
        var keywords = ""
        for (keyword in keywordsList)
        {
            val word = keyword.replace(" ", "")
            if (word !in keywords)
                keywords +=  "$word "
        }

        activeTaskset.keywords = keywords

        val newHistoryTaskset = TasksetHistory(
            id = HistoryId(code, version = activeHistoryTaskset.id.version + 1),
            namespace = activeHistoryTaskset.namespace,
            keywords = keywords,
            nameRu = activeTaskset.nameRu,
            nameEn = activeTaskset.nameEn,
            descriptionShortRu = activeTaskset.descriptionShortRu,
            descriptionShortEn = activeTaskset.descriptionShortEn,
            descriptionRu = activeTaskset.descriptionRu,
            descriptionEn = activeTaskset.descriptionEn,
            subjectType = activeTaskset.subjectType,
            recommendedByCommunity = activeTaskset.recommendedByCommunity,
            otherData = activeTaskset.otherData,
            authorUser = user,
            isActive = true,
            activeDateTo = Constants.MAX_TIME,
            activeDateFrom = now
        )

        activeHistoryTaskset.activeDateTo = now
        activeHistoryTaskset.isActive = false

        tasksetRepository.save(activeTaskset)
        tasksetHistoryRepository.save(activeHistoryTaskset)
        tasksetHistoryRepository.save(newHistoryTaskset)

        // --- handle tasks ---
        val currTasksetTasks = taskset2TaskRepository.findByTasksetCode(code)
        val currTasksetTasksCodes = currTasksetTasks.map { it.task.code }
        val newTasksCodes = tasksetForm.tasks.map { it.code }

        tasksetForm.tasks.forEach {
            try {
                taskService.validateTaskForm(it)
            } catch (e: RuntimeException) {
                throw e
            }

            val taskName = if (!it.nameEn.isNullOrBlank()) it.nameEn!! else it.nameRu!!
            val taskCode = if (it.code != null) {
                it.code!!
            } else {
                commonService.generateCode(
                    entityName = taskName,
                    entityNamespaceCode = it.namespaceCode,
                    entityRepository = taskService
                )
            }
            it.code = taskCode

            if (!currTasksetTasksCodes.contains(taskCode)) {
                if (!taskService.existsByCode(taskCode)) {
                    try {
                        taskService.createFrom(it, user)
                    } catch (e: RuntimeException) {
                        throw CommonException("Cannot create task with code ${taskCode}. Error: ${e.message!!}")
                    }
                } else {
                    try {
                        taskService.updateFromTaskset(activeTaskset, it, user)
                    } catch (e: RuntimeException) {
                        throw CommonException("Cannot update task with code ${taskCode}. Error: ${e.message!!}")
                    }
                }
                val task = taskService.findByCode(taskCode) ?:
                    throw NotFoundException("Task with code=$taskCode")
                taskset2TaskRepository.save(Taskset2Task(taskset = activeTaskset, task = task))

                // add history row
                val taskHistory = taskService.findActiveHistoryByCode(taskCode) ?:
                    throw NotFoundException("Current active history row for task with code=$taskCode")

                taskset2TaskHistoryRepository.save(Taskset2TaskHistory(
                    taskset = newHistoryTaskset,
                    task = taskHistory,
                    isActive = true,
                    activeDateFrom = now,
                    activeDateTo = Constants.MAX_TIME
                ))
            } else {
                val taskPreviousHistoryRow = taskService.findActiveHistoryByCode(it.code!!) ?:
                    throw NotFoundException("History row for task with code=${it.code!!}")

                taskService.updateFromTaskset(activeTaskset, it, user)

                val taskHistoryRow = taskService.findActiveHistoryByCode(it.code!!) ?:
                    throw NotFoundException("History row for task with code=${it.code!!}")

                val previuosTaskset2TaskHistoryRow = taskset2TaskHistoryRepository.findByTasksetIdAndTaskId(
                    tasksetId = activeHistoryTaskset.id,
                    taskId = taskPreviousHistoryRow.id
                ) ?: throw NotFoundException("Previous history row for link between taskset" +
                    " with code=${tasksetForm.code} and task with code=${it.code!!}")
                previuosTaskset2TaskHistoryRow.isActive = false
                previuosTaskset2TaskHistoryRow.activeDateTo = now
                taskset2TaskHistoryRepository.save(previuosTaskset2TaskHistoryRow)

                taskset2TaskHistoryRepository.save(Taskset2TaskHistory(
                    taskset = newHistoryTaskset,
                    task = taskHistoryRow,
                    isActive = true,
                    activeDateFrom = now,
                    activeDateTo = Constants.MAX_TIME
                ))
            }
        }

        currTasksetTasks.forEach {
            if (!newTasksCodes.contains(it.task.code)) {
                taskset2TaskRepository.delete(it)

                // deactivate history row
                val taskHistoryRow = taskService.findActiveHistoryByCode(it.task.code) ?:
                    throw NotFoundException("History row for task with code=${it.task.code}")

                val activeHistoryRow = taskset2TaskHistoryRepository.findByTasksetIdAndTaskId(
                    tasksetId = activeHistoryTaskset.id,
                    taskId = taskHistoryRow.id
                ) ?: throw NotFoundException("Previous history row for link between taskset" +
                    " with code=${tasksetForm.code} and task with code=${it.task.code}")

                activeHistoryRow.isActive = false
                activeHistoryRow.activeDateTo = now
                taskset2TaskHistoryRepository.save(activeHistoryRow)
            }
        }


        // --- handle tags ---
        val currTags = taskset2TagRepository.findByTasksetCode(code)
        val currTagsCodes = currTags.map { it.tag.code }
        val newTagsCodes = if (tasksetForm.tags == null) arrayListOf() else tasksetForm.tags!!
        currTags.forEach {
            if (!newTagsCodes.contains(it.tag.code)) {
                taskset2TagRepository.delete(it)
            }
        }

        newTagsCodes.forEach {
            if (!currTagsCodes.contains(it)) {
                if (!tagRepository.existsByCode(it) || !tagRepository.existsByCodeAndNamespaceCode(it, activeTaskset.namespace.code)) {
                    tagRepository.saveAndFlush(Tag(
                        code = it,
                        namespace = activeTaskset.namespace
                    ))
                }

                val tag = tagRepository.findByCodeAndNamespaceCode(it, activeTaskset.namespace.code)!!
                taskset2TagRepository.save(Taskset2Tag(
                    taskset = activeTaskset,
                    tag = tag
                ))
            }
        }
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun updateTasksetsHistoryConnectionsFromTask(task: Task, excludedTasksetsCodes: ArrayList<String>) {
        logger.info("updateTasksetsFromTask")

        val tasksets2Task = taskset2TaskRepository.findByTaskCode(task.code)
        tasksets2Task.forEach {
            if (!excludedTasksetsCodes.contains(it.taskset.code)) {
                logger.info("update taskset's history connections with task, taskset code=${it.taskset.code}, task code=${task.code}")

                val activeHistoryTask = taskHistoryRepository.findByIdCodeAndIsActiveTrue(task.code)
                    ?: throw NotFoundException("Active history row for task with code=${task.code}")
                val previousTaskHistoryId = HistoryId(code = activeHistoryTask.id.code, version = activeHistoryTask.id.version - 1)

                val activeTaskset = it.taskset
                val activeHistoryTaskset = tasksetHistoryRepository.findByIdCodeAndIsActiveTrue(activeTaskset.code) ?:
                    throw NotFoundException("Active history row of taskset by code=${activeTaskset.code}")

                val taskset2TaskHistoryWithNewTaskVesrion = taskset2TaskHistoryRepository.findByTasksetIdAndTaskId(
                    tasksetId = activeHistoryTaskset.id,
                    taskId = activeHistoryTask.id
                )
                if (taskset2TaskHistoryWithNewTaskVesrion == null) {
                    val taskset2TaskHistory = taskset2TaskHistoryRepository.findByTasksetIdAndTaskId(
                        tasksetId = activeHistoryTaskset.id,
                        taskId = previousTaskHistoryId
                    ) ?: throw NotFoundException("Previous history row for taskset2task where taskset code is ${activeTaskset.code} and task code is ${task.code}")

                    val now = OffsetDateTime.now()
                    val newTaskset2TaskHistory = Taskset2TaskHistory(
                        taskset = activeHistoryTaskset,
                        task = activeHistoryTask,
                        activeDateFrom = now,
                        activeDateTo = Constants.MAX_TIME,
                        isActive = true
                    )

                    taskset2TaskHistory.isActive = false
                    taskset2TaskHistory.activeDateTo = now

                    taskset2TaskHistoryRepository.saveAndFlush(taskset2TaskHistory)
                    taskset2TaskHistoryRepository.saveAndFlush(newTaskset2TaskHistory)
                }
            }
        }
    }

    fun findByKeywords(keywords: String, rowsLimit: Int, offset: Int, namespace: String, authorUserCode: String, subjectType: String) : ArrayList<Taskset> {
        val ids = tasksetRepository.findByKeywordsAndRowsLimitAndOffsetAndNamespaceAndAuthorUserCodeAndSubjectTypeNative(keywords, rowsLimit, offset, namespace, authorUserCode, subjectType)

        return tasksetRepository.findByCodeIn(ids) as ArrayList<Taskset>
    }


    companion object {
        private val logger: Logger = LoggerFactory.getLogger("logic-logs")
    }
}
