package org.example;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import lombok.Getter;

/**
 * Серверное приложение для общения с клиентами.
 */
public class Server {


  public static final int PORT = 8181;
  private static long clientIdCounter = 1L;
  private static Map<Long, SocketWrapper> clients = new HashMap<>();

  /**
   * Точка входа в серверное приложение.
   *
   * @param args
   * @throws IOException если произошла ошибка ввода-вывода
   */
  public static void main(String[] args) throws IOException {
    try (ServerSocket server = new ServerSocket(PORT)) {
      System.out.println("Сервер запущен на порту " + PORT);
      while (true) {
        final Socket client = server.accept();
        final long clientId = clientIdCounter++;
        SocketWrapper wrapper = new SocketWrapper(clientId, client);
        System.out.println("Подключился новый клиент[" + wrapper + "]");
        clients.put(clientId, wrapper);
        new Thread(() -> {
          try (Scanner input = wrapper.getInput(); PrintWriter output = wrapper.getOutput()) {
            output.println("Подключение успешно. Список всех клиентов: " + clients);
            while (true) {
              String clientInput = input.nextLine();
              if (Objects.equals("q", clientInput)) {
                // todo разослать это сообщение всем остальным клиентам
                clients.remove(clientId);
                clients.values()
                    .forEach(it -> it.getOutput().println("Клиент[" + clientId + "] отключился"));
                break;
              }
              if (clientInput.startsWith("@")) {
                // Формат сообщения: "@цифра сообщение"
                long destinationId = Long.parseLong(clientInput.substring(1, 2));
                SocketWrapper destination = clients.get(destinationId);
                destination.getOutput().println("Личное сообщение от клиента[" + clientId + "]: " +
                    clientInput.substring(2));
              } else {
                // Отправляем сообщение всем клиентам, кроме отправителя
                clients.values()
                    .stream()
                    .filter(it -> it.getId() != clientId)
                    .forEach(it -> it.getOutput().println("Сообщение от клиента[" + clientId + "]: " + clientInput));
              }
              // формат сообщения: "цифра сообщение"
              long destinationId = Long.parseLong(clientInput.substring(1, 2));
              SocketWrapper destination = clients.get(destinationId);
              destination.getOutput().println(clientInput);
            }
          }
        }).start();
      }
    }
  }
}

@Getter
class SocketWrapper implements AutoCloseable {

  private final long id;
  private final Socket socket;
  private final Scanner input;
  private final PrintWriter output;

  /**
   * Конструктор класса SocketWrapper.
   *
   * @param id     уникальный идентификатор клиента
   * @param socket сокет, связанный с клиентом
   * @throws IOException если произошла ошибка ввода-вывода
   */
  SocketWrapper(long id, Socket socket) throws IOException {
    this.id = id;
    this.socket = socket;
    this.input = new Scanner(socket.getInputStream());
    this.output = new PrintWriter(socket.getOutputStream(), true);
  }

  /**
   * Метод для закрытия сокета.
   *
   * @throws Exception если произошла ошибка при закрытии сокета
   */
  @Override
  public void close() throws Exception {
    socket.close();
  }

  /**
   * Переопределенный метод toString для представления объекта в виде строки.
   *
   * @return строковое представление объекта
   */
  @Override
  public String toString() {
    return String.format("%s", socket.getInetAddress().toString());
  }
}

