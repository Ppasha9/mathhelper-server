package com.mathhelper.mathhelperserver.api_requests.rule_pack

import com.mathhelper.mathhelperserver.AuthUtil
import com.mathhelper.mathhelperserver.constants.NetworkError
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespaceGrantsRepository
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespacesRepository
import com.mathhelper.mathhelperserver.datatables.rule_pack.RulePack2RulePackRepository
import com.mathhelper.mathhelperserver.datatables.rule_pack.RulePackRepository
import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePack2RulePackHistoryRepository
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePackHistoryRepository
import com.mathhelper.mathhelperserver.forms.rule_pack.RulePackForm
import com.mathhelper.mathhelperserver.forms.rule_pack.RulePackLinkForm
import com.mathhelper.mathhelperserver.services.rule_pack.RulePackService
import com.mathhelper.mathhelperserver.services.user.UserService
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.Timestamp
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private class KPostgreSQLContainer(image: String): PostgreSQLContainer<KPostgreSQLContainer>(image)

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Sql("/test_init.sql")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RulePackTests {
    companion object {
        @Value("#{systemProperties['tests.databasename']}")
        private val dbName: String = ""

        @Container
        private val container = KPostgreSQLContainer("postgres:12").apply {
            withDatabaseName(dbName)
            withUsername(username)
            withPassword(password)
        }
    }

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var rulePackService: RulePackService

    @Autowired
    private lateinit var rulePackRepository: RulePackRepository

    @Autowired
    private lateinit var rulePackHistoryRepository: RulePackHistoryRepository
    
    @Autowired
    private lateinit var rulePack2rulePackRepository: RulePack2RulePackRepository

    @Autowired
    private lateinit var rulePack2rulePackHistoryRepository: RulePack2RulePackHistoryRepository

    @Autowired
    private lateinit var namespaceGrantsRepository: NamespaceGrantsRepository

    @Autowired
    private lateinit var namespacesRepository: NamespacesRepository

    fun checkRulePackSaved(hId: HistoryId, kids: List<HistoryId> = listOf()) {
        assertTrue(rulePackRepository.existsByCode(hId.code))
        assertTrue(rulePackHistoryRepository.existsById(hId))
        kids.forEach {
            assertTrue(rulePack2rulePackRepository.existsByParentRulePackCodeAndChildRulePackCode(hId.code, it.code))
            assertTrue(rulePack2rulePackHistoryRepository.existsByParentRulePackIdAndChildRulePackId(hId, it))
        }
    }

    fun checkSuccessfullyCreated(hId: HistoryId, resActions: ResultActionsDsl, kids: List<HistoryId> = listOf()) {
        resActions.andExpect {
            status { isCreated }
            content { string("Rule pack was successfully created") }
        }
        checkRulePackSaved(hId, kids)
    }

    fun addRulePack(
        rulePackCode: String,
        rulePack: String,
        check: (resActions: ResultActionsDsl) -> Unit
    ) {
        if (rulePackRepository.existsByCode(rulePackCode)) return
        val resActions = mvc.post("/api/rule-pack/create") {
            contentType = MediaType.APPLICATION_JSON
            content = rulePack
            header("Authorization", "Bearer ${AuthUtil.getToken(mvc, userService, namespaceGrantsRepository, namespacesRepository)}")
        }
        check(resActions)
    }

    fun checkRulePack(rulePackStr: String, codeToCheck: String) {
        assertTrue(rulePackRepository.existsByCode(codeToCheck))
        assertTrue(rulePackHistoryRepository.existsByIdCode(codeToCheck))
        val rulePack = JSONObject(rulePackStr)
        val code = rulePack.optString("code")
        val namespaceCode = rulePack.optString("namespaceCode")
        val nameEn = rulePack.optString("nameEn")
        val nameRu = rulePack.optString("nameRu")
        val (originCode, originNamespace, _, _, originNameEn, originNameRu, _, _) = rulePackRepository.findByCode(codeToCheck)!!
        assertEquals(originCode, code)
        assertEquals(originNamespace.code, namespaceCode)
        assertEquals(originNameEn, nameEn)
        assertEquals(originNameRu, nameRu)
    }

    fun getAndExpectOk(url: String, type: MediaType = MediaType.APPLICATION_JSON): MvcResult {
        return mvc.get(url).andExpect {
            status { isOk }
            content { contentType(type) }
        }.andReturn()
    }

    /**************** CREATION ****************/

    @Test
    fun createWithLinking() {
        addRulePack(RulePackTestData.emptyRulePackCode, RulePackTestData.emptyRulePack) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.emptyRulePackCode, 0), it)
        }
        addRulePack(RulePackTestData.fullRulePackCode, RulePackTestData.fullRulePack) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.fullRulePackCode, 0), it,
                listOf(HistoryId(RulePackTestData.emptyRulePackCode, 0)))
        }
    }

    @Test
    fun createWithShortLinking() {
        addRulePack(RulePackTestData.rulePackShortLinkCode, RulePackTestData.rulePackShortLink) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.rulePackShortLinkCode, 0), it,
                listOf(HistoryId(RulePackTestData.baseRulePackCode, 0)))
        }
    }

    @Test
    fun createAlreadyExistsByCode() {
        addRulePack(RulePackTestData.baseRulePackCode, RulePackTestData.baseRulePackSameCode) {
            it.andExpect {
                status { isBadRequest }
                content { string(NetworkError.alreadyExistsWithCode("RulePack", RulePackTestData.baseRulePackCode)) }
            }
        }
    }

    @Test
    fun createAlreadyExistsByName() {
        addRulePack(RulePackTestData.baseRulePackSameNameCode, RulePackTestData.baseRulePackSameName) {
            it.andExpect {
                status { isBadRequest }
                content { string(NetworkError.alreadyExistsWithParams("RulePack", "nameRu or nameEn")) }
            }
        }
    }

    @Test
    fun createSameNameDifferentNamespace() {
        addRulePack(RulePackTestData.emptyRulePackCode, RulePackTestData.emptyRulePack) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.emptyRulePackCode, 0), it)
        }
        addRulePack(RulePackTestData.emptyRulePackAnotherSpaceSameNameCode, RulePackTestData.emptyRulePackAnotherSpaceSameName) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.emptyRulePackAnotherSpaceSameNameCode, 0), it)
        }
    }

    // Invalid Form

    @Test
    fun createEmptyInvalidRulePack() {
        addRulePack("", "") {
            it.andExpect {
                status { isBadRequest }
                content { string(NetworkError.invalidBody) }
            }
        }
        assertTrue(!rulePackRepository.existsByCode(RulePackTestData.rulePackInvalidCode))
    }

    @Test
    fun createRulePackInvalidNamespace() {
        addRulePack(RulePackTestData.rulePackInvalidCode, RulePackTestData.rulePackInvalidNamespace) {
            it.andExpect {
                status { isBadRequest }
            }
        }
        assertTrue(!rulePackRepository.existsByCode(RulePackTestData.rulePackInvalidCode))
    }

    @Test
    fun createRulePackInvalidCode() {
        addRulePack(RulePackTestData.rulePackInvalidCode, RulePackTestData.rulePackCodeInvalid) {
            it.andExpect {
                status { isBadRequest }
            }
        }
        assertTrue(!rulePackRepository.existsByCode(RulePackTestData.rulePackInvalidCode))
    }

    @Test
    fun createRulePackInvalidName() {
        addRulePack(RulePackTestData.rulePackInvalidCode, RulePackTestData.rulePackInvalidName) {
            it.andExpect {
                status { isBadRequest }
            }
        }
        assertTrue(!rulePackRepository.existsByCode(RulePackTestData.rulePackInvalidCode))
    }

    @Test
    fun createRulePackInvalidNoNames() {
        addRulePack(RulePackTestData.rulePackInvalidCode, RulePackTestData.rulePackNoNames) {
            it.andExpect {
                status { isBadRequest }
                content { string("Fail. Bad Request. Error: All names are empty.") }
            }
        }
        assertTrue(!rulePackRepository.existsByCode(RulePackTestData.rulePackInvalidCode))
    }

    @Test
    fun createRulePackInvalidRulePackLinks() {
        addRulePack(RulePackTestData.rulePackInvalidCode, RulePackTestData.rulePackInvalidLinks) {
            it.andExpect {
                status { isBadRequest }
            }
        }
        assertTrue(!rulePackRepository.existsByCode(RulePackTestData.rulePackInvalidCode))
    }

    @Test
    fun createRulePackInvalidOtherData() {
        addRulePack(RulePackTestData.rulePackInvalidCode, RulePackTestData.rulePackInvalidOtherData) {
            it.andExpect {
                status { isBadRequest }
            }
        }
        assertTrue(!rulePackRepository.existsByCode(RulePackTestData.rulePackInvalidCode))
    }

    // Something not found

    @Test
    fun createRulePackNotFoundNamespace() {
        addRulePack(RulePackTestData.rulePackInvalidCode, RulePackTestData.rulePackNotFoundNamespace) {
            it.andExpect {
                status { isBadRequest }
                content { string(NetworkError.notFound("Namespace with code = ${RulePackTestData.validStr}")) }
            }
        }
        assertTrue(!rulePackRepository.existsByCode(RulePackTestData.rulePackInvalidCode))
    }

    @Test
    fun createRulePackNotFoundChildRulePack() {
        addRulePack(RulePackTestData.rulePackInvalidCode, RulePackTestData.rulePackNotFoundChild) {
            it.andExpect {
                status { isBadRequest }
                content { string(NetworkError.notFound("RulePack with code = ${RulePackTestData.validStr}")) }
            }
        }
        assertTrue(!rulePackRepository.existsByCode(RulePackTestData.rulePackInvalidCode))
    }

    /**************** SEARCH ****************/

    @Test
    fun findByCodeOk() {
        val response = mvc.get("/api/rule-pack/${RulePackTestData.baseRulePackCode}")
            .andExpect {
                status { isOk }
                content { contentType(MediaType.APPLICATION_JSON) }
            }.andReturn()
        checkRulePack(response.response.contentAsString, RulePackTestData.baseRulePackCode)
    }

    @Test
    fun findByCodeNotFound() {
        mvc.get("/api/rule-pack/${RulePackTestData.validStr}")
            .andExpect {
                status { isNotFound }
                content { string(NetworkError.notFound("RulePack with code = ${RulePackTestData.validStr}")) }
            }
    }

    @Test
    fun findByCodeInvalid() {
        mvc.get("/api/rule-pack/@")
            .andExpect {
                status { isNotFound }
                content { string(NetworkError.notFound("RulePack with code = @")) }
            }
    }

    @Test
    fun findByParamsAll() {
        // limit=, offset=, substr=, sort-by=, descending= namespace=
        val limit = 2
        addRulePack(RulePackTestData.emptyRulePackCode, RulePackTestData.emptyRulePack) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.emptyRulePackCode, 0), it)
        }
        addRulePack(RulePackTestData.fullRulePackCode, RulePackTestData.fullRulePack) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.fullRulePackCode, 0), it,
                listOf(HistoryId(RulePackTestData.emptyRulePackCode, 0)))
        }
        val res = getAndExpectOk("/api/rule-pack?limit=$limit&offset=1&substr=rule_pack&" +
            "sort-by=name_en&descending=true&namespace=${RulePackTestData.testNamespaceCode}")
        /* expected:
         * [
         *   {test_rule_pack_2},
         *   {request_full_rule_pack____test_namespace_code}
         * ]
         */
        val rulePacks = JSONArray(res.response.contentAsString)
        assertEquals(limit, rulePacks.length())
        /*
        val rulePack1 = rulePacks[0] as JSONObject
        assertEquals("test_rule_pack_2", rulePack1.optString("code"))
        val rulePack2 = rulePacks[1] as JSONObject
        assertEquals("request_full_rule_pack____test_namespace_code", rulePack2.optString("code"))
        */
    }

    @Test
    fun findByParamsInvalidKeys() {
        val res = getAndExpectOk("/api/rule-pack?limet=2&ofset=1&sbstr=rule_pack&sortby=name_en&ascending=true&name_space=${RulePackTestData.testNamespaceCode}")
        val rulePacks = JSONArray(res.response.contentAsString)
        assertEquals(rulePackRepository.count(), rulePacks.length().toLong())
    }

    @Test
    fun findByParamsInvalidValues() {
        mvc.get(
            "/api/rule-pack?limit=no&offset=-14&substr=&sort-by=something&descending=what_is_it&namespace=30"
        )
        .andExpect {
            status { isBadRequest }
        }
    }

    @Test
    fun findByParamsLimit() {
        val limit = 1
        val res = getAndExpectOk("/api/rule-pack?limit=$limit")
        val rulePacks = JSONArray(res.response.contentAsString)
        assertEquals(limit, rulePacks.length())
    }

    @Test
    fun findByParamsOffset() {
        val offset = 1
        val resAll= getAndExpectOk("/api/rule-pack")
        val resOffset = getAndExpectOk("/api/rule-pack?offset=$offset")
        val rulePacksAll = JSONArray(resAll.response.contentAsString)
        val rulePacksOffset = JSONArray(resOffset.response.contentAsString)
        assertEquals(rulePacksAll.length(), rulePacksOffset.length() + offset)
        assertNotEquals(rulePacksAll[0], rulePacksOffset[0])
        assertNotEquals(rulePacksAll[1], rulePacksOffset[0])
    }

    @Test
    fun findByParamsSubstr() {
        addRulePack(RulePackTestData.emptyRulePackCode, RulePackTestData.emptyRulePack) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.emptyRulePackCode, 0), it)
        }
        addRulePack(RulePackTestData.fullRulePackCode, RulePackTestData.fullRulePack) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.fullRulePackCode, 0), it,
                listOf(HistoryId(RulePackTestData.emptyRulePackCode, 0)))
        }
        val substr = "test_rule_pack"
        val res = getAndExpectOk("/api/rule-pack?substr=$substr")
        val rulePacks = JSONArray(res.response.contentAsString)
        for (i in 0 until rulePacks.length()) {
            val rulePack = rulePacks.getJSONObject(i)
            assertTrue(rulePack.optString("nameEn").contains(substr))
        }
    }

    @Test
    fun findByParamsSortBy() {
        addRulePack(RulePackTestData.emptyRulePackCode, RulePackTestData.emptyRulePack) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.emptyRulePackCode, 0), it)
        }
        addRulePack(RulePackTestData.fullRulePackCode, RulePackTestData.fullRulePack) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.fullRulePackCode, 0), it,
                listOf(HistoryId(RulePackTestData.emptyRulePackCode, 0)))
        }
        val col = "name_en"
        val res = getAndExpectOk("/api/rule-pack?sort-by=$col&descending=true")
        val rulePacks = JSONArray(res.response.contentAsString)
        val names = arrayListOf<String>()
        for (i in 0 until rulePacks.length()) {
            val rulePack = rulePacks.getJSONObject(i)
            val name = rulePack.optString("nameEn")
            assertNotNull(name)
            names.add(name)
        }
        assertEquals(names.sortedDescending(), names)
    }

    @Test
    fun findByParamsNamespace() {
        addRulePack(RulePackTestData.emptyRulePackCode, RulePackTestData.emptyRulePack) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.emptyRulePackCode, 0), it)
        }
        addRulePack(RulePackTestData.emptyRulePackAnotherSpaceSameNameCode, RulePackTestData.emptyRulePackAnotherSpaceSameName) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.emptyRulePackAnotherSpaceSameNameCode, 0), it)
        }
        val res = getAndExpectOk("/api/rule-pack?namespace=${RulePackTestData.testNamespaceCode}")
        val rulePacks = JSONArray(res.response.contentAsString)
        for (i in 0 until rulePacks.length()) {
            val rulePack = rulePacks.getJSONObject(i)
            assertEquals(rulePack.optString("namespaceCode"), RulePackTestData.testNamespaceCode)
        }
    }

    /**************** UPDATE ****************/

    @Test
    fun updateRulePackCode() {
        addRulePack(RulePackTestData.rulePackUpdateCode, RulePackTestData.rulePackUpdate) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.rulePackUpdateCode, 0), it)
        }
        mvc.post("/api/rule-pack/update") {
            contentType = MediaType.APPLICATION_JSON
            content = RulePackTestData.rulePackCodeUpdated
            header("Authorization", "Bearer ${AuthUtil.getToken()}")
        }
        .andExpect {
            status { isBadRequest }
            content { string(NetworkError.notFound("RulePack with code = ${RulePackTestData.rulePackCodeUpdatedCode}")) }
        }
    }

    @Test
    fun updateRulePackNamespace() {
        addRulePack(RulePackTestData.rulePackUpdateCode, RulePackTestData.rulePackUpdate) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.rulePackUpdateCode, 0), it)
        }
        mvc.post("/api/rule-pack/update") {
            contentType = MediaType.APPLICATION_JSON
            content = RulePackTestData.rulePackNamespaceUpdated
            header("Authorization", "Bearer ${AuthUtil.getToken()}")
        }
        .andExpect {
            status { isOk } // TODO: is it ok really?
            content { string("Rule pack was successfully updated") }
        }
    }

    @Test
    fun updateRulePackName() {
        addRulePack(RulePackTestData.rulePackUpdateCode, RulePackTestData.rulePackUpdate) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.rulePackUpdateCode, 0), it)
        }
        mvc.post("/api/rule-pack/update") {
            contentType = MediaType.APPLICATION_JSON
            content = RulePackTestData.rulePackNameUpdated
            header("Authorization", "Bearer ${AuthUtil.getToken()}")
        }
        .andExpect {
            status { isOk }
            content { string("Rule pack was successfully updated") }
        }
        val pack = rulePackRepository.findByCode(RulePackTestData.rulePackUpdateCode)
        assertNotNull(pack)
        assertEquals(pack.nameEn, "${RulePackTestData.rulePackUpdateCode}_name_en_updated")
        val packHist = rulePackHistoryRepository.findByIdCodeAndIsActiveTrue(RulePackTestData.rulePackUpdateCode)
        assertNotNull(packHist)
        assertNotEquals(packHist.id.version, 0)
        assertEquals(packHist.nameEn, "${RulePackTestData.rulePackUpdateCode}_name_en_updated")
    }

    @Test
    fun updateRulePackNameWithOverlap() {
        addRulePack(RulePackTestData.rulePackUpdateCode, RulePackTestData.rulePackUpdate) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.rulePackUpdateCode, 0), it)
        }
        var pack = rulePackRepository.findByCode(RulePackTestData.rulePackUpdateCode)
        val oldName = pack?.nameEn
        mvc.post("/api/rule-pack/update") {
            contentType = MediaType.APPLICATION_JSON
            content = RulePackTestData.rulePackNameOverlapUpdated
            header("Authorization", "Bearer ${AuthUtil.getToken()}")
        }
        .andExpect {
            status { isBadRequest }
            content { string(NetworkError.alreadyExistsWithParams("RulePack", "nameRu or nameEn")) }
        }
        pack = rulePackRepository.findByCode(RulePackTestData.rulePackUpdateCode)
        assertEquals(pack?.nameEn, oldName)
    }

    @Test
    fun updateRulePackLinks() {
        addRulePack(RulePackTestData.rulePackUpdateCode, RulePackTestData.rulePackUpdate) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.rulePackUpdateCode, 0), it)
        }
        addRulePack(RulePackTestData.emptyRulePackCode, RulePackTestData.emptyRulePack) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.emptyRulePackCode, 0), it)
        }
        addRulePack(RulePackTestData.fullRulePackCode, RulePackTestData.fullRulePack) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.fullRulePackCode, 0), it,
                listOf(HistoryId(RulePackTestData.emptyRulePackCode, 0)))
        }
        var pack = getAndExpectOk("/api/rule-pack/${RulePackTestData.rulePackUpdateCode}")
        var packObj = JSONObject(pack.response.contentAsString)
        val oldLinks = packObj.getJSONArray("rulePacks")
        mvc.post("/api/rule-pack/update") {
            contentType = MediaType.APPLICATION_JSON
            content = RulePackTestData.rulePackLinksUpdated
            header("Authorization", "Bearer ${AuthUtil.getToken()}")
        }
        .andExpect {
            status { isOk }
            content { string("Rule pack was successfully updated") }
        }
        pack = getAndExpectOk("/api/rule-pack/${RulePackTestData.rulePackUpdateCode}")
        packObj = JSONObject(pack.response.contentAsString)
        assertNotEquals(oldLinks, packObj.getJSONArray("rulePacks"))
    }

    @Test
    fun updateRulePackLinksBad() {
        addRulePack(RulePackTestData.rulePackUpdateCode, RulePackTestData.rulePackUpdate) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.rulePackUpdateCode, 0), it)
        }
        val oldPack = rulePackRepository.findByCode(RulePackTestData.rulePackUpdateCode)
        mvc.post("/api/rule-pack/update") {
            contentType = MediaType.APPLICATION_JSON
            content = RulePackTestData.rulePackLinksAndNameUpdatedBad
            header("Authorization", "Bearer ${AuthUtil.getToken()}")
        }
        .andExpect {
            status { isBadRequest }
            content { string(NetworkError.notFound("RulePack with code = valid")) }
        }
        val pack = rulePackRepository.findByCode(RulePackTestData.rulePackUpdateCode)
        assertEquals(pack, oldPack)
    }

    /**************** UNROLLING ****************/
    @Test
    fun getAllChildren() {
        addRulePack(RulePackTestData.emptyRulePackCode, RulePackTestData.emptyRulePack) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.emptyRulePackCode, 0), it)
        }
        addRulePack(RulePackTestData.fullRulePackCode, RulePackTestData.fullRulePack) {
            checkSuccessfullyCreated(HistoryId(RulePackTestData.fullRulePackCode, 0), it,
                listOf(HistoryId(RulePackTestData.emptyRulePackCode, 0)))
        }
        val form = RulePackForm(
            namespaceCode = RulePackTestData.testNamespaceCode,
            code = "thick_rule_pack",
            nameRu = "yes",
            rulePacks = arrayListOf(
                RulePackLinkForm(rulePackCode = RulePackTestData.baseRulePackCode),
                RulePackLinkForm(rulePackCode = RulePackTestData.baseRulePack2Code),
                RulePackLinkForm(rulePackCode = RulePackTestData.emptyRulePackCode),
                RulePackLinkForm(rulePackCode = RulePackTestData.fullRulePackCode)
            ),
            serverActionTs = Timestamp(System.currentTimeMillis())
        )
        val packs = arrayListOf(form)
        rulePackService.getAllChildRulePacksForForm(form, packs)
        assertEquals(packs.size, 5)
    }
}