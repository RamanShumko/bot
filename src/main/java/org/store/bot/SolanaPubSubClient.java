package org.store.bot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;

public class SolanaPubSubClient extends WebSocketClient {

    public SolanaPubSubClient(String uri) throws URISyntaxException {
        super(new URI(uri));
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("WebSocket подключён.");
        // Отправляем подписку на учётную запись
        String subscriptionMessage = """
                    {
                         "jsonrpc": "2.0",
                         "id": "1",
                         "method": "accountSubscribe",
                         "params": [
                            "CtBYeeLc9rCY3X4TwXvTwU79zgNrk3fmgDfpr99SxiKV",
                             {
                                "encoding": "jsonParsed",
                                "commitment": "finalized"
                             }
                         ]
                    }
                """;

        send(subscriptionMessage);
        System.out.println("Подписка отправлена: " + subscriptionMessage);
//        System.out.println("WebSocket подключен");
//
//        // Пример подписки на события аккаунта
//        String accountSubscription = """
//                    {
//                        "jsonrpc": "2.0",
//                        "id": 1,
//                        "method": "logsSubscribe",
//                        "params": [
//                            {
//                                "mentions": ["CtBYeeLc9rCY3X4TwXvTwU79zgNrk3fmgDfpr99SxiKV"]
//                            }
//                        ]
//                    }
//                """;
//        send(accountSubscription);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Получено сообщение: " + message);
        ObjectMapper objectMapper = new ObjectMapper();

//        JsonNode jsonNode = null;
//        try {
//            jsonNode = objectMapper.readTree(message);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//
//        // Проверяем уведомления о логах транзакций
//        if (jsonNode.has("method") && "logsNotification".equals(jsonNode.get("method").asText())) {
//            JsonNode params = jsonNode.get("params").get("result");
//            String signature = params.get("value").get("signature").asText();
//            boolean isSuccess = params.get("value").get("err").isNull();
//            ArrayNode logs = (ArrayNode) params.get("value").get("logs");
//
//            // Анализируем уведомление
//            System.out.println("Анализ транзакции: ResponseLogSubscribe(signature=" + signature +
//                    ", isSuccess=" + isSuccess + ", logs=" + logs + ")");
//        }
//
//        ResponseLogSubscribe responseLogSubscribe = SolanaParser.responseLogSubscribe(message);
//        analyzeTransaction(responseLogSubscribe);

        try {
            JsonNode jsonNode_1 = objectMapper.readTree(message);

            if (jsonNode_1.has("method") && "accountNotification".equals(jsonNode_1.get("method").asText())) {
                JsonNode value = jsonNode_1.get("params").get("result").get("value");
                long lamports = value.get("lamports").asLong();
                String owner = value.get("owner").asText();
                boolean executable = value.get("executable").asBoolean();

                System.out.println("Обновление учетной записи:");
                System.out.println("Lamports: " + lamports);
                System.out.println("Owner: " + owner);
                System.out.println("Executable: " + executable);

                JsonNode data = value.get("data");
                if (data.has("program")) {
                    System.out.println("Программа: " + data.get("program").asText());
                }

                if (data.has("parsed")) {
                    JsonNode parsedInfo = data.get("parsed").get("info");
                    String authority = parsedInfo.get("authority").asText();
                    String blockhash = parsedInfo.get("blockhash").asText();
                    long fee = parsedInfo.get("feeCalculator").get("lamportsPerSignature").asLong();

                    System.out.println("Authority: " + authority);
                    System.out.println("Blockhash: " + blockhash);
                    System.out.println("Fee per Signature: " + fee);
                }
            } else {
                System.out.println("Неизвестное сообщение: " + message);
            }
        } catch (Exception e) {
            System.err.println("Ошибка обработки сообщения: " + e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket закрыт. Код: " + code + ", причина: " + reason);
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                System.out.println("Попытка переподключения...");
                this.reconnectBlocking(); // Переподключение в новом потоке
                System.out.println("Переподключение успешно.");
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Ошибка при переподключении: " + e.getMessage());
            }
        });
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("Ошибка WebSocket: " + ex.getMessage());
    }

    private void analyzeTransaction(ResponseLogSubscribe message) {
        // TODO: Логика обработки транзакции
        System.out.println("Анализ транзакции: " + message);
    }
}

