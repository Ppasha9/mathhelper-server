package com.mathhelper.mathhelperserver.datatables_history.tasks

import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.mathhelper.mathhelperserver.datatables_history.best_solutions.BestSolutionHistory
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePackHistory
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import javax.persistence.*

@Entity
@Table(name = "task_to_rule_pack_for_autogeneration_history")
data class Task2RulePackForAutogenerationHistory(
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "tasks_to_rule_packs_for_autogeneration_history_gen")
    @SequenceGenerator(
        name = "tasks_to_rule_packs_for_autogeneration_history_gen",
        sequenceName = "tasks_to_rule_packs_for_autogeneration_history_seq")
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


interface Task2RulePackForAutogenerationHistoryRepository : JpaRepository<Task2RulePackForAutogenerationHistory, Long> {
    fun existsByTaskId(taskId: HistoryId): Boolean
    fun existsByTaskIdCode(code: String): Boolean
    fun existsByRulePackId(rulePackId: HistoryId): Boolean
    fun existsByRulePackIdCode(code: String): Boolean

    fun findByTaskId(taskId: HistoryId): List<Task2RulePackForAutogenerationHistory>
    fun findByTaskIdCode(code: String): List<Task2RulePackForAutogenerationHistory>
    fun findByRulePackId(rulePackId: HistoryId): List<Task2RulePackForAutogenerationHistory>
    fun findByRulePackIdCode(code: String): List<Task2RulePackForAutogenerationHistory>
    fun findByTaskIdAndRulePackId(taskId: HistoryId, rulePackId: HistoryId): Task2RulePackForAutogenerationHistory?
}