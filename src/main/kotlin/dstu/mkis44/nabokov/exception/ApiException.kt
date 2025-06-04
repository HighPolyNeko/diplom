package dstu.mkis44.nabokov.exception

import org.springframework.http.HttpStatus

open class ApiException(
    override val message: String,
    val status: HttpStatus
) : RuntimeException(message)

class UserAlreadyExistsException(message: String) : 
    ApiException(message, HttpStatus.CONFLICT)

class InvalidCredentialsException(message: String = "Неверные учетные данные") : 
    ApiException(message, HttpStatus.UNAUTHORIZED)

class InvalidTokenException(message: String) : 
    ApiException(message, HttpStatus.UNAUTHORIZED)

class UserNotFoundException(message: String) : 
    ApiException(message, HttpStatus.NOT_FOUND)

class ValidationException(message: String) : 
    ApiException(message, HttpStatus.BAD_REQUEST) 