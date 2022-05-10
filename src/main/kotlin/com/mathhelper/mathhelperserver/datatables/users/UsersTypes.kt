package com.mathhelper.mathhelperserver.datatables.users

import com.mathhelper.mathhelperserver.constants.Constants
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "user_type")
data class UserType(
    @Id
    @NotEmpty(message = "Please provide UserType code: `admin`, `casual` and etc.")
    @Column(unique = true, length = Constants.STRING_LENGTH_SHORT)
    var code: String = "",

    @NotEmpty(message = "Please provide current UserType description.")
    @Column(columnDefinition = "TEXT")
    var description: String = ""
)


interface UserTypeRepository : JpaRepository<UserType, String> {
    fun existsByCode(code: String): Boolean

    @Transactional
    fun findByCode(code: String): UserType?
}