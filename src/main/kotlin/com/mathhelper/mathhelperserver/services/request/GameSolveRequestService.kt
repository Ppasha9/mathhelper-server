package com.mathhelper.mathhelperserver.services.request

import com.mathhelper.mathhelperserver.datatables.log.AppRepository
import com.mathhelper.mathhelperserver.datatables.requests.GameSolveRequest
import com.mathhelper.mathhelperserver.datatables.requests.GameSolveRequestRepository
import com.mathhelper.mathhelperserver.datatables.tasks.AutoSubTask
import com.mathhelper.mathhelperserver.datatables.tasks.Task
import com.mathhelper.mathhelperserver.datatables.users.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp

@Service
class GameSolveRequestService {
    @Autowired
    private lateinit var gameSolveRequestRepository: GameSolveRequestRepository

    @Autowired
    private lateinit var appRepository: AppRepository

    @Transactional
    fun addNew(method: String, path: String, appName: String, userIP: String, user: User,
               taskType: String, task: Task?, autoSubTask: AutoSubTask?, clientTS: Timestamp?): Long {
        val req = GameSolveRequest(
            method = method,
            path = path,
            app = appRepository.findByCode(appName)!!,
            userIP = userIP,
            user = user,
            taskType = taskType,
            task = task,
            autoSubTask = autoSubTask,
            clientTS = clientTS,
            serverTS = Timestamp(System.currentTimeMillis())
        )

        gameSolveRequestRepository.save(req)

        return req.id!!
    }
}