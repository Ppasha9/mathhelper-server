package com.mathhelper.mathhelperserver.forms.authorization

import com.mathhelper.mathhelperserver.constants.Constants
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class LoginUserForm(
    @field:NotBlank
    @field:Size(min = Constants.LOGIN_OR_EMAIL_MIN_LENGTH, max = Constants.STRING_LENGTH_MIDDLE)
    var loginOrEmail: String = "",

    @field:NotBlank
    @field:Size(min = Constants.PASSWORD_MIN_LENGTH, max = Constants.STRING_LENGTH_MIDDLE)
    var password: String = ""
)