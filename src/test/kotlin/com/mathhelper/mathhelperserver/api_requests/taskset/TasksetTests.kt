package com.mathhelper.mathhelperserver.api_requests.taskset

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mathhelper.mathhelperserver.AuthUtil
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespaceGrantsRepository
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespacesRepository
import com.mathhelper.mathhelperserver.datatables.tasks.TaskRepository
import com.mathhelper.mathhelperserver.datatables.taskset.Taskset2TaskRepository
import com.mathhelper.mathhelperserver.datatables.taskset.TasksetRepository
import com.mathhelper.mathhelperserver.datatables_history.tasks.TaskHistoryRepository
import com.mathhelper.mathhelperserver.datatables_history.taskset.Taskset2TaskHistoryRepository
import com.mathhelper.mathhelperserver.datatables_history.taskset.TasksetHistoryRepository
import com.mathhelper.mathhelperserver.forms.taskset.TasksetForm
import com.mathhelper.mathhelperserver.forms.taskset.TasksetLinkForm
import com.mathhelper.mathhelperserver.services.user.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class KPostgreSQLContainer(image: String) : PostgreSQLContainer<KPostgreSQLContainer>(image)

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts=["file:tools/scripts/import.sql", "file:tools/scripts/data.sql", "/test_init.sql"])
class TasksetTests {
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
    private lateinit var tasksetRepository: TasksetRepository
    @Autowired
    private lateinit var tasksetHistoryRepository: TasksetHistoryRepository
    @Autowired
    private lateinit var taskset2TaskRepository: Taskset2TaskRepository
    @Autowired
    private lateinit var taskset2TaskHistoryRepository: Taskset2TaskHistoryRepository
    @Autowired
    private lateinit var taskRepository: TaskRepository
    @Autowired
    private lateinit var taskHistoryRepository: TaskHistoryRepository
    @Autowired
    private lateinit var namespaceGrantsRepository: NamespaceGrantsRepository
    @Autowired
    private lateinit var namespacesRepository: NamespacesRepository


    @BeforeEach
    fun clearRepos() {
        taskset2TaskHistoryRepository.deleteAll()
        taskset2TaskRepository.deleteAll()
        tasksetHistoryRepository.deleteAll()
        tasksetRepository.deleteAll()
        taskHistoryRepository.deleteAll()
        taskRepository.deleteAll()
    }

    @Test
    fun createTasksetWithTaskAndEditIt() {
        val token = AuthUtil.getToken(mvc, userService, namespaceGrantsRepository, namespacesRepository)

        mvc.post("/api/taskset/create") {
            contentType = MediaType.APPLICATION_JSON
            content = TasksetTestData.createTasksetWithTaskReqBody
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isCreated }
        }

        mvc.post("/api/taskset/update") {
            contentType = MediaType.APPLICATION_JSON
            content = TasksetTestData.editTasksetWithTaskReqBody
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk }
        }
    }

    @Test
    fun getTasksetFullAndLink() {
        val token = AuthUtil.getToken(mvc, userService, namespaceGrantsRepository, namespacesRepository)
        // add taskset
        mvc.post("/api/taskset/create") {
            contentType = MediaType.APPLICATION_JSON
            content = TasksetTestData.createTasksetWithTaskReqBody
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isCreated }
        }
        // get full
        var res = mvc.get("/api/taskset?namespace=test_namespace_code").andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
        }.andReturn()
        var tasksetObj = JsonParser.parseString(res.response.contentAsString).asJsonObject.get("tasksets").asJsonArray.get(0).asJsonObject
        var tasks = tasksetObj.get("tasks").asJsonArray
        for (taskObj in tasks) {
            assertTrue(taskObj.asJsonObject.has("originalExpressionStructureString"))
        }
        // get link
        res = mvc.get("/api/taskset?namespace=test_namespace_code&form=link").andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
        }.andReturn()
        tasksetObj = JsonParser.parseString(res.response.contentAsString).asJsonObject.get("tasksets").asJsonArray.get(0).asJsonObject
        tasks = tasksetObj.get("tasks").asJsonArray
        for (taskObj in tasks) {
            assertFalse(taskObj.asJsonObject.has("originalExpressionStructureString"))
        }
    }

    @Test
    fun getTasksetLinkByCode() {
        val code = "demo_demo"
        val token = AuthUtil.getToken(mvc, userService, namespaceGrantsRepository, namespacesRepository)
        // add taskset
        mvc.post("/api/taskset/create") {
            contentType = MediaType.APPLICATION_JSON
            content = TasksetTestData.createTasksetWithTaskReqBody
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isCreated }
        }
        // get link
        val res = mvc.get("/api/taskset?code=$code&form=link").andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
        }.andReturn()
        val tasksetObj = JsonParser.parseString(res.response.contentAsString).asJsonObject.get("tasksets").asJsonArray.get(0).asJsonObject
        assertEquals(tasksetObj.get("code").asString, code)
        val tasks = tasksetObj.get("tasks").asJsonArray
        for (taskObj in tasks) {
            assertFalse(taskObj.asJsonObject.has("originalExpressionStructureString"))
        }
    }

    @Test
    fun getTasksetLinkByName() {
        // todo
        val code = "demo_demo"
        val token = AuthUtil.getToken(mvc, userService, namespaceGrantsRepository, namespacesRepository)
        // add taskset
        mvc.post("/api/taskset/create") {
            contentType = MediaType.APPLICATION_JSON
            content = TasksetTestData.createTasksetWithTaskReqBody
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isCreated }
        }
        mvc.post("/api/taskset/create") {
            contentType = MediaType.APPLICATION_JSON
            content = TasksetTestData.createDifferentTasksetWithTaskReqBody
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isCreated }
        }
        // get link
        val res = mvc.get("/api/taskset?keywords=$code&form=link").andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
        }.andReturn()
        val tasksets = JsonParser.parseString(res.response.contentAsString).asJsonObject.get("tasksets").asJsonArray
        assertEquals(tasksets.size(), 1)
        val tasksetObj = JsonParser.parseString(res.response.contentAsString).asJsonObject.get("tasksets").asJsonArray.get(0).asJsonObject
        assertEquals(tasksetObj.get("code").asString, code)
    }
}