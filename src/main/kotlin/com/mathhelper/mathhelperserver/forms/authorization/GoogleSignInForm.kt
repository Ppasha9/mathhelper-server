package com.mathhelper.mathhelperserver.forms.authorization

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class GoogleSignInForm(
    @field:NotBlank
    @field:Size(min = 3)
    var idTokenString: String = ""
)