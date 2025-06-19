package dstu.mkis44.nabokov.security.service

import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEEncrypter
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import dstu.mkis44.nabokov.security.model.AccessToken
import dstu.mkis44.nabokov.security.model.RefreshToken
import org.springframework.stereotype.Service
import java.util.*

@Service
open class TokenSerializerService(
    private val accessTokenSigner: JWSSigner,
    private val refreshTokenEncrypter: JWEEncrypter
) {
    private val jwsAlgorithm: JWSAlgorithm = JWSAlgorithm.HS256
    private val jweAlgorithm: JWEAlgorithm = JWEAlgorithm.DIR
    private val encryptionMethod: EncryptionMethod = EncryptionMethod.A128CBC_HS256

    open fun serializeAccessToken(accessToken: AccessToken): String {
        val jwsHeader = JWSHeader.Builder(jwsAlgorithm)
            .keyID(accessToken.id.toString())
            .build()
        val jwtClaimsSet = JWTClaimsSet.Builder()
            .jwtID(accessToken.id.toString())
            .subject(accessToken.subject)
            .issueTime(Date.from(accessToken.createdAt))
            .expirationTime(Date.from(accessToken.expiresAt))
            .claim("authorities", accessToken.authorities)
            .build()
        val signedJWT = SignedJWT(jwsHeader, jwtClaimsSet)
        signedJWT.sign(accessTokenSigner)
        return signedJWT.serialize()
    }

    open fun serializeRefreshToken(refreshToken: RefreshToken): String {
        val jweHeader = JWEHeader.Builder(jweAlgorithm, encryptionMethod)
            .keyID(refreshToken.id.toString())
            .build()
        val jwtClaimsSet = JWTClaimsSet.Builder()
            .jwtID(refreshToken.id.toString())
            .subject(refreshToken.subject)
            .issueTime(Date.from(refreshToken.createdAt))
            .expirationTime(Date.from(refreshToken.expiresAt))
            .claim("authorities", refreshToken.authorities)
            .build()
        val encryptedJWT = EncryptedJWT(jweHeader, jwtClaimsSet)
        encryptedJWT.encrypt(refreshTokenEncrypter)
        return encryptedJWT.serialize()
    }
} 