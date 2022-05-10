package com.mathhelper.mathhelperserver.forms.task

import com.mathhelper.mathhelperserver.forms.rule_pack.RulePackForm

data class OneTaskResponseForm(
    var task: TaskForm,
    var rulePacks: ArrayList<RulePackForm>
)