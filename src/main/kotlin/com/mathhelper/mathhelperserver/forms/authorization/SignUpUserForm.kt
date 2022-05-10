package com.mathhelper.mathhelperserver.forms.authorization

import com.mathhelper.mathhelperserver.constants.Constants
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class SignUpUserForm(
    @field:Size(min = Constants.LOGIN_OR_EMAIL_MIN_LENGTH, max = Constants.STRING_LENGTH_MIDDLE)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    var login: String? = null,

    @field:Size(max = Constants.EMAIL_MAX_LENGTH)
    @field:Email
    var email: String? = null,

    @field:Size(min = Constants.PASSWORD_MIN_LENGTH, max = Constants.STRING_LENGTH_MIDDLE)
    @field:NotBlank
    var password: String = "",

    @field:Size(min = Constants.NAME_MIN_LENGTH, max = Constants.NAME_MAX_LENGTH)
    var name: String? = null,

    @field:Size(min = Constants.NAME_MIN_LENGTH, max = Constants.FULL_NAME_MAX_LENGTH)
    var fullName: String? = null,

    var additional: String? = null,

    @field:Size(min = Constants.LOCALE_LENGTH, max = Constants.LOCALE_LENGTH)
    var locale: String? = null
)