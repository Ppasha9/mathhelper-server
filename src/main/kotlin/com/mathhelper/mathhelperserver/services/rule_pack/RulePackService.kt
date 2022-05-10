package com.mathhelper.mathhelperserver.services.rule_pack

import com.mathhelper.mathhelperserver.constants.*
import com.mathhelper.mathhelperserver.datatables.rule_pack.*
import com.mathhelper.mathhelperserver.datatables.subject_type.SubjectTypeRepository
import com.mathhelper.mathhelperserver.datatables.tasks.Task2RulePack
import com.mathhelper.mathhelperserver.datatables.tasks.Task2RulePackRepository
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePack2RulePackHistory
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePack2RulePackHistoryRepository
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePackHistory
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePackHistoryRepository
import com.mathhelper.mathhelperserver.forms.rule_pack.RulePackForm
import com.mathhelper.mathhelperserver.forms.rule_pack.RulePackLinkForm
import com.mathhelper.mathhelperserver.services.common.CommonService
import com.mathhelper.mathhelperserver.services.namespace.NamespaceService
import com.mathhelper.mathhelperserver.utils.obtainKeywords
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException
import java.sql.Timestamp
import java.time.OffsetDateTime

@Service
class RulePackService {
    @Autowired
    private lateinit var namespaceService: NamespaceService

    @Autowired
    private lateinit var rulePackRepository: RulePackRepository

    @Autowired
    private lateinit var rulePack2RulePackRepository: RulePack2RulePackRepository

    @Autowired
    private lateinit var rulePackHistoryRepository: RulePackHistoryRepository

    @Autowired
    private lateinit var rulePack2RulePackHistoryRepository: RulePack2RulePackHistoryRepository

    @Autowired
    private lateinit var task2RulePackRepository: Task2RulePackRepository

    @Autowired
    private lateinit var subjectTypeRepository: SubjectTypeRepository

    @Autowired
    private lateinit var commonService: CommonService

    companion object {
        private val logger by lazy { LoggerFactory.getLogger("logic-logs") }
    }

    fun findByCode(code: String): RulePack? = rulePackRepository.findByCode(code)

    fun findAll(): List<RulePack> = rulePackRepository.findAll()

    fun getRulePackForm(rulePack: RulePack): RulePackForm {
        logger.info("getRulePackForm")
        val rulePackHistoryActive = rulePackHistoryRepository.findByIdCodeAndIsActiveTrue(rulePack.code)!!
        val rulePackForm = RulePackForm(
            namespaceCode = rulePack.namespace.code,
            code = rulePack.code,
            version = rulePackHistoryActive.id.version,
            nameEn = rulePack.nameEn,
            nameRu = rulePack.nameRu,
            descriptionShortRu = rulePack.descriptionShortRu,
            descriptionShortEn = rulePack.descriptionShortEn,
            descriptionRu = rulePack.descriptionRu,
            descriptionEn = rulePack.descriptionEn,
            subjectType = if (rulePack.subjectType != null) rulePack.subjectType!!.name else "",
            rules = rulePack.rules,
            otherData = rulePack.otherData,
            otherCheckSolutionData = rulePack.otherCheckSolutionData,
            otherAutoGenerationData = rulePack.otherAutoGenerationData,
            serverActionTs = rulePack.serverActionTs
        )
        rulePackForm.rulePacks = arrayListOf()
        rulePack2RulePackRepository.findByParentRulePackCode(rulePack.code).forEach {
            rulePackForm.rulePacks!!.add(getRulePackLinkForm(it.childRulePack))
        }
        if (rulePackForm.rulePacks!!.isEmpty()) {
            rulePackForm.rulePacks = null
        }
        return rulePackForm
    }

    fun getRulePackLinkForm(rulePack: RulePack): RulePackLinkForm {
        return RulePackLinkForm(
            namespaceCode = rulePack.namespace.code,
            rulePackCode = rulePack.code,
            rulePackNameEn = rulePack.nameEn,
            rulePackNameRu = rulePack.nameRu
        )
    }

