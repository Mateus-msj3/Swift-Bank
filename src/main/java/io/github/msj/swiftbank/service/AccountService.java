package io.github.msj.swiftbank.service;

import io.github.msj.swiftbank.entity.Account;
import io.github.msj.swiftbank.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class AccountService {


    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(String ownerName, BigDecimal initialBalance) {
        if (Objects.isNull(initialBalance) || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O saldo inicial deve ser positivo ou zero.");
        }
        Account account = new Account();
        account.setOwnerName(ownerName);
        account.setBalance(initialBalance);
        return accountRepository.save(account);
    }
}
