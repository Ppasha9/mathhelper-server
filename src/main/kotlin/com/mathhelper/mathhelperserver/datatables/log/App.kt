package com.mathhelper.mathhelperserver.datatables.log

import com.mathhelper.mathhelperserver.constants.Constants
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "app")
data class App(
    @Id
    @NotEmpty(message = "Please provide app code")
    @Column(unique = true, length = Constants.STRING_LENGTH_SHORT)
    var code: String = "",

    @NotEmpty(message = "Please provide current app description")
    @Column(columnDefinition = "TEXT")
    var description: String = ""
)

interface AppRepository : JpaRepository<App, String> {
    fun existsByCode(code: String): Boolean

    fun findByCode(code: String): App?
}