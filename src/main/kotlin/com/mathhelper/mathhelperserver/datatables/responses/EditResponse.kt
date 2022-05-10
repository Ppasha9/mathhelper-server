package com.mathhelper.mathhelperserver.datatables.responses

import com.mathhelper.mathhelperserver.datatables.requests.EditRequest
import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePackHistory
import com.mathhelper.mathhelperserver.datatables_history.tasks.TaskHistory
import com.mathhelper.mathhelperserver.datatables_history.taskset.TasksetHistory
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.jpa.repository.JpaRepository
import java.sql.Timestamp
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "edit_response")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class EditResponse(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "edit_responses_gen")
    @SequenceGenerator(name = "edit_responses_gen", sequenceName = "edit_responses_seq")
    var id: Long? = null,

    @OneToOne
    @JoinColumn(name = "request_id")
    @NotEmpty(message = "Please provide depended request")
    var request: EditRequest,

    @ManyToOne
    @JoinColumns(value = [
        JoinColumn(name = "task_code", referencedColumnName = "code"),
        JoinColumn(name = "task_version", referencedColumnName = "version")
    ])
    var task: TaskHistory? = null,

    @ManyToOne
    @JoinColumns(value = [
        JoinColumn(name = "taskset_code", referencedColumnName = "code"),
        JoinColumn(name = "taskset_version", referencedColumnName = "version")
    ])
    var taskset: TasksetHistory? = null,

    @ManyToOne
    @JoinColumns(value = [
        JoinColumn(name = "rule_pack_code", referencedColumnName = "code"),
        JoinColumn(name = "rule_pack_version", referencedColumnName = "version")
    ])
    var rulePack: RulePackHistory? = null,

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

    @NotEmpty(message = "Please provide 'validated' bool flag value")
    var validated: Boolean,

    @Column(name = "server_ts")
    var serverTS: Timestamp? = null
)


interface EditResponseRepository: JpaRepository<EditResponse, Long> {
    fun existsByRequestId(requestId: Long): Boolean
    fun existsByTaskId(taskId: HistoryId): Boolean
    fun existsByTasksetId(tasksetId: HistoryId): Boolean
    fun existsByRulePackId(rulePackId: HistoryId): Boolean

    fun findByRequestId(requestId: Long): EditResponse?
    fun findByTaskId(taskId: HistoryId): List<EditResponse>
    fun findByTasksetId(tasksetId: HistoryId): List<EditResponse>
    fun findByRulePackId(rulePackId: HistoryId): List<EditResponse>
}