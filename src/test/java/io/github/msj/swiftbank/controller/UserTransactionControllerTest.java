package io.github.msj.swiftbank.controller;

import io.github.msj.swiftbank.entity.Account;
import io.github.msj.swiftbank.entity.Transaction;
import io.github.msj.swiftbank.entity.User;
import io.github.msj.swiftbank.service.AccountService;
import io.github.msj.swiftbank.service.TransactionService;
import io.github.msj.swiftbank.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserTransactionControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private UserTransactionController userTransactionController;

    @Nested
    class ShowUserDetailedTransaction {

        @Test
        void shouldShowUserDashboardWithTotalBalance() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");

            BigDecimal totalBalance = new BigDecimal("1000");

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(accountService.calculateTotalBalanceByUser(1L)).thenReturn(totalBalance);

            Model model = new ExtendedModelMap();
            Authentication authentication = mockAuthentication("testuser");
            String viewName = userTransactionController.showDashboard(model, authentication);

            assertEquals("user-dashboard", viewName);
            assertEquals(totalBalance, model.getAttribute("totalBalance"));
            verify(userService, times(1)).findByUsername("testuser");
            verify(accountService, times(1)).calculateTotalBalanceByUser(1L);
        }

        @Test
        void shouldShowUserDashboardWithZeroBalanceWhenNoAccounts() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(accountService.calculateTotalBalanceByUser(1L)).thenReturn(null);

            Model model = new ExtendedModelMap();
            Authentication authentication = mockAuthentication("testuser");
            String viewName = userTransactionController.showDashboard(model, authentication);

            assertEquals("user-dashboard", viewName);
            assertEquals(BigDecimal.ZERO, model.getAttribute("totalBalance"));
            verify(userService, times(1)).findByUsername("testuser");
            verify(accountService, times(1)).calculateTotalBalanceByUser(1L);
        }

        @Test
        void shouldShowTransactionSelectionWithUserAccounts() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");

            Account account1 = new Account();
            account1.setId(1L);

            Account account2 = new Account();
            account2.setId(2L);

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(accountService.getAccountsByUser(1L)).thenReturn(Arrays.asList(account1, account2));

            Model model = new ExtendedModelMap();
            Authentication authentication = mockAuthentication("testuser");
            String viewName = userTransactionController.showTransactionSelection(model, authentication);

            assertEquals("user-transaction-selection", viewName);
            assertNotNull(model.getAttribute("accounts"));
            assertEquals(2, ((List<?>) model.getAttribute("accounts")).size());
            verify(userService, times(1)).findByUsername("testuser");
            verify(accountService, times(1)).getAccountsByUser(1L);
        }

        private Authentication mockAuthentication(String username) {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(username);
            return authentication;
        }

    }

    @Nested
    class ListUserTransactions {

        @Test
        void shouldListUserAccountsSuccessfully() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");

            Account account1 = new Account();
            account1.setId(1L);

            Account account2 = new Account();
            account2.setId(2L);

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(accountService.getAccountsByUser(1L)).thenReturn(Arrays.asList(account1, account2));

            Model model = new ExtendedModelMap();
            Authentication authentication = mockAuthentication("testuser");
            String viewName = userTransactionController.listUserAccounts(model, authentication);

            assertEquals("user-account-list", viewName);
            assertNotNull(model.getAttribute("accounts"));
            assertEquals(2, ((List<?>) Objects.requireNonNull(model.getAttribute("accounts"))).size());
            verify(userService, times(1)).findByUsername("testuser");
            verify(accountService, times(1)).getAccountsByUser(1L);
        }

        @Test
        void shouldListUserTransactionsSuccessfully() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");

            Account account = new Account();
            account.setId(1L);
            account.setUser(user);

            Transaction transaction1 = new Transaction();
            transaction1.setId(1L);
            transaction1.setAmount(new BigDecimal("100"));

            Transaction transaction2 = new Transaction();
            transaction2.setId(2L);
            transaction2.setAmount(new BigDecimal("200"));

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(accountService.findById(1L)).thenReturn(account);
            when(transactionService.getTransactionsByAccount(1L)).thenReturn(Arrays.asList(transaction1, transaction2));

            Model model = new ExtendedModelMap();
            Authentication authentication = mockAuthentication("testuser");
            String viewName = userTransactionController.listUserTransactions(1L, model, authentication);

            assertEquals("user-transaction-list", viewName);
            assertNotNull(model.getAttribute("transactions"));
            assertNotNull(model.getAttribute("selectedAccount"));
            assertEquals(2, ((List<?>) model.getAttribute("transactions")).size());
            assertEquals(account, model.getAttribute("selectedAccount"));
            verify(userService, times(1)).findByUsername("testuser");
            verify(accountService, times(1)).findById(1L);
            verify(transactionService, times(1)).getTransactionsByAccount(1L);
        }

        @Test
        void shouldThrowAccessDeniedExceptionWhenAccountDoesNotBelongToUser() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");

            User otherUser = new User();
            otherUser.setId(2L);

            Account account = new Account();
            account.setId(1L);
            account.setUser(otherUser);

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(accountService.findById(1L)).thenReturn(account);

            Model model = new ExtendedModelMap();
            Authentication authentication = mockAuthentication("testuser");

            AccessDeniedException exception = assertThrows(
                    AccessDeniedException.class,
                    () -> userTransactionController.listUserTransactions(1L, model, authentication)
            );

            assertEquals("Acesso negado: esta conta não pertence ao usuário logado.", exception.getMessage());
            verify(userService, times(1)).findByUsername("testuser");
            verify(accountService, times(1)).findById(1L);
            verify(transactionService, never()).getTransactionsByAccount(anyLong());
        }

        @Test
        void shouldThrowExceptionWhenAccountNotFound() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(accountService.findById(1L)).thenThrow(new IllegalArgumentException("Conta não encontrada."));

            Model model = new ExtendedModelMap();
            Authentication authentication = mockAuthentication("testuser");

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userTransactionController.listUserTransactions(1L, model, authentication)
            );

            assertEquals("Conta não encontrada.", exception.getMessage());
            verify(userService, times(1)).findByUsername("testuser");
            verify(accountService, times(1)).findById(1L);
            verify(transactionService, never()).getTransactionsByAccount(anyLong());
        }

        private Authentication mockAuthentication(String username) {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(username);
            return authentication;
        }
    }

    @Nested
    class CreditAccountUser {

        @Test
        void shouldShowCreditFormWithUserAccounts() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");

            Account account1 = new Account();
            account1.setId(1L);

            Account account2 = new Account();
            account2.setId(2L);

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(accountService.getAccountsByUser(1L)).thenReturn(Arrays.asList(account1, account2));

            Model model = new ExtendedModelMap();
            Authentication authentication = mockAuthentication("testuser");
            String viewName = userTransactionController.showCreditForm(model, authentication);

            assertEquals("credit-account", viewName);
            assertNotNull(model.getAttribute("accounts"));
            assertEquals(2, ((List<?>) Objects.requireNonNull(model.getAttribute("accounts"))).size());
            verify(userService, times(1)).findByUsername("testuser");
            verify(accountService, times(1)).getAccountsByUser(1L);
        }

        @Test
        void shouldCreditAccountSuccessfully() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");

            Account account1 = new Account();
            account1.setId(1L);

            Account account2 = new Account();
            account2.setId(2L);

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(accountService.getAccountsByUser(1L)).thenReturn(Arrays.asList(account1, account2));

            Model model = new ExtendedModelMap();
            Authentication authentication = mockAuthentication("testuser");
            String viewName = userTransactionController.creditAccount(1L, new BigDecimal("200"), model, authentication);

            assertEquals("credit-account", viewName);
            assertNotNull(model.getAttribute("successMessage"));
            assertEquals("Valor creditado com sucesso!", model.getAttribute("successMessage"));
            assertNotNull(model.getAttribute("accounts"));
            assertEquals(2, ((List<?>) Objects.requireNonNull(model.getAttribute("accounts"))).size());
            verify(accountService, times(1)).creditAccount(1L, new BigDecimal("200"));
            verify(userService, times(1)).findByUsername("testuser");
            verify(accountService, times(1)).getAccountsByUser(1L);
        }

        @Test
        void shouldHandleCreditAccountError() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");

            Account account1 = new Account();
            account1.setId(1L);

            Account account2 = new Account();
            account2.setId(2L);

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(accountService.getAccountsByUser(1L)).thenReturn(Arrays.asList(account1, account2));
            doThrow(new IllegalArgumentException("Erro ao creditar o valor.")).when(accountService).creditAccount(1L, new BigDecimal("200"));

            Model model = new ExtendedModelMap();
            Authentication authentication = mockAuthentication("testuser");
            String viewName = userTransactionController.creditAccount(1L, new BigDecimal("200"), model, authentication);

            assertEquals("credit-account", viewName);
            assertNotNull(model.getAttribute("errorMessage"));
            assertEquals("Erro ao creditar o valor.", model.getAttribute("errorMessage"));
            assertNotNull(model.getAttribute("accounts"));
            assertEquals(2, ((List<?>) Objects.requireNonNull(model.getAttribute("accounts"))).size());
            verify(accountService, times(1)).creditAccount(1L, new BigDecimal("200"));
            verify(userService, times(1)).findByUsername("testuser");
            verify(accountService, times(1)).getAccountsByUser(1L);
        }

        private Authentication mockAuthentication(String username) {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(username);
            return authentication;
        }

    }

    @Nested
    class DebitAccountUser {

        @Test
        void shouldShowDebitFormWithUserAccounts() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");

            Account account1 = new Account();
            account1.setId(1L);

            Account account2 = new Account();
            account2.setId(2L);

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(accountService.getAccountsByUser(1L)).thenReturn(Arrays.asList(account1, account2));

            Model model = new ExtendedModelMap();
            Authentication authentication = mockAuthentication("testuser");
            String viewName = userTransactionController.showDebitForm(model, authentication);

            assertEquals("debit-account", viewName);
            assertNotNull(model.getAttribute("accounts"));
            assertEquals(2, ((List<?>) Objects.requireNonNull(model.getAttribute("accounts"))).size());
            verify(userService, times(1)).findByUsername("testuser");
            verify(accountService, times(1)).getAccountsByUser(1L);
        }

        @Test
        void shouldDebitAccountSuccessfully() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");

            Account account1 = new Account();
            account1.setId(1L);

            Account account2 = new Account();
            account2.setId(2L);

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(accountService.getAccountsByUser(1L)).thenReturn(Arrays.asList(account1, account2));

            Model model = new ExtendedModelMap();
            Authentication authentication = mockAuthentication("testuser");
            String viewName = userTransactionController.debitAccount(1L, new BigDecimal("200"), model, authentication);

            assertEquals("debit-account", viewName);
            assertNotNull(model.getAttribute("successMessage"));
            assertEquals("Valor debitado com sucesso!", model.getAttribute("successMessage"));
            assertNotNull(model.getAttribute("accounts"));
            assertEquals(2, ((List<?>) Objects.requireNonNull(model.getAttribute("accounts"))).size());
            verify(accountService, times(1)).debitAccount(1L, new BigDecimal("200"));
            verify(userService, times(1)).findByUsername("testuser");
            verify(accountService, times(1)).getAccountsByUser(1L);
        }

        @Test
        void shouldHandleDebitAccountError() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");

            Account account1 = new Account();
            account1.setId(1L);

            Account account2 = new Account();
            account2.setId(2L);

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(accountService.getAccountsByUser(1L)).thenReturn(Arrays.asList(account1, account2));
            doThrow(new IllegalArgumentException("Erro ao debitar o valor.")).when(accountService).debitAccount(1L, new BigDecimal("200"));

            Model model = new ExtendedModelMap();
            Authentication authentication = mockAuthentication("testuser");
            String viewName = userTransactionController.debitAccount(1L, new BigDecimal("200"), model, authentication);

            assertEquals("debit-account", viewName);
            assertNotNull(model.getAttribute("errorMessage"));
            assertEquals("Erro ao debitar o valor.", model.getAttribute("errorMessage"));
            assertNotNull(model.getAttribute("accounts"));
            assertEquals(2, ((List<?>) Objects.requireNonNull(model.getAttribute("accounts"))).size());
            verify(accountService, times(1)).debitAccount(1L, new BigDecimal("200"));
            verify(userService, times(1)).findByUsername("testuser");
            verify(accountService, times(1)).getAccountsByUser(1L);
        }

        // Método auxiliar para criar autenticação mockada
        private Authentication mockAuthentication(String username) {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(username);
            return authentication;
        }

    }

    @Nested
    class TransferAccountUser {

        @Test
        void shouldShowTransferFormWithUserAccounts() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");

            Account account1 = new Account();
            account1.setId(1L);

            Account account2 = new Account();
            account2.setId(2L);

            Account otherUserAccount = new Account();
            otherUserAccount.setId(3L);

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(accountService.getAccountsByUser(1L)).thenReturn(Arrays.asList(account1, account2));
            when(accountService.getAccountsExcludingUser(1L)).thenReturn(Collections.singletonList(otherUserAccount));

            Model model = new ExtendedModelMap();
            Authentication authentication = mockAuthentication("testuser");
            String viewName = userTransactionController.showTransferForm(model, authentication);

            assertEquals("transfer-account", viewName);
            assertNotNull(model.getAttribute("accounts"));
            assertNotNull(model.getAttribute("targetAccounts"));
            assertEquals(2, ((List<?>) model.getAttribute("accounts")).size());
            assertEquals(1, ((List<?>) model.getAttribute("targetAccounts")).size());
            verify(userService, times(1)).findByUsername("testuser");
            verify(accountService, times(1)).getAccountsByUser(1L);
            verify(accountService, times(1)).getAccountsExcludingUser(1L);
        }

        @Test
        void shouldTransferBetweenAccountsSuccessfully() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");

            Account sourceAccount = new Account();
            sourceAccount.setId(1L);

            Account targetAccount = new Account();
            targetAccount.setId(2L);

            Account otherUserAccount = new Account();
            otherUserAccount.setId(3L);

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(accountService.getAccountsByUser(1L)).thenReturn(Collections.singletonList(sourceAccount));
            when(accountService.getAccountsExcludingUser(1L)).thenReturn(Collections.singletonList(otherUserAccount));

            Model model = new ExtendedModelMap();
            Authentication authentication = mockAuthentication("testuser");
            String viewName = userTransactionController.transferBetweenAccounts(1L, 2L, new BigDecimal("500"), model, authentication);

            assertEquals("transfer-account", viewName);
            assertNotNull(model.getAttribute("successMessage"));
            assertEquals("Transferência realizada com sucesso!", model.getAttribute("successMessage"));
            assertNotNull(model.getAttribute("accounts"));
            assertNotNull(model.getAttribute("targetAccounts"));
            verify(accountService, times(1)).transferBetweenAccounts(1L, 2L, new BigDecimal("500"));
            verify(userService, times(1)).findByUsername("testuser");
            verify(accountService, times(1)).getAccountsByUser(1L);
            verify(accountService, times(1)).getAccountsExcludingUser(1L);
        }

        @Test
        void shouldHandleTransferError() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");

            Account sourceAccount = new Account();
            sourceAccount.setId(1L);

            Account targetAccount = new Account();
            targetAccount.setId(2L);

            Account otherUserAccount = new Account();
            otherUserAccount.setId(3L);

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(accountService.getAccountsByUser(1L)).thenReturn(Collections.singletonList(sourceAccount));
            when(accountService.getAccountsExcludingUser(1L)).thenReturn(Collections.singletonList(otherUserAccount));
            doThrow(new IllegalArgumentException("Erro ao realizar a transferência."))
                    .when(accountService).transferBetweenAccounts(1L, 2L, new BigDecimal("500"));

            Model model = new ExtendedModelMap();
            Authentication authentication = mockAuthentication("testuser");
            String viewName = userTransactionController.transferBetweenAccounts(1L, 2L, new BigDecimal("500"), model, authentication);

            assertEquals("transfer-account", viewName);
            assertNotNull(model.getAttribute("errorMessage"));
            assertEquals("Erro ao realizar a transferência.", model.getAttribute("errorMessage"));
            assertNotNull(model.getAttribute("accounts"));
            assertNotNull(model.getAttribute("targetAccounts"));
            verify(accountService, times(1)).transferBetweenAccounts(1L, 2L, new BigDecimal("500"));
            verify(userService, times(1)).findByUsername("testuser");
            verify(accountService, times(1)).getAccountsByUser(1L);
            verify(accountService, times(1)).getAccountsExcludingUser(1L);
        }

        private Authentication mockAuthentication(String username) {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(username);
            return authentication;
        }

    }

}
