package com.mathhelper.mathhelperserver.api_requests.log

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mathhelper.mathhelperserver.AuthUtil
import com.mathhelper.mathhelperserver.constants.ActionTypes
import com.mathhelper.mathhelperserver.datatables.log.ActivityLogRepository
import com.mathhelper.mathhelperserver.datatables.log.LastStepLogRepository
import com.mathhelper.mathhelperserver.datatables.log.ResultLogRepository
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespaceGrantsRepository
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespacesRepository
import com.mathhelper.mathhelperserver.forms.log.ResultTaskForm
import com.mathhelper.mathhelperserver.forms.log.ResultTasksetForm
import com.mathhelper.mathhelperserver.forms.log.ResultUserForm
import com.mathhelper.mathhelperserver.forms.log.UserStatForm
import com.mathhelper.mathhelperserver.services.user.UserService
import org.apache.catalina.User
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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class KPostgreSQLContainer(image: String) : PostgreSQLContainer<KPostgreSQLContainer>(image)

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Sql("/test_init.sql")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class LogTests {
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
    private lateinit var activityLogRepository: ActivityLogRepository

    @Autowired
    private lateinit var lastStepLogRepository: LastStepLogRepository

    @Autowired
    private lateinit var resultLogRepository: ResultLogRepository

    @Autowired
    private lateinit var namespaceGrantsRepository: NamespaceGrantsRepository

    @Autowired
    private lateinit var namespacesRepository: NamespacesRepository

    fun addOkLog(log: String) {
        mvc.post("/api/log/activity/create") {
            contentType = MediaType.APPLICATION_JSON
            content = log
            header("Authorization", "Bearer ${AuthUtil.getToken(mvc, userService, namespaceGrantsRepository, namespacesRepository)}")
        }.andExpect {
            status { isCreated }
        }
    }

    @BeforeEach
    fun clearRepos() {
        lastStepLogRepository.deleteAll()
        resultLogRepository.deleteAll()
        activityLogRepository.deleteAll()
    }

    @Test
    fun addJustActivityLog() {
        addOkLog(LogTestsData.activitySaveLog)
        /* log/activity:
        | app_code          | activity_type_code    | ... |
        ---------------------------------------------------
        | "test_app_code"   | "save"                | ... |
         */
        val logs = activityLogRepository.findAll()
        assertEquals(logs.size, 1)
        assertEquals(logs[0].activityType.code, ActionTypes.SAVE.str)
        assertEquals(lastStepLogRepository.count(), 0)
        assertEquals(resultLogRepository.count(), 0)
    }

    @Test
    fun addActivityLogLongData() {
        addOkLog(LogTestsData.activitySaveLogLongData)
        /* log/activity:
        | app_code          | activity_type_code    | ... |
        ---------------------------------------------------
        | "test_app_code"   | "save"                | ... |
         */
        val logs = activityLogRepository.findAll()
        assertEquals(logs.size, 1)
        assertEquals(logs[0].activityType.code, ActionTypes.SAVE.str)
        assertEquals(lastStepLogRepository.count(), 0)
        assertEquals(resultLogRepository.count(), 0)
    }

    @Test
    fun addActivityWithStepLog() {
        addOkLog(LogTestsData.activityRuleLog)
        /* log/activity & last_step_log:
        | app_code          | activity_type_code    | ... |
        ---------------------------------------------------
        | "test_app_code"   | "rule"                | ... |
         */
        val logs = activityLogRepository.findAll()
        assertEquals(logs.size, 1)
        assertEquals(logs[0].activityType.code, ActionTypes.RULE.str)
        assertEquals(lastStepLogRepository.count(), 1)
        assertEquals(resultLogRepository.count(), 0)
    }

    @Test
    fun addPlaceAndThenRule() {
        addOkLog(LogTestsData.activityPlaceLog)
        addOkLog(LogTestsData.activityRuleLog)
        /* log/activity:
        | app_code          | activity_type_code    | ... |
        ---------------------------------------------------
        | "test_app_code"   | "place"               | ... |
        ---------------------------------------------------
        | "test_app_code"   | "rule"                | ... |
         */
        /* last_step_log:
        | app_code          | activity_type_code    | ... |
        ---------------------------------------------------
        | "test_app_code"   | "rule"                | ... |
         */
        val logs = activityLogRepository.findAll()
        assertEquals(logs.size, 2)
        assertEquals(lastStepLogRepository.count(), 1)
        val stepLogs = lastStepLogRepository.findByActivityTypeCode(ActionTypes.RULE.str)
        assertEquals(stepLogs.size, 1)
        assertEquals(resultLogRepository.count(), 0)
    }

    @Test
    fun addActivityWithResultLog() {
        addOkLog(LogTestsData.activityWinLog)
        /* log/activity:
        | app_code          | activity_type_code    | ... |
        ---------------------------------------------------
        | "test_app_code"   | "win"                 | ... |
         */
        /* result_log:
        | app_code          | activity_type_code    | ... |
        ---------------------------------------------------
        | "test_app_code"   | "win"                 | ... |
         */
        val logs = activityLogRepository.findAll()
        assertEquals(logs.size, 1)
        assertEquals(logs[0].activityType.code, ActionTypes.WIN.str)
        assertEquals(lastStepLogRepository.count(), 0)
        assertEquals(resultLogRepository.count(), 1)
    }

    @Test
    fun addActivityInterim() {
        addOkLog(LogTestsData.activityInterimLog)
        /* log/activity:
        | app_code          | activity_type_code    | ... |
        ---------------------------------------------------
        | "test_app_code"   | "interim"             | ... |
         */
        /* last_step_log:
        | app_code          | activity_type_code    | ... |
        ---------------------------------------------------
        | "test_app_code"   | "interim"             | ... |
         */
        val logs = activityLogRepository.findAll()
        assertEquals(logs.size, 1)
        assertEquals(logs[0].activityType.code, ActionTypes.INTERIM.str)
        assertEquals(lastStepLogRepository.count(), 1)
        assertEquals(resultLogRepository.count(), 0)
    }

    @Test
    fun addActivityLoose() {
        addOkLog(LogTestsData.activityLooseLog)
        /* log/activity:
        | app_code          | activity_type_code    | ... |
        ---------------------------------------------------
        | "test_app_code"   | "loose"             | ... |
         */
        val logs = activityLogRepository.findAll()
        assertEquals(logs.size, 1)
        assertEquals(logs[0].activityType.code, ActionTypes.LOOSE.str)
        assertEquals(lastStepLogRepository.count(), 0)
        assertEquals(resultLogRepository.count(), 0)
    }

    @Test
    fun findActivityByForm() {
        addOkLog(LogTestsData.activityPlaceLog)
        addOkLog(LogTestsData.activitySaveLog)
        addOkLog(LogTestsData.activityWinLog)
        /* log/activity:
        | app_code          | activity_type_code    | taskset_code          | ... |
        ---------------------------------------------------------------------------
        | "test_app_code"   | "place"               | "test_taskset_code"   | ... |
        ---------------------------------------------------------------------------
        | "test_app_code"   | "save"                | null                  | ... |
        ---------------------------------------------------------------------------
        | "test_app_code"   | "win"                 | "test_taskset_code"   | ... |
         */
        /* result_log:
        | app_code          | activity_type_code    | taskset_code          | ... |
        ---------------------------------------------------------------------------
        | "test_app_code"   | "win"                 | "test_taskset_code"   | ... |
         */
        /* last_step_log:
        | app_code          | activity_type_code    | taskset_code          | ... |
        ---------------------------------------------------------------------------
        | "test_app_code"   | "place"               | "test_taskset_code"   | ... |
         */
        val res = mvc.post("/api/log/activity/find") {
            contentType = MediaType.APPLICATION_JSON
            content = LogTestsData.activitySearchForm
        }.andExpect {
            status { isOk }
        }.andReturn()
        val array = JSONArray(res.response.contentAsString)
        assertEquals(array.length(), 2)
        assertEquals(activityLogRepository.count(), 3)
        assertEquals(resultLogRepository.count(), 1)
        assertEquals(lastStepLogRepository.count(), 1)
    }

    @Test
    fun findActivityAll() {
        addOkLog(LogTestsData.activityPlaceLog)
        addOkLog(LogTestsData.activitySaveLog)
        addOkLog(LogTestsData.activityWinLog)
        /* log/activity:
        | app_code          | activity_type_code    | taskset_code          | ... |
        ---------------------------------------------------------------------------
        | "test_app_code"   | "place"               | "test_taskset_code"   | ... |
        ---------------------------------------------------------------------------
        | "test_app_code"   | "save"                | null                  | ... |
        ---------------------------------------------------------------------------
        | "test_app_code"   | "win"                 | "test_taskset_code"   | ... |
         */
        /* result_log:
        | app_code          | activity_type_code    | taskset_code          | ... |
        ---------------------------------------------------------------------------
        | "test_app_code"   | "win"                 | "test_taskset_code"   | ... |
        */
        /* last_step_log:
        | app_code          | activity_type_code    | taskset_code          | ... |
        ---------------------------------------------------------------------------
        | "test_app_code"   | "place"               | "test_taskset_code"   | ... |
         */
        val res = mvc.post("/api/log/activity/find") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk }
        }.andReturn()
        val array = JSONArray(res.response.contentAsString)
        assertEquals(array.length(), 3)
        assertEquals(activityLogRepository.count(), 3)
        assertEquals(resultLogRepository.count(), 1)
        assertEquals(lastStepLogRepository.count(), 1)
    }

    @Test
    fun findResultByForm() {
        addOkLog(LogTestsData.activityWinLog)
        /* result_log:
        | app_code          | activity_type_code    | taskset_code          | ... |
        ---------------------------------------------------------------------------
        | "test_app_code"   | "win"                 | "test_taskset_code"   | ... |
        */
        val res = mvc.post("/api/log/result/find") {
            contentType = MediaType.APPLICATION_JSON
            content = LogTestsData.activitySearchForm
        }.andExpect {
            status { isOk }
        }.andReturn()
        val array = JSONArray(res.response.contentAsString)
        assertEquals(array.length(), 1)
    }

    @Test
    fun findTasksetsResults() {
        addOkLog(LogTestsData.activityWinLog)
        addOkLog(LogTestsData.activityWinLog3)
        /* result_log:
        | app_code          | activity_type_code    | taskset_code          | task_code          | ... |
        ------------------------------------------------------------------------------------------------
        | "test_app_code"   | "win"                 | "test_taskset_code"   | "test_task_code"   | ... |
        ------------------------------------------------------------------------------------------------
        | "test_app_code"   | "win"               | "test_taskset_code"   | "test_task_2_code" | ... |
        */
        /* task:
        | code                  | difficulty | ... |
        -------------------------------------------
        | "test_task_code"      | 0.5        | ... |
        -------------------------------------------
        | "test_task_2_code"    | 5.5        | ... |
        */
        val res = mvc.post("/api/log/result/find/tasksets") {
            contentType = MediaType.APPLICATION_JSON
            content = LogTestsData.tasksetSearchForm
        }.andExpect {
            status { isOk }
        }.andReturn()
        val array = JSONArray(res.response.contentAsString)
        assertEquals(array.length(), 1)
        val tasksetResult = Gson().fromJson(array.getJSONObject(0).toString(), ResultTasksetForm::class.java)
        assertEquals(tasksetResult.levelsCount, 2)
        assertEquals(tasksetResult.tasksDifficulty, 6.0)
    }

    @Test
    fun findTasksResults() {
        addOkLog(LogTestsData.activityWinLog)
        addOkLog(LogTestsData.activityWinLog2)
        addOkLog(LogTestsData.activityWinLog3)
        /* result_log:
        | app_code          | activity_type_code    | taskset_code          | task_code          | curr_steps_number | ... |
        --------------------------------------------------------------------------------------------------------------------
        | "test_app_code"   | "win"                 | "test_taskset_code"   | "test_task_code"   | 5                 | ... |
        --------------------------------------------------------------------------------------------------------------------
        | "test_app_code"   | "win"                 | "test_taskset_code"   | "test_task_code"   | 25                | ... |
        --------------------------------------------------------------------------------------------------------------------
        | "test_app_code"   | "win"               | "test_taskset_code"   | "test_task_2_code" | 10                | ... |
        */
        val res = mvc.post("/api/log/result/find/tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = LogTestsData.taskSearchForm
        }.andExpect {
            status { isOk }
        }.andReturn()
        val array = JSONArray(res.response.contentAsString)
        assertEquals(array.length(), 2)
        val taskResult0 = Gson().fromJson(array.getJSONObject(0).toString(), ResultTaskForm::class.java)
        val taskResult1 = Gson().fromJson(array.getJSONObject(1).toString(), ResultTaskForm::class.java)
        assertEquals(taskResult0.levelCode, "test_task_2_code")
        assertEquals(taskResult0.steps, 10)
        assertEquals(taskResult1.levelCode, "test_task_code")
        assertEquals(taskResult1.steps, 15)
    }

    @Test
    fun findTasksResultsWithUser() {
        addOkLog(LogTestsData.activityWinLog)
        addOkLog(LogTestsData.activityWinLog2)
        addOkLog(LogTestsData.activityWinLog3)
        /* result_log:
        | app_code          | activity_type_code    | taskset_code          | task_code          | curr_steps_number | ... |
        --------------------------------------------------------------------------------------------------------------------
        | "test_app_code"   | "win"                 | "test_taskset_code"   | "test_task_code"   | 5                 | ... |
        --------------------------------------------------------------------------------------------------------------------
        | "test_app_code"   | "win"                 | "test_taskset_code"   | "test_task_code"   | 25                | ... |
        --------------------------------------------------------------------------------------------------------------------
        | "test_app_code"   | "win"               | "test_taskset_code"   | "test_task_2_code" | 10                | ... |
        */
        val token = AuthUtil.getToken(mvc, userService)
        val res = mvc.post("/api/log/result/find/tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = LogTestsData.taskSearchFormWithUser(AuthUtil.testUserCode)
        }.andExpect {
            status { isOk }
        }.andReturn()
        val array = JSONArray(res.response.contentAsString)
        assertEquals(array.length(), 2)
        val taskResult0 = Gson().fromJson(array.getJSONObject(0).toString(), ResultTaskForm::class.java)
        val taskResult1 = Gson().fromJson(array.getJSONObject(1).toString(), ResultTaskForm::class.java)
        assertEquals(taskResult0.levelCode, "test_task_2_code")
        assertEquals(taskResult0.steps, 10)
        assertEquals(taskResult1.levelCode, "test_task_code")
        assertEquals(taskResult1.steps, 5)
    }

    @Test
    fun findUsersResults() {
        addOkLog(LogTestsData.activityWinLog)
        addOkLog(LogTestsData.activityWinLog2)
        addOkLog(LogTestsData.activityWinLog3)
        /* result_log:
        | app_code          | activity_type_code    | taskset_code          | task_code          | curr_steps_number | ... |
        --------------------------------------------------------------------------------------------------------------------
        | "test_app_code"   | "win"                 | "test_taskset_code"   | "test_task_code"   | 5                 | ... |
        --------------------------------------------------------------------------------------------------------------------
        | "test_app_code"   | "win"                 | "test_taskset_code"   | "test_task_code"   | 25                | ... |
        --------------------------------------------------------------------------------------------------------------------
        | "test_app_code"   | "win"               | "test_taskset_code"   | "test_task_2_code" | 10                | ... |
        */
        /* task:
        | code                  | difficulty | ... |
        -------------------------------------------
        | "test_task_code"      | 0.5        | ... |
        -------------------------------------------
        | "test_task_2_code"    | 5.5        | ... |
        */
        val res = mvc.post("/api/log/result/find/users") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
        }.andExpect {
            status { isOk }
        }.andReturn()
        val array = JSONArray(res.response.contentAsString)
        assertEquals(array.length(), 1)
        val userResult = Gson().fromJson(array.getJSONObject(0).toString(), ResultUserForm::class.java)
        assertEquals(userResult.levelsCount, 2)
        assertEquals(userResult.tasksDifficulty, 6.0)
        assertEquals(userResult.rating, 2.0)
    }

    @Test
    fun getUserStatFor2SetsWithPassedAndPausedTasks() {
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:01:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:02:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.WIN, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:03:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.PLACE, taskset = "test_taskset_code", task = "test_task_2_code", ts = "2021-01-01T01:05:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_2_code", ts = "2021-01-01T01:04:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.PLACE, taskset = "test_taskset_2_code", task = "test_task_code", ts = "2021-01-01T01:06:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_2_code", task = "test_task_code", ts = "2021-01-01T01:07:01"))
        val res = mvc.post("/api/log/user_statistics?app=test_app_code") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
            header("Authorization", "Bearer ${AuthUtil.getToken(mvc, userService, namespaceGrantsRepository, namespacesRepository)}")
        }.andExpect {
            status { isOk }
        }.andReturn()
        val expectedStr = """
        {
            "tasksetStatistics": [
                {
                    "code": "test_taskset_2_code", "passedCount": 0, "pausedCount": 1,
                    "tasksStat": [
                        {"code": "test_task_code", "expression": "next_ex_test", "time": 2000, "state": "PAUSED", "steps": 1.0}                 
                    ]
                },
                {
                    "code": "test_taskset_code", "passedCount": 1, "pausedCount": 1,
                    "tasksStat": [
                        {"code": "test_task_2_code", "expression": "next_ex_test", "time": 2000, "state": "PAUSED", "steps": 1.0},
                        {"code": "test_task_code", "expression": null, "time": 2000, "state": "DONE", "steps": 0.0}                       
                    ]
                }             
            ]
        }
        """.trim()
        val expected = Gson().fromJson(expectedStr, UserStatForm::class.java)
        val stat = Gson().fromJson(res.response.contentAsString, UserStatForm::class.java)
        assertEquals(expected, stat)
    }

    @Test
    fun getUserStatWithPassedOnlyTasks() {
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:01:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:02:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.WIN, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:03:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.PLACE, taskset = "test_taskset_code", task = "test_task_2_code", ts = "2021-01-01T01:04:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.WIN, taskset = "test_taskset_code", task = "test_task_2_code", ts = "2021-01-01T01:05:01"))
        val res = mvc.post("/api/log/user_statistics?app=test_app_code") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
            header("Authorization", "Bearer ${AuthUtil.getToken(mvc, userService, namespaceGrantsRepository, namespacesRepository)}")
        }.andExpect {
            status { isOk }
        }.andReturn()
        val expectedStr = """
        {
            "tasksetStatistics": [               
                {
                    "code": "test_taskset_code", "passedCount": 2, "pausedCount": 0,
                    "tasksStat": [
                        {"code": "test_task_2_code", "expression": null, "time": 2000, "state": "DONE", "steps": 0.0},
                        {"code": "test_task_code", "expression": null, "time": 2000, "state": "DONE", "steps": 0.0}                       
                    ]
                }             
            ]
        }
        """.trim()
        val expected = Gson().fromJson(expectedStr, UserStatForm::class.java)
        val stat = Gson().fromJson(res.response.contentAsString, UserStatForm::class.java)
        assertEquals(expected, stat)
    }

    @Test
    fun getUserStatWithPausedOnlyTasks() {
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:01:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:02:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.WIN, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:00:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.PLACE, taskset = "test_taskset_code", task = "test_task_2_code", ts = "2021-01-01T01:04:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.WIN, taskset = "test_taskset_code", task = "test_task_2_code", ts = "2021-01-01T01:03:01"))
        val res = mvc.post("/api/log/user_statistics?app=test_app_code") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
            header("Authorization", "Bearer ${AuthUtil.getToken(mvc, userService, namespaceGrantsRepository, namespacesRepository)}")
        }.andExpect {
            status { isOk }
        }.andReturn()
        val expectedStr = """
        {
            "tasksetStatistics": [               
                {
                    "code": "test_taskset_code", "passedCount": 0, "pausedCount": 2,
                    "tasksStat": [
                        {"code": "test_task_2_code", "expression": "next_ex_test", "time": 2000, "state": "PAUSED", "steps": 1.0},
                        {"code": "test_task_code", "expression": "next_ex_test", "time": 2000, "state": "PAUSED", "steps": 1.0}                       
                    ]
                }             
            ]
        }
        """.trim()
        val expected = Gson().fromJson(expectedStr, UserStatForm::class.java)
        val stat = Gson().fromJson(res.response.contentAsString, UserStatForm::class.java)
        assertEquals(expected, stat)
    }

    @Test
    fun getUserStatAfterUserClearedStat() {
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:01:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:02:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.WIN, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:03:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.PLACE, taskset = "test_taskset_code", task = "test_task_2_code", ts = "2021-01-01T01:05:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_2_code", ts = "2021-01-01T01:04:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.PLACE, taskset = "test_taskset_2_code", task = "test_task_code", ts = "2021-01-01T01:06:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_2_code", task = "test_task_code", ts = "2021-01-01T01:07:01"))
        mvc.delete("/api/log/user_statistics?app=test_app_code") {
            header("Authorization", "Bearer ${AuthUtil.getToken(mvc, userService, namespaceGrantsRepository, namespacesRepository)}")
        }.andExpect {
            status { isOk }
        }
        mvc.post("/api/log/user_statistics?app=test_app_code") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
            header("Authorization", "Bearer ${AuthUtil.getToken(mvc, userService, namespaceGrantsRepository, namespacesRepository)}")
        }.andExpect {
            status { isNotFound }
        }
    }

    @Test
    fun getUserStatPassedAfterClear() {
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:01:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:02:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.WIN, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:03:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.PLACE, taskset = "test_taskset_code", task = "test_task_2_code", ts = "2021-01-01T01:05:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_2_code", ts = "2021-01-01T01:04:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.PLACE, taskset = "test_taskset_2_code", task = "test_task_code", ts = "2021-01-01T01:06:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_2_code", task = "test_task_code", ts = "2021-01-01T01:07:01"))
        mvc.delete("/api/log/user_statistics?app=test_app_code") {
            header("Authorization", "Bearer ${AuthUtil.getToken(mvc, userService, namespaceGrantsRepository, namespacesRepository)}")
        }.andExpect {
            status { isOk }
        }
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-02T01:02:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.WIN, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-02T01:03:01"))
        val res = mvc.post("/api/log/user_statistics?app=test_app_code") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
            header("Authorization", "Bearer ${AuthUtil.getToken(mvc, userService, namespaceGrantsRepository, namespacesRepository)}")
        }.andExpect {
            status { isOk }
        }.andReturn()
        val expectedStr = """
        {
            "tasksetStatistics": [               
                {
                    "code": "test_taskset_code", "passedCount": 1, "pausedCount": 0,
                    "tasksStat": [
                        {"code": "test_task_code", "expression": null, "time": 2000, "state": "DONE", "steps": 0.0}                       
                    ]
                }             
            ]
        }
        """.trim()
        val expected = Gson().fromJson(expectedStr, UserStatForm::class.java)
        val stat = Gson().fromJson(res.response.contentAsString, UserStatForm::class.java)
        assertEquals(expected, stat)
    }

    @Test
    fun getUserStatWithPausedAfterPassedTask() {
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:01:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:02:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.WIN, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:03:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.PLACE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:04:01"))
        val res = mvc.post("/api/log/user_statistics?app=test_app_code") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
            header("Authorization", "Bearer ${AuthUtil.getToken(mvc, userService, namespaceGrantsRepository, namespacesRepository)}")
        }.andExpect {
            status { isOk }
        }.andReturn()
        val expectedStr = """
        {
            "tasksetStatistics": [               
                {
                    "code": "test_taskset_code", "passedCount": 0, "pausedCount": 1,
                    "tasksStat": [
                        {"code": "test_task_code", "expression": "next_ex_test", "time": 2000, "state": "PAUSED", "steps": 1.0}                       
                    ]
                }             
            ]
        }
        """.trim()
        val expected = Gson().fromJson(expectedStr, UserStatForm::class.java)
        val stat = Gson().fromJson(res.response.contentAsString, UserStatForm::class.java)
        assertEquals(expected, stat)
    }

    @Test
    fun getUserStatWithPassedAfterPausedTask() {
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:01:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.RULE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:02:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.WIN, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:03:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.PLACE, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:04:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.WIN, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:05:01"))
        val res = mvc.post("/api/log/user_statistics?app=test_app_code") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
            header("Authorization", "Bearer ${AuthUtil.getToken(mvc, userService, namespaceGrantsRepository, namespacesRepository)}")
        }.andExpect {
            status { isOk }
        }.andReturn()
        val expectedStr = """
        {
            "tasksetStatistics": [               
                {
                    "code": "test_taskset_code", "passedCount": 1, "pausedCount": 0,
                    "tasksStat": [
                        {"code": "test_task_code", "expression": null, "time": 2000, "state": "DONE", "steps": 0.0}                       
                    ]
                }             
            ]
        }
        """.trim()
        val expected = Gson().fromJson(expectedStr, UserStatForm::class.java)
        val stat = Gson().fromJson(res.response.contentAsString, UserStatForm::class.java)
        assertEquals(expected, stat)
    }

    @Test
    fun getUserStatWithPassedWithoutPausedTask() {
        addOkLog(LogTestsData.createActivityLog(ActionTypes.WIN, taskset = "test_taskset_code", task = "test_task_code", ts = "2021-01-01T01:03:01"))
        addOkLog(LogTestsData.createActivityLog(ActionTypes.WIN, taskset = "test_taskset_code", task = "test_task_2_code", ts = "2021-01-01T01:05:01"))
        mvc.post("/api/log/user_statistics?app=test_app_code") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
            header("Authorization", "Bearer ${AuthUtil.getToken(mvc, userService, namespaceGrantsRepository, namespacesRepository)}")
        }.andExpect {
            status { isNotFound }
        }
    }
}