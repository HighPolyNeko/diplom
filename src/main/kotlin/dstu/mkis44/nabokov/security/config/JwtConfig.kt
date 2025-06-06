package dstu.mkis44.nabokov.security.config

import com.nimbusds.jose.crypto.DirectDecrypter
import com.nimbusds.jose.crypto.DirectEncrypter
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jose.jwk.OctetSequenceKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class JwtConfig {
    @Bean
    open fun accessTokenSigner(@Value("\${security.jwt.access-token.key}") accessTokenKey: String): MACSigner {
        return MACSigner(OctetSequenceKey.parse(accessTokenKey))
    }
    
    @Bean
    open fun accessTokenVerifier(@Value("\${security.jwt.access-token.key}") accessTokenKey: String): MACVerifier {
        return MACVerifier(OctetSequenceKey.parse(accessTokenKey))
    }

    @Bean
    open fun refreshTokenEncrypter(@Value("\${security.jwt.refresh-token.key}") refreshTokenKey: String): DirectEncrypter {
        return DirectEncrypter(OctetSequenceKey.parse(refreshTokenKey))
    }

    @Bean
    open fun refreshTokenDecrypter(@Value("\${security.jwt.refresh-token.key}") refreshTokenKey: String): DirectDecrypter {
        return DirectDecrypter(OctetSequenceKey.parse(refreshTokenKey))
    }
}

