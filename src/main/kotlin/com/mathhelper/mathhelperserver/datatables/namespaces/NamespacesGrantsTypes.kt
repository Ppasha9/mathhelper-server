package com.mathhelper.mathhelperserver.datatables.namespaces

import com.mathhelper.mathhelperserver.constants.Constants
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "namespace_grant_type")
data class NamespaceGrantType(
    @Id
    @NotEmpty(message = "Please provide NamespaceGrantType code: `PUBLIC_READ_WRITE`, `PRIVATE_READ_WRITE` and etc.")
    @Column(unique = true, length = Constants.STRING_LENGTH_SHORT)
    var code: String = "",

    @NotEmpty(message = "Please provide current NamespaceGrantType description.")
    @Column(columnDefinition = "TEXT")
    var description: String = ""
)


interface NamespaceGrantTypesRepository : JpaRepository<NamespaceGrantType, String> {
    fun existsByCode(code: String): Boolean

    fun findByCode(code: String): NamespaceGrantType?
}