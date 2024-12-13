package org.store.bot;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bot")
@AllArgsConstructor
public class BotController {
    private final WalletService walletService;
    private final BlockchainSubscription blockchainSubscription;

    @GetMapping
    public String getBalance(@RequestParam String walletAddress) {
        return walletService.getWalletBalance(walletAddress);
    }

    @PutMapping
    public String transfer(@RequestParam String fromWalletAddress,
                           @RequestParam String toWalletAddress,
                           @RequestParam int amount) {
        return walletService.transfer(fromWalletAddress, toWalletAddress, amount);
    }

    @PutMapping("/token")
    public void transferToken() {
//        return walletService.transferToken(fromTokenAddress, toTokenAddress, amount);
//        walletService.buyToken();
        walletService.transferToken();
    }

    @PutMapping("/token/transferOwnWallet")
    public void transferTokenBetweenOwnWallet() {
        walletService.transferTokenBetweenOwnWallet();
    }

    @PutMapping("/token/transferSomeoneElse")
    public void transferTokenBySomeoneElse() {
        walletService.transferTokenBySomeoneElse();
    }

    @PostMapping("/token/mint")
    public void mintToken() {
        walletService.mintToken();
    }

    @GetMapping("/token")
    public Double getTokenBalance(@RequestParam String tokenAddress) {
        return walletService.getTokenBalance(tokenAddress);
    }

    @PutMapping("/subscribe")
    public void subscribeAccount(@RequestParam String walletAddress) {
        blockchainSubscription.accountSubscribe(walletAddress);
    }
}
