package com.mathhelper.mathhelperserver.datatables.log

import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.controllers.LogController
import com.mathhelper.mathhelperserver.datatables.rule_pack.RulePack
import com.mathhelper.mathhelperserver.datatables.tasks.AutoSubTask
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.mathhelper.mathhelperserver.datatables_history.tasks.TaskHistory
import com.mathhelper.mathhelperserver.datatables_history.taskset.TasksetHistory
import com.mathhelper.mathhelperserver.forms.log.ResultTasksetForm
import com.mathhelper.mathhelperserver.forms.log.StatisticForReport
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.sql.Timestamp
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import kotlin.math.log
import org.springframework.data.jpa.repository.Modifying

data class UserStatRow(
    val tasksetCode: String,
    val passedTotal: Int,
    val pausedTotal: Int,
    val taskCode: String,
    val passedNotPaused: Boolean,
    val steps: Double,
    val time: Long,
    val expression: String?
)

@SqlResultSetMapping(
    name = "UserStatRowMapping",
    classes = [ConstructorResult(
        targetClass = UserStatRow::class,
        columns = arrayOf(
            ColumnResult(name = "taskset_code", type = String::class),
            ColumnResult(name = "passed_total", type = Int::class),
            ColumnResult(name = "paused_total", type = Int::class),
            ColumnResult(name = "task_code", type = String::class),
            ColumnResult(name = "passed_not_paused", type = Boolean::class),
            ColumnResult(name = "steps", type = Double::class),
            ColumnResult(name = "time", type = Long::class),
            ColumnResult(name = "expression", type = String::class)
        )
    )]
)

@Entity
@Table(name = "result_log")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class ResultLog(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "results_log_gen")
    @SequenceGenerator(name = "results_log_gen", sequenceName = "results_log_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_code")
    @NotEmpty(message = "Provide action's source application")
    var app: App,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = [
        JoinColumn(name = "taskset_code", referencedColumnName = "code"),
        JoinColumn(name = "taskset_version", referencedColumnName = "version")
    ])
    var taskset: TasksetHistory? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = [
        JoinColumn(name = "task_code", referencedColumnName = "code"),
        JoinColumn(name = "task_version", referencedColumnName = "version")
    ])
    var task: TaskHistory? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auto_sub_task_code")
    var autoSubTask: AutoSubTask? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code")
    @NotEmpty(message = "Provide user, that triggered action")
    var user: User,

    var difficulty: Double? = null,

    @Column(name = "base_award")
    var baseAward: Double? = null,

    @Column(name = "curr_time_ms")
    var currTimeMs: Long? = null,

    @Column(name = "curr_steps_number")
    var currStepsNumber: Double? = null,

    @Column(name = "client_action_ts")
    var clientActionTs: Timestamp? = null,

    @CreationTimestamp
    @Column(name = "server_action_ts")
    var serverActionTs: Timestamp? = null,

    @Column(name = "quality_data", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var qualityData: MutableMap<String, *>? = null,

    @Column(name = "user_cleared")
    var userCleared: Boolean = false
)

interface CustomResultLogRepository {
    fun findByAppAndTasksetAndTaskAndAutoSubTaskAndUserNative(
        appCode: String?, tasksetCode: String?, tasksetVersion: Int?, taskCode: String?, taskVersion: Int?,
        autoSubTaskCode: String?, userCode: String?, limit: Int?, offset: Int?
    ): List<ResultLog>

    fun findResultTasksetByParamsNative(
        appCode: String?, userCode: String?, sortBy: String?, sortByType: String, limit: Int, offset: Int, onlyNew: Boolean
    ): List<Any>

    fun findResultTaskByParamsNative(
        appCode: String?, tasksetCode: String?, userCode: String?, sortBy: String?, sortByType: String, limit: Int, offset: Int, onlyNew: Boolean
    ): List<Any>

    fun findResultUserByParamsNative(
        appCode: String?, tasksetCode: String?, taskCode: String?, sortBy: String?, sortByType: String, limit: Int, offset: Int, onlyNew: Boolean
    ): List<Any>

    fun getUserStatisticsNative(appCode: String, userCode: String): List<UserStatRow>

    fun getStatisticsForReport(namespaceCode: String, tasksetCode: String, startDate: Timestamp?, endDate: Timestamp?): List<Any>
}

