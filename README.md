# UserService — Antipattern Playground 🧨

> Учебный проект, который **намеренно** нарушает все правила хорошего кода.
> Цель — показать антипаттерны в тестировании, чистом коде, SOLID,
> логировании, observability и безопасности, чтобы потом отрефакторить их
> и увидеть разницу «до/после».

---

## ⚠️ Дисклеймер

**Не используйте этот код в продакшене. Никогда. Ни при каких условиях.**

Это не пример «как надо». Это пример «как не надо» — сознательный,
контролируемый и хорошо задокументированный технический долг ради обучения.

---

## 📦 Стек

| Компонент | Версия | Комментарий |
|-----------|--------|-------------|
| Java      | 17     | toolchain в Gradle |
| Gradle    | 8.x    | сборка |
| H2        | 2.2.224 | БД, файловая, `./data/userdb` |
| Тесты     | —      | **отсутствуют намеренно** |
| Логгер    | —      | `System.out` и `printStackTrace()` |

---

## ▶️ Запуск

```bash
gradle run
```
Или:

```bash
gradle build
java -cp build/libs/user-service-0.0.1-antipattern.jar:$(gradle -q printClasspath) com.example.Main
```
Проще — просто gradle run.

🎬 Что умеет приложение
Консольное меню на голом Scanner:

text
=== User Service (v0.0.1-antipattern) ===
1. Register user
2. Find user by email
3. Delete user
4. List all users
5. Exit
>
Register — ввод name / email / age / password, INSERT в H2.

Find — поиск по email через конкатенацию SQL.

Delete — удаление по id без проверки прав.

List — вывод всех пользователей с паролями.

Exit — System.exit(0) без закрытия ресурсов.

🧨 Каталог антипаттернов
1. Тестирование
❌ Тестов нет вообще — ни unit, ни integration.

❌ Статические методы и поля (DBUtil.*, Main.service, Main.scanner).

❌ Скрытые зависимости: new User(), DriverManager.getConnection() внутри методов.

❌ Побочные эффекты в конструкторе User().

❌ Нет интерфейсов → мокать нечего.

❌ System.exit(0) в main → процесс не завершается корректно в тестах.

❌ Работа с реальной БД без изоляции.

2. Чистый код и SOLID
❌ God Class — UserService знает про валидацию, SQL и логирование.

❌ Магические числа и строки — 150, "admin", "jdbc:h2:...", "@".

❌ Длинные методы — registerUser с 7 уровнями вложенности.

❌ Дублирование — валидация в User и UserService, try/catch в каждом методе.

❌ Анемичная модель — User только с геттерами-полями, без поведения.

❌ public поля — инкапсуляция? Не слышали.

❌ null вместо Optional.

❌ Нарушение SRP — User умеет валидироваться и печататься.

❌ Нарушение OCP — if/else if в Main вместо полиморфизма.

❌ Нарушение DIP — UserService жёстко привязан к DBUtil.

❌ Primitive Obsession — email, password, phone как String.

3. Логирование и observability
❌ System.out.println вместо логгера.

❌ e.printStackTrace() в stderr.

❌ Логирование пароля в открытом виде.

❌ toString() с паролем → утечка при log.info(user).

❌ Нет уровней (INFO/WARN/ERROR), нет контекста, нет timestamp.

❌ catch (Exception e) {} — проглатывание исключений.

❌ Нет correlation / request id.

❌ Нет метрик и health-check.

❌ Логи идут в stdout без ротации и без файла.

4. Безопасность
❌ SQL-инъекции — везде конкатенация строк в SQL.

❌ Пароли в открытом виде — в БД, в логах, в toString().

❌ Хардкод креденшелов — sa / sa прямо в DBUtil.

❌ Нет валидации ввода — принимаем что угодно.

❌ Нет ограничения длины — можно прислать 10 МБ в name.

❌ hashCode() как «хеш пароля».

❌ Нет разграничения доступа — deleteUser удаляет кого угодно.

❌ Нет транзакций — частичные записи при ошибке.

❌ Сообщения об ошибках раскрывают внутренности — SQL печатается в консоль.

❌ Пароль как String — нельзя затереть, живёт в heap dump.

❌ NumberFormatException не обрабатывается → приложение падает.

🔬 Демонстрация проблем
SQL-инъекция
text
> 2
Enter email: ' OR '1'='1
[SQL] SELECT * FROM USERS WHERE EMAIL = '' OR '1'='1'
Found: User{id=1, name=Ivan, email=ivan@test.com, ..., password='qwerty'}
Утечка пароля в логах
text
registerUser called: name=Ivan, email=ivan@test.com, age=25, password=qwerty
...
Found: User{..., password='qwerty'}
Падение на некорректном вводе
text
> 1
Enter age: abc
Exception in thread "main" java.lang.NumberFormatException: For input string: "abc"
Невозможность написать тест
UserService зависит от статического DBUtil — не подменить.

Main использует System.exit и статический Scanner — не перехватить.

Нет ни одного интерфейса → mock-объекты создать не из чего.

🗂 Структура проекта
text
user-service/
├── build.gradle
├── settings.gradle
├── README.md
└── src/main/java/com/example/
    ├── Main.java          // God UI, вся логика ввода/вывода
    ├── User.java          // анемичная модель, public-поля, toString с паролем
    ├── UserService.java   // God Service: валидация + SQL + логи
    └── DBUtil.java        // статика, хардкод кредов, проглоченные исключения
Всего 4 класса — ровно столько, чтобы было больно читать, но не так много,
чтобы потеряться.

🎯 План рефакторинга
Каждый шаг — отдельный коммит, чтобы был виден diff «до/после».

Шаг	Что делаем	Какие антипаттерны закрываем
1	PreparedStatement + BCrypt	SQL-инъекции, plaintext-пароли
2	UserRepository (интерфейс) + H2UserRepository	DIP, тестируемость
3	SLF4J + Logback, убрать PII из логов	observability, утечки
4	JUnit 5 + Mockito + Testcontainers	тестирование
5	Value Objects + Bean Validation	primitive obsession, валидация
6	Слои controller / service / repository + DI	God Class, SRP, DIP
7	Метрики + health-check	observability
8	Транзакции + проверка прав	безопасность
📚 Чему учит этот проект
Антипаттерны выглядят «нормально», пока их не покажешь рядом с хорошим кодом.

Каждый антипаттерн тянет за собой другие: нет интерфейсов → нет тестов;
нет логгера → нет observability; конкатенация SQL → инъекции.

Рефакторинг — это не «переписать всё», а последовательность маленьких шагов,
каждый из которых закрывает одну конкретную проблему.

📄 Лицензия
Этот код не заслуживает лицензии. Используйте его как хотите,
но, пожалуйста, не копируйте в продакшен.

text
