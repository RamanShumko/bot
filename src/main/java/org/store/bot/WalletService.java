package org.store.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.TokenResultObjects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        Account senderAccount = new Account(getSecretKeyWalletFromFile());

        // Адрес токена и его MINT
        String tokenMintAddress = "4zMMC9srt5Ri5X14GAgXhaHii3GnPAEERYPJgZJDncDU";

        // 4. Получение данных о токен счетах двух кошельков, по mint адресу токена
        PublicKey sellerTokenAccount;
        PublicKey buyerTokenAccount;
        try {
            buyerTokenAccount = solanaClient.getRpcClient().getApi().getTokenAccountsByOwner(new PublicKey("CtBYeeLc9rCY3X4TwXvTwU79zgNrk3fmgDfpr99SxiKV"), new PublicKey(tokenMintAddress));
            sellerTokenAccount = solanaClient.getRpcClient().getApi().getTokenAccountsByOwner(new PublicKey("GrNg1XM2ctzeE2mXxXCfhcTUbejM8Z4z4wNVTy2FjMEz"), new PublicKey(tokenMintAddress));
            log.info(buyerTokenAccount + " " + sellerTokenAccount);
        } catch (RpcException e) {
            throw new RuntimeException(e);
        }

        // 5. Подготовьте транзакцию для перевода токенов
        Transaction transaction = new Transaction();
        transaction.addInstruction(
                TokenProgram.transfer(
                        sellerTokenAccount, // От кого (токен-счёт)
                        buyerTokenAccount, // Кому (токен-счёт)
                        20000,                            // Количество токенов в лампортах
                        new PublicKey("GrNg1XM2ctzeE2mXxXCfhcTUbejM8Z4z4wNVTy2FjMEz")   // Отправитель (ваш кошелёк)
                )
        );

        // 7. Отправьте транзакцию в сеть
        try {
            String signature = solanaClient.getRpcClient().getApi().sendTransaction(transaction, senderAccount);
            log.info("Transaction Signature: " + signature);
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

        Account signer = new Account(getSecretKeyWalletFromFile());

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

    // Получение приватного ключа из локального файла
    private byte[] getSecretKeyWalletFromFile() {
        String content;
        try {
            content = Files.readString(Path.of(fileKey));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] intStrings = content.replaceAll("[^0-9,\\-]", "").split(",");

        byte[] secretKey = new byte[intStrings.length];

        for (int i = 0; i < intStrings.length; i++) {
            int intValue = Integer.parseInt(intStrings[i].trim());
            secretKey[i] = (byte) intValue;
        }

        return secretKey;
    }
}
