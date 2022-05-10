package com.mathhelper.mathhelperserver

import com.mathhelper.mathhelperserver.constants.ActionTypes
import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.datatables.log.*
import com.mathhelper.mathhelperserver.datatables.namespaces.*
import com.mathhelper.mathhelperserver.datatables.rule_pack.RulePack2RulePackRepository
import com.mathhelper.mathhelperserver.datatables.rule_pack.RulePackRepository
import com.mathhelper.mathhelperserver.datatables.subject_type.SubjectType
import com.mathhelper.mathhelperserver.datatables.subject_type.SubjectTypeRepository
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables.users.UserRepository
import com.mathhelper.mathhelperserver.datatables.users.UserType
import com.mathhelper.mathhelperserver.datatables.users.UserTypeRepository
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePack2RulePackHistoryRepository
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePackHistoryRepository
import com.mathhelper.mathhelperserver.datatables_history.taskset.TasksetHistoryRepository
import de.dentrassi.crypto.pem.PemKeyStoreProvider
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.password.PasswordEncoder
import java.security.Security

@SpringBootApplication
class MathHelperServerApplication {
    @Bean
    internal fun initDatabase(
        userTypeRepository: UserTypeRepository,
        userRepository: UserRepository,
        namespaceGrantTypesRepository: NamespaceGrantTypesRepository,
        userGrantTypeRepository: UserGrantTypesRepository,
        namespacesRepository: NamespacesRepository,
        rulePackRepository: RulePackRepository,
        rulePack2RulePackRepository: RulePack2RulePackRepository,
        rulePackHistoryRepository: RulePackHistoryRepository,
        rulePack2RulePackHistoryRepository: RulePack2RulePackHistoryRepository,
        subjectTypeRepository: SubjectTypeRepository,
        tasksetHistoryRepository: TasksetHistoryRepository,
        appRepository: AppRepository,
        activityTypeRepository: ActivityTypeRepository,
        activityLogRepository: ActivityLogRepository,
        passwordEncoder: PasswordEncoder
    ): CommandLineRunner {
        return CommandLineRunner { _ ->
            // Add user types to database
            listOf(
                UserType(Constants.ADMIN_USER_TYPE_CODE, "Администратор сервиса"),
                UserType(Constants.DEFAULT_USER_TYPE_CODE, "Обычный пользователь, умеет играть и создавать игры!")
            ).forEach {
                if (!userTypeRepository.existsByCode(it.code)) {
                    userTypeRepository.save(it)
                }
            }

            if (!userRepository.existsByLogin("mathhelper")) {
                val mathhelperUser = User(
                    code = "mathhelper",
                    userType = userTypeRepository.findByCode(Constants.DEFAULT_USER_TYPE_CODE)!!,
                    name = "mathhelper",
                    email = "mathhelper@support.com",
                    login = "mathhelper",
                    additional = "",
                    password = passwordEncoder.encode("mathhelper"),
                    isOauth = false,
                    locale = "ru"
                )
                userRepository.save(mathhelperUser)
            }

            listOf(
                NamespaceGrantType(
                    code = Constants.NAMESPACE_GRANT_TYPE_PUBLIC_READ_PRIVATE_WRITE,
                    description = "Доступ на чтение к данному неймспейсу разрешен всем, а доступ на запись ограничен."
                ),
                NamespaceGrantType(
                    code = Constants.NAMESPACE_GRANT_TYPE_PRIVATE_READ_WRITE,
                    description = "Доступ на чтение и запись к данному неймспейсу ограничен."
                ),
                NamespaceGrantType(
                    code = Constants.NAMESPACE_GRANT_TYPE_PUBLIC_READ_WRITE,
                    description = "Доступ на чтение и запись к данному неймспейсу открыт всем."
                )
            ).forEach {
                if (!namespaceGrantTypesRepository.existsByCode(it.code)) {
                    namespaceGrantTypesRepository.save(it)
                }
            }

            listOf(
                UserGrantType(
                    code = Constants.USER_GRANT_TYPE_NO_READ_WRITE,
                    description = "Данному пользователю закрыт доступ на запись и чтение неймспейса."
                ),
                UserGrantType(
                    code = Constants.USER_GRANT_TYPE_READ_NO_WRITE,
                    description = "Данному пользователю открыт доступ на чтение неймспейса, но закрыт доступ на запись."
                ),
                UserGrantType(
                    code = Constants.USER_GRANT_TYPE_READ_WRITE,
                    description = "Данному пользователю открыт доступ на запись и чтение неймспейса."
                )
            ).forEach {
                if (!userGrantTypeRepository.existsByCode(it.code)) {
                    userGrantTypeRepository.save(it)
                }
            }

            listOf(
                ActivityType(
                    code = ActionTypes.START.str,
                    description = "Начало уровня (тап на название уровня в меню)"
                ),
                ActivityType(
                    code = ActionTypes.WIN.str,
                    description = "Конец уровня, победа (произошло приведение к конечной формуле до тайм-аута)"
                ),
                ActivityType(
                    code = ActionTypes.LOOSE.str,
                    description = "Конец уровня, поражение (тайм-аут), пока тайм-ауты невозможны, игра без времени"
                ),
                ActivityType(
                    code = ActionTypes.UNDO.str,
                    description = "Ход назад (тап на кнопку undo)"
                ),
                ActivityType(
                    code = ActionTypes.MENU.str,
                    description = "Выход в меню"
                ),
                ActivityType(
                    code = ActionTypes.RESTART.str,
                    description = "Перезапуск уровня (тап на кнопку restart)"
                ),
                ActivityType(
                    code = ActionTypes.RULE.str,
                    description = "Выбор применяемого правила"
                ),
                ActivityType(
                    code = ActionTypes.PLACE.str,
                    description = "Выбор моцифицируемого места в выражении"
                ),
                ActivityType(
                    code = ActionTypes.SIGN.str,
                    description = "Авторизация"
                ),
                ActivityType(
                    code = ActionTypes.HELP.str,
                    description = "Просмотр подсказок"
                ),
                ActivityType(
                    code = ActionTypes.SAVE.str,
                    description = "Сохранение текущего состояния"
                ),
                ActivityType(
                    code = ActionTypes.INTERIM.str,
                    description = "Сохранение промежуточного решения задачи"
                )
            ).forEach {
                if (!activityTypeRepository.existsByCode(it.code)) {
                    activityTypeRepository.save(it)
                }
            }

            listOf(
                SubjectType(name = "standard_math"),
                SubjectType(name = "derivations_integrals"),
                SubjectType(name = "combinatorics"),
                SubjectType(name = "probability_and_math_stat"),
                SubjectType(name = "complex_numbers"),
                SubjectType(name = "asymptotic_analysis"),
                SubjectType(name = "set"),
                SubjectType(name = "logic"),
                SubjectType(name = "physics")
            ).forEach {
                if (!subjectTypeRepository.existsByName(it.name)) {
                    subjectTypeRepository.save(it)
                }
            }

            listOf(
                App(code = Constants.APP_CODE_MATIFY_ANDROID, description = "Android game Matify"),
                App(code = Constants.APP_CODE_SOLVE_MATH_WEB, description = "Solving tasksets on web page")
            ).forEach {
                if (!appRepository.existsByCode(it.code)) {
                    appRepository.save(it)
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    Security.addProvider(PemKeyStoreProvider())
    runApplication<MathHelperServerApplication>(*args)
}
