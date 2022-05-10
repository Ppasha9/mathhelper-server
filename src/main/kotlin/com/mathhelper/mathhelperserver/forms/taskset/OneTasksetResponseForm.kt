package com.mathhelper.mathhelperserver.forms.taskset

import com.mathhelper.mathhelperserver.forms.rule_pack.RulePackForm

data class OneTasksetResponseForm(
    var taskset: TasksetForm,
    var rulePacks: ArrayList<RulePackForm>
)