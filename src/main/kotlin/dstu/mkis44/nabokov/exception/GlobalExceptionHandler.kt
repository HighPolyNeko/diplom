package dstu.mkis44.nabokov.exception

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException

@ControllerAdvice
class GlobalExceptionHandler {

    data class ErrorResponse(
        val timestamp: LocalDateTime = LocalDateTime.now(),
        val status: Int,
        val error: String,
        val message: String,
        val path: String
    )

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = ex.status.value(),
            error = ex.status.reasonPhrase,
            message = ex.message,
            path = "" // В реальном приложении здесь будет путь запроса
        )
        return ResponseEntity(errorResponse, ex.status)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors
            .map { it.defaultMessage ?: "Ошибка валидации" }
            .first()

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Error",
            message = errors,
            path = ""
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val errors = ex.constraintViolations
            .map { it.message }
            .first()

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Error",
            message = errors,
            path = ""
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            error = "Forbidden",
            message = "Недостаточно прав для выполнения операции",
            path = ""
        )
        return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidTokenException(ex: InvalidTokenException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "Unauthorized",
            message = ex.message,
            path = ""
        )
        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "Unauthorized",
            message = when (ex) {
                is BadCredentialsException -> "Неверные учетные данные"
                else -> ex.message ?: "Ошибка аутентификации"
            },
            path = ""
        )
        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = ex.message ?: "Произошла внутренняя ошибка сервера",
            path = ""
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
} 