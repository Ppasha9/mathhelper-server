package com.mathhelper.mathhelperserver.forms.taskset

import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.forms.task.TaskForm
import java.sql.Timestamp
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class TasksetForm(
    var code: String? = null,

    var version: Int? = null,

    @field:NotBlank
    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    var namespaceCode: String = "",

    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    var nameEn: String? = "",
    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    var nameRu: String? = "",

    var descriptionShortEn: String? = "",
    var descriptionShortRu: String? = "",
    var descriptionEn: String? = "",
    var descriptionRu: String? = "",

    var subjectType: String? = "",

    var recommendedByCommunity: Boolean = false,

    var otherData: MutableMap<String, *>? = null,

    var tasks: ArrayList<TaskForm> = arrayListOf(),

    var tags: ArrayList<String>? = null,

    var serverActionTs: Timestamp? = null
)
