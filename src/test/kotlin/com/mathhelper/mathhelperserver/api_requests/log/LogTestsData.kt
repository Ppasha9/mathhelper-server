package com.mathhelper.mathhelperserver.api_requests.log

import com.mathhelper.mathhelperserver.AuthUtil
import com.mathhelper.mathhelperserver.constants.ActionTypes

class LogTestsData {
    companion object {
        val activityPlaceLog = """
            {
                "appCode": "test_app_code",
                "activityTypeCode": "${ActionTypes.PLACE.str}",
                "clientActionTs": "2021-01-06T10:11:30",
                "tasksetCode": "test_taskset_code",
                "tasksetVersion": 0,
                "taskCode": "test_task_code",
                "taskVersion": 0,
                "autoSubTaskCode": null,
                "originalExpression": "x+1",
                "goalExpression": "y+2",
                "goalPattern": "SIMPLIFICATION",
                "difficulty": 0.5,
                "currSolution": "",
                "currExpression": "x+1",
                "nextExpression": "x+1",
                "selectedPlace": "x+1",
                "currTimeMs": 700,
                "currStepsNumber": 16,
                "nextStepsNumber": 16
            }
        """.trimIndent()

        val activityRuleLog = """
            {
                "appCode": "test_app_code",
                "activityTypeCode": "${ActionTypes.RULE.str}",
                "clientActionTs": "2021-01-06T10:12:37",
                "tasksetCode": "test_taskset_code",
                "tasksetVersion": 0,
                "taskCode": "test_task_code",
                "taskVersion": 0,
                "autoSubTaskCode": null,
                "originalExpression": "x+1",
                "goalExpression": "y+2",
                "goalPattern": "SIMPLIFICATION",
                "difficulty": 0.5,
                "currSolution": "",
                "currExpression": "x+1",
                "nextExpression": "y+2",
                "appliedRule": {
                    "left": "x+1",
                    "right": "y+2"
                },
                "selectedPlace": "x+1",
                "currTimeMs": 1000,
                "timeFromLastActionMs": 157,
                "currStepsNumber": 16,
                "nextStepsNumber": 17,
                "subActionNumber": 1,
                "subActionsAfterLastTransformation": 1
            }
        """.trimIndent()

        val activityWinLog = """
            {
                "appCode": "test_app_code",
                "activityTypeCode": "${ActionTypes.WIN.str}",
                "clientActionTs": "2021-01-06T20:12:37",
                "tasksetCode": "test_taskset_code",
                "tasksetVersion": 0,
                "taskCode": "test_task_code",
                "taskVersion": 0,
                "autoSubTaskCode": null,
                "originalExpression": "x+1",
                "goalExpression": "y+2",
                "goalPattern": "SIMPLIFICATION",
                "difficulty": 0.5,
                "currSolution": "",
                "currExpression": "y+2",
                "nextExpression": "y+2",
                "currTimeMs": 2000,
                "timeFromLastActionMs": 30,
                "currStepsNumber": 5,
                "nextStepsNumber": 5,
                "subActionNumber": 0,
                "subActionsAfterLastTransformation": 0,
                "qualityData": {
                    "qulity": "data"
                },
                "baseAward": 30
            }
        """.trimIndent()

        val activityWinLog2 = """
            {
                "appCode": "test_app_code",
                "activityTypeCode": "${ActionTypes.WIN.str}",
                "clientActionTs": "2021-02-06T20:12:37",
                "tasksetCode": "test_taskset_code",
                "tasksetVersion": 0,
                "taskCode": "test_task_code",
                "taskVersion": 0,
                "originalExpression": "x+1",
                "goalExpression": "y+2",
                "goalPattern": "SIMPLIFICATION",
                "difficulty": 0.5,
                "currExpression": "y+2",
                "nextExpression": "y+2",
                "currTimeMs": 20000,
                "timeFromLastActionMs": 30,
                "currStepsNumber": 25,
                "nextStepsNumber": 25
            }
        """.trimIndent()

        val activitySaveLog = """
            {
                "appCode": "test_app_code",
                "activityTypeCode": "${ActionTypes.SAVE.str}",
                "clientActionTs": "2021-01-01T20:12:37",
                "otherData": {
                    "other": "data"
                },
                "otherGameStepData": {
                    "other": "game",
                    "step": "data"
                },
                "otherSolutionStepData": {
                    "other": "solution",
                    "step": "data"
                }
            }
        """.trimIndent()

        val activitySaveLogLongData = """
            {
                "appCode": "test_app_code",
                "activityTypeCode": "${ActionTypes.SAVE.str}",
                "clientActionTs": "2021-01-01T20:12:37",
                "otherData": {
                    "other": "data"
                },
                "otherGameStepData": {
                    "other": "game",
                    "step": "data"
                },
                "otherSolutionStepData": {
                    "other": "solution",
                    "step": "data"
                },
                "originalExpression": "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                "goalExpression": "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                "goalPattern": "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                "selectedPlace": "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                "currSolution": "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                "currExpression": "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                "nextExpression": "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
            }
        """.trimIndent()

        val activityWinLog3 = """
            {
                "appCode": "test_app_code",
                "activityTypeCode": "${ActionTypes.WIN.str}",
                "clientActionTs": "2021-01-06T20:12:37",
                "tasksetCode": "test_taskset_code",
                "tasksetVersion": 0,
                "taskCode": "test_task_2_code",
                "taskVersion": 0,
                "autoSubTaskCode": null,
                "originalExpression": "sfvsfv",
                "goalExpression": "sdbfsgb",
                "goalPattern": "DNF",
                "difficulty": 5.5,
                "currSolution": "",
                "currExpression": "y+2",
                "nextExpression": "y+2",
                "currTimeMs": 2000,
                "timeFromLastActionMs": 30,
                "currStepsNumber": 10,
                "nextStepsNumber": 0,
                "subActionNumber": 0,
                "subActionsAfterLastTransformation": 0,
                "baseAward": 0
            }
        """.trimIndent()

        val activityLooseLog = """
            {
                "appCode": "test_app_code",
                "activityTypeCode": "${ActionTypes.LOOSE.str}",
                "clientActionTs": "2021-01-06T20:12:38",
                "tasksetCode": "test_taskset_code",
                "tasksetVersion": 0,
                "taskCode": "test_task_code",
                "taskVersion": 0
            }
        """.trimIndent()

        val activityInterimLog = """
            {
                "activityTypeCode": "interim",
                "appCode": "test_app_code",
                "clientActionTs": "2021-03-14T10:26:23.409Z",
                "currSolution": "tg\\left(x\\right)+1+1=tg\\left(x\\right)+2=\\frac{\\sin\\left(x\\right)}{\\cos\\left(x\\right)}+2",
                "difficulty": 0,
                "goalExpression": "\\frac{\\sin\\left(x\\right)}{\\cos\\left(x\\right)}+2",
                "originalExpression": "tg\\left(x\\right)+1+1",
                "taskCode": "test_task_code",
                "tasksetCode": "test_taskset_code",
                "tasksetVersion": 0,
                "taskVersion": 0
            }
        """.trimIndent()

        val activitySearchForm = """
            {
                "appCode": "test_app_code",
                "tasksetCode": "test_taskset_code",
                "tasksetVersion": 0,
                "taskCode": null, 
                "taskVersion": 0,
                "autoSubTaskCode": null,
                "userCode": null
            }
        """.trimIndent()

        // TODO: "sortBy": "tasksCount",
        val tasksetSearchForm = """
            {
                "sortedBy": "BY_LEVELS_COUNT",
                "descending": true
            }
        """.trimIndent()

        val taskSearchForm = """
            {
                "sortedBy": "BY_STEPS",
                "descending": false
            }
        """.trimIndent()

        fun taskSearchFormWithUser(code: String) = """
            {
                "sortedBy": "BY_DIFFICULTY",
                "descending": true,
                "userCode": "$code"
            }
        """.trimIndent()

        fun createActivityLog(
            type: ActionTypes, app: String = "test_app_code", taskset: String = "test_taskset_code",
            task: String = "test_task_code", ts: String = "2021-12-02T01:01:01",
            dif: Double = 5.0, ms: Long = 2000
        ): String {
            return """
            {
                "appCode": "$app",
                "activityTypeCode": "${type.str}",
                "clientActionTs": "$ts",
                "tasksetCode": "$taskset",
                "tasksetVersion": 0,
                "taskCode": "$task",
                "taskVersion": 0,
                "autoSubTaskCode": null,
                "originalExpression": "orig_ex_test",
                "goalExpression": "goal_ex_test",
                "goalPattern": "goal_ptrn_test",
                "difficulty": $dif,
                "currSolution": "",
                "currExpression": "curr_ex_test",
                "nextExpression": "next_ex_test",
                "currTimeMs": $ms,
                "timeFromLastActionMs": 30,
                "currStepsNumber": 0,
                "nextStepsNumber": 1,
                "subActionNumber": 0,
                "subActionsAfterLastTransformation": 0,
                "baseAward": 0,
                "selectedPlace": "selected_place_test",
                "appliedRule": {
                    "left": "rleft_test",
                    "right": "rright_test"
                }
            }
            """.trimIndent()
        }
    }
}