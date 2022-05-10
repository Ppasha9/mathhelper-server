package com.mathhelper.mathhelperserver.datatables.taskset

import com.mathhelper.mathhelperserver.datatables.tasks.Tag
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "taskset_to_tag")
data class Taskset2Tag(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "taskset_to_tag_gen")
    @SequenceGenerator(name = "taskset_to_tag_gen", sequenceName = "taskset_to_tag_seq", allocationSize = 1)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taskset_code")
    var taskset: Taskset,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_code")
    var tag: Tag
)

interface Taskset2TagRepository: JpaRepository<Taskset2Tag, Long> {
    fun existsByTasksetCode(tasksetCode: String): Boolean
    fun existsByTagCode(tagCode: String): Boolean
    fun existsByTasksetCodeAndTagCode(tasksetCode: String, tagCode: String): Boolean

    fun findByTasksetCode(tasksetCode: String): List<Taskset2Tag>
    fun findByTagCode(tagCode: String): List<Taskset2Tag>
    fun findByTasksetCodeAndTagCode(tasksetCode: String, tagCode: String): Taskset2Tag?
}