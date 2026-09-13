package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Утилитный класс с захардкоженными кредами.
 * Все методы статические — замокать невозможно.
 * Исключения проглатываются или печатаются в stderr.
 */
public class DBUtil {

    // Хардкод креденшелов и URL.
    private static final String URL = "jdbc:h2:./data/userdb;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "sa"; // пароль "sa" — классика.

    // Единственное соединение на всё приложение — антипаттерн shared connection.
    private static Connection connection;

    // Приватный конструктор — но класс всё равно "утилитный".
    private DBUtil() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("DB connected: " + URL);
            }
        } catch (SQLException e) {
            // Печатаем стектрейс — раскрываем внутренности, не логируем структурированно.
            e.printStackTrace();
            // И возвращаем null — пусть вызывающий код падает с NPE.
        }
        return connection;
    }

    // Инициализация схемы — тоже хардкод.
    public static void init() {
        try {
            var stmt = getConnection().createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS USERS (" +
                    "ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "NAME VARCHAR(255), " +
                    "EMAIL VARCHAR(255), " +
                    "AGE INT, " +
                    "PASSWORD VARCHAR(255)" +
                    ")");
            System.out.println("DB initialized");
        } catch (Exception e) {
            // Проглатываем — observability? Нет, не слышали.
        }
    }
}