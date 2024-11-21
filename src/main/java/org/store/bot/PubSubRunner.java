package org.store.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PubSubRunner implements CommandLineRunner {
    @Value("${webSocket.url}")
    private String webSocketUrl;

    @Override
    public void run(String... args) throws Exception {
        SolanaPubSubClient client = new SolanaPubSubClient(webSocketUrl);

        client.connectBlocking(); // Блокируем до подключения
    }
}

