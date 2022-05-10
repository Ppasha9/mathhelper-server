package com.mathhelper.mathhelperserver.forms.namespace

import com.mathhelper.mathhelperserver.constants.Constants
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class NamespaceCreateForm(
    @field:NotBlank(message = "You should provide namespace's code to create it")
    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG,
                message = "Namespace's code size must be within [${Constants.MIN_LENGTH}, ${Constants.STRING_LENGTH_LONG}]")
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL,
                   message = "Namespace's code must apply pattern ${Constants.ALL_EXCEPT_AT_SYMBOL}")
    var code: String = "",

    @field:NotBlank(message = "You should provide namespace's grantType to create it")
    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_MIDDLE,
                message = "Namespace's grantType size must be within [${Constants.MIN_LENGTH}, ${Constants.STRING_LENGTH_MIDDLE}]")
    var grantType: String = "",

    var usersGrants: ArrayList<MutableMap<String, String>>? = null
)