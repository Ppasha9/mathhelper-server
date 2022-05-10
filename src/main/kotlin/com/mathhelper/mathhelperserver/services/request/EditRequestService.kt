package com.mathhelper.mathhelperserver.services.request

import com.mathhelper.mathhelperserver.datatables.log.AppRepository
import com.mathhelper.mathhelperserver.datatables.requests.EditRequest
import com.mathhelper.mathhelperserver.datatables.requests.EditRequestActionTypeRepository
import com.mathhelper.mathhelperserver.datatables.requests.EditRequestRepository
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.services.user.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp

@Service
class EditRequestService {
    @Autowired
    private lateinit var editRequestRepository: EditRequestRepository
    @Autowired
    private lateinit var editRequestActionTypeRepository: EditRequestActionTypeRepository

    @Autowired
    private lateinit var appRepository: AppRepository

    @Transactional
    fun addNew(method: String, path: String, appName: String, userIP: String, user: User?, clientTS: Timestamp?, actionType: String): Long {
        logger.info("Adding new edit request with params: method=$method, path=$path, appName=$appName, userIP=$userIP," +
            "clientTS=$clientTS, actionType=$actionType")

        val editReq = EditRequest(
            method = method,
            path = path,
            app = appRepository.findByCode(appName),
            actionType = editRequestActionTypeRepository.findByCode(actionType),
            userIP = userIP,
            user = user,
            clientTS = clientTS,
            serverTS = Timestamp(System.currentTimeMillis())
        )

        editRequestRepository.save(editReq)

        return editReq.id!!
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("logic-logs")
    }
}