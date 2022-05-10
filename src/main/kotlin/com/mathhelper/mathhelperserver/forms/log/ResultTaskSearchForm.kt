package com.mathhelper.mathhelperserver.forms.log

import com.mathhelper.mathhelperserver.constants.Constants
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

// TODO: remove
val TaskOldRefMap = mapOf(
    "BY_LEVEL_CODE" to "taskCode",
    "BY_GAME_NAME" to "tasksetNameEn",
    "BY_DIFFICULTY" to "difficulty",
    "BY_USERS_COUNT" to "usersCount",
    "BY_STEPS" to "steps"
)

data class ResultTaskSearchForm(
    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    var appCode: String? = null,

    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    // TODO: tasksetCode
    var gameCode: String? = null,

    @field:Size(min = Constants.STRING_LENGTH_SHORT, max = Constants.STRING_LENGTH_MIDDLE)
    var userCode: String? = null,

    // TODO: sortBy
    var sortedBy: String? = null,
    var descending: Boolean = false,

    var limit: Int? = 10000,
    var offset: Int? = 0,

    var onlyNew: Boolean = false
)

data class ResultTaskForm (
    var appCode: String? = null,

    // TODO: tasksetCode
    var gameCode: String? = null,
    // TODO: var tasksetNameEn: String? = null,
    // TODO: var tasksetNameRu: String? = null,
    var gameName: String? = null,

    // TODO: taskCode
    var levelCode: String? = null,
    var taskNameEn: String? = null,
    var taskNameRu: String? = null,

    var difficulty: Float? = null,
    // TODO: why int?
    var steps: Int? = null,
    var usersCount: Long? = null
)