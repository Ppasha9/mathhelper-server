package com.mathhelper.mathhelperserver.datatables.namespaces

import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.datatables.users.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "namespace_grant")
data class NamespaceGrant(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "namespace_grant_gen")
    @SequenceGenerator(name = "namespace_grant_gen", sequenceName = "namespace_grant_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "namespace_code")
    @NotEmpty(message = "Provide a namespace")
    var namespace: Namespace,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_user_code")
    @NotEmpty(message = "Provide a user to grant")
    var grantedUser: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_grant_type")
    @NotEmpty(message = "Provide grant role for the user")
    var userGrantType: UserGrantType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licensor_user_code")
    @NotEmpty(message = "Provide a licensor user")
    var licensorUser: User
)


// some functions initially
interface NamespaceGrantsRepository : JpaRepository<NamespaceGrant, Long> {
    fun existsByNamespaceCodeAndGrantedUserCode(namespaceCode: String, grantedUserCode: String): Boolean
    fun existsByGrantedUserCodeAndUserGrantTypeCode(userCode: String, grantType: String): Boolean
    fun existsByNamespaceCodeAndGrantedUserCodeAndUserGrantTypeCode(namespaceCode: String, grantedUserCode: String, grantTypeCode: String): Boolean

    fun findByNamespaceCodeAndGrantedUserCode(namespaceCode: String, grantedUserCode: String): NamespaceGrant?
    fun findByGrantedUserCodeAndUserGrantTypeCode(userCode: String, grantType: String): NamespaceGrant?

    fun findByNamespaceCode(namespaceCode: String): List<NamespaceGrant>

    @Query("""
        select granted_user_code
        from ${Constants.POSTGRES_SCHEME}.namespace_grant
        where
            (${Constants.POSTGRES_SCHEME}.namespace_grant.namespace_code = :code) and
            (${Constants.POSTGRES_SCHEME}.namespace_grant.user_grant_type = '${Constants.USER_GRANT_TYPE_READ_WRITE}') 
    """, nativeQuery = true)
    fun findUsersCodesByNamespaceCodeAndIsWriteGrantedNative(@Param("code") namespaceCode: String): List<String>

    @Query("""
        select granted_user_code
        from ${Constants.POSTGRES_SCHEME}.namespace_grant
        where
            (${Constants.POSTGRES_SCHEME}.namespace_grant.namespace_code = :code) and
            (
                (${Constants.POSTGRES_SCHEME}.namespace_grant.user_grant_type = '${Constants.USER_GRANT_TYPE_READ_WRITE}') or
                (${Constants.POSTGRES_SCHEME}.namespace_grant.user_grant_type = '${Constants.USER_GRANT_TYPE_READ_NO_WRITE}')
            ) 
    """, nativeQuery = true)
    fun findUsersCodesByNamespaceCodeAndIsReadGrantedNative(@Param("code") namespaceCode: String): List<String>
}
