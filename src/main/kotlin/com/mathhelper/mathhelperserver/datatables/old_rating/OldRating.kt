package com.mathhelper.mathhelperserver.datatables.old_rating

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.jpa.repository.JpaRepository
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "old_rating")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class OldRating(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "old_rating_gen")
    @SequenceGenerator(name = "old_rating_gen", sequenceName = "old_rating_seq")
    var id: Long? = null,

    @Column(name = "user_code")
    var userCode: String,

    @Column(name = "app_code")
    var appCode: String,

    @Column(name = "taskset_code")
    var tasksetCode: String,

    @Column(name = "taskset_version")
    var tasksetVersion: Int,

    @Column(name = "taskset_name")
    var tasksetName: String,

    @Column(name = "task_code")
    var taskCode: String? = "",

    var comment: String? = "",

    var difficulty: Float? = 0f,

    @Column(name = "client_action_ts")
    var clientActionTime: Timestamp? = null,

    @Column(name = "server_action_ts")
    var actionDateServer: Timestamp? = null,

    @Column(name = "context", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var context: MutableMap<*, *>? = null,

    @Column(name = "curr_steps_number")
    var currStepsNumber: Float? = 0f
)

interface OldRatingRepository: JpaRepository<OldRating, Long> {}
