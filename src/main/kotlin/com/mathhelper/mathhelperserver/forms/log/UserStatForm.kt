package com.mathhelper.mathhelperserver.forms.log

enum class StateType {
    DONE, PAUSED, NOT_STARTED
}

data class TaskStat(
    val code: String,
    val steps: Double,
    val time: Long,
    val state: String,
    val expression: String?
)

data class TasksetStat(
    val code: String,
    val passedCount: Int,
    val pausedCount: Int,
    val tasksStat: ArrayList<TaskStat>
)

data class UserStatForm(
    val tasksetStatistics: ArrayList<TasksetStat>
)