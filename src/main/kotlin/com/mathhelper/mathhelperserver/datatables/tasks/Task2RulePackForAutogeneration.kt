package com.mathhelper.mathhelperserver.datatables.tasks

import com.mathhelper.mathhelperserver.datatables.rule_pack.RulePack
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "task_to_rule_pack_for_autogeneration")
data class Task2RulePackForAutogeneration(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tasks_to_rule_packs_for_autogeneration_gen")
    @SequenceGenerator(
        name = "tasks_to_rule_packs_for_autogeneration_gen",
        sequenceName = "tasks_to_rule_packs_for_autogeneration_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_code")
    var task: Task,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_pack_code")
    var rulePack: RulePack
)


interface Task2RulePackForAutogenerationRepository : JpaRepository<Task2RulePackForAutogeneration, Long> {
    fun existsByTaskCode(taskCode: String): Boolean
    fun existsByRulePackCode(rulePackCode: String): Boolean
    fun existsByTaskCodeAndRulePackCode(taskCode: String, rulePackCode: String): Boolean

    fun findByTaskCode(taskCode: String): List<Task2RulePackForAutogeneration>
    fun findByRulePackCode(rulePackCode: String): List<Task2RulePackForAutogeneration>
    fun findByTaskCodeAndRulePackCode(taskCode: String, rulePackCode: String): List<Task2RulePackForAutogeneration>
}