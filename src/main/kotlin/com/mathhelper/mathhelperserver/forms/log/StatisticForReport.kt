package com.mathhelper.mathhelperserver.forms.log

import java.sql.Timestamp

data class StatisticForReport (
    val tasksetCode: String,
    val tasksetVersion: Int,
    val taskCode: String,
    val taskVersion: Int,
    val taskNameRu: String,
    val taskNameEn: String,
    val userCode: String,
    val userLogin: String,
    val userFullName: String,
    val userAdditional: String,
    val stepsNumber: Double,
    val timeMS: Long,
    val difficulty: Double,
    val clientActionTS: Timestamp,
    val appName: String
)