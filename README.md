# Diplom API

REST API для дипломного проекта на Spring Boot и Kotlin.

## Технологии

- Kotlin 1.9.22
- Spring Boot 3.2.3
- Spring Security
- JWT Authentication
- PostgreSQL
- OpenAPI/Swagger
- Maven

## Требования

- JDK 21
- Maven
- PostgreSQL

## Установка и запуск

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
```

2. Настройте базу данных PostgreSQL и обновите параметры подключения в `application.yml`

3. Соберите проект:
```bash
mvn clean install
```

4. Запустите приложение:
```bash
mvn spring-boot:run
```

API будет доступно по адресу: http://localhost:8080
Swagger UI: http://localhost:8080/api/swagger-ui/index.html

## API Endpoints

### Аутентификация

#### Регистрация
```http
POST /auth/register
Content-Type: application/json

{
  "username": "user123",
  "password": "password123!",
  "email": "user@example.com"
}
```

Требования:
- Имя пользователя: 3-20 символов (буквы, цифры, дефис, подчеркивание)
- Пароль: минимум 8 символов, включая букву, цифру и спецсимвол
- Email: корректный формат email адреса

#### Вход
```http
POST /auth/login
Content-Type: application/json

{
  "credentials": "Basic dXNlcjEyMzpwYXNzd29yZDEyMyE="
}
```

Формат credentials: `Basic Base64(username:password)`

#### Обновление токена
```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Ответы API

#### Успешная регистрация/вход
```json
{
  "accessToken": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "Bearer eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiZGlyIn0...",
  "tokenType": "Bearer"
}
```

#### Успешное обновление токена
```json
{
  "accessToken": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

#### Ответ с ошибкой
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Описание ошибки"
}
```

## Безопасность

- Все пароли хешируются перед сохранением
- Используется JWT для аутентификации
- Реализована система refresh токенов
- Валидация всех входных данных
- Срок действия токенов:
  - Access Token: 5 минут
  - Refresh Token: 1 час

## Разработка

### Запуск тестов
```bash
mvn test
```

### Проверка стиля кода
```bash
mvn ktlint:check
```

### Документация API

Полная документация API доступна через Swagger UI после запуска приложения:
http://localhost:8080/api/swagger-ui/index.html

Основные разделы документации:
- Подробные описания всех эндпоинтов
- Примеры запросов и ответов
- Схемы валидации данных
- Коды ошибок и их описания
- Требования безопасности