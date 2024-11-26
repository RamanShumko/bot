package org.store.bot;

import org.p2p.solanaj.ws.SubscriptionWebSocketClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class PubSubRunner implements CommandLineRunner {

    SubscriptionWebSocketClient socketClient;

    @Override
    public void run(String... args) throws URISyntaxException {
    }

    @Bean
    public SubscriptionWebSocketClient getWebSocketClient() {
        return socketClient;
    }
}

