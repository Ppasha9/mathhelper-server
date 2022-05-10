package com.mathhelper.mathhelperserver.datatables.users

import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.mathhelper.mathhelperserver.datatables_history.tasks.TaskHistory
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.jpa.repository.JpaRepository
import java.sql.Timestamp
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "user_comment")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class UserComments(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usrcmnt_gen")
    @SequenceGenerator(name = "usrcmnt_gen", sequenceName = "usrcmnt_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code")
    @NotEmpty(message = "Please provide comment's creator")
    var user: User,

    // TODO: change to 'taskSet: TaskSetHistory?'
    @Column(name = "taskset_code")
    var taskSetCode: String? = "",
    @Column(name = "taskset_version")
    var taskSetVersion: Int? = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = [
        JoinColumn(name = "task_code", referencedColumnName = "code"),
        JoinColumn(name = "task_version", referencedColumnName = "version")
    ])
    var task: TaskHistory,

    @Column(columnDefinition = "TEXT")
    @NotEmpty(message = "Please provide the comment")
    var comment: String = "",

    @Column(columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var context: MutableMap<String, *>? = null,

    var rating: Float? = null,

    @Column(name = "client_action_time")
    var clientActionTime: Timestamp? = null,

    @CreationTimestamp
    @Column(name = "server_action_time")
    var serverActionTime: Timestamp? = null
)


// just some functions for first time
interface UserCommentsRepository : JpaRepository<UserComments, Long> {
    // TODO: findByTaskSet(taskSet: TaskSetHistory)
    fun existsByTaskId(taskId: HistoryId): Boolean

    fun findByTaskId(taskId: HistoryId): List<UserComments>
    fun findByTaskIdOrderByServerActionTime(taskId: HistoryId): List<UserComments>
}