package com.mathhelper.mathhelperserver.forms.log

import com.mathhelper.mathhelperserver.constants.Constants
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class ActivityLogSearchForm(
    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    var appCode: String? = null,

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

    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_MIDDLE)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    var userCode: String? = null,

    var limit: Int? = null,
    var offset: Int? = null
)