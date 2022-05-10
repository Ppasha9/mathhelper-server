package com.mathhelper.mathhelperserver.datatables.rule_pack

import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.datatables.namespaces.Namespace
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
import javax.persistence.*
import javax.validation.constraints.NotEmpty


@Entity
@Table(name = "rule_pack")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class RulePack(
    @Id
    @NotEmpty(message = "Please provide rule pack's code")
    @Column(unique = true, length = Constants.STRING_LENGTH_LONG)
    var code: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "namespace_code")
    var namespace: Namespace,

    @Column(name="keywords", columnDefinition = "TEXT")
    @NotEmpty
    var keywords: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_code")
    var authorUser: User,

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

    @Column(columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var rules: ArrayList<*>? = null,

    @Column(name = "other_data", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var otherData: MutableMap<String, *>? = null,

    @Column(name = "other_check_solution_data", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var otherCheckSolutionData: MutableMap<String, *>? = null,

    @Column(name = "other_auto_generation_data", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var otherAutoGenerationData: MutableMap<String, *>? = null,

    @Column(name = "server_action_ts")
    var serverActionTs: Timestamp
)

interface CodeWithNames {
    val code: String
    val nameEn: String
    val nameRu: String
}

interface RulePackRepository : JpaRepository<RulePack, String>, CustomRulePackRepository {
    fun existsByCode(code: String): Boolean

    fun findByCode(code: String): RulePack?
    fun findByNamespaceCode(namespaceCode: String): List<RulePack>
    @Query("""
        select code, name_en as nameEn, name_ru as nameRu
        from ${Constants.POSTGRES_SCHEME}.rule_pack
        where namespace_code = :code
    """, nativeQuery = true)
    fun findNamesOnlyByNamespaceCodeNative(@Param("code") code: String): List<CodeWithNames>

    @Query(value = """
        select word from ${Constants.POSTGRES_SCHEME}.rule_pack_word
        where word % :keyword order by similarity(word, :keyword) desc, word limit 1
    """, nativeQuery = true)
    fun findKeywordNative(@Param("keyword") keyword: String): String?

    @Query(value = """
        select * from ${Constants.POSTGRES_SCHEME}.find_rule_packs_by_keywords(:keywords, :namespace, :subjectType, :rows_limit, :offset)
    """, nativeQuery = true)
    fun findByKeywordsAndRowsLimitAndOffsetAndNamespaceAndSubjectTypeNative(
        @Param("keywords") keywords: String,
        @Param("rows_limit") rowsLimit: Int,
        @Param("offset") offset: Int,
        @Param("namespace") namespace: String,
        @Param("subjectType") subjectType: String
    ) : List<String>

    fun findByCodeIn(codes: List<String>): List<RulePack>
}

interface CustomRulePackRepository {
    fun findByLimitAndOffsetAndSubstrAndSortByAndDescendingAndNamespaseNative(
        limit: String, offset: Int, substring: String, sortBy: String, sortByType: String, namespace: String, subjectType: String
    ): List<RulePack>
}

class CustomRulePackRepositoryImpl: CustomRulePackRepository {
    @Autowired
    private val entityManager: EntityManager? = null

    override fun findByLimitAndOffsetAndSubstrAndSortByAndDescendingAndNamespaseNative(
        limit: String, offset: Int, substring: String, sortBy: String, sortByType: String, namespace: String, subjectType: String
    ): List<RulePack> {
        val query = """
            select * 
            from ${Constants.POSTGRES_SCHEME}.rule_pack
            where
                ('$substring' = '' or name_en like '%$substring%' or name_ru like '%$substring%') and
                ('$namespace' = '' or namespace_code = '$namespace') and
                ('$subjectType' = '' or subject_type = '$subjectType')
            order by
                $sortBy $sortByType
            limit $limit offset $offset 
        """.trimIndent()

        return entityManager!!.createNativeQuery(query, RulePack::class.java).resultList as List<RulePack>
    }
}