    @Transactional
    fun getByParams(limit: Int?, offset: Int?, substring: String?, keywords: String?, sortBy: String?,
                    descending: Boolean?, namespace: String?, subjectType: String?, currUser: User?): List<RulePack> {
        logger.info("getByParams")
        if (sortBy != null && !(sortBy == "name_ru" || sortBy == "name_en")) {
            return listOf()
        }

        val rulePacks: ArrayList<RulePack>
        if (keywords.isNullOrBlank()) {
            rulePacks = rulePackRepository.findByLimitAndOffsetAndSubstrAndSortByAndDescendingAndNamespaseNative(
                limit = limit?.toString() ?: "all",
                offset = offset ?: 0,
                substring = if (substring.isNullOrBlank()) "" else substring.replace("\"", ""),
                sortBy = if (sortBy.isNullOrBlank()) "name_ru" else sortBy.replace("\"", ""),
                sortByType = if (descending == true) "desc" else "asc",
                namespace = if (namespace.isNullOrBlank()) "" else namespace.replace("\"", ""),
                subjectType = if (subjectType.isNullOrBlank()) "" else subjectType.replace("\"", "")
            ) as ArrayList<RulePack>
        } else {
            val limitSearch = Math.min(Math.max(limit
                ?: 0, Constants.KEYWORDS_SEARCHING_LIMIT_MIN_SIZE), Constants.KEYWORDS_SEARCHING_LIMIT_MAX_SIZE)
            val offsetSearch = offset ?: 0
            val obtainKeywords = obtainKeywords(keywords, rulePackRepository)

            if (obtainKeywords.keywordsFormatted == null) {
                return listOf()
            }

            rulePacks = findByKeywords(
                keywords = obtainKeywords.keywordsFormatted,
                rowsLimit = limitSearch,
                offset = offsetSearch,
                namespace = if (namespace.isNullOrBlank()) "" else namespace.replace("\"", ""),
                subjectType = if (subjectType.isNullOrBlank()) "" else subjectTypeRepository.findByName(subjectType)!!.name
            )
        }

        return namespaceService.filterEntitiesByNamespacesGrantsAndUserForRead(rulePacks, currUser)
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun createFrom(rulePackForm: RulePackForm, user: User) {
        logger.info("createFrom")

        // Validation
        val namespace = namespaceService.findByCode(rulePackForm.namespaceCode) ?:
            throw NotFoundException("Namespace with code = ${rulePackForm.namespaceCode}")

        val names = rulePackRepository.findNamesOnlyByNamespaceCodeNative(rulePackForm.namespaceCode)
        names.forEach {
            if (rulePackForm.nameEn == it.nameEn || rulePackForm.nameRu == it.nameRu)
                throw AlreadyExistsWithParamsException("RulePack", "nameRu or nameEn")
        }

        if (rulePackForm.nameEn.isNullOrBlank() && rulePackForm.nameRu.isNullOrBlank()) {
            logger.error("All names are empty.")
            throw BadRequestWithErrorException("All names are empty.")
        }

        if (rulePackForm.rulePacks != null) {
            rulePackForm.rulePacks!!.forEach {
                if (!rulePackRepository.existsByCode(it.rulePackCode))
                    throw NotFoundException("RulePack with code = ${it.rulePackCode}")
                if (!rulePackHistoryRepository.existsByIdCodeAndIsActiveTrue(it.rulePackCode))
                    throw NotFoundException("RulePackHistory with code = ${it.rulePackCode}")
            }
        }

        val entityName = if (!rulePackForm.nameEn.isNullOrBlank()) rulePackForm.nameEn!! else rulePackForm.nameRu!!
        val rulePackCode = if (rulePackForm.code != null && !rulePackRepository.existsByCode(rulePackForm.code!!)) {
            rulePackForm.code!!
        } else {
            commonService.generateCode(
                    entityName = entityName,
                    entityNamespaceCode = rulePackForm.namespaceCode,
                    entityRepository = rulePackRepository
            )
        }

        val keywordsList = rulePackForm.nameEn!!.split(" ", ",", "-", ";", "/", "\\") +
            rulePackForm.nameRu!!.split(" ", ",", "-", ";", "/", "\\")
        var keywords = ""
        for (keyword in keywordsList)
        {
            val word = keyword.replace(" ", "")
            if (word !in keywords)
                keywords +=  "$word "
        }

        // Creation
        val now = OffsetDateTime.now()
        val rulePack = RulePack(
            code = rulePackCode,
            namespace = namespace,
            keywords = keywords,
            authorUser = user,
            nameEn = rulePackForm.nameEn ?: "",
            nameRu = rulePackForm.nameRu ?: "",
            descriptionShortRu = rulePackForm.descriptionShortRu ?: "",
            descriptionShortEn = rulePackForm.descriptionShortEn ?: "",
            descriptionRu = rulePackForm.descriptionRu ?: rulePackForm.descriptionShortRu ?: "",
            descriptionEn = rulePackForm.descriptionEn ?: rulePackForm.descriptionShortEn ?: "",
            subjectType = subjectTypeRepository.findByName(rulePackForm.subjectType ?: ""),
            rules = rulePackForm.rules,
            otherData = rulePackForm.otherData,
            otherCheckSolutionData = rulePackForm.otherCheckSolutionData,
            otherAutoGenerationData = rulePackForm.otherAutoGenerationData,
            serverActionTs = Timestamp(System.currentTimeMillis())
        )
        val rulePackHistory = RulePackHistory(
            id = HistoryId(rulePack.code, 0),
            namespace = namespace,
            keywords = keywords,
            authorUser = user,
            nameEn = rulePack.nameEn,
            nameRu = rulePack.nameRu,
            descriptionShortRu = rulePack.descriptionShortRu,
            descriptionShortEn = rulePack.descriptionShortEn,
            descriptionRu = rulePack.descriptionRu,
            descriptionEn = rulePack.descriptionEn,
            subjectType = subjectTypeRepository.findByName(rulePackForm.subjectType ?: ""),
            rules = rulePackForm.rules,
            otherData = rulePackForm.otherData,
            otherCheckSolutionData = rulePackForm.otherCheckSolutionData,
            otherAutoGenerationData = rulePackForm.otherAutoGenerationData,
            activeDateFrom = now,
            activeDateTo = Constants.MAX_TIME
        )
        rulePackRepository.save(rulePack)
        rulePackHistoryRepository.save(rulePackHistory)
        if (rulePackForm.rulePacks != null) {
            rulePackForm.rulePacks!!.forEach {
                val child = rulePackRepository.findByCode(it.rulePackCode)!!
                val childHistory = rulePackHistoryRepository.findByIdCodeAndIsActiveTrue(it.rulePackCode)!!
                val rp2rp = RulePack2RulePack(parentRulePack = rulePack, childRulePack = child)
                val rp2rph = RulePack2RulePackHistory(
                    parentRulePack = rulePackHistory, childRulePack = childHistory,
                    activeDateFrom = now, activeDateTo = Constants.MAX_TIME
                )
                rulePack2RulePackRepository.save(rp2rp)
                rulePack2RulePackHistoryRepository.save(rp2rph)
            }
        }
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun updateWith(rulePackForm: RulePackForm, user: User) {
        logger.info("updateWith")

        if (rulePackForm.code == null) {
            throw CommonException("You should provide code of RulePack that you want to update")
        }

        // Validation
        val code = rulePackForm.code!!
        val activeRulePack = rulePackRepository.findByCode(code) ?:
            throw NotFoundException("RulePack with code = $code")
        val activeRulePackHistory = rulePackHistoryRepository.findByIdCodeAndIsActiveTrue(code) ?:
            throw NotFoundException("RulePackHistory with code = $code")
        val names = rulePackRepository.findNamesOnlyByNamespaceCodeNative(rulePackForm.namespaceCode)
        names.forEach {
            if (code != it.code && (rulePackForm.nameEn == it.nameEn || rulePackForm.nameRu == it.nameRu))
                throw AlreadyExistsWithParamsException("RulePack", "nameRu or nameEn")
        }
        rulePackForm.rulePacks?.map {
            if (!rulePackRepository.existsByCode(it.rulePackCode))
                throw NotFoundException("RulePack with code = ${it.rulePackCode}")
            if (!rulePackHistoryRepository.existsByIdCodeAndIsActiveTrue(it.rulePackCode))
                throw NotFoundException("RulePackHistory with code = ${it.rulePackCode}")
        }
        val now = OffsetDateTime.now()
        // Update rule pack
        activeRulePack.authorUser = user
        activeRulePack.nameEn = rulePackForm.nameEn ?: ""
        activeRulePack.nameRu = rulePackForm.nameRu ?: ""
        activeRulePack.descriptionEn = rulePackForm.descriptionEn ?: ""
        activeRulePack.descriptionRu = rulePackForm.descriptionRu ?: ""
        activeRulePack.descriptionShortEn = rulePackForm.descriptionShortEn ?: ""
        activeRulePack.descriptionShortRu = rulePackForm.descriptionShortRu ?: ""
        activeRulePack.subjectType = subjectTypeRepository.findByName(rulePackForm.subjectType ?: "")
        activeRulePack.rules = rulePackForm.rules
        activeRulePack.otherData = rulePackForm.otherData
        activeRulePack.otherCheckSolutionData = rulePackForm.otherCheckSolutionData
        activeRulePack.otherAutoGenerationData = rulePackForm.otherAutoGenerationData
        activeRulePack.serverActionTs = Timestamp(System.currentTimeMillis())

        val keywordsList = activeRulePack.nameEn.split(" ", ",", "-", ";", "/", "\\") +
            activeRulePack.nameRu.split(" ", ",", "-", ";", "/", "\\")
        var keywords = ""
        for (keyword in keywordsList)
        {
            val word = keyword.replace(" ", "")
            if (word !in keywords)
                keywords +=  "$word "
        }

        activeRulePack.keywords = keywords

        // Update history
        val newRulePackHistory = RulePackHistory(
            id = HistoryId(activeRulePack.code, activeRulePackHistory.id.version + 1),
            namespace = activeRulePack.namespace,
            keywords = keywords,
            authorUser = user,
            nameEn = activeRulePack.nameEn,
            nameRu = activeRulePack.nameRu,
            descriptionShortRu = activeRulePack.descriptionShortRu,
            descriptionShortEn = activeRulePack.descriptionShortEn,
            descriptionRu = activeRulePack.descriptionRu,
            descriptionEn = activeRulePack.descriptionEn,
            subjectType = subjectTypeRepository.findByName(rulePackForm.subjectType ?: ""),
            rules = activeRulePack.rules,
            otherData = activeRulePack.otherData,
            otherCheckSolutionData = activeRulePack.otherCheckSolutionData,
            otherAutoGenerationData = activeRulePack.otherAutoGenerationData,
            activeDateFrom = now,
            activeDateTo = Constants.MAX_TIME
        )
        // Invalidate previous history
        activeRulePackHistory.activeDateTo = now
        activeRulePackHistory.isActive = false
        // Save
        rulePackRepository.save(activeRulePack)
        rulePackHistoryRepository.save(activeRulePackHistory)
        rulePackHistoryRepository.save(newRulePackHistory)
        // RulePack to PulePack relation
        val rulePackChildren = rulePack2RulePackRepository.findByParentRulePackCode(code)
        val rulePackChildrenHistory = rulePack2RulePackHistoryRepository.findByParentRulePackId(activeRulePackHistory.id)
        var newCodes = rulePackForm.rulePacks?.map { it.rulePackCode }
        newCodes = newCodes ?: listOf()
        val remainCodes = arrayListOf<String>()
        // Remove old links if not found in new link array
        rulePackChildren.forEach {
            if (!newCodes.contains(it.childRulePack.code))
                rulePack2RulePackRepository.delete(it)
            else
                remainCodes.add(it.childRulePack.code)
        }
        // Add new links if not found in current link array
        newCodes.forEach {
            if (!remainCodes.contains(it)) {
                val child = rulePackRepository.findByCode(it)!!
                val newLink = RulePack2RulePack(parentRulePack = activeRulePack, childRulePack = child)
                rulePack2RulePackRepository.save(newLink)
            }
        }
        // RulePack to PulePack history relation
        rulePackChildrenHistory.forEach {
            it.isActive = false
            it.activeDateTo = now
            rulePack2RulePackHistoryRepository.save(it)
        }
        rulePackForm.rulePacks?.forEach {
            val childHistoryActive = rulePackHistoryRepository.findByIdCodeAndIsActiveTrue(it.rulePackCode)!!
            val newLinkHistory = RulePack2RulePackHistory(
                parentRulePack = newRulePackHistory, childRulePack = childHistoryActive,
                activeDateFrom = now, activeDateTo = Constants.MAX_TIME
            )
            rulePack2RulePackHistoryRepository.save(newLinkHistory)
        }
    }

    fun getAllChildRulePacksForForm(form: RulePackForm, resultList: ArrayList<RulePackForm>) {
        form.rulePacks?.forEach { link: RulePackLinkForm ->
            val resCodes = resultList.map { rpf -> rpf.code }
            if (!resCodes.contains(link.rulePackCode)) {
                val linkPack = rulePackRepository.findByCode(link.rulePackCode)
                if (linkPack != null) {
                    val linkForm = getRulePackForm(linkPack)
                    resultList.add(linkForm)
                    getAllChildRulePacksForForm(linkForm, resultList)
                }
            }
        }
    }

    fun getByTaskCodes(taskCodes: List<String>): List<RulePackForm> {
        val resultList = arrayListOf<RulePackForm>()
        taskCodes.forEach {
            task2RulePackRepository.findByTaskCode(it).forEach { t2rp: Task2RulePack ->
                val resCodes = resultList.map { rpf -> rpf.code }
                if (!resCodes.contains(t2rp.rulePack.code)) {
                    val form = getRulePackForm(t2rp.rulePack)
                    resultList.add(form)
                    getAllChildRulePacksForForm(form, resultList)
                }
            }
        }
        return resultList
    }

    fun findByKeywords(keywords: String, rowsLimit: Int, offset: Int, namespace: String, subjectType: String) : ArrayList<RulePack> {
        val ids = rulePackRepository.findByKeywordsAndRowsLimitAndOffsetAndNamespaceAndSubjectTypeNative(keywords, rowsLimit, offset, namespace, subjectType)

        return rulePackRepository.findByCodeIn(ids) as ArrayList<RulePack>
    }
}