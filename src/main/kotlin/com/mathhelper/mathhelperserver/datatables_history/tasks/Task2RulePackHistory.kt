package com.mathhelper.mathhelperserver.datatables_history.tasks

import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePackHistory
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import javax.persistence.*

@Entity
@Table(name = "task_to_rule_pack_history")
data class Task2RulePackHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_to_rule_pack_history_gen")
    @SequenceGenerator(name = "task_to_rule_pack_history_gen", sequenceName = "task_to_rule_pack_history_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = [
        JoinColumn(name = "task_code", referencedColumnName = "code"),
        JoinColumn(name = "task_version", referencedColumnName = "version")
    ])
    var task: TaskHistory,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = [
        JoinColumn(name = "rule_pack_code", referencedColumnName = "code"),
        JoinColumn(name = "rule_pack_version", referencedColumnName = "version")
    ])
    var rulePack: RulePackHistory,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "active_date_from")
    var activeDateFrom: OffsetDateTime? = null,

    @Column(name = "active_date_to")
    var activeDateTo: OffsetDateTime? = null
)


interface Task2RulePackHistoryRepository : JpaRepository<Task2RulePackHistory, Long> {
    fun existsByTaskId(taskId: HistoryId): Boolean
    fun existsByTaskIdCode(code: String): Boolean
    fun existsByRulePackId(rulePackId: HistoryId): Boolean
    fun existsByRulePackIdCode(code: String): Boolean

    fun findByTaskId(taskId: HistoryId): List<Task2RulePackHistory>
    fun findByTaskIdCode(code: String): List<Task2RulePackHistory>
    fun findByRulePackId(rulePackId: HistoryId): List<Task2RulePackHistory>
    fun findByRulePackIdCode(code: String): List<Task2RulePackHistory>
    fun findByTaskIdAndRulePackId(taskId: HistoryId, rulePackId: HistoryId): Task2RulePackHistory?
}