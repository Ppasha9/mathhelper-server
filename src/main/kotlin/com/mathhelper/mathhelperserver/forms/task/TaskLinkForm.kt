package com.mathhelper.mathhelperserver.forms.task

data class TaskLinkForm(
    var namespaceCode: String,
    var code: String? = null,
    var version: Int? = null,
    var taskCreationType: String? = "",
    var nameEn: String? = null,
    var nameRu: String? = null,
    var descriptionShortEn: String? = "",
    var descriptionShortRu: String? = "",
    var descriptionEn: String? = "",
    var descriptionRu: String? = "",
    var subjectType: String? = "",
    var tags: ArrayList<String>? = null,
    var stepsNumber: Int? = null,
    var time: Int? = null,
    var difficulty: Double
)
