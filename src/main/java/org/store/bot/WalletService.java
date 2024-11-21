package org.store.bot;

import lombok.RequiredArgsConstructor;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.rpc.RpcException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final SolanaClient solanaClient;
    @Value("${wallet.keypair.path}")
    private String fileKey;

    public String getWalletBalance(String walletAddress) {
        try {
            long balance = solanaClient.getRpcClient().getApi().getBalance(new PublicKey(walletAddress));
            return String.format("Баланс кошелька %s: %s", walletAddress, balance);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения баланса: " + e.getMessage());
        }
    }

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
