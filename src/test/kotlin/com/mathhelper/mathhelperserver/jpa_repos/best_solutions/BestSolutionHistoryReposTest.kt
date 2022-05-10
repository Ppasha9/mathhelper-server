package com.mathhelper.mathhelperserver.jpa_repos.best_solutions

import com.mathhelper.mathhelperserver.*
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespaceGrantTypesRepository
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespacesRepository
import com.mathhelper.mathhelperserver.datatables.tasks.GoalType
import com.mathhelper.mathhelperserver.datatables.users.UserRepository
import com.mathhelper.mathhelperserver.datatables.users.UserTypeRepository
import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.mathhelper.mathhelperserver.datatables_history.best_solutions.BestSolutionHistory
import com.mathhelper.mathhelperserver.datatables_history.best_solutions.BestSolutionHistoryRepository
import com.mathhelper.mathhelperserver.datatables_history.tasks.TaskHistory
import com.mathhelper.mathhelperserver.datatables_history.tasks.TaskHistoryRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

private class KPostgreSQLContainer(image: String): PostgreSQLContainer<KPostgreSQLContainer>(image)

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class BestSolutionHistoryReposTests {
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
    private lateinit var bestSolutionHistoryRepository: BestSolutionHistoryRepository
    @Autowired
    private lateinit var taskHistoryRepository: TaskHistoryRepository
    @Autowired
    private lateinit var namespacesRepository: NamespacesRepository
    @Autowired
    private lateinit var namespaceGrantTypeRepository: NamespaceGrantTypesRepository
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var userTypeRepository: UserTypeRepository

    @Test
    fun testInjectedComponentsAreNotNull() {
        assertNotNull(bestSolutionHistoryRepository)
        assertNotNull(taskHistoryRepository)
        assertNotNull(namespacesRepository)
        assertNotNull(userRepository)
        assertNotNull(userTypeRepository)
        assertNotNull(namespaceGrantTypeRepository)
    }

    @Test
    fun testRelatedTablesAreEmpty() {
        assertEquals(bestSolutionHistoryRepository.count().toInt(), 0)
        assertEquals(taskHistoryRepository.count().toInt(), 0)
        assertEquals(namespacesRepository.count().toInt(), 0)
        assertEquals(userRepository.count().toInt(), 0)
        assertEquals(userTypeRepository.count().toInt(), 0)
        assertEquals(namespaceGrantTypeRepository.count().toInt(), 0)
    }

    @Test
    fun testBestSolutionHistorySearchFunctionality() {
        val taskCode = "__test_task_code_1__"
        val taskNameEn = "test_name_en"
        val taskNameRu = "тестовое имя"
        val solution1 = "x = 59"
        val solution2 = "x = 30"

        // 1. Ensure that table is empty
        assertFalse(bestSolutionHistoryRepository.existsByTaskIdCode(taskCode))

        // 2. Test with one task and one best solution
        val authorUserType = createTestUserType()
        userTypeRepository.saveAndFlush(authorUserType)

        val authorUser = createTestUser(userType = authorUserType)
        userRepository.saveAndFlush(authorUser)

        // Getting saved user in case of users.code autogeneration
        val savedAuthorUser = userRepository.findAll()[0]

        val namespaceGrantType = createTestNamespaceGrantType()
        namespaceGrantTypeRepository.saveAndFlush(namespaceGrantType)

        val namespace = createTestNamespace(authorUser = savedAuthorUser, namespaceGrantType = namespaceGrantType)
        namespacesRepository.saveAndFlush(namespace)

        //// Add test task to history table
        val task1 = TaskHistory(
            id = HistoryId(taskCode, 0),
            nameEn = taskNameEn,
            nameRu = taskNameRu,
            namespace = namespace,
            authorUser = savedAuthorUser,
            goalType = GoalType.CUSTOM
        )
        taskHistoryRepository.saveAndFlush(task1)
        assertEquals(taskHistoryRepository.count().toInt(), 1)

        //// Add best solution to history table and connect it with test task1
        val bestSolution1 = BestSolutionHistory(
            id = 0,
            solution = solution1,
            task = task1
        )
        bestSolutionHistoryRepository.saveAndFlush(bestSolution1)

        //// Test search functionality
        var bestSolutionsByTaskCode : List<BestSolutionHistory> = bestSolutionHistoryRepository.findByTaskIdCode(taskCode)
        assertEquals(bestSolutionsByTaskCode.count(), 1)
        assertEquals(bestSolution1, bestSolutionsByTaskCode[0])

        // 3. Add some more data
        val task2 = TaskHistory(
            id = HistoryId(taskCode, 1),
            nameEn = taskNameEn,
            nameRu = taskNameRu,
            namespace = namespace,
            authorUser = savedAuthorUser,
            goalType = GoalType.CNF
        )
        taskHistoryRepository.saveAndFlush(task2)
        assertEquals(taskHistoryRepository.count().toInt(), 2)

        //// Add one more best solution to history table and connect it with test task2
        val bestSolution2 = BestSolutionHistory(
            id = 0,
            solution = solution2,
            task = task2
        )
        bestSolutionHistoryRepository.saveAndFlush(bestSolution2)

        //// Test search functionality
        bestSolutionsByTaskCode  = bestSolutionHistoryRepository.findByTaskIdCode(taskCode)
        assertEquals(bestSolutionsByTaskCode.count(), 2)
        assertEquals(bestSolution1, bestSolutionsByTaskCode[0])
        assertEquals(bestSolution2, bestSolutionsByTaskCode[1])
    }
}