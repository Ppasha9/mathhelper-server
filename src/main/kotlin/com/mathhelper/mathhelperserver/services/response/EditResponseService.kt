package com.mathhelper.mathhelperserver.services.response

import com.mathhelper.mathhelperserver.datatables.requests.EditRequestRepository
import com.mathhelper.mathhelperserver.datatables.responses.EditResponse
import com.mathhelper.mathhelperserver.datatables.responses.EditResponseRepository
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePackHistory
import com.mathhelper.mathhelperserver.datatables_history.tasks.TaskHistory
import com.mathhelper.mathhelperserver.datatables_history.taskset.TasksetHistory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp

@Service
class EditResponseService {
    @Autowired
    private lateinit var editResponseRepository: EditResponseRepository
    @Autowired
    private lateinit var editRequestRepository: EditRequestRepository

    @Transactional
    fun addNew(requestID: Long, task: TaskHistory?, taskset: TasksetHistory?, rulePack: RulePackHistory?,
               headers: MutableMap<String, *>?, responseBody: MutableMap<String, *>, returnCode: Int, validated: Boolean): Long {
        val editResp = EditResponse(
            request = editRequestRepository.findById(requestID).get(),
            task = task,
            taskset = taskset,
            rulePack = rulePack,
            headers = headers,
            responseBody = responseBody,
            returnCode = returnCode,
            validated = validated,
            serverTS = Timestamp(System.currentTimeMillis())
        )

        editResponseRepository.save(editResp)

        return editResp.id!!
    }
}