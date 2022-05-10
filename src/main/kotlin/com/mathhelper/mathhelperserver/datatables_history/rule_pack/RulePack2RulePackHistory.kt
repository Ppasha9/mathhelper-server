package com.mathhelper.mathhelperserver.datatables_history.rule_pack

import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import javax.persistence.*

@Entity
@Table(name = "rule_pack_to_rule_pack_history")
data class RulePack2RulePackHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rule_pack_relation_history_gen")
    @SequenceGenerator(name = "rule_pack_relation_history_gen", sequenceName = "rule_pack_relation_history_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = [
        JoinColumn(name = "parent_rule_pack_code", referencedColumnName = "code"),
        JoinColumn(name = "parent_rule_pack_version", referencedColumnName = "version")
    ])
    var parentRulePack: RulePackHistory,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = [
        JoinColumn(name = "child_rule_pack_code", referencedColumnName = "code"),
        JoinColumn(name = "child_rule_pack_version", referencedColumnName = "version")
    ])
    var childRulePack: RulePackHistory,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "active_date_from")
    var activeDateFrom: OffsetDateTime? = null,

    @Column(name = "active_date_to")
    var activeDateTo: OffsetDateTime? = null
)

interface RulePack2RulePackHistoryRepository : JpaRepository<RulePack2RulePackHistory, Long> {
    fun existsByParentRulePackId(id: HistoryId): Boolean
    fun existsByChildRulePackId(id: HistoryId): Boolean
    fun existsByParentRulePackIdAndChildRulePackId(parentId: HistoryId, childId: HistoryId): Boolean

    fun findByParentRulePackId(id: HistoryId): List<RulePack2RulePackHistory>
    fun findByChildRulePackId(id: HistoryId): List<RulePack2RulePackHistory>
}