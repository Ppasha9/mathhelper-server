package com.mathhelper.mathhelperserver.services.response

import com.mathhelper.mathhelperserver.datatables.log.ActivityLog
import com.mathhelper.mathhelperserver.datatables.log.ActivityLogRepository
import com.mathhelper.mathhelperserver.datatables.requests.GameSolveRequestRepository
import com.mathhelper.mathhelperserver.datatables.responses.GameSolveResponse
import com.mathhelper.mathhelperserver.datatables.responses.GameSolveResponseRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp

@Service
class GameSolveResponseService {
    @Autowired
    private lateinit var gameSolveResponseRepository: GameSolveResponseRepository
    @Autowired
    private lateinit var gameSolveRequestRepository: GameSolveRequestRepository

    @Autowired
    private lateinit var activityLogRepository: ActivityLogRepository

    @Transactional
    fun addNew(requestID: Long, activityLog: ActivityLog?, headers: MutableMap<String, *>?,
               responseBody: MutableMap<String, *>, returnCode: Int): Long {
        val resp = GameSolveResponse(
            request = gameSolveRequestRepository.findById(requestID).get(),
            activityLog = activityLog,
            headers = headers,
            responseBody = responseBody,
            returnCode = returnCode,
            serverTS = Timestamp(System.currentTimeMillis())
        )

        gameSolveResponseRepository.save(resp)

        return resp.id!!
    }
}