package com.mathhelper.mathhelperserver.controllers

import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.constants.NetworkError
import com.mathhelper.mathhelperserver.forms.namespace.*
import com.mathhelper.mathhelperserver.services.namespace.NamespaceService
import com.mathhelper.mathhelperserver.services.user.UserService
import org.slf4j.Logger
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
@RequestMapping("/api/namespace")
class NamespaceController {
    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var namespaceService: NamespaceService

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/create")
    fun createNamespace(@Valid @RequestBody requestForm: NamespaceCreateForm?): ResponseEntity<Any> {
        logger.info("Creating new namespace")
        logger.info("RequestBody: ${requestForm.toString()}")

        if (requestForm == null) {
            return ResponseEntity(Constants.EXCEPTION_INVALID_BODY_STRING, HttpStatus.BAD_REQUEST)
        }

        val currUser = userService.getCurrentAuthorizedUser()!!
        try {
            namespaceService.createFrom(requestForm, currUser)
        } catch (e: RuntimeException) {
            return ResponseEntity(e.message!!, HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity("Namespace was successfully created!", HttpStatus.CREATED)
    }

    @GetMapping("")
    fun getNamespaces(
        @RequestParam(name = "author-user-code", required = false) authorUserCode: String?,
        @RequestParam(name = "edit-user-code", required = false) editUserCode: String?,
        @RequestParam(name = "substring", required = false) namespaceCodeFilter: String?
    ): ResponseEntity<Any> {
        logger.info("Getting namespaces")
        logger.info("Params: author-user-code=$authorUserCode, edit-user-code=$editUserCode, substring=$namespaceCodeFilter")

        val filteredNamespaces = namespaceService.filterNamespaces(authorUserCode, editUserCode, namespaceCodeFilter)
        if (filteredNamespaces.isEmpty())
            return ResponseEntity("Namespaces by this params not found", HttpStatus.NOT_FOUND)

        val forms = arrayListOf<NamespaceReturnForm>()
        filteredNamespaces.forEach { namespace -> forms.add(namespaceService.getForm(namespace)) }

        return ResponseEntity.ok(NamespaceFilterForm(namespaces = forms))
    }

    @GetMapping("/{code}")
    fun getNamespaceByCode(@PathVariable("code") code: String): ResponseEntity<Any> {
        logger.info("Getting namespace by code $code")

        val namespace = namespaceService.findByCode(code) ?:
            return ResponseEntity(NetworkError.notFound("Namespace with code $code"), HttpStatus.NOT_FOUND)

        val currUser = userService.getCurrentAuthorizedUser()
        if (currUser == null && !namespaceService.isNamespacePublicRead(namespace)) {
            return ResponseEntity("This namespace doesn't have public read access", HttpStatus.FORBIDDEN)
        } else if (currUser != null && !namespaceService.isUserHaveReadAccessToNamespace(namespace, currUser)) {
            return ResponseEntity("Current authorized user doesn't have read access to namespace", HttpStatus.FORBIDDEN)
        }

        return ResponseEntity.ok(namespaceService.getForm(namespace))
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("/update")
    fun updateUserGrantType(@Valid @RequestBody updateForm: NamespaceCreateForm?): ResponseEntity<Any> {
        logger.info("Updating user grant type for namespace")
        logger.info("Request body: ${updateForm.toString()}")

        if (updateForm == null) {
            return ResponseEntity(Constants.EXCEPTION_INVALID_BODY_STRING, HttpStatus.BAD_REQUEST)
        }

        val currUser = userService.getCurrentAuthorizedUser()!!
        if (!namespaceService.isUserAuthor(namespaceCode = updateForm.code, user = currUser)) {
            return ResponseEntity("Current authorized user is not the author of requested namespace", HttpStatus.FORBIDDEN)
        }

        try {
            namespaceService.updateWith(updateForm, currUser)
        } catch (e: RuntimeException) {
            return ResponseEntity(e.message!!, HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity.ok("Grant type was updated successfully.")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("logic-logs")
    }
}