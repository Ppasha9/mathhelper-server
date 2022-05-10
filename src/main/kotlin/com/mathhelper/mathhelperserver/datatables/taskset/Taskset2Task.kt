package com.mathhelper.mathhelperserver.datatables.taskset

import com.mathhelper.mathhelperserver.datatables.tasks.Task
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "taskset_to_task")
data class Taskset2Task(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "taskset_to_task_gen")
    @SequenceGenerator(name = "taskset_to_task_gen", sequenceName = "taskset_to_task_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taskset_code")
    var taskset: Taskset,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_code")
    var task: Task
)


interface Taskset2TaskRepository : JpaRepository<Taskset2Task, Long> {
    fun existsByTasksetCode(tasksetCode: String): Boolean
    fun existsByTaskCode(taskCode: String): Boolean
    fun existsByTasksetCodeAndTaskCode(tasksetCode: String, taskCode: String): Boolean

    fun findByTasksetCode(tasksetCode: String): List<Taskset2Task>
    fun findByTaskCode(taskCode: String): List<Taskset2Task>
    fun findByTasksetCodeAndTaskCode(tasksetCode: String, taskCode: String): List<Taskset2Task>
}