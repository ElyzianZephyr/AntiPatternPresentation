package com.example;

/**
 * Анемичная модель. Никакой валидации, никаких value-объектов.
 * Поля public — инкапсуляция? Не слышали.
 * toString() светит пароль — привет, observability и security.
 */
public class User {

    // public поля — нарушение инкапсуляции.
    public int id;
    public String name;
    public String email;
    public int age;
    public String password; // пароль как String, в открытом виде, не затирается.

    // Пустой конструктор — на всякий случай, "чтобы было".
    public User() {
        // Побочный эффект: логируем создание каждого пользователя.
        System.out.println("User created: " + System.currentTimeMillis());
    }

    public User(int id, String name, String email, int age, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.password = password;
    }

    // toString() с паролем — утечка PII в логи.
    @Override
    public String toString() {
        return "User{id=" + id + ", name=" + name + ", email=" + email +
                ", age=" + age + ", password='" + password + "'}";
    }

    // Магическое число 150 прямо в модели.
    public boolean isValid() {
        return name != null && name.length() > 0
                && email != null && email.contains("@")
                && age > 0 && age < 150
                && password != null && password.length() > 0;
    }
}