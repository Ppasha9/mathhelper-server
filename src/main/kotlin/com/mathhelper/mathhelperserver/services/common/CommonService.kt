package com.mathhelper.mathhelperserver.services.common

import com.ibm.icu.text.Transliterator
import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.datatables.rule_pack.RulePackRepository
import com.mathhelper.mathhelperserver.datatables.tasks.TaskRepository
import com.mathhelper.mathhelperserver.datatables.taskset.TasksetRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CommonService {
    private fun <EntityRepository> existsEntityByCode(entityCode: String, entityRepository: EntityRepository): Boolean {
        return when (entityRepository) {
            is RulePackRepository -> {
                entityRepository.existsByCode(entityCode)
            }
            is TaskRepository -> {
                entityRepository.existsByCode(entityCode)
            }
            is TasksetRepository -> {
                entityRepository.existsByCode(entityCode)
            }
            else -> {
                false
            }
        }
    }

    fun <EntityRepository> generateCode(entityName: String, entityNamespaceCode: String, entityRepository: EntityRepository): String {
        val toLatinTransliterator = Transliterator.getInstance(Constants.CYRILLIC_TO_LATIN)
        val latinName = toLatinTransliterator.transliterate(entityName)

        var convertedName = ""

        var i = 0
        while (i < latinName.count()) {
            when {
                latinName[i] == '_' -> {
                    convertedName += "__"
                }
                latinName[i] == ' ' -> {
                    while (latinName[i + 1] == ' ') {
                        i++
                    }
                    convertedName += "_"
                }
                !"${latinName[i]}".matches(Regex("\\w+")) -> {
                    convertedName += "${latinName[i]}".toCharArray()
                }
                else -> {
                    convertedName += latinName[i]
                }
            }

            i++
        }

        val convertedNameWithoutNamespaceCode = convertedName
        val ending = "____$entityNamespaceCode"
        convertedName += ending
        var k = 0
        while (existsEntityByCode(convertedName, entityRepository)) {
            k++
            convertedName = convertedNameWithoutNamespaceCode + "_$k" + ending
        }

        return convertedName
    }
}