package com.example;

import java.util.List;
import java.util.Scanner;

/**
 * Точка входа. Меню + парсинг ввода + вызовы сервиса.
 * Никакой обработки ошибок ввода — падаем на NumberFormatException.
 */
public class Main {

    // Статический Scanner — общий на всё приложение.
    private static final Scanner scanner = new Scanner(System.in);

    // Статический сервис — синглтон по-неволе, тестировать невозможно.
    private static final UserService service = new UserService();

    public static void main(String[] args) {
        // Инициализация БД — побочный эффект в main.
        DBUtil.init();

        while (true) {
            printMenu();
            String choice = scanner.nextLine(); // не trim, не обрабатываем пустоту

            if (choice.equals("1")) {
                register();
            } else if (choice.equals("2")) {
                find();
            } else if (choice.equals("3")) {
                delete();
            } else if (choice.equals("4")) {
                list();
            } else if (choice.equals("5")) {
                System.out.println("Bye!");
                // Соединение не закрываем — пусть JVM сама разбирается.
                System.exit(0);
            } else {
                System.out.println("Unknown option: " + choice);
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=== User Service (v0.0.1-antipattern) ===");
        System.out.println("1. Register user");
        System.out.println("2. Find user by email");
        System.out.println("3. Delete user");
        System.out.println("4. List all users");
        System.out.println("5. Exit");
        System.out.print("> ");
    }

    private static void register() {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter age: ");
        // NumberFormatException, если ввести "abc" — не обрабатываем.
        int age = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        service.registerUser(name, email, age, password);
    }

    private static void find() {
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        User u = service.findByEmail(email);
        if (u == null) {
            System.out.println("Not found");
        }
        // Если найден — сервис уже напечатал его с паролем.
    }

    private static void delete() {
        System.out.print("Enter user id: ");
        int id = Integer.parseInt(scanner.nextLine()); // снова без обработки
        service.deleteUser(id);
    }

    private static void list() {
        List<User> users = service.findAll();
        if (users.isEmpty()) {
            System.out.println("No users");
        }
        // Никакого форматирования — просто toString с паролями.
        for (User u : users) {
            System.out.println(u);
        }
    }
}