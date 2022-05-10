package com.mathhelper.mathhelperserver.datatables.tasks

import com.mathhelper.mathhelperserver.datatables.rule_pack.RulePack
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "task_to_rule_pack")
data class Task2RulePack(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_to_rule_pack_gen")
    @SequenceGenerator(name = "task_to_rule_pack_gen", sequenceName = "task_to_rule_pack_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_code")
    var task: Task,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_pack_code")
    var rulePack: RulePack
)


interface Task2RulePackRepository : JpaRepository<Task2RulePack, Long> {
    fun existsByTaskCode(taskCode: String): Boolean
    fun existsByRulePackCode(rulePackCode: String): Boolean
    fun existsByTaskCodeAndRulePackCode(taskCode: String, rulePackCode: String): Boolean

    fun findByTaskCode(taskCode: String): List<Task2RulePack>
    fun findByRulePackCode(rulePackCode: String): List<Task2RulePack>
    fun findByTaskCodeAndRulePackCode(taskCode: String, rulePackCode: String): List<Task2RulePack>
}