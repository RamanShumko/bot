package org.store.bot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class SolanaParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static public void parseResponseAccountNotification(String message) {
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

    static public ResponseLogSubscribe parseResponseLogSubscribe(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);

            // Переходим к нужным узлам
            JsonNode resultNode = rootNode.path("params").path("result").path("value");

            // Извлекаем данные
            String signature = resultNode.path("signature").asText();
            boolean isSuccess = resultNode.path("err").isNull(); // Если err == null, транзакция успешна
            List<String> logs = new ArrayList<>();

            // Возвращаем объект
            return new ResponseLogSubscribe(signature, isSuccess, logs);

        } catch (Exception e) {
            System.err.println("Ошибка при разборе ответа уведомления: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