interface ResultLogRepository : JpaRepository<ResultLog, Long>, CustomResultLogRepository {
    fun existsByAppCode(code: String): Boolean
    fun existsByTasksetId(id: HistoryId): Boolean
    fun existsByTaskId(id: HistoryId): Boolean
    fun existsByAutoSubTaskCode(code: String): Boolean
    fun existsByUserCode(code: String): Boolean

    fun findByAppCode(code: String): List<ResultLog>
    fun findByTasksetId(id: HistoryId): List<ResultLog>
    fun findByTaskId(id: HistoryId): List<ResultLog>
    fun findByAutoSubTaskCode(code: String): List<ResultLog>
    fun findByUserCode(code: String): List<ResultLog>

    @Modifying
    @Query("""
        update ResultLog
        set user_cleared = true 
        where (
            user_cleared = false and 
            user_code = :user_code and
            app_code = :app_code
        )
    """)
    fun resetResultLogByAppAndUser(@Param("app_code") appCode: String, @Param("user_code") userCode: String)
}

class CustomResultLogRepositoryImpl: CustomResultLogRepository {
    companion object {
        private val logger by lazy { LoggerFactory.getLogger("logic-logs") }
    }

    @Autowired
    private val entityManager: EntityManager? = null

    override fun findByAppAndTasksetAndTaskAndAutoSubTaskAndUserNative(
        appCode: String?, tasksetCode: String?, tasksetVersion: Int?, taskCode: String?, taskVersion: Int?,
        autoSubTaskCode: String?, userCode: String?, limit: Int?, offset: Int?
    ): List<ResultLog> {
        logger.info("findByAppAndTasksetAndTaskAndAutoSubTaskAndUserNative()")
        val query = """
            select * 
            from ${Constants.POSTGRES_SCHEME}.result_log
            where
                ('$appCode' = 'null' or app_code = '$appCode') and
                ('$tasksetCode' = 'null' or taskset_code = '$tasksetCode') and
                ('$tasksetVersion' = 'null' or taskset_version = '$tasksetVersion') and
                ('$taskCode' = 'null' or task_code = '$taskCode') and
                ('$taskVersion' = 'null' or task_version = '$taskVersion') and
                ('$autoSubTaskCode' = 'null' or auto_sub_task_code = '$autoSubTaskCode') and
                ('$userCode' = 'null' or user_code = '$userCode')
            limit ${limit ?: "all"} offset ${offset ?: 0} 
        """
        logger.info("query = $query")
        val res = entityManager!!.createNativeQuery(query, ResultLog::class.java).resultList as List<ResultLog>
        logger.info("query result size == ${res.size}")
        return res
    }

    override fun findResultTasksetByParamsNative(
        appCode: String?, userCode: String?, sortBy: String?, sortByType: String, limit: Int, offset: Int, onlyNew: Boolean
    ): List<Any> {
        logger.info("findResultTasksetByParamsNative()")
        val resTable = "${Constants.POSTGRES_SCHEME}.result_log"
        val oldResTable = "${Constants.POSTGRES_SCHEME}.old_rating"
        var query = """
            with
            united_res as (
                select user_code, app_code, task_code, taskset_code, curr_steps_number 
                from $resTable
                ${
                    if (onlyNew)
                        ""
                    else
                        "union\nselect user_code, app_code, task_code, taskset_code, curr_steps_number\nfrom $oldResTable"
                }
            )
            select 
                tcd.app_code as appCode, tcd.taskset_code as tasksetCode, tcd.tasks_count as tasksCount, tcd.tasks_difficulty as tasksDifficulty,
                uc.users_count as usersCount, i.name_en as tasksetNameEn, i.name_ru as tasksetNameRu 
            from (
                select distinct app_code, taskset_code 
                from united_res
                where 
                    ('$userCode' = 'null' or user_code = '$userCode') and 
                    ('$appCode' = 'null' or app_code = '$appCode')
            ) com
            inner join (
                select t.app_code, t.taskset_code, count(t.task_code) as tasks_count, sum(ti.difficulty) as tasks_difficulty
                from (
                    select app_code, taskset_code, task_code 
                    from united_res
                    where ('$userCode' = 'null' or user_code = '$userCode')
                    group by app_code, taskset_code, task_code
                ) t 
                inner join (
                    select code, difficulty
                    from ${Constants.POSTGRES_SCHEME}.task
                    ${
                        if (onlyNew)
                            ""
                        else
                            "union\nselect task_code as code, min(difficulty) as difficulty\nfrom $oldResTable\ngroup by task_code"         
                    }
                ) ti
                on (ti.code = t.task_code)
                group by t.app_code, t.taskset_code
            ) tcd
            on (tcd.taskset_code = com.taskset_code) and (tcd.app_code = com.app_code)
            inner join (
                select u.app_code, u.taskset_code, count(*) as users_count 
                from (
                    select app_code, taskset_code, user_code 
                    from united_res
                    group by app_code, taskset_code, user_code
                ) u
                group by u.app_code, u.taskset_code
            ) uc
            on (uc.taskset_code = com.taskset_code) and (uc.app_code = com.app_code)
            inner join (
                select code, name_en, name_ru
                from ${Constants.POSTGRES_SCHEME}.taskset
                ${
                    if (onlyNew)
                        ""
                    else
                        "union\nselect taskset_code as code, taskset_name as name_en, taskset_name as name_ru\nfrom $oldResTable\ngroup by taskset_code, taskset_name"
                }
            ) i
            on (i.code = com.taskset_code)
        """.trimIndent()
        if (sortBy != null) query += "\norder by $sortBy $sortByType\n"
        query += "\nlimit $limit offset $offset"
        logger.info("query = $query")
        val res = entityManager!!.createNativeQuery(query).resultList as List<Any>
        logger.info("query result size == ${res.size}")
        return res
    }

