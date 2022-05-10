package com.mathhelper.mathhelperserver.forms.namespace

import com.mathhelper.mathhelperserver.constants.Constants
import java.sql.Timestamp
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class NamespaceReturnForm(
    @field:NotBlank
    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    var code: String = "",

    @field:NotBlank
    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_MIDDLE)
    var grantType: String = "",

    @field:NotBlank
    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    var authorUserCode: String = "",

    var writeGrantedUsers: ArrayList<String> = arrayListOf(),
    var readGrantedUsers: ArrayList<String> = arrayListOf(),

    var serverActionTs: Timestamp
)