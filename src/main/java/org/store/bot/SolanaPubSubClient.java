package org.store.bot;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;

@Slf4j
public class SolanaPubSubClient extends WebSocketClient {

    public SolanaPubSubClient(String uri) throws URISyntaxException {
        super(new URI(uri));
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        log.info("WebSocket подключён.");
        String subscriptionMessage = SolanaParser.accountSubscribe();

        // Отправляем подписку на учётную запись
        send(subscriptionMessage);
        log.info("Подписка отправлена: " + subscriptionMessage);
    }

    @Override
    public void onMessage(String message) {
        log.info("Получено сообщение: " + message);

        // Парсим ответ метода "accountNotification"
        SolanaParser.parseResponseAccountNotification(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.error("WebSocket закрыт. Код: " + code + ", причина: " + reason);
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                log.info("Попытка переподключения...");
                this.reconnectBlocking(); // Переподключение в новом потоке
                log.info("Переподключение успешно.");
            } catch (InterruptedException e) {
                log.error("Ошибка при переподключении: " + e.getMessage());
            }
        });
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("Ошибка WebSocket: " + ex.getMessage());
    }

    private void analyzeTransaction(ResponseLogSubscribe message) {
        // TODO: Логика обработки транзакции
        log.info("Анализ транзакции: " + message);
    }
}

