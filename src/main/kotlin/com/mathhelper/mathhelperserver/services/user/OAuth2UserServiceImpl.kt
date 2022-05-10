package com.mathhelper.mathhelperserver.services.user

import com.mathhelper.mathhelperserver.constants.Constants
import com.mathhelper.mathhelperserver.datatables.users.User
import com.mathhelper.mathhelperserver.datatables.users.UserTypeRepository
import com.mathhelper.mathhelperserver.datatables.users.toHashMap
import com.mathhelper.mathhelperserver.utils.resolveLocale
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class OAuth2UserServiceImpl : DefaultOAuth2UserService() {
    @Autowired
    private lateinit var userService: UserService
    @Autowired
    private lateinit var userTypeRepository: UserTypeRepository

    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val attributes = oAuth2User.attributes
        val locale = resolveLocale(attributes["locale"] as String?)

        val fullName = attributes["name"] as String?
        var name = ""

        if (fullName != null) {
            val divided = fullName.split(" ", limit = 2)
            name = divided[0]
        }

        var user = User(
            code = "",
            userType = userTypeRepository.findByCode(Constants.DEFAULT_USER_TYPE_CODE)!!,
            login = attributes["login"] as String,
            email = attributes["email"] as String,
            name = name,
            fullName = fullName!!,
            locale = locale,
            isOauth = true
        )

        user = userService.findOrRegister(user)

        val authoritySet: Set<GrantedAuthority> = HashSet(setOf(SimpleGrantedAuthority(user.userType.code)))
        return DefaultOAuth2User(authoritySet, user.toHashMap(), "id")
    }
}