package com.mathhelper.mathhelperserver.datatables_history.best_solutions

import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.mathhelper.mathhelperserver.datatables_history.tasks.TaskHistory
import org.springframework.data.jpa.repository.JpaRepository
import java.sql.Timestamp
import java.time.OffsetDateTime
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "best_solution_history")
data class BestSolutionHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "best_solutions_history_gen")
    @SequenceGenerator(name = "best_solutions_history_gen", sequenceName = "best_solutions_history_seq")
    var id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = [
        JoinColumn(name = "task_code", referencedColumnName = "code"),
        JoinColumn(name = "task_version", referencedColumnName = "version")
    ])
    var task: TaskHistory,

    @NotEmpty(message = "Please, provide the exact solution, it couldn't be empty")
    @Column(columnDefinition = "TEXT")
    var solution: String,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "active_date_from")
    var activeDateFrom: OffsetDateTime? = null,

    @Column(name = "active_date_to")
    var activeDateTo: OffsetDateTime? = null
)


interface BestSolutionHistoryRepository : JpaRepository<BestSolutionHistory, Long> {
    fun existsByTaskId(taskId: HistoryId): Boolean
    fun existsByTaskIdCode(code: String): Boolean

    fun findByTaskId(taskId: HistoryId): List<BestSolutionHistory>
    fun findByTaskIdCode(code: String): List<BestSolutionHistory>
}