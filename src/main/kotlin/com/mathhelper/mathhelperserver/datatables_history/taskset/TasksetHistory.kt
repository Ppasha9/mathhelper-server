package com.mathhelper.mathhelperserver.datatables_history.taskset

import com.mathhelper.mathhelperserver.datatables.namespaces.Namespace
import com.mathhelper.mathhelperserver.datatables.subject_type.SubjectType
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "taskset_history")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class TasksetHistory(
    @EmbeddedId
    var id: HistoryId,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "namespace_code")
    var namespace: Namespace,

    @Column(name="keywords", columnDefinition = "TEXT")
    @NotEmpty
    var keywords: String = "",

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_code")
    var authorUser: User,

    @Column(name = "recommended_by_community")
    var recommendedByCommunity: Boolean = false,

    @Column(name = "access_start_time")
    var accessStartTime: OffsetDateTime? = null,

    @Column(name = "access_end_time")
    var accessEndTime: OffsetDateTime? = null,

    @Column(name = "other_data", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var otherData: MutableMap<String, *>? = null,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "active_date_from")
    var activeDateFrom: OffsetDateTime? = null,

    @Column(name = "active_date_to")
    var activeDateTo: OffsetDateTime? = null
)

interface TasksetHistoryRepository : JpaRepository<TasksetHistory, HistoryId> {
    fun findByIdCodeAndIsActiveTrue(code: String): TasksetHistory?
}