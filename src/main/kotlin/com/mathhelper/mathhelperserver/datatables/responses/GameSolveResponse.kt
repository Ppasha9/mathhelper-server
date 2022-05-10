package com.mathhelper.mathhelperserver.datatables.responses

import com.mathhelper.mathhelperserver.datatables.log.ActivityLog
import com.mathhelper.mathhelperserver.datatables.requests.GameSolveRequest
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.jpa.repository.JpaRepository
import java.sql.Timestamp
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "game_solve_response")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class GameSolveResponse(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "game_solve_responses_gen")
    @SequenceGenerator(name = "game_solve_responses_gen", sequenceName = "game_solve_responses_seq")
    var id: Long? = null,

    @OneToOne
    @JoinColumn(name = "request_id")
    @NotEmpty(message = "Please provide depended request")
    var request: GameSolveRequest,

    @OneToOne
    @JoinColumn(name = "activity_log_id")
    var activityLog: ActivityLog?,

    @Column(columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var headers: MutableMap<String, *>?,

    @Column(name = "response_body", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    @NotEmpty(message = "Please provide the body of response")
    var responseBody: MutableMap<String, *>,

    @Column(name = "return_code")
    @NotEmpty(message = "Please provide response status code")
    var returnCode: Int,

    @Column(name = "server_ts")
    var serverTS: Timestamp? = null
)


interface GameSolveResponseRepository: JpaRepository<GameSolveResponse, Long> {
    fun existsByRequestId(requestId: Long): Boolean

    fun findByRequestId(requestId: Long): GameSolveResponse?
}