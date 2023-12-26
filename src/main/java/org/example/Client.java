package org.example;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;

/**
 * Клиентское приложение для общения с сервером.
 */
public class Client {

  /**
   * Точка входа в программу.
   *
   * @param args
   * @throws IOException если произошла ошибка ввода-вывода
   */
  public static void main(String[] args) throws IOException {
    // Устанавливаем соединение с сервером
    final Socket client = new Socket("localhost", Server.PORT);

    // Чтение сообщений от сервера в отдельном потоке
    new Thread(() -> {
      try (Scanner input = new Scanner(client.getInputStream())) {
        while (true) {
          System.out.println(input.nextLine());
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).start();

    // Отправка сообщений серверу в отдельном потоке
    new Thread(() -> {
      try (PrintWriter output = new PrintWriter(client.getOutputStream(), true)) {
        Scanner consoleScanner = new Scanner(System.in);
        while (true) {
          String consoleInput = consoleScanner.nextLine();

          if (consoleInput.startsWith("@")) {
            output.println(consoleInput);
          } else {
            // Формат сообщения: "цифра сообщение"
            output.println(Thread.currentThread().getId() + " " + consoleInput);
          }
          if (Objects.equals("q", consoleInput)) {
            client.close();
            break;
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).start();
  }
}



