package com.mathhelper.mathhelperserver.api_requests.task

import com.google.gson.Gson
import com.mathhelper.mathhelperserver.createTestNamespace
import com.mathhelper.mathhelperserver.createTestNamespaceGrantType
import com.mathhelper.mathhelperserver.createTestUser
import com.mathhelper.mathhelperserver.createTestUserType
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespaceGrantTypesRepository
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespacesRepository
import com.mathhelper.mathhelperserver.datatables.tasks.GoalType
import com.mathhelper.mathhelperserver.datatables.tasks.Task
import com.mathhelper.mathhelperserver.datatables.users.UserRepository
import com.mathhelper.mathhelperserver.datatables.users.UserTypeRepository
import com.mathhelper.mathhelperserver.forms.task.FilterTasksForm
import com.mathhelper.mathhelperserver.services.task.TaskService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.Timestamp
import kotlin.test.assertEquals

private class KPostgreSQLContainer(image: String): PostgreSQLContainer<KPostgreSQLContainer>(image)

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TaskTests {
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

    private var testUserCode: String = ""

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var taskService: TaskService

    @Autowired
    private lateinit var namespaceRepository: NamespacesRepository
    @Autowired
    private lateinit var namespaceGrantTypesRepository: NamespaceGrantTypesRepository
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var userTypeRepository: UserTypeRepository

    fun addDefaultUserAndNamespace() {
        if (userTypeRepository.existsByCode("__test_user_type_code__"))
            return

        val userType = createTestUserType()
        userTypeRepository.saveAndFlush(userType)

        val user = createTestUser(userType = userType)
        userRepository.saveAndFlush(user)

        // Getting saved user in case of users.code autogeneration
        val savedUser = userRepository.findAll()[0]
        testUserCode = savedUser.code

        val namespaceGrantType = createTestNamespaceGrantType()
        namespaceGrantTypesRepository.saveAndFlush(namespaceGrantType)

        val namespace = createTestNamespace(authorUser = savedUser, namespaceGrantType = namespaceGrantType)
        namespaceRepository.saveAndFlush(namespace)
    }

    @Test
    @DisplayName("Get all tasks")
    fun testTaskFiltering() {
        addDefaultUserAndNamespace()

        val task1 = Task(
            code = "test_1_task_1",
            namespace = namespaceRepository.findByCode("__test_namespace_code__")!!,
            nameEn = "test_1_task_1_nameEn",
            nameRu = "test_1_task_1_nameRu",
            authorUser = userRepository.findByCode(testUserCode)!!,
            difficulty = 1.0,
            goalType = GoalType.CUSTOM,
            serverActionTs = Timestamp(System.currentTimeMillis())
        )

        val task2 = Task(
            code = "test_1_task_2",
            namespace = namespaceRepository.findByCode("__test_namespace_code__")!!,
            nameEn = "test_1_task_2_nameEn",
            nameRu = "test_1_task_2_nameRu",
            authorUser = userRepository.findByCode(testUserCode)!!,
            difficulty = 2.0,
            goalType = GoalType.CUSTOM,
            serverActionTs = Timestamp(System.currentTimeMillis())
        )

        taskService.save(task1)
        taskService.save(task2)

        val res = mvc.perform(MockMvcRequestBuilders.get("/api/task"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseContent: FilterTasksForm = Gson().fromJson(res.response.contentAsString, FilterTasksForm::class.java)
        assertEquals(responseContent.tasks.count(), 2)

        val task1Form = taskService.getTaskForm(task1)
        val task2Form = taskService.getTaskForm(task2)

        assertEquals(responseContent.tasks[0], task1Form)
        assertEquals(responseContent.tasks[1], task2Form)
    }
}