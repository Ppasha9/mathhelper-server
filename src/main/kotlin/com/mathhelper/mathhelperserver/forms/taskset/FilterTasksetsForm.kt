package com.mathhelper.mathhelperserver.forms.taskset

data class FilterTasksetsForm(
    var tasksets: ArrayList<TasksetForm>
)

data class FilterTasksetsLinkForm(
    var tasksets: ArrayList<TasksetLinkForm>
)

data class FilterTasksetsCuttedLinkForm(
    var tasksets: ArrayList<TasksetCuttedLinkForm>
)