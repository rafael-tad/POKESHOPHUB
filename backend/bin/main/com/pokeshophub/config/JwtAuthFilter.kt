package com.pokeshophub.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(private val jwtService: JwtService) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            println("JwtAuthFilter: No auth header or does not start with Bearer")
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)
        println("JwtAuthFilter: Received token: ${token.take(15)}...")

        if (jwtService.isTokenValid(token)) {
            val email = jwtService.extractEmail(token)
            val rol = jwtService.extractRol(token)
            val userId = jwtService.extractUserId(token)
            println("JwtAuthFilter: Token valid. Email: $email, Rol: $rol, UserId: $userId")

            if (email != null && rol != null && userId != null && SecurityContextHolder.getContext().authentication == null) {
                val principal = UserPrincipal(email, userId, rol)
                val auth = UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_$rol"))
                )
                SecurityContextHolder.getContext().authentication = auth
                println("JwtAuthFilter: Authentication set in SecurityContext with UserPrincipal")
            }
        } else {
            println("JwtAuthFilter: Token is invalid")
        }

        filterChain.doFilter(request, response)
    }
}

data class UserPrincipal(
    val email: String,
    val userId: Long,
    val rol: String
)
