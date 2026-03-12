package com.github.vylegzhaninn.wallet.account;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/create")
    public Account createAccount(@RequestBody AccountDto request){
        return accountService.create(request);
    }

    @PatchMapping("/deposit")
    public Account depositAccount(@RequestBody AccountDto request){
        return accountService.deposit(request);
    }
}
