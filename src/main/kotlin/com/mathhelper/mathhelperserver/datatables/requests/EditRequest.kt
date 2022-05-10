package com.mathhelper.mathhelperserver.datatables.requests

import com.mathhelper.mathhelperserver.datatables.log.App
import com.mathhelper.mathhelperserver.datatables.users.User
import org.springframework.data.jpa.repository.JpaRepository
import java.sql.Timestamp
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "edit_request")
data class EditRequest(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "edit_requests_gen")
    @SequenceGenerator(name = "edit_requests_gen", sequenceName = "edit_requests_seq")
    var id: Long? = null,

    @NotEmpty(message = "Please provide edit request method (POST, GET, ...)")
    var method: String,

    @NotEmpty(message = "Please provide edit request path")
    @Column(columnDefinition = "TEXT")
    var path: String,

    @ManyToOne
    @JoinColumn(name = "app_code")
    @NotEmpty(message = "Please provide edit request author app code")
    var app: App?,

    @ManyToOne
    @JoinColumn(name = "action_type")
    @NotEmpty(message = "Please provide edit request action type")
    var actionType: EditRequestActionType?,

    @Column(name = "user_ip")
    @NotEmpty(message = "Please provide edit request author user's ip")
    var userIP: String,

    @ManyToOne
    @JoinColumn(name = "user_code")
    @NotEmpty(message = "Please provide edit request author user")
    var user: User?,

    @Column(name = "client_ts")
    var clientTS: Timestamp? = null,

    @Column(name = "server_ts")
    var serverTS: Timestamp? = null
)


interface EditRequestRepository: JpaRepository<EditRequest, Long> {
    fun existsByPath(path: String): Boolean
    fun existsByUserCode(userCode: String): Boolean
    fun existsByAppCode(appCode: String): Boolean
    fun existsByActionTypeCode(actionTypeCode: String): Boolean

    fun findByPath(path: String): List<EditRequest>
    fun findByUserCode(userCode: String): List<EditRequest>
    fun findByAppCode(appCode: String): List<EditRequest>
    fun findByActionTypeCode(actionTypeCode: String): List<EditRequest>
}