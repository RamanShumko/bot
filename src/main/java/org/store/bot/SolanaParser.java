package org.store.bot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SolanaParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static public ResponseLogSubscribe responseLogSubscribe(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);

            // Переходим к нужным узлам
            JsonNode resultNode = rootNode.path("params").path("result").path("value");

            // Извлекаем данные
            String signature = resultNode.path("signature").asText();
            boolean isSuccess = resultNode.path("err").isNull(); // Если err == null, транзакция успешна
            List<String> logs = new ArrayList<>();

            // Логи
            JsonNode logsNode = resultNode.path("logs");
            if (logsNode.isArray()) {
                Iterator<JsonNode> iterator = logsNode.elements();
                while (iterator.hasNext()) {
                    logs.add(iterator.next().asText());
                }
            }

            // Возвращаем объект
            return new ResponseLogSubscribe(signature, isSuccess, logs);

        } catch (Exception e) {
            System.err.println("Ошибка при разборе ответа уведомления: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
