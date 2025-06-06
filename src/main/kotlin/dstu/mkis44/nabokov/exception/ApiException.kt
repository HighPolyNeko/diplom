package dstu.mkis44.nabokov.exception

import org.springframework.http.HttpStatus

/**
 * Базовый класс для всех исключений API
 */
open class ApiException(
    override val message: String,
    val status: HttpStatus,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Исключение, связанное с уже существующим пользователем
 */
class UserAlreadyExistsException(message: String) : 
    ApiException(message, HttpStatus.CONFLICT)

/**
 * Исключение, связанное с неверными учетными данными
 */
class InvalidCredentialsException(message: String = "Неверные учетные данные") : 
    ApiException(message, HttpStatus.UNAUTHORIZED)

/**
 * Исключение, связанное с недействительным токеном
 */
class InvalidTokenException(message: String) : 
    ApiException(message, HttpStatus.UNAUTHORIZED)

/**
 * Исключение, связанное с отсутствием пользователя
 */
class UserNotFoundException(message: String) : 
    ApiException(message, HttpStatus.NOT_FOUND)

/**
 * Исключение, связанное с ошибкой валидации
 */
class ValidationException(message: String) : 
    ApiException(message, HttpStatus.BAD_REQUEST)

/**
 * Исключение, связанное с отсутствием прав доступа
 */
class AccessDeniedException(message: String) : 
    ApiException(message, HttpStatus.FORBIDDEN)

/**
 * Исключение, связанное с отсутствием ресурса
 */
class ResourceNotFoundException(message: String) : 
    ApiException(message, HttpStatus.NOT_FOUND)

