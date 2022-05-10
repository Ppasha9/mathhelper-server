package com.mathhelper.mathhelperserver.datatables.tasks

import java.util.function.Predicate
import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class GoalType(val code: String) {
    CUSTOM("CUSTOM"),
    EXPRESSION("EXPRESSION"),
    COMPUTATION("COMPUTATION"),
    SIMPLIFICATION("SIMPLIFICATION"),
    CNF("CNF"),
    DNF("DNF"),
    FACTORIZATION("FACTORIZATION"),
    UNKNOWN("UNKNOWN"),
    REDUCTION("REDUCTION"),
    POLYNOM("POLYNOM");

    companion object {
        private val map = values().associateBy(GoalType::code)
        fun fromString(code: String) = map[code]
    }
}


@Converter(autoApply = true)
class CategoryConverter : AttributeConverter<GoalType, String> {
    override fun convertToDatabaseColumn(p0: GoalType?): String {
        return p0!!.code
    }

    override fun convertToEntityAttribute(p0: String?): GoalType {
        val strp0 = p0 ?: throw IllegalArgumentException("Input string cannot be null")
        return GoalType.fromString(strp0) ?: throw IllegalArgumentException("There isn't any GoalType by '$p0' string")
    }
}