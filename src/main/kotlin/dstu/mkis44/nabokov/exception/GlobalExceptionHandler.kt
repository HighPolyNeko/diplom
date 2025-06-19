package dstu.mkis44.nabokov.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException as SpringAccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.multipart.MaxUploadSizeExceededException
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    data class ErrorResponse(
        val timestamp: LocalDateTime = LocalDateTime.now(),
        val status: Int,
        val error: String,
        val message: String,
        val path: String
    )

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("API Exception: {}", ex.message, ex)
        
        val errorResponse = ErrorResponse(
            status = ex.status.value(),
            error = ex.status.reasonPhrase,
            message = ex.message,
            path = request.requestURI
        )
        return ResponseEntity(errorResponse, ex.status)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Validation Error: {}", ex.message, ex)
        
        val errors = ex.bindingResult.fieldErrors
            .map { it.defaultMessage ?: "Ошибка валидации" }
            .joinToString(", ")

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Error",
            message = errors,
            path = request.requestURI
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Constraint Violation: {}", ex.message, ex)
        
        val errors = ex.constraintViolations
            .map { it.message }
            .joinToString(", ")

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Error",
            message = errors,
            path = request.requestURI
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(SpringAccessDeniedException::class)
    fun handleAccessDenied(ex: SpringAccessDeniedException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Access Denied: {}", ex.message, ex)
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            error = "Forbidden",
            message = "Недостаточно прав для выполнения операции",
            path = request.requestURI
        )
        return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Authentication Exception: {}", ex.message, ex)
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "Unauthorized",
            message = when (ex) {
                is BadCredentialsException -> "Неверные учетные данные"
                else -> ex.message ?: "Ошибка аутентификации"
            },
            path = request.requestURI
        )
        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxSizeException(ex: MaxUploadSizeExceededException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("File Size Exceeded: {}", ex.message, ex)
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.PAYLOAD_TOO_LARGE.value(),
            error = "Payload Too Large",
            message = "Размер файла превышает максимально допустимый",
            path = request.requestURI
        )
        return ResponseEntity(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE)
    }
    
    @ExceptionHandler(FileStorageException::class)
    fun handleFileStorageException(ex: FileStorageException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("File Storage Exception: {}", ex.message, ex)
        
        val errorResponse = ErrorResponse(
            status = ex.status.value(),
            error = ex.status.reasonPhrase,
            message = ex.message,
            path = request.requestURI
        )
        return ResponseEntity(errorResponse, ex.status)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled Exception: {}", ex.message, ex)
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "Произошла внутренняя ошибка сервера",
            path = request.requestURI
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

