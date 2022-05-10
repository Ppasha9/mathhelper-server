package com.mathhelper.mathhelperserver.forms.log

import com.mathhelper.mathhelperserver.constants.Constants
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

// TODO: remove
val UserOldRefMap = mapOf(
    "BY_USER_LOGIN" to "userLogin",
    "BY_USER_NAME" to "userName",
    "BY_USER_FULL_NAME" to "userFullName",
    "BY_LEVELS_COUNT" to "tasksCount",
    "BY_RATING" to "rating"
)

data class ResultUserSearchForm(
    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    var appCode: String? = null,

    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    // TODO: tasksetCode
    var gameCode: String? = null,

    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    // TODO: taskCode
    var levelCode: String? = null,

    // TODO: sortBy
    var sortedBy: String? = null,
    var descending: Boolean = false,

    var limit: Int? = 10000,
    var offset: Int? = 0,

    var onlyNew: Boolean = false
)

data class ResultUserForm (
    var userCode: String? = null,
    var userLogin: String? = null,
    var userName: String? = null,
    var userFullName: String? = null,
    var additionalInfo: String? = null,
    // TODO: tasksCount
    var levelsCount: Long? = null,
    var tasksDifficulty: Double? = null,
    var rating: Double? = null
)