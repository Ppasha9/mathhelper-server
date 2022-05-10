package com.mathhelper.mathhelperserver.datatables.namespaces

import com.mathhelper.mathhelperserver.constants.Constants
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "user_grant_type")
data class UserGrantType(
    @Id
    @NotEmpty(message = "Please provide UserGrantType code: `READ_WRITE`, `READ_NO_WRITE` and 'NO_READ_WRITE'")
    @Column(unique = true, length = Constants.STRING_LENGTH_SHORT)
    var code: String = "",

    @NotEmpty(message = "Please provide current UserGrantType description.")
    @Column(columnDefinition = "TEXT")
    var description: String = ""
)


interface UserGrantTypesRepository : JpaRepository<UserGrantType, String> {
    fun existsByCode(code: String): Boolean

    fun findByCode(code: String): UserGrantType?
}