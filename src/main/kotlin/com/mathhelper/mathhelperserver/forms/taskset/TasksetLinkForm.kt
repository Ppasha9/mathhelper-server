package com.mathhelper.mathhelperserver.forms.taskset

import com.mathhelper.mathhelperserver.forms.task.TaskLinkForm

data class TasksetLinkForm(
    var code: String? = null,
    var version: Int? = null,
    var namespaceCode: String = "",
    var nameEn: String? = "",
    var nameRu: String? = "",
    var descriptionShortEn: String? = "",
    var descriptionShortRu: String? = "",
    var descriptionEn: String? = "",
    var descriptionRu: String? = "",
    var subjectType: String? = "",
    var recommendedByCommunity: Boolean = false,
    var tasks: ArrayList<TaskLinkForm> = arrayListOf(),
    var tags: ArrayList<String>? = null
)
