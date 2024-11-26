package org.store.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.p2p.solanaj.ws.SubscriptionWebSocketClient;
import org.p2p.solanaj.ws.listeners.NotificationEventListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@RequiredArgsConstructor
@Component
@Slf4j
public class BlockchainSubscription {
    @Value("${webSocket.url}")
    private String webSocketUrl;

    public void accountSubscribe(String account) {
        SubscriptionWebSocketClient socketClient;
        try {
            socketClient = new SubscriptionWebSocketClient(new URI(webSocketUrl));
            socketClient.connectBlocking();
        } catch (URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        NotificationEventListener notificationEventListener = data -> log.info("Notification event received: {}", data);

        socketClient.accountSubscribe(account, notificationEventListener);
        log.info("Subscribed to account {}", account);
        log.info("Subscribed to account {}", socketClient.getSubscriptionId(account));
    }
}
