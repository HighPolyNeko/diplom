package dstu.mkis44.nabokov.exception

import org.springframework.http.HttpStatus

/**
 * Исключение, связанное с хранением файлов
 */
class FileStorageException(message: String, cause: Throwable? = null) : 
    ApiException(message, HttpStatus.INTERNAL_SERVER_ERROR, cause)

/**
 * Исключение, связанное с недопустимым типом файла
 */
class InvalidFileTypeException(message: String) : 
    ApiException(message, HttpStatus.BAD_REQUEST)

/**
 * Исключение, связанное с отсутствием файла
 */
class FileNotFoundException(message: String) : 
    ApiException(message, HttpStatus.NOT_FOUND)

/**
 * Исключение, связанное с превышением размера файла
 */
class FileSizeExceededException(message: String) : 
    ApiException(message, HttpStatus.PAYLOAD_TOO_LARGE)

