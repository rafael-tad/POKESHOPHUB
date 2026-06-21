package com.pokeshophub.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService {

    @Value("\${jwt.secret}")
    private lateinit var secret: String

    @Value("\${jwt.expiration-ms}")
    private var expirationMs: Long = 86400000

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))
    }

    /**
     * Genera un token JWT.
     * @param subject email del usuario
     * @param rol rol del usuario (CLIENTE, TRABAJADOR, ADMIN)
     * @param userId id de la entidad en la BD
     */
    fun generateToken(subject: String, rol: String, userId: Long): String {
        return Jwts.builder()
            .subject(subject)
            .claim("rol", rol)
            .claim("userId", userId)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(key)
            .compact()
    }

    fun extractEmail(token: String): String? = getClaims(token)?.subject

    fun extractRol(token: String): String? = getClaims(token)?.get("rol", String::class.java)

    fun extractUserId(token: String): Long? = getClaims(token)?.get("userId", Number::class.java)?.toLong()

    fun isTokenValid(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            claims != null && claims.expiration.after(Date())
        } catch (e: Exception) {
            false
        }
    }

    private fun getClaims(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            println("JWT PARSE ERROR: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
