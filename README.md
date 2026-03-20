# Wallet Service

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.3-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Gradle](https://img.shields.io/badge/Gradle-Build-02303A)

REST API сервис для управления пользовательскими счетами и выполнения финансовых транзакций. Проект разработан как MVP банковского сервиса с акцентом на консистентность данных при переводах, обработку конкурентных запросов и чистоту архитектуры.

## Функциональность

- **Пользователи**: Регистрация и управление профилями.
- **Счета (Wallets)**: Открытие счетов, проверка баланса.
- **Транзакции**:
  - Переводы между счетами (атомарные операции).
  - История операций.
- **Безопасность**: Базовая аутентификация (Spring Security).
- **Миграции**: Версионирование схемы БД через Liquibase.

## Технологический стек

- **Core**: Java 21, Spring Boot 3
- **Database**: PostgreSQL (Driver), H2 (Tests)
- **Migration**: Liquibase
- **ORM**: Hibernate / Spring Data JPA
- **Utils**: Lombok, Spring Validation
- **Build**: Gradle

## Предварительная настройка

Для запуска проекта вам понадобится установленный **Java 21** и запущенный экземпляр **PostgreSQL**.

### Конфигурация окружения

Для запуска приложения необходимо задать следующие переменные окружения (Environment Variables):

```bash
export DB_URL
export DB_USERNAME
export DB_PASSWORD
```

## Запуск

### Локальный запуск
Используя Gradle wrapper:

```bash
./gradlew bootRun
```

### Сборка JAR
```bash
./gradlew clean build
```

### Запуск тестов
```bash
./gradlew test
```

## API Endpoints

Основные маршруты API (примерная структура):

| Method | Endpoint | Описание |
|---|---|---|
| `POST` | `/api/v1/users` | Создание пользователя |
| `POST` | `/api/v1/accounts` | Открытие счета |
| `GET` | `/api/v1/accounts/{id}` | Получение баланса |
| `POST` | `/api/v1/transfers` | Перевод средств |
| `GET` | `/api/v1/transfers/history` | История транзакций |

## Архитектура

Проект следует принципам **Feature-based** структуры пакетов (группировка по доменам, а не по слоям):

```
src/main/java/com/github/vylegzhaninn/wallet/
├── account/       # Логика работы со счетами
├── transfer/      # Ядро переводов (транзакции, блокировки)
├── user/          # Управление пользователями
├── config/        # Конфигурация Security, Swagger и др.
└── exception/     # Глобальная обработка ошибок
```

## Contributing

Буду рад любым предложениям по улучшению! Пожалуйста, создавайте Issue или Pull Request.

---
*Автор: [vylegzhaninn](https://github.com/vylegzhaninn)*
