package com.mathhelper.mathhelperserver.controllers

import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.constants.NetworkError
import com.mathhelper.mathhelperserver.forms.rule_pack.RulePackForm
import com.mathhelper.mathhelperserver.services.namespace.NamespaceService
import com.mathhelper.mathhelperserver.services.rule_pack.RulePackService
import com.mathhelper.mathhelperserver.services.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.lang.RuntimeException
import javax.validation.Valid

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/rule-pack")
class RulePackController {
    @Autowired
    private lateinit var rulePackService: RulePackService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var namespaceService: NamespaceService

    companion object {
        private val logger by lazy { LoggerFactory.getLogger("logic-logs") }
    }

    @GetMapping("")
    fun findRulePacks(
        @RequestParam(name = "limit", required = false) limit: Int?,
        @RequestParam(name = "offset", required = false) offset: Int?,
        @RequestParam(name = "substr", required = false) substring: String?,
        @RequestParam(name = "keywords", required = false) keywords: String?,
        @RequestParam(name = "sort-by", required = false) sortBy: String?,
        @RequestParam(name = "descending", required = false) descending: Boolean?,
        @RequestParam(name = "namespace", required = false) namespace: String?,
        @RequestParam(name = "subject-type", required = false) subjectType: String?
    ): ResponseEntity<Any> {
        logger.info("getRulePacks")
        logger.info("Params: limit={}, offset={}, substring={}, keywords={}, sort-by={}, descending={}, namespace={}, subjectType={}"
            .format(limit, offset, substring, keywords, sortBy, descending, namespace, subjectType))
        val all = arrayListOf<RulePackForm>()
        val currUser = userService.getCurrentAuthorizedUser()
        val res = rulePackService.getByParams(limit, offset, substring, keywords, sortBy, descending, namespace, subjectType, currUser)
        return if (res.isEmpty())
            ResponseEntity("Rule packs with these params not found", HttpStatus.NOT_FOUND)
        else {
            res.forEach { all.add(rulePackService.getRulePackForm(it)) }
            ResponseEntity.ok(all)
        }
    }

    @GetMapping("/{code}")
    fun findRulePackByCode(@PathVariable("code") code: String): ResponseEntity<Any> {
        logger.info("getRulePackByCode")
        logger.info("Code = {}".format(code))
        val rulePack = rulePackService.findByCode(code) ?:
            return ResponseEntity(NetworkError.notFound("RulePack with code = $code"), HttpStatus.NOT_FOUND)

        val currUser = userService.getCurrentAuthorizedUser()
        if (currUser == null && !namespaceService.isNamespacePublicRead(rulePack.namespace)) {
            return ResponseEntity("Rule pack's namespace doesn't have public read access", HttpStatus.FORBIDDEN)
        } else if (currUser != null && !namespaceService.isUserHaveReadAccessToNamespace(rulePack.namespace, currUser)) {
            return ResponseEntity("Current authorized user doesn't have read access to rule pack's namespace", HttpStatus.FORBIDDEN)
        }

        val rulePackForm = rulePackService.getRulePackForm(rulePack)
        return ResponseEntity.ok(rulePackForm)
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/create")
    fun createRulePack(@Valid @RequestBody rulePackForm: RulePackForm?): ResponseEntity<String> {
        logger.info("createRulePack")
        logger.info("RulePackForm = {}".format(rulePackForm))
        if (rulePackForm == null) {
            return ResponseEntity(Constants.EXCEPTION_INVALID_BODY_STRING, HttpStatus.BAD_REQUEST)
        }

        val user = userService.getCurrentAuthorizedUser()!!
        val namespace = namespaceService.findByCode(rulePackForm.namespaceCode) ?:
            return ResponseEntity(NetworkError.notFound("Namespace with code = ${rulePackForm.namespaceCode}"), HttpStatus.BAD_REQUEST)
        if (!namespaceService.isUserHaveWriteAccessToNamespace(namespace, user)) {
            return ResponseEntity("Current authorized user doesn't have write access to rule pack's namespace", HttpStatus.FORBIDDEN)
        }

        try {
            rulePackService.createFrom(rulePackForm, user)
        } catch (e: RuntimeException) {
            return ResponseEntity(e.message!!, HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity("Rule pack was successfully created", HttpStatus.CREATED)
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/update")
    fun updateRulePack(@Valid @RequestBody rulePackForm: RulePackForm?): ResponseEntity<String> {
        logger.info("updateRulePack")
        logger.info("RulePackForm = {}".format(rulePackForm))
        if (rulePackForm == null) {
            return ResponseEntity(Constants.EXCEPTION_INVALID_BODY_STRING, HttpStatus.BAD_REQUEST)
        }

        val user = userService.getCurrentAuthorizedUser()!!
        val namespace = namespaceService.findByCode(rulePackForm.namespaceCode) ?:
            return ResponseEntity(NetworkError.notFound("Namespace with code = ${rulePackForm.namespaceCode}"), HttpStatus.BAD_REQUEST)
        if (!namespaceService.isUserHaveWriteAccessToNamespace(namespace, user)) {
            return ResponseEntity("Current authorized user doesn't have write access to rule pack's namespace", HttpStatus.FORBIDDEN)
        }

        try {
            rulePackService.updateWith(rulePackForm, user)
        } catch (e: RuntimeException) {
            return ResponseEntity(e.message!!, HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity.ok("Rule pack was successfully updated")
    }
}