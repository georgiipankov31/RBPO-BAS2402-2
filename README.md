# РБПО — вариант 22

Распределение мест в общежитии. Оформление по образцу первой лабораторной:
Java 21, Spring Boot 3.5.16, Maven Wrapper, пакет `ru.mtuci.rbpo.lab2`, DTO-record.

## Запуск

```bash
./mvnw clean package
java -jar target/rbpo-lab2-0.0.1-SNAPSHOT.jar
```

Или `./mvnw spring-boot:run`. Адрес: `http://localhost:1218`.
Тестовых файлов и тестовых зависимостей нет.

## Операции

Для каждого ресурса есть пять операций:

| Метод | Путь | Ответ |
| --- | --- | --- |
| GET | `/api/{resource}` | 200, JSON-массив |
| GET | `/api/{resource}/{id}` | 200, JSON-объект |
| POST | `/api/{resource}` | 201, созданный объект с id |
| PATCH | `/api/{resource}/{id}` | 200, обновлённый объект |
| DELETE | `/api/{resource}/{id}` | 204, без тела |

Поля для создания:

| Ресурс | Поля |
| --- | --- |
| `users` | `name` |
| `rooms` | `number` (строка), `capacity` (целое), `available` (boolean) |
| `housing-applications` | `userId`, `status` |
| `residence-contracts` | `applicationId`, `roomId`, `checkInDate`, `checkOutDate`, `status` |
| `room-transfer-requests` | `contractId`, `targetRoomId`, `status` |

`id` назначается сервером. Пользователь описан минимально: id и имя, поскольку
конкретные поля пользователя в задании не перечислены.
Статус — строка: задание не определяет допустимые значения и переходы между ними.
Изменение статуса само по себе не меняет другие сущности.

У заявления и заявки на переселение сервер задаёт `submittedAt` при создании.
При обновлении дата подачи сохраняется. В запросах это поле отсутствует.
Даты заселения и выезда задаются в формате `YYYY-MM-DD`.
`checkOutDate` может быть `null`, если дата выезда ещё не указана.

PATCH содержит только изменяемые поля. Пропущенные поля сохраняются.
Поля читаются явно через if; остальные поля PATCH игнорируются, в том числе id и submittedAt.
Связанные id должны существовать. Собственного формата ошибок нет:
используется стандартная обработка Spring Boot. Для отсутствующего id возвращается
404; проверка связанных id нужна, чтобы не сохранять недействительные ссылки.

Не добавлены правила уникальности, проверки занятости комнат, одобрение заявлений,
автоматическое переселение или ограничения статусов: видимое задание их не требует.

## Удаление связанных данных

Чтобы не оставлять недействительные ссылки:

- при удалении пользователя удаляются его заявления, их договоры и заявки на переселение;
- при удалении заявления удаляются его договоры и их заявки на переселение;
- при удалении комнаты удаляются договоры этой комнаты и связанные с ними заявки;
  также удаляются заявки, где эта комната указана как целевая;
- при удалении договора удаляются его заявки на переселение;
- при удалении заявки удаляется только она.

## JSON-хранилище

Файл `data/housing.json` создаётся при первой записи. Содержит счётчик id и коллекции
всех пяти сущностей. После перезапуска данные остаются.
Репозиторий читает JSON при запуске и записывает его после изменений.
Это простая учебная реализация для последовательных запросов, без защиты
от одновременных записей и сбоев во время записи файла.

Настройки в `src/main/resources/application.yml`:

| Ключ | Тип | Значение |
| --- | --- | --- |
| `spring.application.name` | строка | `1БАС24015` |
| `server.port` | целое | `1218` |
| `housing.storage-file` | строка | `./data/housing.json` |

Путь отсчитывается от папки запуска. Пример переопределения:

```bash
SERVER_PORT=8080 HOUSING_STORAGE_FILE=/tmp/housing.json java -jar target/rbpo-lab2-0.0.1-SNAPSHOT.jar
```

## Примеры

Для нового пустого хранилища:

```bash
curl -i -X POST http://localhost:1218/api/users -H 'Content-Type: application/json' \
  -d '{"name":"Георгий"}'
# id = 1

curl -i -X POST http://localhost:1218/api/rooms -H 'Content-Type: application/json' \
  -d '{"number":"101","capacity":2,"available":true}'
# id = 2

curl -i -X POST http://localhost:1218/api/housing-applications -H 'Content-Type: application/json' \
  -d '{"userId":1,"status":"подано"}'
# id = 3

curl -i -X POST http://localhost:1218/api/residence-contracts -H 'Content-Type: application/json' \
  -d '{"applicationId":3,"roomId":2,"checkInDate":"2026-10-09","checkOutDate":null,"status":"действует"}'
# id = 4

curl -i -X POST http://localhost:1218/api/rooms -H 'Content-Type: application/json' \
  -d '{"number":"102","capacity":1,"available":true}'
# id = 5

curl -i -X POST http://localhost:1218/api/room-transfer-requests -H 'Content-Type: application/json' \
  -d '{"contractId":4,"targetRoomId":5,"status":"подано"}'
# id = 6

curl -i -X PATCH http://localhost:1218/api/room-transfer-requests/6 -H 'Content-Type: application/json' \
  -d '{"status":"рассмотрено"}'

curl -i http://localhost:1218/api/rooms
curl -i http://localhost:1218/api/residence-contracts/4
curl -i -X DELETE http://localhost:1218/api/users/1
```

## Файлы

- `controller/` — HTTP-запросы, маппинг PATCH, ответы.
- `dto/` — входные данные.
- `model/` — сущности.
- `service/` — CRUD и удаление зависимых записей.
- `repository/` — чтение и запись JSON.

Собственных методов с `<T> T`, `Function` и `Supplier` нет. Методы используют
конкретные типы сущностей. `List<User>` и `Map<Long, User>` указывают типы элементов
стандартных коллекций Java.

