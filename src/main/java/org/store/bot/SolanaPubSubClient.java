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
        // Отправляем подписку на учётную запись
        String subscriptionMessage = """
                    {
                         "jsonrpc": "2.0",
                         "id": "1",
                         "method": "accountSubscribe",
                         "params": [
                            "GVdCTmkVF7qXE8wPe9vWandwMevsw4K1yqwV9F9R2yn3",
                             {
                                "encoding": "jsonParsed",
                                "commitment": "finalized"
                             }
                         ]
                    }
                """;

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
                e.printStackTrace();
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

