package dstu.mkis44.nabokov.security.filter

import com.nimbusds.jwt.SignedJWT
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

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
            val claims = signedJWT.jwtClaimsSet

            if (claims.expirationTime.before(java.util.Date())) {
                filterChain.doFilter(request, response)
                return
            }

            val username = claims.subject
            val authorities = (claims.getClaim("authorities") as List<*>)
                .map { SimpleGrantedAuthority(it.toString()) }

            val userDetails = userDetailsService.loadUserByUsername(username)
            val authentication = UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities
            )
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
        } catch (e: Exception) {
            // В случае ошибки парсинга или проверки токена, просто продолжаем цепочку фильтров
        }

        filterChain.doFilter(request, response)
    }
} 