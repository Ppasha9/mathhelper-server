package com.mathhelper.mathhelperserver.authorization

import com.mathhelper.mathhelperserver.datatables.users.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class JwtUserDetailsImpl(userDB: User) : UserDetails {
    val user = userDB

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return HashSet(setOf<GrantedAuthority>(SimpleGrantedAuthority(user.userType.code)))
    }

    override fun getUsername(): String {
        return user.login
    }

    override fun getPassword(): String {
        return user.password
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}