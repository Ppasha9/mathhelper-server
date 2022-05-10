package com.mathhelper.mathhelperserver.forms.log

import java.sql.Timestamp

data class ResultLogForm (
    var appCode: String? = null,
    var tasksetCode: String? = null,
    var tasksetVersion: Int? = null,
    var taskCode: String? = null,
    var taskVersion: Int? = null,
    var autoSubTaskCode: String? = null,
    var userCode: String? = null,
    var difficulty: Double? = null,
    var baseAward: Double? = null,
    var currTimeMs: Long? = null,
    var currStepsNumber: Double? = null,
    var clientActionTs: Timestamp? = null,
    var serverActionTs: Timestamp? = null,
    var qualityData: MutableMap<String, *>? = null
)