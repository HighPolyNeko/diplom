package dstu.mkis44.nabokov

import dstu.mkis44.nabokov.security.config.SecurityProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity

@EnableMethodSecurity
@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
