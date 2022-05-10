package com.mathhelper.mathhelperserver.jpa_repos.rule_pack

import com.mathhelper.mathhelperserver.datatables.namespaces.Namespace
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespaceGrantType
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespaceGrantTypesRepository
import com.mathhelper.mathhelperserver.datatables.namespaces.NamespacesRepository
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables.users.UserRepository
import com.mathhelper.mathhelperserver.datatables.users.UserType
import com.mathhelper.mathhelperserver.datatables.users.UserTypeRepository
import com.mathhelper.mathhelperserver.datatables_history.HistoryId
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePack2RulePackHistory
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePack2RulePackHistoryRepository
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePackHistory
import com.mathhelper.mathhelperserver.datatables_history.rule_pack.RulePackHistoryRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers;
import java.sql.Timestamp

private class KPostgreSQLContainer(image: String): PostgreSQLContainer<KPostgreSQLContainer>(image)

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class RulePackReposTests {
    companion object {
        @Value("#{systemProperties['tests.databasename']}")
        private val dbName: String = ""

        @Container
        private val container = KPostgreSQLContainer("postgres:12").apply {
            withDatabaseName(dbName)
            withUsername(username)
            withPassword(password)
        }
    }

    @Autowired
    private lateinit var userTypeRepository: UserTypeRepository
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var namespaceGrantTypesRepository: NamespaceGrantTypesRepository
    @Autowired
    private lateinit var namespacesRepository: NamespacesRepository
    @Autowired
    private lateinit var rulePackHistoryRepository: RulePackHistoryRepository
    @Autowired
    private lateinit var rulePack2RulePackHistoryRepository: RulePack2RulePackHistoryRepository

    @Test
    fun injectedComponentsAreNotNull() {
        assertThat(userTypeRepository).isNotNull
        assertThat(userRepository).isNotNull
        assertThat(namespaceGrantTypesRepository).isNotNull
        assertThat(namespacesRepository).isNotNull
        assertThat(rulePackHistoryRepository).isNotNull
    }

    @Test
    fun checkTablesAreEmpty() {
        assert(userTypeRepository.count().toInt() == 0)
        assert(userRepository.count().toInt() == 0)
        assert(namespaceGrantTypesRepository.count().toInt() == 0)
        assert(namespacesRepository.count().toInt() == 0)
        assert(rulePackHistoryRepository.count().toInt() == 0)
    }

    @Test
    fun rulePacksWithHistoryAndRelation() {
        val userType = UserType("user_type_code", "user_type_description")
        userTypeRepository.saveAndFlush(userType)
        assert(userTypeRepository.count().toInt() == 1)
        val user = User(code = "user_code", userType = userType, email = "email@mail.ru", login = "test_user", name = "test")
        userRepository.saveAndFlush(user)
        // Getting saved user in case of users.code autogeneration
        val savedUser = userRepository.findAll()[0]
        assert(userRepository.count().toInt() == 1)
        val namespaceType = NamespaceGrantType("namespace_type_code", "namespace_type_description")
        namespaceGrantTypesRepository.saveAndFlush(namespaceType)
        assert(namespaceGrantTypesRepository.count().toInt() == 1)
        val namespace = Namespace("namespace_code", savedUser, namespaceType, serverActionTs = Timestamp(System.currentTimeMillis()))
        namespacesRepository.saveAndFlush(namespace)
        assert(namespacesRepository.count().toInt() == 1)
        val id1 = HistoryId("rule_pack_1_code", 0)
        val id2 = HistoryId("rule_pack_2_code", 1)
        val rulePack1 = RulePackHistory(id1, namespace, "", savedUser, "name_en", "name_ru")
        val rulePack2 = RulePackHistory(id2, namespace, "", savedUser, "name_en_2", "name_ru_2")
        listOf(rulePack1, rulePack2)
            .forEach {
                rulePackHistoryRepository.saveAndFlush(it)
            }
        assert(rulePackHistoryRepository.count().toInt() == 2)
        val rulePack2RulePack = RulePack2RulePackHistory(parentRulePack = rulePack1, childRulePack = rulePack2)
        rulePack2RulePackHistoryRepository.saveAndFlush(rulePack2RulePack)
        assert(rulePack2RulePackHistoryRepository.count().toInt() == 1)
        // Getting saved user in case of rule_packs_to_rule_packs_history.id autogeneration
        val savedRelation = rulePack2RulePackHistoryRepository.findAll()[0]
        var foundRulePack = rulePackHistoryRepository.findByIdOrNull(id1)
        assertThat(foundRulePack).isEqualTo(rulePack1)
        foundRulePack = rulePackHistoryRepository.findByIdOrNull(id2)
        assertThat(foundRulePack).isEqualTo(rulePack2)
        val foundRelation = rulePack2RulePackHistoryRepository.findByParentRulePackId(id1)
        assertThat(foundRelation[0]).isEqualTo(savedRelation)
        val notFound = rulePack2RulePackHistoryRepository.findByChildRulePackId(id1)
        assert(notFound.isEmpty())
    }
}