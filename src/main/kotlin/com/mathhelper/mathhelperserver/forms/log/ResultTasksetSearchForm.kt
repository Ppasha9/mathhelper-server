package com.mathhelper.mathhelperserver.forms.log

import com.mathhelper.mathhelperserver.constants.Constants
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

// TODO: remove
val TasksetOldRefMap = mapOf(
    "BY_GAME_NAME" to "tasksetNameEn",
    "BY_LEVELS_COUNT" to "tasksCount",
    "BY_USERS_COUNT" to "usersCount"
)

data class ResultTasksetSearchForm(
    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    var appCode: String? = null,

    @field:Size(min = Constants.STRING_LENGTH_SHORT, max = Constants.STRING_LENGTH_MIDDLE)
    var userCode: String? = null,

    // TODO: sortBy
    var sortedBy: String? = null,
    var descending: Boolean = false,

    var limit: Int? = 10000,
    var offset: Int? = 0,

    var onlyNew: Boolean = false
)

data class ResultTasksetForm (
    var appCode: String? = null,
    // TODO: tasksetCode
    var gameCode: String? = null,
    // TODO: tasksCount
    var levelsCount: Int? = null,
    var tasksDifficulty: Double? = null,
    var usersCount: Long? = null,
    // TODO: var tasksetNameEn: String? = null,
    // TODO: var tasksetNameRu: String? = null
    var gameName: String? = null
)