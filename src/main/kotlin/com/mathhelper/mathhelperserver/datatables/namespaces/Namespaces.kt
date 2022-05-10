package com.mathhelper.mathhelperserver.datatables.namespaces

import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.datatables.users.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import java.sql.Timestamp
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "namespace")
data class Namespace(
    @Id
    @NotEmpty(message = "Please provide a namespace's code")
    @Column(unique = true, length = Constants.STRING_LENGTH_LONG)
    var code: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_code")
    @NotEmpty(message = "Please provide author user of namespace")
    var authorUser: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "namespace_grant_type")
    @NotEmpty(message = "Please provide current namespace's grant type")
    var namespaceGrantType: NamespaceGrantType,

    @Column(name = "server_action_ts")
    var serverActionTs: Timestamp
)


// some functions initially
interface NamespacesRepository : JpaRepository<Namespace, String>, CustomNamespacesRepository {
    fun existsByCode(code: String): Boolean

    fun findByCode(code: String): Namespace?
    fun findByAuthorUserCode(authorUserCode: String): List<Namespace>
    fun findByCodeAndAuthorUserCode(code: String, authorUserCode: String): Namespace?
}

interface CustomNamespacesRepository {
    fun findByAuthorUserCodeAndEditUserCodeAndCodeFilterNative(
        authorUserCode: String,
        editUserCode: String,
        namespaceCodeFilter: String
    ): List<Namespace>
}

class CustomNamespacesRepositoryImpl: CustomNamespacesRepository {
    @Autowired
    private lateinit var entityManager: EntityManager

    override fun findByAuthorUserCodeAndEditUserCodeAndCodeFilterNative(
        authorUserCode: String,
        editUserCode: String,
        namespaceCodeFilter: String
    ): List<Namespace> {
        var query = """
            select 
                ${Constants.POSTGRES_SCHEME}.namespace.code,
                ${Constants.POSTGRES_SCHEME}.namespace.author_user_code,
                ${Constants.POSTGRES_SCHEME}.namespace.namespace_grant_type,
                ${Constants.POSTGRES_SCHEME}.namespace.server_action_ts
            from ${Constants.POSTGRES_SCHEME}.namespace
            where
                ('$namespaceCodeFilter' = '' or code like '%$namespaceCodeFilter%') and
                ('$authorUserCode' = '' or author_user_code = '$authorUserCode')
        """

        if (!editUserCode.isBlank()) {
            query += """
                left join ${Constants.POSTGRES_SCHEME}.namespace_grant
                on
                    (${Constants.POSTGRES_SCHEME}.namespace_grant.granted_user_code = '$editUserCode')
            """
        }

        return entityManager.createNativeQuery(query, Namespace::class.java).resultList as List<Namespace>
    }
}