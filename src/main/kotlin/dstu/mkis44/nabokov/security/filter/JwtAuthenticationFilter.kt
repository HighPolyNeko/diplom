package dstu.mkis44.nabokov.security.filter

import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jwt.SignedJWT
import dstu.mkis44.nabokov.exception.InvalidTokenException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.text.ParseException
import java.util.*

@Component
class JwtAuthenticationFilter(
    private val userDetailsService: UserDetailsService,
    @Value("\${security.jwt.access-token.key}") private val secret: String
) : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    private val jwsVerifier: JWSVerifier =  MACVerifier(OctetSequenceKey.parse(secret))

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val token = authHeader.substring(7)
            val signedJWT = SignedJWT.parse(token)
            
            // Проверка подписи токена
            if (!signedJWT.verify(jwsVerifier)) {
                log.warn("JWT token signature verification failed")
                filterChain.doFilter(request, response)
                return
            }
            
            val claims = signedJWT.jwtClaimsSet

            // Проверка срока действия токена
            if (claims.expirationTime.before(Date())) {
                log.warn("JWT token is expired")
                filterChain.doFilter(request, response)
                return
            }

            val username = claims.subject
            val authorities = (claims.getClaim("authorities") as List<*>)
                .map { SimpleGrantedAuthority(it.toString()) }

            val userDetails = userDetailsService.loadUserByUsername(username)
            
            // Проверка, что пользователь активен
            if (!userDetails.isEnabled) {
                log.warn("User account is disabled: {}", username)
                filterChain.doFilter(request, response)
                return
            }
            
            val authentication = UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities
            )
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication

            log.debug("Authentication successful for user: {}", username)
        } catch (e: ParseException) {
            log.error("Failed to parse JWT token", e)
        } catch (e: Exception) {
            log.error("Error processing JWT token", e)
        }

        filterChain.doFilter(request, response)
    }
}

