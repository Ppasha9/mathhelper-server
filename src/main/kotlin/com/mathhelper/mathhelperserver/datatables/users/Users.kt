package com.mathhelper.mathhelperserver.datatables.users

import com.mathhelper.mathhelperserver.constants.Constants
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "user_entity")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class User(
    @Id
    @Column(unique = true, length = Constants.STRING_LENGTH_MIDDLE)
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var code: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_type_code")
    @NotEmpty(message = "Please provide user's type")
    var userType: UserType,

    var login: String = "",

    var email: String = "",

    @Column(columnDefinition = "TEXT")
    var additional: String = "",

    var name: String = "",

    @Column(name = "full_name")
    var fullName: String = "",

    @Column(length = 3)
    var locale: String = Constants.RUSSIAN_LOCALE_NAME,

    var password: String = "",

    @Column(name = "external_code")
    var externalCode: String = "",

    @Column(name = "is_oauth")
    var isOauth: Boolean = false,

    @Column(name = "other_data", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var otherData: MutableMap<String, *>? = null
)

fun User.toHashMap(): HashMap<String, Any> {
    val res = HashMap<String, Any>()
    res["id"] = code
    res["userType"] = userType.toString()
    res["login"] = login
    res["email"] = email
    res["name"] = name
    res["fullName"] = fullName
    res["locale"] = locale
    res["externalCode"] = externalCode

    return res
}

// just some functions for first time
interface UserRepository : JpaRepository<User, String>, CustomUserRepository {
    fun existsByEmail(email: String): Boolean
    fun existsByLogin(login: String): Boolean
    fun existsByCode(code: String): Boolean

    fun findAllByEmailOrLogin(email: String, login: String): List<User>
    fun findByCode(code: String): User?
    fun findByLogin(login: String): User?
    fun findByEmail(email: String): User?
    fun findByExternalCode(externalCode: String): User?
}

interface CustomUserRepository {
    fun findAllByLimitAndOffset(limit: String, offset: Int): List<User>
}

class CustomUserRepositoryImpl : CustomUserRepository {
    @Autowired
    private lateinit var entityManager: EntityManager

    override fun findAllByLimitAndOffset(limit: String, offset: Int): List<User> {
        val query = """
            select * from ${Constants.POSTGRES_SCHEME}.user_entity
            order by
                login
            limit $limit offset $offset 
        """

        return entityManager.createNativeQuery(query, User::class.java).resultList as List<User>
    }
}