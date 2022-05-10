package com.mathhelper.mathhelperserver.datatables.log

import com.mathhelper.mathhelperserver.datatables.tasks.AutoSubTask
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.mathhelper.mathhelperserver.datatables_history.tasks.TaskHistory
import com.mathhelper.mathhelperserver.datatables_history.taskset.TasksetHistory
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.sql.Timestamp
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "last_step_log")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class LastStepLog(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "last_step_log_gen")
    @SequenceGenerator(name = "last_step_log_gen", sequenceName = "last_step_log_seq")
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

    @Column(name = "curr_solution", columnDefinition = "TEXT")
    var currSolution: String? = null,

    @Column(name = "curr_expression", columnDefinition = "TEXT")
    var currExpression: String? = null,

    @Column(name = "curr_time_ms")
    var currTimeMs: Long? = null,

    @Column(name = "curr_steps_number")
    var currStepsNumber: Double? = null,

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
    var otherSolutionStepData: MutableMap<String, *>? = null,

    @Column(name = "user_cleared")
    var userCleared: Boolean = false
)

interface LastStepLogRepository : JpaRepository<LastStepLog, Long> {
    fun existsByAppCode(code: String): Boolean
    fun existsByActivityTypeCode(code: String): Boolean
    fun existsByTasksetId(id: HistoryId): Boolean
    fun existsByTaskId(id: HistoryId): Boolean
    fun existsByAutoSubTaskCode(code: String): Boolean
    fun existsByUserCode(code: String): Boolean

    fun findByAppCode(code: String): List<LastStepLog>
    fun findByActivityTypeCode(code: String): List<LastStepLog>
    fun findByTasksetId(id: HistoryId): List<LastStepLog>
    fun findByTaskId(id: HistoryId): List<LastStepLog>
    fun findByAutoSubTaskCode(code: String): List<LastStepLog>
    fun findByUserCode(code: String): List<LastStepLog>
    fun findByAppCodeAndTasksetIdAndTaskIdAndUserCode(appCode: String, tasksetId: HistoryId, taskId: HistoryId, userCode: String): LastStepLog?
    // TODO: is it unique?
    fun findByAppCodeAndTasksetIdAndAutoSubTaskCodeAndUserCode(appCode: String, tasksetId: HistoryId, autoTaskCode: String, userCode: String): LastStepLog?

    @Modifying
    @Query("""
        update LastStepLog
        set user_cleared = true 
        where (
            user_cleared = false and 
            user_code = :user_code and
            app_code = :app_code
        )
    """)
    fun resetLastStepLogByAppAndUser(@Param("app_code") appCode: String, @Param("user_code") userCode: String)
}