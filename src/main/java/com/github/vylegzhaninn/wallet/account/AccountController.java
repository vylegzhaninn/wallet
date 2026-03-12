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
        return accountService.createAccount(request);
    }
}