    override fun findResultTaskByParamsNative(
        appCode: String?, tasksetCode: String?, userCode: String?, sortBy: String?, sortByType: String, limit: Int, offset: Int, onlyNew: Boolean
    ): List<Any> {
        logger.info("findResultTaskByParamsNative()")
        val resTable = "${Constants.POSTGRES_SCHEME}.result_log"
        val oldResTable = "${Constants.POSTGRES_SCHEME}.old_rating"
        val stepFunc = if (userCode != null) "min(curr_steps_number)" else "round(avg(curr_steps_number))"
        var query = """
            with
            united_task as (
                select code, difficulty
                from ${Constants.POSTGRES_SCHEME}.task
                ${
                    if (onlyNew)
                        ""
                    else
                        "union\nselect task_code as code, min(difficulty) as difficulty\nfrom $oldResTable\ngroup by task_code"
                }
            ),
            united_res as (
                select user_code, app_code, task_code, taskset_code, curr_steps_number 
                from $resTable 
                ${
                    if (onlyNew)
                        ""
                    else
                        "union\nselect user_code, app_code, task_code, taskset_code, curr_steps_number\nfrom $oldResTable"
                }
            )
            select 
                com.app_code as appCode, tsi.code as tasksetCode, tsi.name_en as tasksetNameEn, tsi.name_ru as tasksetNameRu, 
                ti.code as taskCode, ti.name_en as taskNameEn, ti.name_ru as taskNameRu, ti.difficulty as difficulty, 
                s.steps as steps, uc.users_count as usersCount
            from (
                select distinct app_code, taskset_code, task_code  
                from united_res
                where 
                    ('$userCode' = 'null' or user_code = '$userCode') and 
                    ('$appCode' = 'null' or app_code = '$appCode') and
                    ('$tasksetCode' = 'null' or taskset_code = '$tasksetCode')
            ) com
            inner join (
                select code, name_en, name_ru
                from ${Constants.POSTGRES_SCHEME}.taskset
                ${
                    if (onlyNew)
                        ""
                    else
                        "union\nselect taskset_code as code, taskset_name as name_en, taskset_name as name_ru\nfrom $oldResTable\ngroup by taskset_code, taskset_name"
                }
            ) tsi
            on (tsi.code = com.taskset_code)
            inner join (
                select code, name_en, name_ru, difficulty
                from ${Constants.POSTGRES_SCHEME}.task
                ${
                    if (onlyNew)
                        ""
                    else
                        "union\nselect task_code as code, taskset_name as name_en, taskset_name as name_ru, min(difficulty) as difficulty\nfrom $oldResTable\ngroup by task_code, taskset_name"
                }
            ) ti
            on (ti.code = com.task_code)
            inner join (
                select app_code, taskset_code, task_code, $stepFunc as steps 
                from united_res
                where ('$userCode' = 'null' or user_code = '$userCode') 
                group by app_code, taskset_code, task_code
            ) s
            on (s.app_code = com.app_code) and (s.taskset_code = com.taskset_code) and (s.task_code = com.task_code)
            inner join (
                select app_code, taskset_code, task_code, count(distinct user_code) as users_count 
                from united_res
                group by app_code, taskset_code, task_code
            ) uc
            on (uc.app_code = com.app_code) and (uc.taskset_code = com.taskset_code) and (uc.task_code = com.task_code)
        """.trimIndent()
        if (sortBy != null) query += "\norder by $sortBy $sortByType\n"
        query += "\nlimit $limit offset $offset"
        logger.info("query = $query")
        val res = entityManager!!.createNativeQuery(query).resultList as List<Any>
        logger.info("query result size == ${res.size}")
        return res
    }

