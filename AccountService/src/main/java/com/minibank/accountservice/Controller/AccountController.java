package com.minibank.accountservice.Controller;

import com.minibank.accountservice.DTO.AccountDto;
import com.minibank.accountservice.Entity.Account;
import com.minibank.accountservice.Services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // CREATE ACCOUNT
    @PostMapping
    public AccountDto createAccount(@RequestBody Account account) {
        return accountService.createAccount(account);
    }

    // GET ACCOUNT BY ID
    @GetMapping("/{id}")
    public AccountDto getAccountById(@PathVariable UUID id) {
        return accountService.getAccountById(id);
    }

    // GET ACCOUNT BY CUSTOMER ID
    @GetMapping("/customer/{customerId}")
    public AccountDto getAccountByCustomerId(@PathVariable UUID customerId) {
        return accountService.getAccountByCustomerId(customerId);
    }

    // GET ALL ACCOUNTS
    @GetMapping
    public List<AccountDto> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    // UPDATE ACCOUNT
    @PutMapping("/{id}")
    public AccountDto updateAccount(@PathVariable UUID id, @RequestBody Account updatedAccount) {
        return accountService.updateAccount(id, updatedAccount);
    }

    // DEPOSIT
    @PostMapping("/{id}/deposit")
    public AccountDto deposit(@PathVariable UUID id, @RequestParam double amount) {
        return accountService.deposit(id, amount);
    }

    // WITHDRAW
    @PostMapping("/{id}/withdraw")
    public AccountDto withdraw(@PathVariable UUID id, @RequestParam double amount) {
        return accountService.withdraw(id, amount);
    }

    // BLOCK ACCOUNT
    @PutMapping("/{id}/block")
    public AccountDto blockAccount(@PathVariable UUID id) {
        return accountService.blockAccount(id);
    }

    // ACTIVATE ACCOUNT
    @PutMapping("/{id}/activate")
    public AccountDto activateAccount(@PathVariable UUID id) {
        return accountService.activateAccount(id);
    }

    @GetMapping("/{id}/exists")
    public Boolean accountExists(@PathVariable UUID id) {
        return accountService.accountExists(id);
    }

    // DELETE ACCOUNT
    @DeleteMapping("/{id}")
    public String deleteAccount(@PathVariable UUID id) {
        accountService.deleteAccount(id);
        return "Account deleted successfully!";
    }
}
