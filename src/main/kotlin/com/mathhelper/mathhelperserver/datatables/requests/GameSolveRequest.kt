package com.mathhelper.mathhelperserver.datatables.requests

import com.mathhelper.mathhelperserver.datatables.log.App
import com.mathhelper.mathhelperserver.datatables.tasks.AutoSubTask
import com.mathhelper.mathhelperserver.datatables.tasks.Task
import com.mathhelper.mathhelperserver.datatables.users.User
import org.springframework.data.jpa.repository.JpaRepository
import java.sql.Timestamp
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "game_solve_request")
data class GameSolveRequest(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "game_solve_requests_gen")
    @SequenceGenerator(name = "game_solve_requests_gen", sequenceName = "game_solve_requests_seq")
    var id: Long? = null,

    @NotEmpty(message = "Please provide game solve request method (POST, GET, ...)")
    var method: String,

    @NotEmpty(message = "Please provide game solve request path")
    @Column(columnDefinition = "TEXT")
    var path: String,

    @ManyToOne
    @JoinColumn(name = "app_code")
    @NotEmpty(message = "Please provide game solve request author app code")
    var app: App,

    @Column(name = "user_ip")
    @NotEmpty(message = "Please provide game solve request author user's ip")
    var userIP: String,

    @ManyToOne
    @JoinColumn(name = "user_code")
    @NotEmpty(message = "Please provide game solve request author user")
    var user: User,

    @Column(name = "task_type")
    @NotEmpty(message = "Please provide game solve request author target task type")
    var taskType: String,

    @ManyToOne
    @JoinColumn(name = "task_code")
    var task: Task? = null,

    @ManyToOne
    @JoinColumn(name = "auto_sub_task_code")
    var autoSubTask: AutoSubTask? = null,

    @Column(name = "client_ts")
    var clientTS: Timestamp? = null,

    @Column(name = "server_ts")
    var serverTS: Timestamp? = null
)


interface GameSolveRequestRepository: JpaRepository<GameSolveRequest, Long> {
    fun existsByPath(path: String): Boolean
    fun existsByUserCode(userCode: String): Boolean
    fun existsByAppCode(appCode: String): Boolean
    fun existsByTaskCode(taskCode: String): Boolean
    fun existsByAutoSubTaskCode(autoSubTaskCode: String): Boolean

    fun findByPath(path: String): List<GameSolveRequest>
    fun findByUserCode(userCode: String): List<GameSolveRequest>
    fun findByAppCode(appCode: String): List<GameSolveRequest>
    fun findByTaskCode(taskCode: String): List<GameSolveRequest>
    fun findByAutoSubTaskCode(autoSubTaskCode: String): List<GameSolveRequest>
}