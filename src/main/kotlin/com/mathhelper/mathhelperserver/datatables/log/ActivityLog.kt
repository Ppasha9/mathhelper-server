package com.mathhelper.mathhelperserver.datatables.log

import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.datatables.tasks.AutoSubTask
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.mathhelper.mathhelperserver.datatables_history.tasks.TaskHistory
import com.mathhelper.mathhelperserver.datatables_history.taskset.TasksetHistory
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import java.sql.Timestamp
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "activity_log")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class ActivityLog(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "activity_log_gen")
    @SequenceGenerator(name = "activity_log_gen", sequenceName = "activity_log_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_code")
    @NotEmpty(message = "Provide action's source application")
    var app: App,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_type_code")
    @NotEmpty(message = "Provide action's type")
    var activityType: ActivityType,

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

    @Column(name = "original_expression", columnDefinition = "TEXT")
    var originalExpression: String? = null,

    @Column(name = "goal_expression", columnDefinition = "TEXT")
    var goalExpression: String? = null,

    @Column(name = "goal_pattern", columnDefinition = "TEXT")
    var goalPattern: String? = null,

    var difficulty: Double? = null,

    @Column(name = "curr_solution", columnDefinition = "TEXT")
    var currSolution: String? = null,

    @Column(name = "curr_expression", columnDefinition = "TEXT")
    var currExpression: String? = null,

    @Column(name = "next_expression", columnDefinition = "TEXT")
    var nextExpression: String? = null,

    @Column(name = "applied_rule", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var appliedRule: MutableMap<String, *>? = null,

    @Column(name = "selected_place", columnDefinition = "TEXT")
    var selectedPlace: String? = null,

    @Column(name = "curr_time_ms")
    var currTimeMs: Long? = null,

    @Column(name = "time_from_last_action_ms")
    var timeFromLastActionMs: Long? = null,

    @Column(name = "curr_steps_number")
    var currStepsNumber: Double? = null,

    @Column(name = "next_steps_number")
    var nextStepsNumber: Double? = null,

    @Column(name = "sub_action_number")
    var subActionNumber: Int? = null,

    @Column(name = "sub_actions_after_last_transformation")
    var subActionsAfterLastTransformation: Int? = null,

    @Column(name = "client_action_ts")
    var clientActionTs: Timestamp? = null,

    @CreationTimestamp
    @Column(name = "server_action_ts")
    var serverActionTs: Timestamp? = null,

    @Column(name = "other_data", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var otherData: MutableMap<String, *>? = null,

    @Column(name = "other_game_step_data", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var otherGameStepData: MutableMap<String, *>? = null,

    @Column(name = "other_solution_step_data", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var otherSolutionStepData: MutableMap<String, *>? = null
)

interface CustomActivityLogRepository {
    fun findByAppAndTasksetAndTaskAndAutoSubTaskAndUserNative(
        appCode: String?, tasksetCode: String?, tasksetVersion: Int?, taskCode: String?, taskVersion: Int?,
        autoSubTaskCode: String?, userCode: String?, limit: Int?, offset: Int?
    ): List<ActivityLog>
}

interface ActivityLogRepository : JpaRepository<ActivityLog, Long>, CustomActivityLogRepository {
    fun existsByAppCode(code: String): Boolean
    fun existsByActivityTypeCode(code: String): Boolean
    fun existsByTasksetId(id: HistoryId): Boolean
    fun existsByTaskId(id: HistoryId): Boolean
    fun existsByAutoSubTaskCode(code: String): Boolean
    fun existsByUserCode(code: String): Boolean

    fun findByAppCode(code: String): List<ActivityLog>
    fun findByActivityTypeCode(code: String): List<ActivityLog>
    fun findByTasksetId(id: HistoryId): List<ActivityLog>
    fun findByTaskId(id: HistoryId): List<ActivityLog>
    fun findByAutoSubTaskCode(code: String): List<ActivityLog>
    fun findByUserCode(code: String): List<ActivityLog>
}

class CustomActivityLogRepositoryImpl: CustomActivityLogRepository {
    @Autowired
    private val entityManager: EntityManager? = null

    override fun findByAppAndTasksetAndTaskAndAutoSubTaskAndUserNative(
        appCode: String?, tasksetCode: String?, tasksetVersion: Int?, taskCode: String?, taskVersion: Int?,
        autoSubTaskCode: String?, userCode: String?, limit: Int?, offset: Int?
    ): List<ActivityLog> {
        return entityManager!!.createNativeQuery("""
            select * 
            from ${Constants.POSTGRES_SCHEME}.activity_log
            where
                ('$appCode' = 'null' or app_code = '$appCode') and
                ('$tasksetCode' = 'null' or taskset_code = '$tasksetCode') and
                ('$tasksetVersion' = 'null' or taskset_version = '$tasksetVersion') and
                ('$taskCode' = 'null' or task_code = '$taskCode') and
                ('$taskVersion' = 'null' or task_version = '$taskVersion') and
                ('$autoSubTaskCode' = 'null' or auto_sub_task_code = '$autoSubTaskCode') and
                ('$userCode' = 'null' or user_code = '$userCode')
            limit ${limit ?: "all"} offset ${offset ?: 0} 
        """, ActivityLog::class.java).resultList as List<ActivityLog>
    }
}