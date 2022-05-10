package com.mathhelper.mathhelperserver.services.namespace

import com.mathhelper.mathhelperserver.constants.AlreadyExistsWithCodeException
import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.constants.NotFoundException
import com.mathhelper.mathhelperserver.datatables.namespaces.*
import com.mathhelper.mathhelperserver.datatables.rule_pack.RulePack
import com.mathhelper.mathhelperserver.datatables.tasks.Task
import com.mathhelper.mathhelperserver.datatables.taskset.Taskset
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables.users.UserRepository
import com.mathhelper.mathhelperserver.forms.namespace.NamespaceCreateForm
import com.mathhelper.mathhelperserver.forms.namespace.NamespaceReturnForm
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException
import java.sql.Timestamp

@Service
class NamespaceService {
    @Autowired
    private lateinit var namespaceRepository: NamespacesRepository
    @Autowired
    private lateinit var namespaceGrantTypesRepository: NamespaceGrantTypesRepository
    @Autowired
    private lateinit var userGrantTypesRepository: UserGrantTypesRepository
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var namespaceGrantsRepository: NamespaceGrantsRepository

    fun existsByCode(code: String): Boolean = namespaceRepository.existsByCode(code)

    @Transactional
    fun findByCode(code: String): Namespace? = namespaceRepository.findByCode(code)

