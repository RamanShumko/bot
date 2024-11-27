package org.store.bot;

import com.mmorrell.openbook.manager.OpenBookManager;
import com.mmorrell.openbook.model.OpenBookMarket;
import com.mmorrell.serum.model.*;
import com.mmorrell.serum.program.SerumProgram;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.AssociatedTokenProgram;
import org.p2p.solanaj.programs.Program;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;
import org.p2p.solanaj.rpc.types.TokenResultObjects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
    private final SolanaClient solanaClient;
    @Value("${wallet.keypair.path}")
    private String fileKey;

    // Перевод токена между двумя токен-счётами
    public void buyToken() {
        // Ваш приватный ключ (отправитель)
        Account senderAccount = new Account(getSecretKeyWallet());
        // Адрес токена и его MINT
        String tokenMintAddress = "4zMMC9srt5Ri5X14GAgXhaHii3GnPAEERYPJgZJDncDU";

        // Получение данных о токен-счетах двух кошельков, по mint адресу токена
        PublicKey sellerTokenAccount;
        PublicKey buyerTokenAccount;
        Transaction transaction_0 = new Transaction();

        // Динамическое создание токен-счёта для кошелька
        TransactionInstruction transactionInstruction = AssociatedTokenProgram.create(senderAccount.getPublicKey(), senderAccount.getPublicKey(), PublicKey.valueOf(tokenMintAddress));
        transaction_0.addInstruction(transactionInstruction);
        try {
            solanaClient.getRpcClient().getApi().sendTransaction(transaction_0, senderAccount);
        } catch (RpcException e) {
            log.warn("Token account for this wallet already exists: {}", e.getMessage());
        }

        // Получение токен-счётов у кошельков
        try {
            buyerTokenAccount = solanaClient.getRpcClient().getApi().getTokenAccountsByOwner(new PublicKey("CtBYeeLc9rCY3X4TwXvTwU79zgNrk3fmgDfpr99SxiKV"), new PublicKey(tokenMintAddress));
            sellerTokenAccount = solanaClient.getRpcClient().getApi().getTokenAccountsByOwner(new PublicKey("GrNg1XM2ctzeE2mXxXCfhcTUbejM8Z4z4wNVTy2FjMEz"), new PublicKey(tokenMintAddress));
            log.info(buyerTokenAccount + " " + sellerTokenAccount);
        } catch (RpcException e) {
            throw new RuntimeException(e);
        }

        final Market solUsdcMarket = new MarketBuilder()
                .setClient(solanaClient.getRpcClient())
                .setPublicKey(new PublicKey("C3YPL3kYCSYKsmHcHrPWx1632GUXGqi2yMXJbfeCc57q"))
                .setRetrieveOrderBooks(true)
                .build();

        OpenBookManager openBookManager = new OpenBookManager(solanaClient.getRpcClient());

//        OpenBookMarket solUsdcMarket = openBookManager.getMarket(
//                PublicKey.valueOf("C3YPL3kYCSYKsmHcHrPWx1632GUXGqi2yMXJbfeCc57q"),
//                false, // Не загружать рынок заново, если он уже кэширован
//                true   // Загружать ордербуки
//        ).get();
//
//        log.info("Bids: {}", solUsdcMarket.getBidOrders()); // Заявки на покупку
//        log.info("Asks: {}", solUsdcMarket.getAskOrders()); // Заявки на продажу

        // 3. Создайте лимитный ордер
        Order order = Order.builder()
                .price(1000000000)  // цена, симулируем очень высокую цену, чтобы ордер был выполнен сразу, а не ждал указанную желаемую цену за токен
                .quantity(25)  // Количество
                .owner(new PublicKey("CtBYeeLc9rCY3X4TwXvTwU79zgNrk3fmgDfpr99SxiKV"))
                .orderTypeLayout(OrderTypeLayout.LIMIT)  // Укажите правильный тип
                .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)  // Укажите правильное поведение
                .buy(true)
                .build();



        OpenOrdersAccount openOrdersAccount = SerumUtils.findOpenOrdersAccountForOwner(solanaClient.getRpcClient(), solUsdcMarket.getOwnAddress(), senderAccount.getPublicKey());

        // Отправка ордера на рынок
        TransactionInstruction placeOrderInstruction = SerumProgram.placeOrder(
                senderAccount, buyerTokenAccount, openOrdersAccount.getOwnPubkey(), new Market(), order, null);

        // Подготовьте транзакцию для перевода токенов
        Transaction transaction = new Transaction();
        transaction.addInstruction(placeOrderInstruction);

        // Отправьте транзакцию в сеть
        try {
            String signature = solanaClient.getRpcClient().getApi().sendTransaction(transaction, senderAccount);
            log.info("Transaction Signature: " + signature);
            log.info("Transaction status: {}", solanaClient.getRpcClient().getApi().getTransaction(signature));
        } catch (RpcException e) {
            throw new RuntimeException(e);
        }


    }

    public Double getTokenBalance(String token) {
        try {

            TokenResultObjects.TokenAmountInfo tokenResult = solanaClient.getRpcClient().getApi().getTokenAccountBalance(new PublicKey(token));
            return tokenResult.getUiAmount();
        } catch (RpcException e) {
            throw new RuntimeException(e);
        }

    }

    public String getWalletBalance(String walletAddress) {
        try {
            long balance = solanaClient.getRpcClient().getApi().getBalance(new PublicKey(walletAddress));
            return String.format("Баланс кошелька %s: %s", walletAddress, balance);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения баланса: " + e.getMessage());
        }
    }

    // Перевод между двумя кошельками
    public String transfer(String fromWalletAddress, String toWalletAddress, int amount) {
        PublicKey fromPublicKey = new PublicKey(fromWalletAddress);
        PublicKey toPublickKey = new PublicKey(toWalletAddress);
        int lamports = amount;

        Account signer = new Account(getSecretKeyWallet());

        Transaction transaction = new Transaction();
        transaction.addInstruction(SystemProgram.transfer(fromPublicKey, toPublickKey, lamports));

        String signature;

        try {
            signature = solanaClient.getRpcClient().getApi().sendTransaction(transaction, signer);
        } catch (RpcException e) {
            throw new RuntimeException(e);
        }

        return String.format("Successfully transferred %s to %s, result: %s", fromWalletAddress, toWalletAddress, signature);
    }

    // Получение приватного ключа
    private byte[] getSecretKeyWallet() {
        String[] intStrings = fileKey.replaceAll("[^0-9,\\-]", "").split(",");

        byte[] secretKey = new byte[intStrings.length];

        for (int i = 0; i < intStrings.length; i++) {
            int intValue = Integer.parseInt(intStrings[i].trim());
            secretKey[i] = (byte) intValue;
        }

        return secretKey;
    }
}
