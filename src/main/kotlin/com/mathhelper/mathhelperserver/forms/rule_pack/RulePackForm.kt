package com.mathhelper.mathhelperserver.forms.rule_pack

import com.mathhelper.mathhelperserver.constants.Constants
import java.sql.Timestamp
import javax.persistence.Column
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class RulePackForm(
    @field:NotBlank
    @field:Size(min = Constants.MIN_LENGTH, max = Constants.STRING_LENGTH_LONG)
    @field:Pattern(regexp = Constants.ALL_EXCEPT_AT_SYMBOL)
    var namespaceCode: String = "",

    var code: String? = "",

    var version: Int? = null,

    var nameEn: String? = "",

    var nameRu: String? = "",

    var descriptionShortEn: String? = "",

    var descriptionShortRu: String? = "",

    var descriptionEn: String? = "",

    var descriptionRu: String? = "",

    var subjectType: String? = "",

    @field:Valid
    var rulePacks: ArrayList<RulePackLinkForm>? = null,

    var rules: ArrayList<*>? = null,

    var otherData: MutableMap<String, *>? = null,

    var otherCheckSolutionData: MutableMap<String, *>? = null,

    var otherAutoGenerationData: MutableMap<String, *>? = null,

    var serverActionTs: Timestamp? = null
)