    @Transactional
    fun isUserAuthor(namespaceCode: String, user: User): Boolean {
        val namespace = namespaceRepository.findByCodeAndAuthorUserCode(code = namespaceCode, authorUserCode = user.code)
        return namespace != null
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun createFrom(requestForm: NamespaceCreateForm, user: User) {
        logger.info("Creating namespace in service")

        if (namespaceRepository.existsByCode(requestForm.code)) {
            throw AlreadyExistsWithCodeException(entity = "Namespace", code = requestForm.code)
        }

        val namespace = Namespace(
            code = requestForm.code,
            authorUser = user,
            namespaceGrantType = namespaceGrantTypesRepository.findByCode(requestForm.grantType)!!,
            serverActionTs = Timestamp(System.currentTimeMillis())
        )

        namespaceRepository.saveAndFlush(namespace)

        requestForm.usersGrants?.forEach {
            val userCode = it.keys.elementAt(0)
            val userGrantType = it.values.elementAt(0)

            if (!userRepository.existsByCode(userCode)) {
                throw NotFoundException("User with code $userCode")
            }

            if (!userGrantTypesRepository.existsByCode(userGrantType)) {
                throw NotFoundException("User grant type with code $userGrantType")
            }

            namespaceGrantsRepository.save(NamespaceGrant(
                namespace = namespace,
                grantedUser = userRepository.findByCode(userCode)!!,
                userGrantType = userGrantTypesRepository.findByCode(userGrantType)!!,
                licensorUser = user
            ))
        }

        namespaceGrantsRepository.save(NamespaceGrant(
            namespace = namespace,
            grantedUser = user,
            userGrantType = userGrantTypesRepository.findByCode(Constants.USER_GRANT_TYPE_READ_WRITE)!!,
            licensorUser = user
        ))
    }

    @Transactional
    fun filterNamespaces(authorUserCode: String?, editUserCode: String?, namespaceCodeFilter: String?): List<Namespace> {
        logger.info("Filtering namespaces")

        return namespaceRepository.findByAuthorUserCodeAndEditUserCodeAndCodeFilterNative(
            authorUserCode = if (authorUserCode.isNullOrBlank()) "" else authorUserCode,
            editUserCode = if (editUserCode.isNullOrBlank()) "" else editUserCode,
            namespaceCodeFilter = if (namespaceCodeFilter.isNullOrBlank()) "" else namespaceCodeFilter
        )
    }

    @Transactional
    fun getForm(namespace: Namespace): NamespaceReturnForm {
        val res = NamespaceReturnForm(
            code = namespace.code,
            grantType = namespace.namespaceGrantType.code,
            authorUserCode = namespace.authorUser.code,
            serverActionTs = namespace.serverActionTs
        )

        when (res.grantType) {
            Constants.NAMESPACE_GRANT_TYPE_PUBLIC_READ_WRITE -> {
                return res
            }
            Constants.NAMESPACE_GRANT_TYPE_PRIVATE_READ_WRITE -> {
                res.writeGrantedUsers = namespaceGrantsRepository.findUsersCodesByNamespaceCodeAndIsWriteGrantedNative(
                    namespaceCode = namespace.code
                ) as ArrayList<String>

                res.readGrantedUsers = namespaceGrantsRepository.findUsersCodesByNamespaceCodeAndIsReadGrantedNative(
                    namespaceCode = namespace.code
                ) as ArrayList<String>
            }
            Constants.NAMESPACE_GRANT_TYPE_PUBLIC_READ_PRIVATE_WRITE -> {
                res.writeGrantedUsers = namespaceGrantsRepository.findUsersCodesByNamespaceCodeAndIsWriteGrantedNative(
                    namespaceCode = namespace.code
                ) as ArrayList<String>
            }
        }

        return res
    }

    @Throws(RuntimeException::class)
    @Transactional
    fun updateWith(updateForm: NamespaceCreateForm, user: User) {
        if (!namespaceRepository.existsByCode(updateForm.code)) {
            throw NotFoundException("Namespace with code ${updateForm.code}")
        }

        if (!namespaceGrantTypesRepository.existsByCode(updateForm.grantType)) {
            throw NotFoundException("Namespace grant type with code ${updateForm.grantType}")
        }

        val namespace = namespaceRepository.findByCode(updateForm.code)!!
        namespace.namespaceGrantType = namespaceGrantTypesRepository.findByCode(updateForm.grantType)!!
        namespace.serverActionTs = Timestamp(System.currentTimeMillis())
        namespaceRepository.save(namespace)

        if (namespace.namespaceGrantType.code == Constants.NAMESPACE_GRANT_TYPE_PUBLIC_READ_WRITE) {
            // we need to delete all namespaceGrants cause now namespace is public
            val usersGrants = namespaceGrantsRepository.findByNamespaceCode(namespaceCode = namespace.code)
            usersGrants.forEach {
                namespaceGrantsRepository.delete(it)
            }
            return
        }

        if (namespace.namespaceGrantType.code == Constants.NAMESPACE_GRANT_TYPE_PUBLIC_READ_PRIVATE_WRITE) {
            // we need to delete all namespaceGrants for reading
            val usersGrants = namespaceGrantsRepository.findByNamespaceCode(namespaceCode = namespace.code)
            usersGrants.forEach {
                if (it.userGrantType.code == Constants.USER_GRANT_TYPE_READ_NO_WRITE || it.userGrantType.code == Constants.USER_GRANT_TYPE_READ_WRITE) {
                    namespaceGrantsRepository.delete(it)
                }
            }

            namespaceGrantsRepository.save(NamespaceGrant(
                namespace = namespace,
                grantedUser = user,
                licensorUser = user,
                userGrantType = userGrantTypesRepository.findByCode(Constants.USER_GRANT_TYPE_READ_WRITE)!!
            ))

            return
        }

        if (namespace.namespaceGrantType.code == Constants.NAMESPACE_GRANT_TYPE_PRIVATE_READ_WRITE) {
            if (namespaceGrantsRepository.existsByNamespaceCodeAndGrantedUserCodeAndUserGrantTypeCode(
                    namespaceCode = namespace.code,
                    grantedUserCode = user.code,
                    grantTypeCode = Constants.USER_GRANT_TYPE_READ_WRITE
                )) {
                namespaceGrantsRepository.save(NamespaceGrant(
                    namespace = namespace,
                    grantedUser = user,
                    licensorUser = user,
                    userGrantType = userGrantTypesRepository.findByCode(Constants.USER_GRANT_TYPE_READ_WRITE)!!
                ))
            }

            // we need to delete all namespaceGrants for reading and writing, but if there was READ_WRITE grants, they will no be deleted
            val usersGrants = namespaceGrantsRepository.findByNamespaceCode(namespaceCode = namespace.code)
            usersGrants.forEach {
                if (it.userGrantType.code != Constants.USER_GRANT_TYPE_READ_WRITE) {
                    namespaceGrantsRepository.delete(it)
                }
            }
            return
        }

        // handle users grants
        if (updateForm.usersGrants.isNullOrEmpty()) {
            return
        }

        for (it in updateForm.usersGrants!!) {
            val userCode = it.keys.elementAt(0)
            val userGrantType = it.values.elementAt(0)

            if (!userRepository.existsByCode(userCode)) {
                throw NotFoundException("User with code $userCode")
            }

            if (!userGrantTypesRepository.existsByCode(userGrantType)) {
                throw NotFoundException("User grant type with code $userGrantType")
            }

            if (namespace.namespaceGrantType.code == Constants.NAMESPACE_GRANT_TYPE_PUBLIC_READ_PRIVATE_WRITE) {
                if (userGrantType == Constants.USER_GRANT_TYPE_READ_NO_WRITE) {
                    continue
                }
            }

            if (!namespaceGrantsRepository.existsByNamespaceCodeAndGrantedUserCodeAndUserGrantTypeCode(
                    namespaceCode = namespace.code,
                    grantedUserCode = userCode,
                    grantTypeCode = userGrantType)) {
                namespaceGrantsRepository.save(NamespaceGrant(
                    namespace = namespace,
                    grantedUser = userRepository.findByCode(userCode)!!,
                    userGrantType = userGrantTypesRepository.findByCode(userGrantType)!!,
                    licensorUser = user
                ))
            }
        }
    }

    private fun <EntityType> getEntityNamespace(it: EntityType): Namespace? {
        return when (it) {
            is Taskset -> {
                it.namespace
            }
            is Task -> {
                it.namespace
            }
            is RulePack -> {
                it.namespace
            }
            else -> null
        }
    }

    @Transactional
    fun <EntityType> filterEntitiesByNamespacesGrantsAndUserForRead(entities: ArrayList<EntityType>, currUser: User?): ArrayList<EntityType> {
        val filteredEntities = arrayListOf<EntityType>()

        for (it: EntityType in entities) {
            val namespace = getEntityNamespace(it)

            if (namespace != null) {
                if (isNamespacePublicRead(namespace)) {
                    filteredEntities.add(it)
                } else if (currUser != null) {
                    val grantType = namespaceGrantsRepository.findByNamespaceCodeAndGrantedUserCode(namespace.code, currUser.code)
                    if (grantType != null) {
                        if (grantType.userGrantType.code == Constants.USER_GRANT_TYPE_READ_NO_WRITE ||
                                grantType.userGrantType.code == Constants.USER_GRANT_TYPE_READ_WRITE) {
                            filteredEntities.add(it)
                        }
                    }
                }
            }
        }

        return filteredEntities
    }

    @Transactional
    fun isUserHaveWriteAccessToNamespace(namespace: Namespace, user: User): Boolean {
        return isNamespacePublicWrite(namespace) || namespaceGrantsRepository.existsByNamespaceCodeAndGrantedUserCodeAndUserGrantTypeCode(
                namespaceCode = namespace.code,
                grantedUserCode = user.code,
                grantTypeCode = Constants.USER_GRANT_TYPE_READ_WRITE
        )
    }

    @Transactional
    fun isUserHaveReadAccessToNamespace(namespace: Namespace, user: User): Boolean {
        if (isNamespacePublicRead(namespace)) {
            return true
        }

        val isReadWrite = namespaceGrantsRepository.existsByNamespaceCodeAndGrantedUserCodeAndUserGrantTypeCode(
                namespaceCode = namespace.code,
                grantedUserCode = user.code,
                grantTypeCode = Constants.USER_GRANT_TYPE_READ_WRITE
        )
        val isReadNoWrite = namespaceGrantsRepository.existsByNamespaceCodeAndGrantedUserCodeAndUserGrantTypeCode(
                namespaceCode = namespace.code,
                grantedUserCode = user.code,
                grantTypeCode = Constants.USER_GRANT_TYPE_READ_NO_WRITE
        )

        return isReadWrite || isReadNoWrite
    }

    fun isNamespacePublicWrite(namespace: Namespace): Boolean {
        return namespace.namespaceGrantType.code == Constants.NAMESPACE_GRANT_TYPE_PUBLIC_READ_WRITE
    }

    fun isNamespacePublicRead(namespace: Namespace): Boolean {
        return namespace.namespaceGrantType.code == Constants.NAMESPACE_GRANT_TYPE_PUBLIC_READ_WRITE ||
               namespace.namespaceGrantType.code == Constants.NAMESPACE_GRANT_TYPE_PUBLIC_READ_PRIVATE_WRITE
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("logic-logs")
    }
}