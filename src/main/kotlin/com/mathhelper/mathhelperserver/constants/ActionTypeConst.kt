package com.mathhelper.mathhelperserver.constants

enum class ActionTypes(val str: String) {
    START("start"),
    WIN("win"),
    LOOSE("loose"),
    UNDO("undo"),
    MENU("menu"),
    RESTART("restart"),
    RULE("rule"),
    PLACE("place"),
    SIGN("sign"),
    HELP("help"),
    SAVE("save"),
    INTERIM("interim");

    fun isStep() = this in arrayOf(UNDO, RULE, PLACE, INTERIM)
    fun isEnd() = this in arrayOf(WIN)
}