package com.mathhelper.mathhelperserver.api_requests.rule_pack

class RulePackTestData {
    companion object {
        const val validStr = "valid"

        const val testNamespaceCode = "test_namespace_code"
        const val secondTestNamespaceCode = "second_test_namespace_code"

        const val baseRulePackCode = "test_rule_pack"
        const val baseRulePackNameEn = "test_rule_pack_en"
        const val baseRulePack2Code = "test_rule_pack_2"
        const val baseRulePack2NameEn = "test_rule_pack_2_en"
        val baseRulePackSameCode = """
            {
                "namespaceCode": "$testNamespaceCode",
                "code": "$baseRulePackCode",
                "nameEn": "${baseRulePackCode}_name_en"
            }
        """.trimIndent()
        const val baseRulePackSameNameCode = "another_$baseRulePackCode"
        val baseRulePackSameName = """
            {
                "namespaceCode": "$testNamespaceCode",
                "code": "another_$baseRulePackCode",
                "nameEn": "$baseRulePackNameEn"
            }
        """.trimIndent()

        const val emptyRulePackCode = "request_empty_rule_pack____$testNamespaceCode"
        const val emptyRulePackName = "${emptyRulePackCode}_name_en"
        val emptyRulePack = """
            {
                "namespaceCode": "$testNamespaceCode",
                "code": "$emptyRulePackCode",
                "nameEn": "$emptyRulePackName"
            }
        """.trimIndent()

        const val emptyRulePackAnotherSpaceSameNameCode = "empty_rule_pack_another_space_same_name____${secondTestNamespaceCode}"
        val emptyRulePackAnotherSpaceSameName = """
            {
                "namespaceCode": "$secondTestNamespaceCode",
                "code": "$emptyRulePackAnotherSpaceSameNameCode",
                "nameEn": "${emptyRulePackCode}_name_en"
            }
        """.trimIndent()

        const val fullRulePackCode = "request_full_rule_pack____$testNamespaceCode"
        const val fullRulePackName = "${fullRulePackCode}_name_en"
        val fullRulePack = """
            {
                "namespaceCode": "$testNamespaceCode",
                "code": "$fullRulePackCode",
                "nameEn": "$fullRulePackName",
                "nameRu": "${fullRulePackCode}_name_ru",
                "rulePacks": [
                    {
                        "namespaceCode": "$testNamespaceCode",
                        "rulePackCode": "$emptyRulePackCode",
                        "rulePackNameEn": "${emptyRulePackCode}_name_en"
                    }
                ],
                "rules": [
                    {"left": "x", "right": "y"},
                    {"left": "12", "right": 12, "easy": true}
                ],
                "otherData": {
                    "some": "body",
                    "once": "told me"
                }
            }
        """.trimIndent()
        const val rulePackShortLinkCode = "short_link_$fullRulePackCode"
        val rulePackShortLink = """
            {
                "namespaceCode": "$testNamespaceCode",
                "code": "$rulePackShortLinkCode",
                "nameEn": "${rulePackShortLinkCode}_name_en",
                "nameRu": "${rulePackShortLinkCode}_name_ru",
                "rulePacks": [
                    {"rulePackCode": "$baseRulePackCode"}
                ]
            }
        """.trimIndent()

        const val rulePackInvalidCode = "rule_pack_invalid"
        val rulePackInvalidNamespace = """
            {
                "namespaceCode": "@#",
                "code": "$rulePackInvalidCode",
                "nameEn": "${rulePackInvalidCode}_name_en"
            }
        """.trimIndent()
        val rulePackCodeInvalid = """
            {
                "namespaceCode": "$testNamespaceCode",
                "code": "_",
                "nameEn": "${rulePackInvalidCode}_name_en"
            }
        """.trimIndent()
        val rulePackInvalidName = """
            {
                "namespaceCode": "$testNamespaceCode",
                "code": "_",
                "nameEn": "en"
                "nameRu": ""
            }
        """.trimIndent()
        val rulePackNoNames = """
            {
                "namespaceCode": "$testNamespaceCode",
                "code": "$rulePackInvalidCode"
            }
        """.trimIndent()
        val rulePackInvalidLinks = """
            {
                "namespaceCode": "$testNamespaceCode",
                "code": "$rulePackInvalidCode",
                "nameEn": "${rulePackInvalidCode}_name_en",
                "rulePacks": [
                    {"rulePackCode": "@_"}
                ]
            }
        """.trimIndent()
        val rulePackInvalidOtherData = """
            {
                "namespaceCode": "$testNamespaceCode",
                "code": "$rulePackInvalidCode",
                "nameEn": "${rulePackInvalidCode}_name_en",
                "otherData": [
                    {"i": "am"}
                ]
            }
        """.trimIndent()
        val rulePackNotFoundNamespace = """
            {
                "namespaceCode": "$validStr",
                "code": "$rulePackInvalidCode",
                "nameEn": "${rulePackInvalidCode}_name_en"
            }
        """.trimIndent()
        val rulePackNotFoundChild = """
            {
                "namespaceCode": "$testNamespaceCode",
                "code": "$rulePackInvalidCode",
                "nameEn": "${rulePackInvalidCode}_name_en",
                "rulePacks": [
                    {"rulePackCode": "$validStr"}
                ]
            }
        """.trimIndent()

        const val rulePackUpdateCode = "request_update_code_rule_pack____$testNamespaceCode"
        const val rulePackCodeUpdatedCode = "request_UPDATED_code_rule_pack____$testNamespaceCode"
        val rulePackUpdate = """
            {
                "namespaceCode": "$testNamespaceCode",
                "code": "$rulePackUpdateCode",
                "nameEn": "${rulePackUpdateCode}_name_en",
                "rulePacks": [
                    {"rulePackCode": "$baseRulePackCode"},
                    {"rulePackCode": "$baseRulePack2Code"}
                ]
            }
        """.trimIndent()
        val rulePackCodeUpdated = """
            {
                "namespaceCode": "$testNamespaceCode",
                "code": "$rulePackCodeUpdatedCode",
                "nameEn": "${rulePackUpdateCode}_name_en",
                "rulePacks": [
                    {"rulePackCode": "$baseRulePackCode"},
                    {"rulePackCode": "$baseRulePack2Code"}
                ]
            }
        """.trimIndent()
        val rulePackNamespaceUpdated = """
            {
                "namespaceCode": "$secondTestNamespaceCode",
                "code": "$rulePackUpdateCode",
                "nameEn": "request_update_code_rule_pack____${secondTestNamespaceCode}_name_en",
                "rulePacks": [
                    {"rulePackCode": "$baseRulePackCode"},
                    {"rulePackCode": "$baseRulePack2Code"}
                ]
            }
        """.trimIndent()
        val rulePackNameUpdated = """
            {
                "namespaceCode": "$testNamespaceCode",
                "code": "$rulePackUpdateCode",
                "nameEn": "${rulePackUpdateCode}_name_en_updated",
                "rulePacks": [
                    {"rulePackCode": "$baseRulePackCode"},
                    {"rulePackCode": "$baseRulePack2Code"}
                ]
            }
        """.trimIndent()
        val rulePackNameOverlapUpdated = """
            {
                "namespaceCode": "$testNamespaceCode",
                "code": "$rulePackUpdateCode",
                "nameEn": "$baseRulePackNameEn",
                "rulePacks": [
                    {"rulePackCode": "$baseRulePackCode"},
                    {"rulePackCode": "$baseRulePack2Code"}
                ]
            }
        """.trimIndent()
        val rulePackLinksUpdated = """
             {
                "namespaceCode": "$testNamespaceCode",
                "code": "$rulePackUpdateCode",
                "nameEn": "${rulePackUpdateCode}_name_en",
                "rulePacks": [
                    {"rulePackCode": "$emptyRulePackCode"},
                    {"rulePackCode": "$baseRulePack2Code"},
                    {"rulePackCode": "$fullRulePackCode"}
                ]
            }
        """.trimIndent()
        val rulePackLinksAndNameUpdatedBad = """
             {
                "namespaceCode": "$testNamespaceCode",
                "code": "$rulePackUpdateCode",
                "nameEn": "another_name_en",
                "rulePacks": [
                    {"rulePackCode": "$validStr"}
                ]
            }
        """.trimIndent()
    }
}