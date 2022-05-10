package com.mathhelper.mathhelperserver.datatables.subject_type

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "subject_type")
data class SubjectType(
    @Id
    @NotEmpty(message = "Please provide subject type name")
    @Column(unique = true)
    var name: String = ""
)

interface SubjectTypeRepository : JpaRepository<SubjectType, String> {
    fun existsByName(name: String): Boolean
    fun findByName(name: String): SubjectType?
}