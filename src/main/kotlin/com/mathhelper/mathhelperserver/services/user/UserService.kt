package com.mathhelper.mathhelperserver.services.user

import com.mathhelper.mathhelperserver.authorization.UserAlreadyExistException
import com.mathhelper.mathhelperserver.constants.CommonException
import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables.users.UserRepository
import com.mathhelper.mathhelperserver.datatables.users.UserTypeRepository
import com.mathhelper.mathhelperserver.forms.authorization.SignUpUserForm
import com.mathhelper.mathhelperserver.forms.user.UserCuttedForm
import com.mathhelper.mathhelperserver.utils.generateRandStr
import com.mathhelper.mathhelperserver.utils.resolveLocale
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException

@Service
class UserService {
    @Autowired
    private lateinit var userTypeRepository: UserTypeRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Transactional
    fun findAll(): List<User> = userRepository.findAll()

    @Transactional
    fun findByLogin(login: String): User? = userRepository.findByLogin(login)

    @Transactional
    fun findByExternalCode(externalCode: String): User? = userRepository.findByExternalCode(externalCode)

    @Transactional
    fun existsByLogin(login: String): Boolean = userRepository.existsByLogin(login)

    fun save(user: User) = userRepository.save(user)

    @Transactional
    fun findByLoginOrEmail(loginOrEmail: String): User? {
        val users = userRepository.findAllByEmailOrLogin(loginOrEmail, loginOrEmail)
        if (users.isEmpty()) {
            return null
        }

        return users[0]
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun signUp(signUpUserForm: SignUpUserForm) {
        if (signUpUserForm.email.isNullOrBlank() && signUpUserForm.login.isNullOrBlank())
            throw CommonException("Failed to execute sign up. Fields `login` or `email` are required, but they are empty in this request.")

        if (!signUpUserForm.email.isNullOrBlank() && userRepository.existsByEmail(signUpUserForm.email!!))
            throw CommonException("Failed to execute sign up. There is already a user with email ${signUpUserForm.email!!}")

        if (!signUpUserForm.login.isNullOrBlank() && userRepository.existsByLogin(signUpUserForm.login!!))
            throw CommonException("Failed to execute sign up. There is already a user with login ${signUpUserForm.login!!}")

        val userType = userTypeRepository.findByCode(Constants.DEFAULT_USER_TYPE_CODE)
        val password = signUpUserForm.password ?: generateRandPassword()
        val encodedPassword = passwordEncoder.encode(password)

        val locale = resolveLocale(signUpUserForm.locale)
        val user = User(
            code = "",
            userType = userType!!,
            login = signUpUserForm.login ?: "",
            email = signUpUserForm.email ?: "",
            additional = signUpUserForm.additional ?: "",
            name = signUpUserForm.name ?: "",
            fullName = signUpUserForm.fullName ?: "",
            locale = locale,
            password = encodedPassword)
        if (user.login.isBlank())
            user.login = (if (!user.name.isBlank()) user.name else "user") + "-${user.code}"
        userRepository.save(user)

        logger.info("Successfully registered new user. Add new user to database `users`: $user")
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun edit(authorizedUser: User, editForm: SignUpUserForm) {
        if (!editForm.email.isNullOrBlank() && editForm.email != authorizedUser.email && userRepository.existsByEmail(editForm.email!!))
            throw CommonException("Failed to edit user. There is already a user with such email: ${editForm.email}. Try another one.")

        if (editForm.login != authorizedUser.login)
            throw CommonException("Failed to edit user. There is already a user with such login: ${editForm.login}. Try another one.")

        authorizedUser.login = if (!editForm.login.isNullOrBlank()) editForm.login!! else authorizedUser.login
        authorizedUser.email = if (!editForm.email.isNullOrBlank()) editForm.email!! else authorizedUser.email
        authorizedUser.name = if (!editForm.name.isNullOrBlank()) editForm.name!! else authorizedUser.name
        authorizedUser.fullName = if (!editForm.fullName.isNullOrBlank()) editForm.fullName!! else authorizedUser.fullName
        authorizedUser.additional = if (!editForm.additional.isNullOrBlank()) editForm.additional!! else authorizedUser.additional
        authorizedUser.locale = if (!editForm.locale.isNullOrBlank()) resolveLocale(editForm.locale!!) else authorizedUser.locale
        authorizedUser.password = if (!editForm.password.isBlank()) passwordEncoder.encode(editForm.password) else authorizedUser.password

        userRepository.save(authorizedUser)
        logger.info("Successfully edited user. User info: $authorizedUser")
    }

    @Throws(AuthenticationException::class)
    @Transactional
    fun findOrRegister(user: User?): User {
        if (user == null)
            throw BadCredentialsException("Empty user params!")

        if (user.email.isBlank() && user.login.isBlank())
            throw BadCredentialsException("Email and login not set!")

        val users = userRepository.findAllByEmailOrLogin(user.email, user.login)
        if (users.size > 1)
            throw UserAlreadyExistException("User with email `${user.email}` or login `${user.login}` already exists!")

        var dbUser = user
        if (users.size == 1) {
            dbUser = users[0]
            dbUser.email = if (user.email.isNotBlank() && dbUser.email.isBlank()) user.email else dbUser.email
            dbUser.login = if (user.login.isNotBlank() && dbUser.login.isBlank()) user.login else dbUser.login
            dbUser.additional = if (user.additional.isNotBlank()) user.additional else dbUser.additional
            dbUser.locale = if (user.locale in listOf("rus", "eng")) user.locale else dbUser.locale
            dbUser.name = if (user.name.isNotBlank()) user.name else dbUser.name
            dbUser.fullName = if (user.fullName.isNotBlank()) user.fullName else dbUser.fullName
        }

        if (dbUser.password == "") {
            val pass = generateRandPassword()
            dbUser.password = passwordEncoder.encode(pass)
            dbUser.externalCode = pass
        }

        userRepository.save(dbUser)
        logger.info("Successfully edited user. User info: $dbUser")

        return dbUser
    }

    @Transactional
    fun getAllUsersCuttedForm(limit: Int?, offset: Int?): ArrayList<UserCuttedForm> {
        val res = arrayListOf<UserCuttedForm>()
        val users = userRepository.findAllByLimitAndOffset(limit = limit?.toString() ?: "all", offset = offset ?: 0)
        if (users.isEmpty()) {
            return res
        }

        users.forEach { res.add(UserCuttedForm(code = it.code, additional = it.additional, login = it.login)) }
        return res
    }

    fun getCurrentAuthorizedUser(): User? = findByLogin(SecurityContextHolder.getContext().authentication.name)

    fun generateRandPassword(passwordLength: Int = Constants.STRING_LENGTH_SHORT) = generateRandStr()

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("logic-logs")
    }
}