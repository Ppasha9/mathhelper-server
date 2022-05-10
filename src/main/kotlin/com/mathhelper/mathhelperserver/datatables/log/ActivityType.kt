package com.mathhelper.mathhelperserver.datatables.log

import com.mathhelper.mathhelperserver.constants.Constants
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "activity_type")
data class ActivityType(
    @Id
    @NotEmpty(message = "Please provide activity type code")
    @Column(unique = true, length = Constants.STRING_LENGTH_SHORT)
    var code: String = "",

    @NotEmpty(message = "Please provide current activity type description")
    @Column(columnDefinition = "TEXT")
    var description: String = ""
)

interface ActivityTypeRepository : JpaRepository<ActivityType, String> {
    fun existsByCode(code: String): Boolean

    fun findByCode(code: String): ActivityType?
}