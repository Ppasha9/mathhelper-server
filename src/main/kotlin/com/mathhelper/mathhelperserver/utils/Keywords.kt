package com.mathhelper.mathhelperserver.utils

import com.mathhelper.mathhelperserver.datatables.rule_pack.RulePackRepository
import com.mathhelper.mathhelperserver.datatables.tasks.TaskRepository
import com.mathhelper.mathhelperserver.datatables.taskset.TasksetRepository

data class KeywordsFormatted(
    val error: String?,
    val replaces: HashMap<String, String>?,
    val keywordsFormatted: String?
)

fun <EntityRepository> findEntityKeywordNative(word: String, entityRepository: EntityRepository): String? {
    return when (entityRepository) {
        is TasksetRepository -> {
            entityRepository.findKeywordNative(word)
        }
        is TaskRepository -> {
            entityRepository.findKeywordNative(word)
        }
        is RulePackRepository -> {
            entityRepository.findKeywordNative(word)
        }
        else -> {
            ""
        }
    }
}

fun <EntityRepository> obtainKeywords(keywords: String?, entityRepository: EntityRepository): KeywordsFormatted {
    if (keywords.isNullOrBlank()) {
        return KeywordsFormatted("Empty keywords search", null, null)
    }

    var curWord = ""
    var curWordNotFormatted = ""
    val words = mutableListOf<String>()
    val wordsFormatted: HashMap<String, String> = hashMapOf()

    var foundWordsCount: Long = 0

    ("$keywords ").forEachIndexed { _, letter ->
        if (letter in listOf(' ')) {
            if (curWord != "") {
                var wordFound = false
                for (funcConverter in listOf<(String) -> String>(
                    ({ str -> wordConvertNone(str) }),
                    ({ str -> wordConvertRusToEng(str) }),
                    ({ str -> wordConvertEngToRus(str) }),
                    ({ str -> wordConvertEngToRusAndRusToEng(str) })
                )) {
                    val convertedWord = funcConverter(curWord)
                    val keyword = findEntityKeywordNative(convertedWord, entityRepository)
                    if (!keyword.isNullOrBlank()) {
                        curWord = keyword
                        wordFound = true
                        break
                    }
                }
                if (!wordFound) {
                    curWord = ""
                } else {
                    foundWordsCount++
                }

                words.add(curWordNotFormatted)
                wordsFormatted[curWordNotFormatted] = curWord
            } else if (curWordNotFormatted != "") {
                words.add(curWordNotFormatted)
                wordsFormatted[curWordNotFormatted] = ""
            }

            curWord = ""
            curWordNotFormatted = ""
        }
        else {
            curWordNotFormatted += letter.toLowerCase()
            if (isAllowedSymbol(letter)) {
                curWord += letter.toLowerCase()
            }
        }
    }

    if (foundWordsCount == 0L) {
        return KeywordsFormatted("Nothing found on request", null, null)
    }

    var searchStr = ""
    var i = 0L
    for (word in words) {
        val replaceWord = wordsFormatted[word]

        if (!replaceWord.isNullOrBlank()) {
            searchStr = searchOrAddWord(searchStr, replaceWord)
            i++
        }
        if (i == foundWordsCount) {
            break
        }
    }

    return KeywordsFormatted(null, wordsFormatted, searchStr)
}
