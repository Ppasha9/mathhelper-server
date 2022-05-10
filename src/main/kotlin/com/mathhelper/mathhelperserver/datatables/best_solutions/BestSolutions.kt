package com.mathhelper.mathhelperserver.datatables.best_solutions

import com.mathhelper.mathhelperserver.datatables.tasks.Task
import org.springframework.data.jpa.repository.JpaRepository
import java.sql.Timestamp
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "best_solution")
data class BestSolution(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "best_solutions_gen")
    @SequenceGenerator(name = "best_solutions_gen", sequenceName = "best_solutions_seq")
    var id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_code")
    var task: Task,

    @NotEmpty(message = "Please, provide the exact solution, it couldn't be empty")
    @Column(columnDefinition = "TEXT")
    var solution: String,

    @Column(name = "server_action_ts")
    var serverActionTs: Timestamp
)


interface BestSolutionRepository : JpaRepository<BestSolution, Long> {
    fun existsByTaskCode(code: String): Boolean

    fun findByTaskCode(code: String): BestSolution?
}