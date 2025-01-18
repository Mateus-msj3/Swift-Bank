package io.github.msj.swiftbank.controller;

import io.github.msj.swiftbank.entity.Account;
import io.github.msj.swiftbank.entity.Transaction;
import io.github.msj.swiftbank.entity.User;
import io.github.msj.swiftbank.service.AccountService;
import io.github.msj.swiftbank.service.TransactionService;
import io.github.msj.swiftbank.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAccountControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AdminAccountController adminAccountController;

    @Test
    void shouldListAllAccounts() {
        Account account1 = new Account();
        account1.setId(1L);
        account1.setOwnerName("User1");

        Account account2 = new Account();
        account2.setId(2L);
        account2.setOwnerName("User2");

        when(accountService.findAll()).thenReturn(Arrays.asList(account1, account2));

        Model model = new ExtendedModelMap();
        String viewName = adminAccountController.listAllAccounts(model);

        assertEquals("admin-account-list", viewName);
        assertNotNull(model.getAttribute("accounts"));
        assertEquals(2, ((List<?>) Objects.requireNonNull(model.getAttribute("accounts"))).size());
        verify(accountService, times(1)).findAll();
    }

    @Test
    void shouldShowDashboard() {
        when(accountService.countAccounts()).thenReturn(10L);
        when(accountService.calculateTotalBalance()).thenReturn(new BigDecimal("50000"));

        Model model = new ExtendedModelMap();
        String viewName = adminAccountController.showDashboard(model);

        assertEquals("admin-dashboard", viewName);
        assertEquals(10L, model.getAttribute("totalAccounts"));
        assertEquals(new BigDecimal("50000"), model.getAttribute("totalBalance"));
        verify(accountService, times(1)).countAccounts();
        verify(accountService, times(1)).calculateTotalBalance();
    }

    @Test
    void shouldShowTransactionSelection() {
        Account account1 = new Account();
        account1.setId(1L);

        Account account2 = new Account();
        account2.setId(2L);

        when(accountService.findAll()).thenReturn(Arrays.asList(account1, account2));

        Model model = new ExtendedModelMap();
        String viewName = adminAccountController.showTransactionSelection(model);

        assertEquals("admin-transaction-selection", viewName);
        assertNotNull(model.getAttribute("accounts"));
        verify(accountService, times(1)).findAll();
    }

    @Test
    void shouldListAdminTransactions() {
        Account account = new Account();
        account.setId(1L);
        account.setOwnerName("User1");

        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setAmount(new BigDecimal("100"));

        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setAmount(new BigDecimal("200"));

        when(accountService.findById(1L)).thenReturn(account);
        when(transactionService.getTransactionsByAccount(1L)).thenReturn(Arrays.asList(transaction1, transaction2));

        Model model = new ExtendedModelMap();
        String viewName = adminAccountController.listAdminTransactions(1L, model);

        assertEquals("admin-transaction-list", viewName);
        assertNotNull(model.getAttribute("transactions"));
        assertEquals(account, model.getAttribute("selectedAccount"));
        verify(accountService, times(1)).findById(1L);
        verify(transactionService, times(1)).getTransactionsByAccount(1L);
    }

    @Test
    void shouldShowCreateAccountForm() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        when(userService.findAll()).thenReturn(Arrays.asList(user1, user2));

        Model model = new ExtendedModelMap();
        String viewName = adminAccountController.showCreateAccountForm(model);

        assertEquals("create-account", viewName);
        assertNotNull(model.getAttribute("users"));
        verify(userService, times(1)).findAll();
    }

    @Test
    void shouldCreateAccountSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setUsername("User1");

        when(userService.findAll()).thenReturn(Collections.singletonList(user));

        Model model = new ExtendedModelMap();
        String viewName = adminAccountController.createAccount("User1", new BigDecimal("1000"), 1L, model);

        assertEquals("create-account", viewName);
        assertNotNull(model.getAttribute("successMessage"));
        assertEquals("Conta criada com sucesso!", model.getAttribute("successMessage"));
        verify(accountService, times(1)).createAccount("User1", new BigDecimal("1000"), 1L);
    }

    @Test
    void shouldHandleAccountCreationFailure() {
        when(accountService.createAccount("User1", new BigDecimal("1000"), 1L))
                .thenThrow(new IllegalArgumentException("Erro ao criar conta."));
        User user = new User();
        user.setId(1L);

        when(userService.findAll()).thenReturn(Collections.singletonList(user));

        Model model = new ExtendedModelMap();
        String viewName = adminAccountController.createAccount("User1", new BigDecimal("1000"), 1L, model);

        assertEquals("create-account", viewName);
        assertNotNull(model.getAttribute("errorMessage"));
        assertEquals("Erro ao criar conta.", model.getAttribute("errorMessage"));
        verify(accountService, times(1)).createAccount("User1", new BigDecimal("1000"), 1L);
        verify(userService, times(1)).findAll();
    }

}