    override fun findResultUserByParamsNative(
            appCode: String?, tasksetCode: String?, taskCode: String?, sortBy: String?, sortByType: String, limit: Int, offset: Int, onlyNew: Boolean
    ): List<Any> {
        logger.info("findResultUserByParamsNative()")
        val resTable = "${Constants.POSTGRES_SCHEME}.result_log"
        val oldResTable = "${Constants.POSTGRES_SCHEME}.old_rating"
        var query = """
            with
            united_res as (
                select user_code, app_code, task_code, taskset_code, curr_steps_number 
                from $resTable
                ${
                    if (onlyNew)
                        ""
                    else
                        "union\nselect user_code, app_code, task_code, taskset_code, curr_steps_number\nfrom $oldResTable"
                }
            ),
            user_task_wins as (
                select user_code, task_code, min(curr_steps_number) as steps
                from united_res
                where
                    ('$appCode' = 'null' or app_code = '$appCode') and
                    ('$tasksetCode' = 'null' or taskset_code = '$tasksetCode') and
                    ('$taskCode' = 'null' or task_code = '$taskCode') 
                group by user_code, task_code
            ),
            all_task_wins as (
                select user_code, task_code 
                from united_res
                group by user_code, task_code
            )
            select 
                u.code as userCode, u.login as userLogin, u.name as userName, u.full_name as userFullName, 
                u.additional as additionalInfo, utrd.tasks_count as tasksCount, utrd.tasks_difficulty as tasksDifficulty, utrd.rating * uac.active_users_count as rating
            from (
                select code, login, name, full_name, additional 
                from ${Constants.POSTGRES_SCHEME}.user_entity
            ) u            
            cross join (
                select count(*) as active_users_count 
                from (
                    select user_code, count(*) as passed_tasks 
                    from all_task_wins 
                    group by user_code
                ) ut 
                where ut.passed_tasks >= ${Constants.ACTIVE_USER_COND} 
            ) uac            
            inner join (
                select utr.user_code, count(utr.task_code) as tasks_count, sum(utr.task_rating) as rating, sum(ti.difficulty) as tasks_difficulty 
                from (
                    select utw.user_code, utw.task_code, utw.steps, bw.best_steps, coalesce((bw.best_steps / (utw.steps * bw.winners_count)), 0.0) as task_rating 
                    from user_task_wins utw
                    inner join (
                        select task_code, min(steps) as best_steps, count(*) as winners_count 
                        from user_task_wins 
                        group by task_code
                    ) as bw
                    on (bw.task_code = utw.task_code)
                ) utr
                inner join (
                    select code, difficulty
                    from ${Constants.POSTGRES_SCHEME}.task
                    ${
                        if (onlyNew)
                            ""
                        else
                            "union\nselect task_code as code, min(difficulty) as difficulty\nfrom $oldResTable\ngroup by task_code"
                    }
                ) ti
                on (ti.code = utr.task_code)
                group by utr.user_code
            ) utrd
            on (utrd.user_code = u.code)
        """.trimIndent()
        if (sortBy != null) query += "\norder by $sortBy $sortByType\n"
        query += "\nlimit $limit offset $offset"
        logger.info("query = $query")
        val res = entityManager!!.createNativeQuery(query).resultList as List<Any>
        logger.info("query result size == ${res.size}")
        return res
    }

