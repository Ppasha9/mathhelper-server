package com.mathhelper.mathhelperserver.datatables.tasks

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "task_to_tag")
data class Task2Tag(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_to_tag_gen")
    @SequenceGenerator(name = "task_to_tag_gen", sequenceName = "task_to_tag_seq", allocationSize = 1)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_code")
    var task: Task,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_code")
    var tag: Tag
)

interface Task2TagRepository: JpaRepository<Task2Tag, Long> {
    fun existsByTaskCode(taskCode: String): Boolean
    fun existsByTagCode(tagCode: String): Boolean
    fun existsByTaskCodeAndTagCode(taskCode: String, tagCode: String): Boolean

    fun findByTaskCode(taskCode: String): List<Task2Tag>
    fun findByTagCode(tagCode: String): List<Task2Tag>
    fun findByTaskCodeAndTagCode(taskCode: String, tagCode: String): Task2Tag?
}