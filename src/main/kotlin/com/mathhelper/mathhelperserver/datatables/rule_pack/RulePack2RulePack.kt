package com.mathhelper.mathhelperserver.datatables.rule_pack

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "rule_pack_to_rule_pack")
data class RulePack2RulePack(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rule_pack_relation_gen")
    @SequenceGenerator(name = "rule_pack_relation_gen", sequenceName = "rule_pack_relation_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_rule_pack_code")
    var parentRulePack: RulePack,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_rule_pack_code")
    var childRulePack: RulePack
)

interface RulePack2RulePackRepository : JpaRepository<RulePack2RulePack, Long> {
    fun existsByParentRulePackCode(code: String): Boolean
    fun existsByChildRulePackCode(code: String): Boolean
    fun existsByParentRulePackCodeAndChildRulePackCode(parentCode: String, childCode: String): Boolean

    fun findByParentRulePackCode(code: String): List<RulePack2RulePack>
    fun findByChildRulePackCode(code: String): List<RulePack2RulePack>
}