package com.mathhelper.mathhelperserver.utils

import com.mathhelper.mathhelperserver.constants.Constants
import java.util.*
import kotlin.streams.asSequence

fun generateRandStr(
    length: Int = Constants.STRING_LENGTH_SHORT,
    lang: String = Constants.ENGLISH_LOCALE_NAME
) : String {
    val source = when (lang) {
        "eng" -> Constants.ENGLISH_SYMBOLS
        else -> Constants.RUSSIAN_SYMBOLS
    }

    return Random().ints(length.toLong(), 0, source.length)
        .asSequence()
        .map(source::get)
        .joinToString("")
}

fun resolveLocale(locale: String?) : String {
    if (locale.isNullOrBlank()) {
        return Constants.RUSSIAN_LOCALE_NAME
    }

    return when (locale.toLowerCase()) {
        "rus", "ru" -> Constants.RUSSIAN_LOCALE_NAME
        else -> Constants.ENGLISH_LOCALE_NAME
    }
}

fun isAllowedSymbol(letter: Char): Boolean = Constants.ALLOWED_SYMBOLS.contains(letter, true)

fun searchOrAddWord(str: String, searchWord: String): String = if (str.isBlank()) searchWord else "$str | $searchWord"

fun wordConvertNone(str: String): String = str

fun wordConvertRusToEng(str: String): String {
    var result = ""
    for (letter in str) {
        var addLetter = letter.toLowerCase()
        val index = Constants.RUSSIAN_KEYBOARDS_SYMBOLS.indexOf(addLetter)
        if (index != -1) {
            addLetter = Constants.ENGLISH_KEYBOARDS_SYMBOLS[index]
        }
        result += addLetter
    }
    return result
}

fun wordConvertEngToRus(str: String): String {
    var result = ""
    for (letter in str) {
        var addLetter = letter.toLowerCase()
        val index = Constants.ENGLISH_KEYBOARDS_SYMBOLS.indexOf(addLetter)
        if (index != -1) {
            addLetter = Constants.RUSSIAN_KEYBOARDS_SYMBOLS[index]
        }
        result += addLetter
    }
    return result
}

fun wordConvertEngToRusAndRusToEng(str: String): String {
    var result = ""
    for (letter in str) {
        var addLetter = letter.toLowerCase()
        var index = Constants.ENGLISH_KEYBOARDS_SYMBOLS.indexOf(addLetter)
        if (index != -1) {
            addLetter = Constants.RUSSIAN_KEYBOARDS_SYMBOLS[index]
        }  else {
            index = Constants.RUSSIAN_KEYBOARDS_SYMBOLS.indexOf(addLetter)
            if (index != -1) {
                addLetter = Constants.ENGLISH_KEYBOARDS_SYMBOLS[index]
            }
        }
        result += addLetter
    }
    return result
}