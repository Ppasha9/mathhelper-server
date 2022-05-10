package com.mathhelper.mathhelperserver.datatables.requests

import com.mathhelper.mathhelperserver.constants.Constants
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "edit_request_action_type")
data class EditRequestActionType(
    @Id
    @NotEmpty(message = "Please provide edit request action type code")
    @Column(unique = true, length = Constants.STRING_LENGTH_SHORT)
    var code: String = "",

    @NotEmpty(message = "Please provide current action type description")
    @Column(columnDefinition = "TEXT")
    var description: String = ""
)


interface EditRequestActionTypeRepository: JpaRepository<EditRequestActionType, String> {
    fun findByCode(code: String): EditRequestActionType?
}
