package io.github.msj.swiftbank.service;

import io.github.msj.swiftbank.entity.Account;
import io.github.msj.swiftbank.entity.Transaction;
import io.github.msj.swiftbank.entity.User;
import io.github.msj.swiftbank.repository.AccountRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    AccountRepository accountRepository;

    @Mock
    TransactionService transactionService;

    @Mock
    UserService userService;

    @InjectMocks
    AccountService accountService;

    @Nested
    class FindAccounts {

        @Test
        void shouldFindAllAccounts() {
            Account mockAccount = new Account();
            mockAccount.setBalance(new BigDecimal("1000"));
            mockAccount.setId(1L);
            mockAccount.setUser(new User());

            Account mockAccount2 = new Account();
            mockAccount.setBalance(new BigDecimal("2000"));
            mockAccount.setId(1L);
            mockAccount.setUser(new User());

            List<Account> mockAccounts = Arrays.asList(mockAccount, mockAccount2);

            when(accountRepository.findAll()).thenReturn(mockAccounts);

            List<Account> accounts = accountService.findAll();

            assertEquals(2, accounts.size());
            verify(accountRepository, times(1)).findAll();
        }

        @Test
        void shouldFindAccountById() {
            Account mockAccount = new Account();
            mockAccount.setBalance(new BigDecimal("1000"));
            mockAccount.setId(1L);
            mockAccount.setUser(new User());

            when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccount));

            Account account = accountService.findById(1L);

            assertNotNull(account);
            assertEquals(1L, account.getId());
            verify(accountRepository, times(1)).findById(1L);
        }

        @Test
        void shouldThrowExceptionWhenAccountNotFoundById() {
            when(accountRepository.findById(1L)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.findById(1L)
            );
            assertEquals("Conta não encontrada.", exception.getMessage());
            verify(accountRepository, times(1)).findById(1L);
        }

        @Test
        void shouldGetAccountsByUser() {
            Account mockAccount = new Account();
            mockAccount.setBalance(new BigDecimal("1000"));
            mockAccount.setId(1L);
            mockAccount.setUser(new User());


            Account mockAccount2 = new Account();
            mockAccount.setBalance(new BigDecimal("2000"));
            mockAccount.setId(1L);
            mockAccount.setUser(new User());

            List<Account> mockAccounts = Arrays.asList(mockAccount, mockAccount2);

            when(accountRepository.findByUserId(1L)).thenReturn(mockAccounts);

            List<Account> accounts = accountService.getAccountsByUser(1L);

            assertEquals(2, accounts.size());
            verify(accountRepository, times(1)).findByUserId(1L);
        }

        @Test
        void shouldGetAccountsExcludingUser() {
            Account mockAccount = new Account();
            mockAccount.setBalance(new BigDecimal("1000"));
            mockAccount.setId(1L);
            mockAccount.setUser(new User());

            Account mockAccount2 = new Account();
            mockAccount.setBalance(new BigDecimal("2000"));
            mockAccount.setId(1L);
            mockAccount.setUser(new User());

            List<Account> mockAccounts = Arrays.asList(mockAccount, mockAccount2);

            when(accountRepository.findAccountsExcludingUser(1L)).thenReturn(mockAccounts);

            List<Account> accounts = accountService.getAccountsExcludingUser(1L);

            assertEquals(2, accounts.size());
            verify(accountRepository, times(1)).findAccountsExcludingUser(1L);
        }

        @Test
        void shouldCountAccounts() {
            when(accountRepository.count()).thenReturn(5L);

            Long accountCount = accountService.countAccounts();

            assertEquals(5L, accountCount);
            verify(accountRepository, times(1)).count();
        }

        @Test
        void shouldCalculateTotalBalance() {
            when(accountRepository.calculateTotalBalance()).thenReturn(new BigDecimal("10000"));

            BigDecimal totalBalance = accountService.calculateTotalBalance();

            assertEquals(new BigDecimal("10000"), totalBalance);
            verify(accountRepository, times(1)).calculateTotalBalance();
        }

        @Test
        void shouldCalculateTotalBalanceByUser() {
            when(accountRepository.calculateTotalBalanceByUser(1L)).thenReturn(new BigDecimal("3000"));

            BigDecimal totalBalanceByUser = accountService.calculateTotalBalanceByUser(1L);

            assertEquals(new BigDecimal("3000"), totalBalanceByUser);
            verify(accountRepository, times(1)).calculateTotalBalanceByUser(1L);
        }
    }

    @Nested
    class CreateAccount {

        @Test
        void shouldCreateAccountWithValidData() {
            User user = new User();
            user.setId(1L);
            user.setUsername("test@example.com");
            user.setName("John Doe");
            user.setEnabled(true);
            user.setRoles(new HashSet<>());
            user.setPassword("password");

            Account mockAccount = new Account();
            mockAccount.setBalance(new BigDecimal("1000"));
            mockAccount.setId(1L);
            mockAccount.setOwnerName("John Doe");
            mockAccount.setUser(user);

            when(userService.findById(1L)).thenReturn(user);
            when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);

            Account createdAccount = accountService.createAccount("John Doe", new BigDecimal("1000"), 1L);

            assertNotNull(createdAccount);
            assertEquals("John Doe", createdAccount.getOwnerName());
            assertEquals(new BigDecimal("1000"), createdAccount.getBalance());
            assertEquals(1L, createdAccount.getUser().getId());
            verify(userService, times(1)).findById(1L);
            verify(accountRepository, times(1)).save(any(Account.class));
        }

        @Test
        void shouldThrowExceptionWhenInitialBalanceIsNull() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.createAccount("John Doe", null, 1L)
            );
            assertEquals("O saldo inicial deve ser positivo ou zero.", exception.getMessage());
            verify(userService, never()).findById(anyLong());
            verify(accountRepository, never()).save(any(Account.class));
        }

        @Test
        void shouldThrowExceptionWhenInitialBalanceIsNegative() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.createAccount("John Doe", new BigDecimal("-100"), 1L)
            );
            assertEquals("O saldo inicial deve ser positivo ou zero.", exception.getMessage());
            verify(userService, never()).findById(anyLong());
            verify(accountRepository, never()).save(any(Account.class));
        }

        @Test
        void shouldThrowExceptionWhenUserNotFound() {
            when(userService.findById(1L)).thenThrow(new IllegalArgumentException("Usuário não encontrado."));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.createAccount("John Doe", new BigDecimal("1000"), 1L)
            );
            assertEquals("Usuário não encontrado.", exception.getMessage());
            verify(userService, times(1)).findById(1L);
            verify(accountRepository, never()).save(any(Account.class));
        }

        @Test
        void shouldCreateAccountWithZeroBalance() {
            User user = new User();
            user.setId(1L);
            user.setName("John Doe");
            user.setUsername("test@example.com");
            user.setEnabled(true);
            user.setRoles(new HashSet<>());
            user.setPassword("password");

            Account mockAccount = new Account();
            mockAccount.setBalance(BigDecimal.ZERO);
            mockAccount.setId(1L);
            mockAccount.setOwnerName("John Doe");
            mockAccount.setUser(user);


            when(userService.findById(1L)).thenReturn(user);
            when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);

            Account createdAccount = accountService.createAccount("John Doe", BigDecimal.ZERO, 1L);

            assertNotNull(createdAccount);
            assertEquals("John Doe", createdAccount.getOwnerName());
            assertEquals(BigDecimal.ZERO, createdAccount.getBalance());
            assertEquals(1L, createdAccount.getUser().getId());
            verify(userService, times(1)).findById(1L);
            verify(accountRepository, times(1)).save(any(Account.class));
        }

    }

    @Nested
    class CreditAccount {
        @Test
        void shouldCreditAccountSuccessfully() {
            Account account = new Account();
            account.setId(1L);
            account.setBalance(new BigDecimal("1000"));

            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(accountRepository.save(any(Account.class))).thenReturn(account);

            accountService.creditAccount(1L, new BigDecimal("200"));

            assertEquals(new BigDecimal("1200"), account.getBalance());
            verify(accountRepository, times(1)).findById(1L);
            verify(accountRepository, times(1)).save(account);
            verify(transactionService, times(1)).save(any(Transaction.class));
        }

        @Test
        void shouldThrowExceptionWhenAmountIsZeroOrNegative() {
            BigDecimal negativeAmount = new BigDecimal("-100");
            BigDecimal zeroAmount = BigDecimal.ZERO;

            IllegalArgumentException negativeException = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.creditAccount(1L, negativeAmount)
            );
            assertEquals("O valor do crédito deve ser maior que zero.", negativeException.getMessage());

            IllegalArgumentException zeroException = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.creditAccount(1L, zeroAmount)
            );
            assertEquals("O valor do crédito deve ser maior que zero.", zeroException.getMessage());

            verify(accountRepository, never()).findById(anyLong());
            verify(accountRepository, never()).save(any(Account.class));
            verify(transactionService, never()).save(any(Transaction.class));
        }

        @Test
        void shouldThrowExceptionWhenAccountNotFound() {
            when(accountRepository.findById(1L)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.creditAccount(1L, new BigDecimal("200"))
            );
            assertEquals("Conta não encontrada.", exception.getMessage());
            verify(accountRepository, times(1)).findById(1L);
            verify(accountRepository, never()).save(any(Account.class));
            verify(transactionService, never()).save(any(Transaction.class));
        }
    }

    @Nested
    class DebitAccount {

        @Test
        void shouldDebitAccountSuccessfully() {
            Account account = new Account();
            account.setId(1L);
            account.setBalance(new BigDecimal("1000"));

            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(accountRepository.save(any(Account.class))).thenReturn(account);

            accountService.debitAccount(1L, new BigDecimal("200"));

            assertEquals(new BigDecimal("800"), account.getBalance());
            verify(accountRepository, times(1)).findById(1L);
            verify(accountRepository, times(1)).save(account);
            verify(transactionService, times(1)).save(any(Transaction.class));
        }

        @Test
        void shouldThrowExceptionWhenAmountIsZeroOrNegative() {
            BigDecimal negativeAmount = new BigDecimal("-100");
            BigDecimal zeroAmount = BigDecimal.ZERO;

            IllegalArgumentException negativeException = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.debitAccount(1L, negativeAmount)
            );
            assertEquals("O valor do débito deve ser maior que zero.", negativeException.getMessage());

            IllegalArgumentException zeroException = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.debitAccount(1L, zeroAmount)
            );
            assertEquals("O valor do débito deve ser maior que zero.", zeroException.getMessage());

            verify(accountRepository, never()).findById(anyLong());
            verify(accountRepository, never()).save(any(Account.class));
            verify(transactionService, never()).save(any(Transaction.class));
        }

        @Test
        void shouldThrowExceptionWhenAccountNotFound() {
            when(accountRepository.findById(1L)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.debitAccount(1L, new BigDecimal("200"))
            );
            assertEquals("Conta não encontrada.", exception.getMessage());
            verify(accountRepository, times(1)).findById(1L);
            verify(accountRepository, never()).save(any(Account.class));
            verify(transactionService, never()).save(any(Transaction.class));
        }

        @Test
        void shouldThrowExceptionWhenInsufficientBalance() {
            Account account = new Account();
            account.setId(1L);
            account.setBalance(new BigDecimal("100"));

            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.debitAccount(1L, new BigDecimal("200"))
            );
            assertEquals("Saldo insuficiente para realizar o débito.", exception.getMessage());
            verify(accountRepository, times(1)).findById(1L);
            verify(accountRepository, never()).save(any(Account.class));
            verify(transactionService, never()).save(any(Transaction.class));
        }

    }

    @Nested
    class TransferBetweenAccounts {

        @Test
        void shouldTransferBetweenAccountsSuccessfully() {
            Account sourceAccount = new Account();
            sourceAccount.setId(1L);
            sourceAccount.setBalance(new BigDecimal("1000"));

            Account targetAccount = new Account();
            targetAccount.setId(2L);
            targetAccount.setBalance(new BigDecimal("500"));

            when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
            when(accountRepository.findById(2L)).thenReturn(Optional.of(targetAccount));

            accountService.transferBetweenAccounts(1L, 2L, new BigDecimal("200"));

            assertEquals(new BigDecimal("800"), sourceAccount.getBalance());
            assertEquals(new BigDecimal("700"), targetAccount.getBalance());
            verify(accountRepository, times(1)).findById(1L);
            verify(accountRepository, times(1)).findById(2L);
            verify(accountRepository, times(1)).save(sourceAccount);
            verify(accountRepository, times(1)).save(targetAccount);
            verify(transactionService, times(2)).save(any(Transaction.class));
        }

        @Test
        void shouldThrowExceptionWhenAmountIsZeroOrNegative() {
            BigDecimal zeroAmount = BigDecimal.ZERO;
            BigDecimal negativeAmount = new BigDecimal("-100");

            IllegalArgumentException zeroException = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.transferBetweenAccounts(1L, 2L, zeroAmount)
            );
            assertEquals("O valor da transferência deve ser maior que zero.", zeroException.getMessage());

            IllegalArgumentException negativeException = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.transferBetweenAccounts(1L, 2L, negativeAmount)
            );
            assertEquals("O valor da transferência deve ser maior que zero.", negativeException.getMessage());

            verify(accountRepository, never()).findById(anyLong());
            verify(accountRepository, never()).save(any(Account.class));
            verify(transactionService, never()).save(any(Transaction.class));
        }

        @Test
        void shouldThrowExceptionWhenSourceAndTargetAccountsAreTheSame() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.transferBetweenAccounts(1L, 1L, new BigDecimal("200"))
            );
            assertEquals("A conta de origem e destino devem ser diferentes.", exception.getMessage());
            verify(accountRepository, never()).findById(anyLong());
            verify(accountRepository, never()).save(any(Account.class));
            verify(transactionService, never()).save(any(Transaction.class));
        }

        @Test
        void shouldThrowExceptionWhenSourceAccountNotFound() {
            when(accountRepository.findById(1L)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.transferBetweenAccounts(1L, 2L, new BigDecimal("200"))
            );
            assertEquals("Conta de origem não encontrada.", exception.getMessage());
            verify(accountRepository, times(1)).findById(1L);
            verify(accountRepository, never()).findById(2L);
            verify(accountRepository, never()).save(any(Account.class));
            verify(transactionService, never()).save(any(Transaction.class));
        }

        @Test
        void shouldThrowExceptionWhenTargetAccountNotFound() {
            Account sourceAccount = new Account();
            sourceAccount.setId(1L);
            sourceAccount.setBalance(new BigDecimal("1000"));

            when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
            when(accountRepository.findById(2L)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.transferBetweenAccounts(1L, 2L, new BigDecimal("200"))
            );
            assertEquals("Conta de destino não encontrada.", exception.getMessage());
            verify(accountRepository, times(1)).findById(1L);
            verify(accountRepository, times(1)).findById(2L);
            verify(accountRepository, never()).save(any(Account.class));
            verify(transactionService, never()).save(any(Transaction.class));
        }

        @Test
        void shouldThrowExceptionWhenInsufficientBalance() {
            Account sourceAccount = new Account();
            sourceAccount.setId(1L);
            sourceAccount.setBalance(new BigDecimal("100"));

            Account targetAccount = new Account();
            targetAccount.setId(2L);
            targetAccount.setBalance(new BigDecimal("500"));

            when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
            when(accountRepository.findById(2L)).thenReturn(Optional.of(targetAccount));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.transferBetweenAccounts(1L, 2L, new BigDecimal("200"))
            );
            assertEquals("Saldo insuficiente na conta de origem.", exception.getMessage());
            verify(accountRepository, times(1)).findById(1L);
            verify(accountRepository, times(1)).findById(2L);
            verify(accountRepository, never()).save(any(Account.class));
            verify(transactionService, never()).save(any(Transaction.class));
        }

        @Test
        void shouldThrowOptimisticLockException() {
            Account sourceAccount = new Account();
            sourceAccount.setId(1L);
            sourceAccount.setBalance(new BigDecimal("1000"));

            Account targetAccount = new Account();
            targetAccount.setId(2L);
            targetAccount.setBalance(new BigDecimal("500"));

            when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
            when(accountRepository.findById(2L)).thenReturn(Optional.of(targetAccount));
            doThrow(OptimisticLockException.class).when(accountRepository).save(any(Account.class));

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> accountService.transferBetweenAccounts(1L, 2L, new BigDecimal("200"))
            );
            assertEquals("Conflito detectado ao tentar realizar a transferência. Tente novamente.", exception.getMessage());
            verify(accountRepository, times(1)).findById(1L);
            verify(accountRepository, times(1)).findById(2L);
            verify(accountRepository, times(1)).save(sourceAccount);
        }

        @Test
        void shouldThrowExceptionForUnexpectedError() {
            Account sourceAccount = new Account();
            sourceAccount.setId(1L);
            sourceAccount.setBalance(new BigDecimal("1000"));

            Account targetAccount = new Account();
            targetAccount.setId(2L);
            targetAccount.setBalance(new BigDecimal("500"));

            when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
            when(accountRepository.findById(2L)).thenReturn(Optional.of(targetAccount));
            doThrow(RuntimeException.class).when(accountRepository).save(any(Account.class));

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> accountService.transferBetweenAccounts(1L, 2L, new BigDecimal("200"))
            );
            assertEquals("Erro inesperado ao processar a transferência. Entre em contato com o suporte.", exception.getMessage());
            verify(accountRepository, times(1)).findById(1L);
            verify(accountRepository, times(1)).findById(2L);
            verify(accountRepository, times(1)).save(sourceAccount);
            verify(accountRepository, never()).save(targetAccount); // Não salva a conta de destino
        }
    }

}
