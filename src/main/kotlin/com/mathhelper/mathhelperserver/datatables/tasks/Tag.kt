package com.mathhelper.mathhelperserver.datatables.tasks

import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.datatables.namespaces.Namespace
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "tag")
data class Tag(
    @Id
    @NotEmpty(message = "Please provide tag's code")
    @Column(unique = true, length = Constants.STRING_LENGTH_LONG)
    var code: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "namespace_code")
    var namespace: Namespace,

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
    var descriptionRu: String = ""
)

interface TagRepository: JpaRepository<Tag, String> {
    fun existsByCode(code: String): Boolean
    fun existsByNamespaceCode(namespaceCode: String): Boolean
    fun existsByCodeAndNamespaceCode(code: String, namespaceCode: String): Boolean

    fun findByCode(code: String): Tag?
    fun findByNamespaceCode(namespaceCode: String): List<Tag>
    fun findByCodeAndNamespaceCode(code: String, namespaceCode: String): Tag?
}