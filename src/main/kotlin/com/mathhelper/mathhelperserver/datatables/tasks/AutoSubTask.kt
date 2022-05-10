package com.mathhelper.mathhelperserver.datatables.tasks

import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.datatables.namespaces.Namespace
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.jpa.repository.JpaRepository
import java.sql.Timestamp
import java.time.OffsetDateTime
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "auto_sub_task")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class AutoSubTask(
    @Id
    @NotEmpty(message = "Please prove autosubtask's code")
    @Column(unique = true, length = Constants.STRING_LENGTH_LONG)
    var code: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "namespace_code")
    var namespace: Namespace,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_code")
    var parentTask: Task,

    @NotEmpty(message = "Please, provide task's name in English")
    @Column(name = "name_en")
    var nameEn: String,

    @NotEmpty(message = "Please, provide task's name in Russian")
    @Column(name = "name_ru")
    var nameRu: String,

    @Column(name = "original_expression_plain_text", columnDefinition = "TEXT")
    var originalExpressionPlainText: String = "",

    @Column(name = "original_expression_tex", columnDefinition = "TEXT")
    var originalExpressionTex: String = "",

    @Column(name = "original_expression_structure_string", columnDefinition = "TEXT")
    var originalExpressionStructureString: String = "",

    @NotEmpty(message = "Please, provide task's goal type")
    @Column(name = "goal_type")
    var goalType: GoalType,

    @Column(name = "goal_expression_plain_text", columnDefinition = "TEXT")
    var goalExpressionPlainText: String = "",

    @Column(name = "goal_expression_tex", columnDefinition = "TEXT")
    var goalExpressionTex: String = "",

    @Column(name = "goal_expression_structure_string", columnDefinition = "TEXT")
    var goalExpressionStructureString: String = "",

    @Column(name = "goal_number_property")
    var goalNumberProperty: Int = 0,

    @Column(name = "goal_pattern", columnDefinition = "TEXT")
    var goalPattern: String = "",

    @Column(name = "steps_number")
    var stepsNumber: Int = 0,

    var time: Int = 0,

    var difficulty: Double,

    @Column(columnDefinition = "TEXT")
    var solution: String = "",

    @Column(name = "access_start_time")
    var accessStartTime: OffsetDateTime? = null,

    @Column(name = "access_end_time")
    var accessEndTime: OffsetDateTime? = null,

    @Column(name = "other_data", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var otherData: MutableMap<String, String>? = null,

    @Column(name = "server_action_ts")
    var serverActionTs: Timestamp
)


interface AutoSubTaskRepository : JpaRepository<AutoSubTask, String> {
    fun existsByCode(code: String): Boolean
    fun existsByParentTaskCode(parentTaskCode: String): Boolean
    fun existsByNamespaceCode(namespaceCode: String): Boolean

    fun findByCode(code: String): AutoSubTask?
    fun findByParentTaskCode(parentTaskCode: String): List<AutoSubTask>
    fun findByNamespaceCode(namespaceCode: String): List<AutoSubTask>
}