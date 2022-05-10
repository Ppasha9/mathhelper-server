package com.mathhelper.mathhelperserver.datatables_history.rule_pack

import com.mathhelper.mathhelperserver.datatables.namespaces.Namespace
import com.mathhelper.mathhelperserver.datatables.subject_type.SubjectType
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.jpa.repository.JpaRepository
import java.sql.Timestamp
import java.time.OffsetDateTime
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "rule_pack_history")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class RulePackHistory(
    @EmbeddedId
    var id: HistoryId,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "namespace_code")
    var namespace: Namespace,

    @Column(name="keywords", columnDefinition = "TEXT")
    @NotEmpty
    var keywords: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_code")
    var authorUser: User,

    @Column(name = "name_en")
    var nameEn: String = "",

    @Column(name = "name_ru")
    var nameRu: String = "",

    @Column(name = "description_short_en")
    var descriptionShortEn: String = "",

    @Column(name = "description_short_ru")
    var descriptionShortRu: String = "",

    @Column(name = "description_en", columnDefinition = "TEXT")
    var descriptionEn: String = "",

    @Column(name = "description_ru", columnDefinition = "TEXT")
    var descriptionRu: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_type")
    var subjectType: SubjectType? = null,

    @Column(columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var rules: ArrayList<*>? = null,

    @Column(name = "other_data", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var otherData: MutableMap<String, *>? = null,

    @Column(name = "other_check_solution_data", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var otherCheckSolutionData: MutableMap<String, *>? = null,

    @Column(name = "other_auto_generation_data", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var otherAutoGenerationData: MutableMap<String, *>? = null,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "active_date_from")
    var activeDateFrom: OffsetDateTime? = null,

    @Column(name = "active_date_to")
    var activeDateTo: OffsetDateTime? = null
)

// some functions initially
interface RulePackHistoryRepository : JpaRepository<RulePackHistory, HistoryId> {
    fun existsByIdCode(code: String): Boolean
    fun existsByIdCodeAndIsActiveTrue(code: String): Boolean

    fun findByIdCode(code: String): List<RulePackHistory>
    fun findByIdCodeAndIsActiveTrue(code: String): RulePackHistory?
}