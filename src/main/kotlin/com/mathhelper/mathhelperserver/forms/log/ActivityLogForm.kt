package com.mathhelper.mathhelperserver.forms.log

import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.datatables.log.ActivityType
import com.mathhelper.mathhelperserver.datatables.log.App
import com.mathhelper.mathhelperserver.datatables.tasks.AutoSubTask
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables_history.tasks.TaskHistory
import com.mathhelper.mathhelperserver.datatables_history.taskset.TasksetHistory
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import java.sql.Timestamp
import javax.persistence.*
import javax.validation.constraints.*

data class ActivityLogForm(
    @field:NotBlank
    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    var appCode: String = "",

    @field:NotBlank
    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    var activityTypeCode: String = "",

    @field:NotNull
    var clientActionTs: Timestamp? = null,

    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    var tasksetCode: String? = null,
    var tasksetVersion: Int? = null,

    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    var taskCode: String? = null,
    var taskVersion: Int? = null,

    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    var autoSubTaskCode: String? = null,

    var originalExpression: String? = null,

    var goalExpression: String? = null,

    var goalPattern: String? = null,

    var difficulty: Double? = null,

    var currSolution: String? = null,

    var currExpression: String? = null,

    var nextExpression: String? = null,

    var appliedRule: MutableMap<String, *>? = null,

    var selectedPlace: String? = null,

    var currTimeMs: Long? = null,

    var timeFromLastActionMs: Long? = null,

    var currStepsNumber: Double? = null,

    var nextStepsNumber: Double? = null,

    var subActionNumber: Int? = null,

    var subActionsAfterLastTransformation: Int? = null,

    var otherData: MutableMap<String, *>? = null,

    var otherGameStepData: MutableMap<String, *>? = null,

    var otherSolutionStepData: MutableMap<String, *>? = null,

    // Result data
    var qualityData: MutableMap<String, *>? = null,
    var baseAward: Double? = null,

    // Need for response
    var userCode: String? = null,
    var serverActionTs: Timestamp? = null
)