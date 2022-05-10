package com.mathhelper.mathhelperserver.forms.namespace

import com.mathhelper.mathhelperserver.constants.Constants
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class NamespaceDeleteForm(
    @field:NotBlank
    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    var code: String = ""
)