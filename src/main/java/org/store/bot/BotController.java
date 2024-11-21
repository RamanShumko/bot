package org.store.bot;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bot")
@AllArgsConstructor
public class BotController {
    private final WalletService walletService;

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
}
