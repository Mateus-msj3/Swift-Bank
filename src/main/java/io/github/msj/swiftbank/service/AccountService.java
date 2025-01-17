package io.github.msj.swiftbank.service;

import io.github.msj.swiftbank.entity.Account;
import io.github.msj.swiftbank.entity.Transaction;
import io.github.msj.swiftbank.entity.User;
import io.github.msj.swiftbank.repository.AccountRepository;
import io.github.msj.swiftbank.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AccountService {


    private final AccountRepository accountRepository;

    private final TransactionService transactionService;

    private final UserService userService;

    public AccountService(AccountRepository accountRepository, TransactionService transactionService, UserService userService) {
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
        this.userService = userService;
    }

    public List<Account> getAccountsByUser(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Account createAccount(String ownerName, BigDecimal initialBalance, Long userId) {
        if (Objects.isNull(initialBalance) || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O saldo inicial deve ser positivo ou zero.");
        }

        Account account = new Account();
        account.setOwnerName(ownerName);
        account.setBalance(initialBalance);
        account.setUser(userService.findById(userId));

        return accountRepository.save(account);
    }

    public void creditAccount(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do crédito deve ser maior que zero.");
        }

        Optional<Account> optionalAccount = accountRepository.findById(accountId);
        if (!optionalAccount.isPresent()) {
            throw new IllegalArgumentException("Conta não encontrada.");
        }

        Account account = optionalAccount.get();
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(amount);
        transaction.setTransactionType("CREDIT");
        transactionService.save(transaction);
    }

    public void debitAccount(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do débito deve ser maior que zero.");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada."));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para realizar o débito.");
        }

        // Atualizar o saldo da conta
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        // Registrar a transação
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(amount.negate()); // Débito é um valor negativo
        transaction.setTransactionType("DEBIT");
        transactionService.save(transaction);
    }
}
