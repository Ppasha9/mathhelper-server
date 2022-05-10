package com.mathhelper.mathhelperserver.datatables.taskset

import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.datatables.namespaces.Namespace
import com.mathhelper.mathhelperserver.datatables.rule_pack.CodeWithNames
import com.mathhelper.mathhelperserver.datatables.subject_type.SubjectType
import com.mathhelper.mathhelperserver.datatables.users.User
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.sql.Timestamp
import java.time.OffsetDateTime
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "taskset")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class Taskset(
    @Id
    @NotEmpty(message = "Please provide taskset's code")
    @Column(unique = true, length = Constants.STRING_LENGTH_LONG)
    var code: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "namespace_code")
    var namespace: Namespace,

    @Column(name="keywords", columnDefinition = "TEXT")
    @NotEmpty
    var keywords: String = "",

    @Column(name = "name_en")
    var nameEn: String = "",

    @Column(name = "name_ru")
    var nameRu: String = "",

    @Column(name = "description_short_en")
    var descriptionShortEn: String = "",

    @Column(name = "description_short_ru")
    var descriptionShortRu: String = "",

    @Column(name = "description_en", columnDefinition = "TEXT")
    var descriptionEn: String = "",

    @Column(name = "description_ru", columnDefinition = "TEXT")
    var descriptionRu: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_type")
    var subjectType: SubjectType? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_code")
    var authorUser: User,

    @Column(name = "recommended_by_community")
    var recommendedByCommunity: Boolean = false,

    @Column(name = "access_start_time")
    var accessStartTime: OffsetDateTime? = null,

    @Column(name = "access_end_time")
    var accessEndTime: OffsetDateTime? = null,

    @Column(name = "other_data", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var otherData: MutableMap<String, *>? = null,

    @Column(name = "server_action_ts")
    var serverActionTs: Timestamp
)

interface TasksetRepository : JpaRepository<Taskset, String>, CustomTasksetRepository {
    fun existsByCode(code: String): Boolean

    fun findByCode(code: String): Taskset?

    @Query("""
        select code, name_en as nameEn, name_ru as nameRu
        from ${Constants.POSTGRES_SCHEME}.taskset
        where namespace_code = :code
    """, nativeQuery = true)
    fun findNamesOnlyByNamespaceCodeNative(@Param("code") code: String): List<CodeWithNames>

    @Query(value = """
        select word from ${Constants.POSTGRES_SCHEME}.taskset_word
        where word % :keyword order by similarity(word, :keyword) desc, word limit 1
    """, nativeQuery = true)
    fun findKeywordNative(@Param("keyword") keyword: String): String?

    @Query(value = """
        select * from ${Constants.POSTGRES_SCHEME}.find_tasksets_by_keywords(:keywords, :namespace, :authorUserCode, :subjectType, :rows_limit, :offset)
    """, nativeQuery = true)
    fun findByKeywordsAndRowsLimitAndOffsetAndNamespaceAndAuthorUserCodeAndSubjectTypeNative(
        @Param("keywords") keywords: String,
        @Param("rows_limit") rowsLimit: Int,
        @Param("offset") offset: Int,
        @Param("namespace") namespace: String,
        @Param("authorUserCode") authorUserCode: String,
        @Param("subjectType") subjectType: String
    ) : List<String>

    fun findByCodeIn(codes: List<String>): List<Taskset>
}

interface CustomTasksetRepository {
    fun findByLimitAndOffsetAndSubstrAndSortByAndDescendingAndNamespaceAndAuthorUserCodeAndSubjectTypeNative(
        limit: String,
        offset: Int,
        substring: String,
        sortBy: String,
        sortByType: String,
        namespace: String,
        authorUserCode: String,
        subjectType: SubjectType?
    ): List<Taskset>
}

class CustomTasksetRepositoryImpl: CustomTasksetRepository {
    @Autowired
    private lateinit var entityManager: EntityManager

    override fun findByLimitAndOffsetAndSubstrAndSortByAndDescendingAndNamespaceAndAuthorUserCodeAndSubjectTypeNative(
        limit: String,
        offset: Int,
        substring: String,
        sortBy: String,
        sortByType: String,
        namespace: String,
        authorUserCode: String,
        subjectType: SubjectType?
    ): List<Taskset> {
        val query = """
                select *
                from ${Constants.POSTGRES_SCHEME}.taskset
                where
                    ('$substring' = '' or name_en like '%$substring%' or name_ru like '%$substring%') and
                    ('$namespace' = '' or namespace_code = '$namespace') and
                    ('$authorUserCode' = '' or author_user_code = '$authorUserCode') and
                    ('${subjectType?.name ?: ""}' = '' or subject_type = '${subjectType?.name ?: ""}')
                order by
                    $sortBy $sortByType
                limit $limit offset $offset  
            """

        return entityManager.createNativeQuery(query, Taskset::class.java).resultList as List<Taskset>
    }
}