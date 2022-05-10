package com.mathhelper.mathhelperserver.datatables_history.taskset

import com.mathhelper.mathhelperserver.datatables.tasks.Task
import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.mathhelper.mathhelperserver.datatables_history.tasks.TaskHistory
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import javax.persistence.*

@Entity
@Table(name = "taskset_to_task_history")
data class Taskset2TaskHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "taskset_to_task_history_gen")
    @SequenceGenerator(name = "taskset_to_task_history_gen", sequenceName = "taskset_to_task_history_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = [
        JoinColumn(name = "taskset_code", referencedColumnName = "code"),
        JoinColumn(name = "taskset_version", referencedColumnName = "version")
    ])
    var taskset: TasksetHistory,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = [
        JoinColumn(name = "task_code", referencedColumnName = "code"),
        JoinColumn(name = "task_version", referencedColumnName = "version")
    ])
    var task: TaskHistory,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "active_date_from")
    var activeDateFrom: OffsetDateTime? = null,

    @Column(name = "active_date_to")
    var activeDateTo: OffsetDateTime? = null
)

interface Taskset2TaskHistoryRepository : JpaRepository<Taskset2TaskHistory, Long> {
    fun existsByTasksetId(tasksetId: HistoryId): Boolean
    fun existsByTaskId(taskId: HistoryId): Boolean
    fun existsByTasksetIdAndTaskId(tasksetId: HistoryId, taskId: HistoryId): Boolean

    fun findByTasksetId(tasksetId: HistoryId): List<Taskset2TaskHistory>
    fun findByTaskId(taskId: HistoryId): List<Taskset2TaskHistory>
    fun findByTasksetIdAndTaskId(tasksetId: HistoryId, taskId: HistoryId): Taskset2TaskHistory?
}