    override fun getUserStatisticsNative(appCode: String, userCode: String): List<UserStatRow> {
        logger.info("getUserStatisticsNative()")
        val resTable = "${Constants.POSTGRES_SCHEME}.result_log"
        val stepTable = "${Constants.POSTGRES_SCHEME}.last_step_log"
        val query = """
            with rlactual as (
                select rl.taskset_code, rl.task_code, rl.curr_steps_number, rl.curr_time_ms, rlclear.last_client_ts
                from $resTable rl
                join (
                    select app_code, user_code, taskset_code, task_code, max(client_action_ts) as last_client_ts
                    from $resTable
                    where user_cleared = false and app_code = '$appCode' and user_code = '$userCode'
                    group by app_code, user_code, taskset_code, task_code
                ) rlclear
                on (rlclear.app_code = rl.app_code and rlclear.user_code = rl.user_code and rlclear.taskset_code = rl.taskset_code and rlclear.task_code = rl.task_code and rlclear.last_client_ts = rl.client_action_ts)
                order by taskset_code, task_code
            ),
            lslactual as (
                select 
                    lsl.taskset_code, lsl.task_code, lsl.curr_steps_number, lsl.curr_time_ms, lsl.curr_expression, lslclear.last_client_ts
                from $stepTable lsl
                join (
                    select app_code, user_code, taskset_code, task_code, max(client_action_ts) as last_client_ts
                    from $stepTable
                    where user_cleared = false and app_code = '$appCode' and user_code = '$userCode'
                    group by app_code, user_code, taskset_code, task_code
                ) lslclear
                on (lslclear.app_code = lsl.app_code and lslclear.user_code = lsl.user_code and lslclear.taskset_code = lsl.taskset_code and lslclear.task_code = lsl.task_code and lslclear.last_client_ts = lsl.client_action_ts)
                order by taskset_code, task_code
            ),
            tstp as (
                select 
                    lslactual.taskset_code, lslactual.task_code, 
                    (rlactual.last_client_ts is not null and rlactual.last_client_ts > lslactual.last_client_ts) as passed_not_paused 
                from lslactual
                left join rlactual
                on (rlactual.taskset_code = lslactual.taskset_code and rlactual.task_code = lslactual.task_code)
            ),
            tspp as (
                select distinct tstp.taskset_code as taskset_code, 
                    count(case when passed_not_paused=true then tstp.task_code end) as passed_total, 
                    count(case when passed_not_paused=false then tstp.task_code end) as paused_total
                from tstp
                group by taskset_code
            )
            select 
                tstp.taskset_code, tspp.passed_total, tspp.paused_total, tstp.task_code, tstp.passed_not_paused,
                (case when tstp.passed_not_paused=true then rlactual.curr_steps_number else lslactual.curr_steps_number end) as steps,
                (case when tstp.passed_not_paused=true then rlactual.curr_time_ms else lslactual.curr_time_ms end) as time,
                (case when tstp.passed_not_paused=false then lslactual.curr_expression end) as expression
            from tstp
            join tspp
            on tspp.taskset_code = tstp.taskset_code
            left join rlactual
            on (rlactual.taskset_code = tstp.taskset_code and rlactual.task_code = tstp.task_code)
            join lslactual
            on (lslactual.taskset_code = tstp.taskset_code and lslactual.task_code = tstp.task_code)
        """.trimIndent()
        logger.info("query = $query")
        val res = entityManager!!.createNativeQuery(query, "UserStatRowMapping").resultList as List<UserStatRow>
        logger.info("query result size == ${res.size}")
        return res
    }

    override fun getStatisticsForReport(namespaceCode: String, tasksetCode: String, startDate: Timestamp?, endDate: Timestamp?): List<Any> {
        logger.info("getStatisticsForReport(namespaceCode = $namespaceCode, tasksetCode = $tasksetCode)")
        var query = """
            select
                rl.taskset_code,
                rl.taskset_version,
                rl.task_code,
                rl.task_version,
                t.name_ru,
                t.name_en,
                rl.user_code,
                u.login,
                u.full_name,
                u.additional,
                rl.curr_steps_number,
                rl.curr_time_ms,
                rl.difficulty,
                rl.client_action_ts,
                rl.app_code
            from public.result_log as rl
            left join public.task as t
            on t.code = rl.task_code
            left join public.user_entity as u
            on u.code = rl.user_code
        """

        query += if (namespaceCode.isNotBlank()) {
            """
            where t.namespace_code = '$namespaceCode'    
            """
        } else {
            """
            where rl.taskset_code = '$tasksetCode' 
            """
        }

        if (startDate != null && endDate != null) {
            query += """
            and rl.client_action_ts >= timestamp '$startDate' and rl.client_action_ts <= timestamp '$endDate'
            """
        }

        logger.info("query = $query")
        val res = entityManager!!.createNativeQuery(query).resultList as List<Any>
        logger.info("query result size == ${res.size}")
        return res
    }
}