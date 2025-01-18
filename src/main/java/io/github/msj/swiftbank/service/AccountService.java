package io.github.msj.swiftbank.service;

import io.github.msj.swiftbank.entity.Account;
import io.github.msj.swiftbank.entity.Transaction;
import io.github.msj.swiftbank.repository.AccountRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
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

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Account findById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada."));
    }

    public List<Account> getAccountsByUser(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public List<Account> getAccountsExcludingUser(Long userId) {
        return accountRepository.findAccountsExcludingUser(userId);
    }

    public Long countAccounts() {
        return accountRepository.count();
    }

    public BigDecimal calculateTotalBalance() {
        return accountRepository.calculateTotalBalance();
    }

    public BigDecimal calculateTotalBalanceByUser(Long userId) {
        return accountRepository.calculateTotalBalanceByUser(userId);
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

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(amount.negate());
        transaction.setTransactionType("DEBIT");
        transactionService.save(transaction);
    }

    @Transactional
    public void transferBetweenAccounts(Long sourceAccountId, Long targetAccountId, BigDecimal amount) {
        try {
            validateTransfer(sourceAccountId, targetAccountId, amount);

            Account sourceAccount = getAccountById(sourceAccountId, "Conta de origem não encontrada.");
            Account targetAccount = getAccountById(targetAccountId, "Conta de destino não encontrada.");

            validateSufficientBalance(sourceAccount, amount);

            performBalanceUpdate(sourceAccount, targetAccount, amount);
            recordTransactions(sourceAccount, targetAccount, amount);

        } catch (OptimisticLockException e) {
            throw new IllegalStateException("Conflito detectado ao tentar realizar a transferência. Tente novamente.", e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Erro inesperado ao processar a transferência. Entre em contato com o suporte.", e);
        }
    }

    private Account getAccountById(Long accountId, String errorMessage) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException(errorMessage));
    }


    private void validateTransfer(Long sourceAccountId, Long targetAccountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transferência deve ser maior que zero.");
        }

        if (sourceAccountId.equals(targetAccountId)) {
            throw new IllegalArgumentException("A conta de origem e destino devem ser diferentes.");
        }
    }

    private void validateSufficientBalance(Account sourceAccount, BigDecimal amount) {
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente na conta de origem.");
        }
    }

    private void performBalanceUpdate(Account sourceAccount, Account targetAccount, BigDecimal amount) {
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        targetAccount.setBalance(targetAccount.getBalance().add(amount));

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);
    }

    private void recordTransactions(Account sourceAccount, Account targetAccount, BigDecimal amount) {
        Transaction debitTransaction = new Transaction();
        debitTransaction.setAccount(sourceAccount);
        debitTransaction.setAmount(amount.negate());
        debitTransaction.setTransactionType("TRANSFER_OUT");
        transactionService.save(debitTransaction);

        Transaction creditTransaction = new Transaction();
        creditTransaction.setAccount(targetAccount);
        creditTransaction.setAmount(amount);
        creditTransaction.setTransactionType("TRANSFER_IN");
        transactionService.save(creditTransaction);
    }




}
