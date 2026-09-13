package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * God Service: валидация, SQL, логирование, бизнес-логика — всё здесь.
 * SQL через конкатенацию → SQL-инъекции.
 */
public class UserService {

    // Магические константы вместо enum.
    private static final String ADMIN = "admin";
    private static final String DEFAULT_ROLE = "user";

    /**
     * Регистрация пользователя.
     * 7 уровней вложенности, дублирование try/catch, магические строки.
     */
    public boolean registerUser(String name, String email, int age, String password) {
        System.out.println("registerUser called: name=" + name + ", email=" + email +
                ", age=" + age + ", password=" + password); // логируем пароль

        // Валидация размазана: часть здесь, часть в User.isValid().
        if (name == null || name.isEmpty()) {
            System.out.println("Error: name is empty");
            return false;
        }
        if (email == null || !email.contains("@")) {
            System.out.println("Error: email invalid");
            return false;
        }
        if (age < 0 || age > 150) { // магическое число, дублирует User
            System.out.println("Error: age invalid");
            return false;
        }
        if (password == null || password.length() < 3) {
            System.out.println("Error: password too short");
            return false;
        }

        User u = new User();
        u.name = name;
        u.email = email;
        u.age = age;
        u.password = password; // открытый текст

        if (!u.isValid()) {
            System.out.println("Error: user invalid");
            return false;
        }

        try {
            Connection conn = DBUtil.getConnection();

            // Проверка на дубликат через SELECT + конкатенация.
            String checkSql = "SELECT COUNT(*) FROM USERS WHERE EMAIL = '" + email + "'";
            System.out.println("[SQL] " + checkSql);
            Statement checkStmt = conn.createStatement();
            ResultSet rs = checkStmt.executeQuery(checkSql);
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Error: user already exists");
                return false;
            }

            // Вставка через конкатенацию — SQL-инъекция.
            String sql = "INSERT INTO USERS (NAME, EMAIL, AGE, PASSWORD) VALUES ('" +
                    name + "', '" + email + "', " + age + ", '" + password + "')";
            System.out.println("[SQL] " + sql);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);

            System.out.println("User saved!");
            return true;

        } catch (SQLException e) {
            // Печатаем всё, включая SQL — раскрытие внутренностей.
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            // Дублирующий catch, который ловит всё остальное.
            System.out.println("Unknown error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Поиск по email. Тоже конкатенация → инъекция.
     */
    public User findByEmail(String email) {
        System.out.println("findByEmail: " + email);
        try {
            Connection conn = DBUtil.getConnection();
            // Уязвимо к ' OR '1'='1
            String sql = "SELECT * FROM USERS WHERE EMAIL = '" + email + "'";
            System.out.println("[SQL] " + sql);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                User u = new User(
                        rs.getInt("ID"),
                        rs.getString("NAME"),
                        rs.getString("EMAIL"),
                        rs.getInt("AGE"),
                        rs.getString("PASSWORD") // достаем пароль из БД и светим
                );
                System.out.println("Found: " + u); // toString с паролем
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // null вместо Optional — классика.
    }

    /**
     * Удаление без проверки прав, без подтверждения, без транзакции.
     */
    public boolean deleteUser(int id) {
        System.out.println("deleteUser id=" + id);
        try {
            Connection conn = DBUtil.getConnection();
            String sql = "DELETE FROM USERS WHERE ID = " + id; // даже тут конкатенация
            System.out.println("[SQL] " + sql);
            Statement stmt = conn.createStatement();
            int rows = stmt.executeUpdate(sql);
            System.out.println("Deleted rows: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Список всех пользователей.
     * N+1 не тут, но зато всё грузим в память и светим пароли.
     */
    public List<User> findAll() {
        List<User> result = new ArrayList<>();
        try {
            Connection conn = DBUtil.getConnection();
            String sql = "SELECT * FROM USERS";
            System.out.println("[SQL] " + sql);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                result.add(new User(
                        rs.getInt("ID"),
                        rs.getString("NAME"),
                        rs.getString("EMAIL"),
                        rs.getInt("AGE"),
                        rs.getString("PASSWORD")
                ));
            }
            System.out.println("Loaded " + result.size() + " users");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * "Хеширование" пароля через hashCode — верх security-антипаттерна.
     * Метод не используется, но лежит как напоминание.
     */
    public String hashPassword(String password) {
        return String.valueOf(password.hashCode());
    }
}